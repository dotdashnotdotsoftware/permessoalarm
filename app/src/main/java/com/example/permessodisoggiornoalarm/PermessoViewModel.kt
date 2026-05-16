package com.example.permessodisoggiornoalarm

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class PermessoViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("permesso_prefs", Context.MODE_PRIVATE)
    val permessoItems = mutableStateListOf<PermessoItem>()
    
    var selectedLanguage by mutableStateOf(prefs.getString("language", "Italiano") ?: "Italiano")
        private set
    
    var randomTime by mutableStateOf("")
        private set

    init {
        loadItems()
        loadRandomTime()
        scheduleDailyWork(application)
    }

    private fun loadItems() {
        val savedString = prefs.getString("items", "") ?: ""
        if (savedString.isNotEmpty()) {
            val lines = savedString.split("\n")
            lines.forEach { line ->
                try {
                    val json = JSONObject(line)
                    val name = json.getString("name")
                    val requestId = json.getString("requestId")
                    permessoItems.add(PermessoItem(name, requestId))
                } catch (e: Exception) {
                    // Ignore malformed entries
                }
            }
        }
    }

    private fun loadRandomTime() {
        val savedTime = prefs.getString("random_time", "") ?: ""
        if (savedTime.isNotEmpty()) {
            randomTime = savedTime
        } else {
            val hours = (0..23).random()
            val minutes = (0..59).random()
            randomTime = "$hours:${minutes.toString().padStart(2, '0')}"
            prefs.edit().putString("random_time", randomTime).apply()
        }
    }

    fun addItem(name: String, requestId: String) {
        if (permessoItems.size < 5) {
            permessoItems.add(PermessoItem(name, requestId))
            saveItems()
            scheduleDailyWork(getApplication())
        }
    }

    fun removeItem(item: PermessoItem) {
        permessoItems.remove(item)
        saveItems()
        scheduleDailyWork(getApplication())
    }

    private fun saveItems() {
        val stringToSave = permessoItems.joinToString("\n") { item ->
            val json = JSONObject()
            json.put("name", item.name)
            json.put("requestId", item.requestId)
            json.toString()
        }
        prefs.edit().putString("items", stringToSave).apply()
    }

    fun setLanguage(language: String) {
        selectedLanguage = language
        prefs.edit().putString("language", language).apply()
    }

    fun updateRandomTime(time: String) {
        val msg = "Updating time to $time"
        LogHelper.log(getApplication(), msg)
        randomTime = time
        prefs.edit().putString("random_time", time).apply()
        scheduleDailyWork(getApplication())
    }

    fun getLangParam(): String {
        return when (selectedLanguage) {
            "Italiano" -> "italian"
            "English" -> "english"
            "Español" -> "espanol"
            "Français" -> "french"
            "Русский" -> "russion"
            "український" -> "ukrainian"
            "الْعَرَبيّة" -> "arabic"
            else -> "italian"
        }
    }

    fun checkStatus(requestId: String, onResult: (String) -> Unit) {
        val errorMsg = getApplication<Application>().getString(R.string.msg_something_went_wrong)
        viewModelScope.launch {
            val langParam = getLangParam()
            val result = withContext(Dispatchers.IO) {
                try {
                    val url = URL("https://questure.poliziadistato.it/servizio/stranieri?lang=$langParam&pratica=$requestId&invia=Invia&mime=4")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 10000
                    connection.readTimeout = 10000

                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        parseXml(connection.inputStream)?.trim() ?: errorMsg
                    } else {
                        errorMsg
                    }
                } catch (e: Exception) {
                    errorMsg
                }
            }
            LogHelper.log(getApplication(), "Check ID $requestId: $result")
            onResult(result)
        }
    }

    private fun parseXml(inputStream: java.io.InputStream): String? {
        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(inputStream, null)

            var eventType = parser.eventType
            var inItem = false
            var currentTag = ""

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name
                        if (currentTag == "item") {
                            inItem = true
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (inItem && currentTag == "description") {
                            return parser.text
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "item") {
                            inItem = false
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    companion object {
        fun scheduleDailyWork(context: Context) {
            val prefs = context.getSharedPreferences("permesso_prefs", Context.MODE_PRIVATE)
            
            val savedItemsString = prefs.getString("items", "") ?: ""
            val itemsCount = if (savedItemsString.isBlank()) 0 else savedItemsString.split("\n").filter { it.isNotBlank() }.size
            
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, PermessoReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (itemsCount < 1) {
                LogHelper.log(context, "No items found, cancelling any existing alarm.")
                alarmManager.cancel(pendingIntent)
                return
            }

            val time = prefs.getString("random_time", "12:00") ?: "12:00"
            val timeParts = time.split(":")
            val targetHour = timeParts.getOrNull(0)?.toIntOrNull() ?: 0
            val targetMinute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

            val currentDate = Calendar.getInstance()
            val dueDate = Calendar.getInstance()
            dueDate.set(Calendar.HOUR_OF_DAY, targetHour)
            dueDate.set(Calendar.MINUTE, targetMinute)
            dueDate.set(Calendar.SECOND, 0)
            dueDate.set(Calendar.MILLISECOND, 0)
            
            if (dueDate.before(currentDate)) {
                dueDate.add(Calendar.HOUR_OF_DAY, 24)
            }

            val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }

            if (canScheduleExact) {
                val msg = "Scheduling EXACT alarm at $time (in ${(dueDate.timeInMillis - currentDate.timeInMillis) / 1000} seconds)"
                LogHelper.log(context, msg)
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    dueDate.timeInMillis,
                    pendingIntent
                )
            } else {
                LogHelper.log(context, "Scheduling inexact alarm at $time (permission not granted)")
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    dueDate.timeInMillis,
                    pendingIntent
                )
            }
        }
    }
}
