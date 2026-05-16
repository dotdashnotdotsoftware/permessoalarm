package com.example.permessodisoggiornoalarm

import android.app.Application
import android.content.Context
import android.util.Log
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

    fun addItem(name: String, requestId: String) {
        if (permessoItems.size < 5) {
            permessoItems.add(PermessoItem(name, requestId))
            saveItems()
        }
    }

    fun removeItem(item: PermessoItem) {
        permessoItems.remove(item)
        saveItems()
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
        randomTime = time
        prefs.edit().putString("random_time", time).apply()
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
                        parseXml(connection.inputStream)?.trim() ?: "Something went wrong"
                    } else {
                        "Something went wrong"
                    }
                } catch (e: Exception) {
                    "Something went wrong"
                }
            }
            Log.d("PermessoViewModel", "Status check result for $requestId: $result")
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
}
