package com.savanna.browser.manager

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.savanna.browser.model.Bookmark
import java.io.File

class BookmarkManager(private val context: Context) {
    private val bookmarks = mutableListOf<Bookmark>()
    private val gson = Gson()
    private val fileName = "bookmarks.json"

    init {
        loadFromFile()
    }

    val allBookmarks: List<Bookmark> get() = bookmarks.sortedByDescending { it.createdAt }

    fun addBookmark(url: String, title: String): Bookmark {
        val existing = bookmarks.find { it.url == url }
        if (existing != null) return existing

        val bookmark = Bookmark(url = url, title = title.ifBlank { url })
        bookmarks.add(0, bookmark)
        saveToFile()
        return bookmark
    }

    fun removeBookmark(id: String) {
        bookmarks.removeAll { it.id == id }
        saveToFile()
    }

    fun removeByUrl(url: String) {
        bookmarks.removeAll { it.url == url }
        saveToFile()
    }

    fun isBookmarked(url: String): Boolean = bookmarks.any { it.url == url }

    fun search(query: String): List<Bookmark> {
        val q = query.lowercase()
        return bookmarks.filter {
            it.title.lowercase().contains(q) || it.url.lowercase().contains(q)
        }
    }

    fun clearBookmarks() {
        bookmarks.clear()
        saveToFile()
    }

    private fun saveToFile() {
        try {
            val file = File(context.filesDir, fileName)
            file.writeText(gson.toJson(bookmarks))
        } catch (_: Exception) { }
    }

    private fun loadFromFile() {
        try {
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                val type = object : TypeToken<List<Bookmark>>() {}.type
                val items: List<Bookmark> = gson.fromJson(file.readText(), type)
                bookmarks.addAll(items)
            }
        } catch (_: Exception) { }
    }
}
