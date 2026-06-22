package com.savanna.browser.fragment

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.savanna.browser.MainActivity
import com.savanna.browser.R
import com.savanna.browser.manager.ThemeManager

class ThemeFragment : Fragment() {

    private lateinit var themeManager: ThemeManager
    private val sizeButtons   = mutableListOf<TextView>()
    private val styleButtons  = mutableListOf<TextView>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_theme, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as MainActivity
        themeManager = activity.themeManager
        view.setBackgroundColor(themeManager.bgColor)

        view.findViewById<ImageView>(R.id.btn_close_theme).setOnClickListener {
            activity.closeOverlay()
        }

        setupDarkMode(view)
        setupUrlBarStyles(view)
        setupTextSize(view)
        setupAutoHide(view)
    }

    private fun setupDarkMode(root: View) {
        val sw = root.findViewById<com.google.android.material.materialswitch.MaterialSwitch>(R.id.switch_dark_mode)
        sw.isChecked = themeManager.isDarkMode
        sw.setOnCheckedChangeListener { _, checked ->
            themeManager.isDarkMode = checked
            // rebuild UI with new theme
            AppCompatDelegate.setDefaultNightMode(
                if (checked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
            requireActivity().recreate()
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
}
