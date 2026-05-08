package com.savanna.browser.manager

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.view.Window

data class ThemePreset(
    val id: String,
    val name: String,
    val bgColor: Int,
    val accentColor: Int
)

class ThemeManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("savanna_theme", Context.MODE_PRIVATE)

    companion object {
        const val STYLE_GLASS   = "glass"
        const val STYLE_FROSTED = "frosted"
        const val STYLE_SOLID   = "solid"

        val PRESETS = listOf(
            ThemePreset("oled",     "OLED Black",    Color.parseColor("#000000"), Color.parseColor("#B6A8FF")),
            ThemePreset("slate",    "Slate",          Color.parseColor("#0C0C0E"), Color.parseColor("#A8D4FF")),
            ThemePreset("midnight", "Midnight Blue",  Color.parseColor("#000814"), Color.parseColor("#60A5FA")),
            ThemePreset("forest",   "Forest",         Color.parseColor("#0A1A0A"), Color.parseColor("#4ADE80")),
            ThemePreset("purple",   "Purple Haze",    Color.parseColor("#0D0014"), Color.parseColor("#D946EF")),
            ThemePreset("amber",    "Warm Amber",     Color.parseColor("#150800"), Color.parseColor("#FB923C"))
        )

        val ACCENT_COLORS = listOf(
            "#B6A8FF",  // Lavender (default)
            "#60A5FA",  // Sky Blue
            "#4ADE80",  // Mint Green
            "#FB923C",  // Orange
            "#F87171",  // Coral
            "#D946EF",  // Magenta
            "#FACC15",  // Gold
            "#22D3EE"   // Cyan
        )

        const val SIZE_SMALL  = "small"
        const val SIZE_MEDIUM = "medium"
        const val SIZE_LARGE  = "large"
    }

    // ── Preferences ──────────────────────────────────────────────────────────

    var themeId: String
        get() = prefs.getString("theme_id", "oled") ?: "oled"
        set(v) { prefs.edit().putString("theme_id", v).apply() }

    var customAccentHex: String
        get() = prefs.getString("accent_hex", "") ?: ""
        set(v) { prefs.edit().putString("accent_hex", v).apply() }

    var urlBarStyle: String
        get() = prefs.getString("url_bar_style", STYLE_GLASS) ?: STYLE_GLASS
        set(v) { prefs.edit().putString("url_bar_style", v).apply() }

    var textSize: String
        get() = prefs.getString("text_size", SIZE_MEDIUM) ?: SIZE_MEDIUM
        set(v) { prefs.edit().putString("text_size", v).apply() }

    var autoHideBars: Boolean
        get() = prefs.getBoolean("auto_hide_bars", false)
        set(v) { prefs.edit().putBoolean("auto_hide_bars", v).apply() }

    // ── Resolved values ───────────────────────────────────────────────────────

    val activePreset: ThemePreset
        get() = PRESETS.find { it.id == themeId } ?: PRESETS[0]

    val accentColor: Int
        get() {
            val hex = customAccentHex
            return if (hex.isNotBlank()) {
                try { Color.parseColor(hex) } catch (_: Exception) { activePreset.accentColor }
            } else activePreset.accentColor
        }

    val textSizeMultiplier: Float
        get() = when (textSize) {
            SIZE_SMALL -> 0.875f
            SIZE_LARGE -> 1.15f
            else       -> 1.0f
        }

    // ── Apply to window ───────────────────────────────────────────────────────

    fun applyToWindow(window: Window) {
        val bg = activePreset.bgColor
        window.statusBarColor    = bg
        window.navigationBarColor = bg
        window.decorView.setBackgroundColor(bg)
    }
}
