package com.dotdashnotdot.permessodisoggiornoalarm

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object LogHelper {
    private const val LOG_FILE_NAME = "app_logs.txt"

    fun log(context: Context, message: String) {
        Log.d("PermessoAlarm", message)
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val logEntry = "[$timestamp] $message\n"
        
        try {
            val file = File(context.filesDir, LOG_FILE_NAME)
            file.appendText(logEntry)
            
            // Optional: Limit file size (e.g., keep last 100KB)
            if (file.length() > 100 * 1024) {
                val lines = file.readLines()
                if (lines.size > 100) {
                    file.writeText(lines.takeLast(100).joinToString("\n") + "\n")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getLogs(context: Context): String {
        return try {
            val file = File(context.filesDir, LOG_FILE_NAME)
            if (file.exists()) file.readText() else "No logs found."
        } catch (e: Exception) {
            "Error reading logs: ${e.message}"
        }
    }

    fun clearLogs(context: Context) {
        try {
            val file = File(context.filesDir, LOG_FILE_NAME)
            if (file.exists()) file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
