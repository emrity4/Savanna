package com.savanna.browser.manager

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.view.Window

class ThemeManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("savanna_theme", Context.MODE_PRIVATE)

    companion object {
        const val STYLE_GLASS   = "glass"
        const val STYLE_FROSTED = "frosted"
        const val STYLE_SOLID   = "solid"

        const val SIZE_SMALL  = "small"
        const val SIZE_MEDIUM = "medium"
        const val SIZE_LARGE  = "large"

        val DARK_BG  = 0xFF000000.toInt()
        val LIGHT_BG = 0xFFFFFFFF.toInt()
        val ACCENT   = 0xFFB6A8FF.toInt()
    }

    var isDarkMode: Boolean
        get() = prefs.getBoolean("dark_mode", true)
        set(v) { prefs.edit().putBoolean("dark_mode", v).apply() }

    var urlBarStyle: String
        get() = prefs.getString("url_bar_style", STYLE_GLASS) ?: STYLE_GLASS
        set(v) { prefs.edit().putString("url_bar_style", v).apply() }

    var textSize: String
        get() = prefs.getString("text_size", SIZE_MEDIUM) ?: SIZE_MEDIUM
        set(v) { prefs.edit().putString("text_size", v).apply() }

    var autoHideBars: Boolean
        get() = prefs.getBoolean("auto_hide_bars", false)
        set(v) { prefs.edit().putBoolean("auto_hide_bars", v).apply() }

    val bgColor: Int get() = if (isDarkMode) DARK_BG else LIGHT_BG
    val accentColor: Int get() = ACCENT

    val textSizeMultiplier: Float
        get() = when (textSize) {
            SIZE_SMALL -> 0.875f
            SIZE_LARGE -> 1.15f
            else       -> 1.0f
        }

    fun applyToWindow(window: Window) {
        val bg = bgColor
        window.statusBarColor    = bg
        window.navigationBarColor = bg
        window.decorView.setBackgroundColor(bg)
    }
}
