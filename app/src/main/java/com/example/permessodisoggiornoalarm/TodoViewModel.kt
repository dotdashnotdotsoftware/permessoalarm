package com.example.permessodisoggiornoalarm

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel

class TodoViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("todo_prefs", Context.MODE_PRIVATE)
    val todoItems = mutableStateListOf<String>()

    init {
        loadItems()
    }

    private fun loadItems() {
        val savedString = prefs.getString("items", "") ?: ""
        if (savedString.isNotEmpty()) {
            val items = savedString.split("\n")
            todoItems.addAll(items)
        }
    }

    fun addItem(item: String) {
        todoItems.add(item)
        saveItems()
    }

    fun removeItem(item: String) {
        todoItems.remove(item)
        saveItems()
    }

    private fun saveItems() {
        val stringToSave = todoItems.joinToString("\n")
        prefs.edit().putString("items", stringToSave).apply()
    }
}
