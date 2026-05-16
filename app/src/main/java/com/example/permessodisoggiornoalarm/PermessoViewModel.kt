package com.example.permessodisoggiornoalarm

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import org.json.JSONObject

class PermessoViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("permesso_prefs", Context.MODE_PRIVATE)
    val permessoItems = mutableStateListOf<PermessoItem>()

    init {
        loadItems()
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
}
