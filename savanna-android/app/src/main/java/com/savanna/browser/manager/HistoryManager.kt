package com.savanna.browser.manager

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.savanna.browser.model.HistoryItem
import java.io.File

class HistoryManager(private val context: Context) {
    private val history = mutableListOf<HistoryItem>()
    private val gson = Gson()
    private val fileName = "history.json"

    init {
        loadFromFile()
    }

    val allHistory: List<HistoryItem> get() = history.sortedByDescending { it.timestamp }

    fun addEntry(url: String, title: String) {
        if (url.isBlank() || url == "about:blank") return
        val item = HistoryItem(url = url, title = title.ifBlank { url })
        history.add(0, item)
        if (history.size > 500) {
            history.removeAt(history.size - 1)
        }
        saveToFile()
    }

    fun removeEntry(id: String) {
        history.removeAll { it.id == id }
        saveToFile()
    }

    fun clearHistory() {
        history.clear()
        saveToFile()
    }

    fun search(query: String): List<HistoryItem> {
        val q = query.lowercase()
        return history.filter {
            it.title.lowercase().contains(q) || it.url.lowercase().contains(q)
        }.sortedByDescending { it.timestamp }
    }

    fun getGroupedByDate(): Map<String, List<HistoryItem>> {
        val dateFormat = java.text.SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.getDefault())
        return allHistory.groupBy { dateFormat.format(java.util.Date(it.timestamp)) }
    }

    private fun saveToFile() {
        try {
            val file = File(context.filesDir, fileName)
            file.writeText(gson.toJson(history))
        } catch (_: Exception) { }
    }

    private fun loadFromFile() {
        try {
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                val type = object : TypeToken<List<HistoryItem>>() {}.type
                val items: List<HistoryItem> = gson.fromJson(file.readText(), type)
                history.addAll(items)
            }
        } catch (_: Exception) { }
    }
}
