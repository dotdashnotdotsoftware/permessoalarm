package com.example.permessodisoggiornoalarm

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import org.json.JSONObject

class TodoViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("todo_prefs", Context.MODE_PRIVATE)
    val todoItems = mutableStateListOf<TodoItem>()

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
                    val value = json.getString("value")
                    todoItems.add(TodoItem(name, value))
                } catch (e: Exception) {
                    // Ignore malformed entries or handle appropriately
                }
            }
        }
    }

    fun addItem(name: String, value: String) {
        todoItems.add(TodoItem(name, value))
        saveItems()
    }

    fun removeItem(item: TodoItem) {
        todoItems.remove(item)
        saveItems()
    }

    private fun saveItems() {
        val stringToSave = todoItems.joinToString("\n") { item ->
            val json = JSONObject()
            json.put("name", item.name)
            json.put("value", item.value)
            json.toString()
        }
        prefs.edit().putString("items", stringToSave).apply()
    }
}
