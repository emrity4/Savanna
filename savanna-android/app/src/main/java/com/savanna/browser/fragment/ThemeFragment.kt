package com.savanna.browser.fragment

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.savanna.browser.MainActivity
import com.savanna.browser.R
import com.savanna.browser.manager.ThemeManager

class ThemeFragment : Fragment() {

    private lateinit var themeManager: ThemeManager
    private val presetCards   = mutableListOf<LinearLayout>()
    private val accentDots    = mutableListOf<View>()
    private val sizeButtons   = mutableListOf<TextView>()
    private val styleButtons  = mutableListOf<TextView>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_theme, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as MainActivity
        themeManager = activity.themeManager
        view.setBackgroundColor(themeManager.activePreset.bgColor)

        view.findViewById<ImageView>(R.id.btn_close_theme).setOnClickListener {
            activity.closeOverlay()
        }

        setupPresetCards(view)
        setupAccentColors(view)
        setupUrlBarStyles(view)
        setupTextSize(view)
        setupAutoHide(view)
    }

    private fun setupPresetCards(root: View) {
        val container = root.findViewById<LinearLayout>(R.id.theme_presets_container)
        container.removeAllViews()
        presetCards.clear()

        val selected = themeManager.themeId

        ThemeManager.PRESETS.forEach { preset ->
            val card = layoutInflater.inflate(R.layout.item_theme_preset, container, false) as LinearLayout
            val circle: View = card.findViewById(R.id.preset_color_circle)
            val name: TextView = card.findViewById(R.id.preset_name)
            val accent: View = card.findViewById(R.id.preset_accent_dot)

            (circle.background as? GradientDrawable)?.setColor(preset.bgColor)
                ?: circle.setBackgroundColor(preset.bgColor)
            (accent.background as? GradientDrawable)?.setColor(preset.accentColor)

            name.text = preset.name
            applyCardSelection(card, preset.id == selected)

            card.setOnClickListener {
                themeManager.themeId = preset.id
                themeManager.customAccentHex = ""
                highlightPreset(preset.id)
                view?.setBackgroundColor(preset.bgColor)
                applyThemeNow()
            }

            container.addView(card)
            presetCards.add(card)
        }
    }

    private fun highlightPreset(selectedId: String) {
        ThemeManager.PRESETS.forEachIndexed { i, preset ->
            applyCardSelection(presetCards[i], preset.id == selectedId)
        }
    }

    private fun applyCardSelection(card: LinearLayout, selected: Boolean) {
        card.setBackgroundResource(if (selected) R.drawable.theme_card_selected else R.drawable.theme_card_background)
    }

    private fun setupAccentColors(root: View) {
        val container = root.findViewById<LinearLayout>(R.id.accent_colors_container)
        container.removeAllViews()
        accentDots.clear()

        val currentHex = themeManager.customAccentHex.ifBlank {
            String.format("#%06X", 0xFFFFFF and themeManager.activePreset.accentColor)
        }

        ThemeManager.ACCENT_COLORS.forEach { hex ->
            val dot = View(requireContext())
            val size = (42 * resources.displayMetrics.density).toInt()
            val margin = (6 * resources.displayMetrics.density).toInt()
            val lp = LinearLayout.LayoutParams(size, size).apply { setMargins(margin, margin, margin, margin) }
            dot.layoutParams = lp

            val color = try { Color.parseColor(hex) } catch (_: Exception) { Color.WHITE }
            val shape = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(color)
                setStroke((if (hex.equals(currentHex, ignoreCase = true)) 3 else 0) * resources.displayMetrics.density.toInt(), Color.WHITE)
            }
            dot.background = shape

            dot.setOnClickListener {
                themeManager.customAccentHex = hex
                updateAccentSelection(hex)
                applyThemeNow()
            }

            container.addView(dot)
            accentDots.add(dot)
        }
    }

    private fun updateAccentSelection(selectedHex: String) {
        ThemeManager.ACCENT_COLORS.forEachIndexed { i, hex ->
            val d = accentDots[i].background as? GradientDrawable ?: return@forEachIndexed
            val selected = hex.equals(selectedHex, ignoreCase = true)
            d.setStroke((if (selected) 3 else 0) * resources.displayMetrics.density.toInt(), Color.WHITE)
        }
    }

    private fun setupUrlBarStyles(root: View) {
        val btnGlass   = root.findViewById<TextView>(R.id.style_glass)
        val btnFrosted = root.findViewById<TextView>(R.id.style_frosted)
        val btnSolid   = root.findViewById<TextView>(R.id.style_solid)
        styleButtons.clear()
        styleButtons.addAll(listOf(btnGlass, btnFrosted, btnSolid))

        val styles = listOf(ThemeManager.STYLE_GLASS, ThemeManager.STYLE_FROSTED, ThemeManager.STYLE_SOLID)
        val current = themeManager.urlBarStyle

        styles.forEachIndexed { i, s -> applyChipSelection(styleButtons[i], s == current) }

        btnGlass.setOnClickListener   { selectStyle(ThemeManager.STYLE_GLASS, styles) }
        btnFrosted.setOnClickListener { selectStyle(ThemeManager.STYLE_FROSTED, styles) }
        btnSolid.setOnClickListener   { selectStyle(ThemeManager.STYLE_SOLID, styles) }
    }

    private fun selectStyle(style: String, all: List<String>) {
        themeManager.urlBarStyle = style
        all.forEachIndexed { i, s -> applyChipSelection(styleButtons[i], s == style) }
    }

    private fun setupTextSize(root: View) {
        val btnSmall  = root.findViewById<TextView>(R.id.size_small)
        val btnMedium = root.findViewById<TextView>(R.id.size_medium)
        val btnLarge  = root.findViewById<TextView>(R.id.size_large)
        sizeButtons.clear()
        sizeButtons.addAll(listOf(btnSmall, btnMedium, btnLarge))

        val sizes   = listOf(ThemeManager.SIZE_SMALL, ThemeManager.SIZE_MEDIUM, ThemeManager.SIZE_LARGE)
        val current = themeManager.textSize

        sizes.forEachIndexed { i, s -> applyChipSelection(sizeButtons[i], s == current) }

        btnSmall.setOnClickListener  { selectSize(ThemeManager.SIZE_SMALL, sizes) }
        btnMedium.setOnClickListener { selectSize(ThemeManager.SIZE_MEDIUM, sizes) }
        btnLarge.setOnClickListener  { selectSize(ThemeManager.SIZE_LARGE, sizes) }
    }

    private fun selectSize(size: String, all: List<String>) {
        themeManager.textSize = size
        all.forEachIndexed { i, s -> applyChipSelection(sizeButtons[i], s == size) }
    }

    private fun setupAutoHide(root: View) {
        val sw = root.findViewById<com.google.android.material.materialswitch.MaterialSwitch>(R.id.switch_auto_hide_bars)
        sw.isChecked = themeManager.autoHideBars
        sw.setOnCheckedChangeListener { _, checked -> themeManager.autoHideBars = checked }
    }

    private fun applyChipSelection(chip: TextView, selected: Boolean) {
        val accent = themeManager.accentColor
        chip.setBackgroundResource(R.drawable.action_chip_background)
        chip.text = chip.text
        chip.setTextColor(if (selected) accent else Color.parseColor("#99FFFFFF"))
        chip.backgroundTintList = if (selected) {
            ColorStateList.valueOf(Color.argb(34, Color.red(accent), Color.green(accent), Color.blue(accent)))
        } else {
            null
        }
    }

    private fun applyThemeNow() {
        val activity = requireActivity() as MainActivity
        activity.themeManager.applyToWindow(activity.window)
    }
}
