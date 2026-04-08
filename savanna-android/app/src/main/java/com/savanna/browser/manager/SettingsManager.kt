package com.savanna.browser.manager

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("savanna_settings", Context.MODE_PRIVATE)

    companion object {
        const val KEY_SEARCH_ENGINE = "search_engine"
        const val KEY_BLOCK_TRACKERS = "block_trackers"
        const val KEY_BLOCK_ADS = "block_ads"
        const val KEY_JAVASCRIPT_ENABLED = "javascript_enabled"
        const val KEY_DARK_MODE = "dark_mode"
        const val KEY_TAB_MODE = "tab_mode"
        const val KEY_HOMEPAGE = "homepage"
        const val KEY_CLEAR_ON_EXIT = "clear_on_exit"
        const val KEY_BLOCK_POPUPS = "block_popups"
        const val KEY_DO_NOT_TRACK = "do_not_track"

        const val SEARCH_GOOGLE = "google"
        const val SEARCH_DUCKDUCKGO = "duckduckgo"
        const val SEARCH_BING = "bing"

        const val TAB_MODE_COMPACT = "compact"
        const val TAB_MODE_BOTTOM = "bottom"
    }

    var searchEngine: String
        get() = prefs.getString(KEY_SEARCH_ENGINE, SEARCH_GOOGLE) ?: SEARCH_GOOGLE
        set(value) = prefs.edit().putString(KEY_SEARCH_ENGINE, value).apply()

    var blockTrackers: Boolean
        get() = prefs.getBoolean(KEY_BLOCK_TRACKERS, true)
        set(value) = prefs.edit().putBoolean(KEY_BLOCK_TRACKERS, value).apply()

    var blockAds: Boolean
        get() = prefs.getBoolean(KEY_BLOCK_ADS, true)
        set(value) = prefs.edit().putBoolean(KEY_BLOCK_ADS, value).apply()

    var javascriptEnabled: Boolean
        get() = prefs.getBoolean(KEY_JAVASCRIPT_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_JAVASCRIPT_ENABLED, value).apply()

    var darkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, true)
        set(value) = prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()

    var tabMode: String
        get() = prefs.getString(KEY_TAB_MODE, TAB_MODE_BOTTOM) ?: TAB_MODE_BOTTOM
        set(value) = prefs.edit().putString(KEY_TAB_MODE, value).apply()

    var homepage: String
        get() = prefs.getString(KEY_HOMEPAGE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_HOMEPAGE, value).apply()

    var clearOnExit: Boolean
        get() = prefs.getBoolean(KEY_CLEAR_ON_EXIT, false)
        set(value) = prefs.edit().putBoolean(KEY_CLEAR_ON_EXIT, value).apply()

    var blockPopups: Boolean
        get() = prefs.getBoolean(KEY_BLOCK_POPUPS, true)
        set(value) = prefs.edit().putBoolean(KEY_BLOCK_POPUPS, value).apply()

    var doNotTrack: Boolean
        get() = prefs.getBoolean(KEY_DO_NOT_TRACK, true)
        set(value) = prefs.edit().putBoolean(KEY_DO_NOT_TRACK, value).apply()

    fun getSearchUrl(query: String): String {
        return when (searchEngine) {
            SEARCH_DUCKDUCKGO -> "https://duckduckgo.com/?q=${java.net.URLEncoder.encode(query, "UTF-8")}"
            SEARCH_BING -> "https://www.bing.com/search?q=${java.net.URLEncoder.encode(query, "UTF-8")}"
            else -> "https://www.google.com/search?q=${java.net.URLEncoder.encode(query, "UTF-8")}"
        }
    }
}
