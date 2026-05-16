package com.example.permessodisoggiornoalarm

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.net.HttpURLConnection
import java.net.URL

class PermessoWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("permesso_prefs", Context.MODE_PRIVATE)
        val savedString = prefs.getString("items", "") ?: ""
        
        if (savedString.isEmpty()) return Result.success()

        val lines = savedString.split("\n")
        if (lines.isEmpty()) return Result.success()

        val firstLine = lines[0]
        return try {
            val json = JSONObject(firstLine)
            val name = json.getString("name")
            val requestId = json.getString("requestId")
            val selectedLanguage = prefs.getString("language", "Italiano") ?: "Italiano"
            
            val langParam = when (selectedLanguage) {
                "Italiano" -> "italian"
                "English" -> "english"
                "Español" -> "espanol"
                "Français" -> "french"
                "Русский" -> "russion"
                "український" -> "ukrainian"
                "الْعَرَبيّة" -> "arabic"
                else -> "italian"
            }

            val resultText = fetchStatus(requestId, langParam)
            Log.d("PermessoWorker", "Background check result for $requestId: $resultText")
            
            NotificationHelper.showNotification(applicationContext, requestId, resultText, langParam)
            
            Result.success()
        } catch (e: Exception) {
            Log.e("PermessoWorker", "Error in background check", e)
            Result.retry()
        }
    }

    private fun fetchStatus(requestId: String, lang: String): String {
        return try {
            val url = URL("https://questure.poliziadistato.it/servizio/stranieri?lang=$lang&pratica=$requestId&invia=Invia&mime=4")
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
                        if (currentTag == "item") inItem = true
                    }
                    XmlPullParser.TEXT -> {
                        if (inItem && currentTag == "description") return parser.text
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "item") inItem = false
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
