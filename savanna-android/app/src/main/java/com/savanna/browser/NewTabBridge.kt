package com.savanna.browser

import android.webkit.JavascriptInterface
import com.google.gson.Gson
import com.savanna.browser.manager.HistoryManager

/**
 * JavaScript ↔ Kotlin bridge exposed to new_tab.html.
 * Methods called via  Android.xxx()  in the page script.
 */
class NewTabBridge(
    private val historyManager: HistoryManager,
    private val onNavigate: (String) -> Unit,
    private val onFocusUrlBar: () -> Unit,
    private val onOpenFile: () -> Unit
) {

    private val gson = Gson()

    /** Returns recent history as JSON array of {url, title} objects. */
    @JavascriptInterface
    fun getRecentHistory(): String {
        return try {
            val items = historyManager.allHistory.take(50).map {
                mapOf("url" to it.url, "title" to it.title)
            }
            gson.toJson(items)
        } catch (_: Exception) { "[]" }
    }

    /** Navigate to a URL — called when user taps a site tile. */
    @JavascriptInterface
    fun navigate(url: String) {
        onNavigate(url)
    }

    /** Focus the native URL bar — called when user taps the glass search bar. */
    @JavascriptInterface
    fun focusUrlBar() {
        onFocusUrlBar()
    }

    /** Open a file — called when user taps the file open button. */
    @JavascriptInterface
    fun openFile() {
        onOpenFile()
    }
}
