package com.savanna.browser.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import com.savanna.browser.MainActivity
import com.savanna.browser.R
import com.savanna.browser.manager.SettingsManager

class SettingsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as MainActivity
        val settings = activity.settingsManager
        val theme = activity.themeManager

        val btnClose = view.findViewById<ImageView>(R.id.btn_close_settings)
        val themeValue = view.findViewById<TextView>(R.id.theme_value)
        val searchEngineValue = view.findViewById<TextView>(R.id.search_engine_value)
        val tabModeValue = view.findViewById<TextView>(R.id.tab_mode_value)
        val switchJs = view.findViewById<MaterialSwitch>(R.id.switch_javascript)
        val switchTrackers = view.findViewById<MaterialSwitch>(R.id.switch_block_trackers)
        val switchAds = view.findViewById<MaterialSwitch>(R.id.switch_block_ads)
        val switchPopups = view.findViewById<MaterialSwitch>(R.id.switch_block_popups)
        val switchDnt = view.findViewById<MaterialSwitch>(R.id.switch_do_not_track)
        val switchClearOnExit = view.findViewById<MaterialSwitch>(R.id.switch_clear_on_exit)
        val btnFire = view.findViewById<TextView>(R.id.btn_fire)

        themeValue.text = theme.activePreset.name
        searchEngineValue.text = getSearchEngineName(settings.searchEngine)
        tabModeValue.text = getTabModeName(settings.tabMode)
        switchJs.isChecked = settings.javascriptEnabled
        switchTrackers.isChecked = settings.blockTrackers
        switchAds.isChecked = settings.blockAds
        switchPopups.isChecked = settings.blockPopups
        switchDnt.isChecked = settings.doNotTrack
        switchClearOnExit.isChecked = settings.clearOnExit

        view.findViewById<View>(R.id.setting_theme).setOnClickListener {
            activity.closeOverlay()
            activity.showTheme()
        }

        view.findViewById<View>(R.id.setting_search_engine).setOnClickListener {
            showSearchEngineDialog(settings, searchEngineValue)
        }

        view.findViewById<View>(R.id.setting_tab_mode).setOnClickListener {
            showTabModeDialog(settings, tabModeValue)
        }

        switchJs.setOnCheckedChangeListener { _, v -> settings.javascriptEnabled = v }
        switchTrackers.setOnCheckedChangeListener { _, v -> settings.blockTrackers = v }
        switchAds.setOnCheckedChangeListener { _, v -> settings.blockAds = v }
        switchPopups.setOnCheckedChangeListener { _, v -> settings.blockPopups = v }
        switchDnt.setOnCheckedChangeListener { _, v -> settings.doNotTrack = v }
        switchClearOnExit.setOnCheckedChangeListener { _, v -> settings.clearOnExit = v }

        btnFire.setOnClickListener { showFireDialog(activity) }
        btnClose.setOnClickListener { activity.closeOverlay() }
    }

    private fun showSearchEngineDialog(settings: SettingsManager, valueView: TextView) {
        val options = arrayOf("Google", "DuckDuckGo", "Bing")
        val values = arrayOf(SettingsManager.SEARCH_GOOGLE, SettingsManager.SEARCH_DUCKDUCKGO, SettingsManager.SEARCH_BING)
        val current = values.indexOf(settings.searchEngine).coerceAtLeast(0)
        AlertDialog.Builder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
            .setTitle("Search Engine")
            .setSingleChoiceItems(options, current) { d, i ->
                settings.searchEngine = values[i]
                valueView.text = options[i]
                d.dismiss()
            }.show()
    }

    private fun showTabModeDialog(settings: SettingsManager, valueView: TextView) {
        val options = arrayOf("Bottom", "Compact")
        val values = arrayOf(SettingsManager.TAB_MODE_BOTTOM, SettingsManager.TAB_MODE_COMPACT)
        val current = values.indexOf(settings.tabMode).coerceAtLeast(0)
        AlertDialog.Builder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
            .setTitle("Tab Mode")
            .setSingleChoiceItems(options, current) { d, i ->
                settings.tabMode = values[i]
                valueView.text = options[i]
                d.dismiss()
            }.show()
    }

    private fun showFireDialog(activity: MainActivity) {
        AlertDialog.Builder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
            .setTitle("Fire")
            .setMessage("This will clear browsing history, bookmarks, cookies, site data, permissions, and local browser data.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Fire") { _, _ -> clearAllData(activity) }
            .show()
    }

    private fun clearAllData(activity: MainActivity) {
        activity.historyManager.clearHistory()
        activity.bookmarkManager.clearBookmarks()
        activity.settingsManager.clearOnExit = false
        android.webkit.CookieManager.getInstance().removeAllCookies(null)
        android.webkit.CookieManager.getInstance().flush()
        android.webkit.WebStorage.getInstance().deleteAllData()
    }

    private fun getSearchEngineName(value: String) = when (value) {
        SettingsManager.SEARCH_DUCKDUCKGO -> "DuckDuckGo"
        SettingsManager.SEARCH_BING -> "Bing"
        else -> "Google"
    }

    private fun getTabModeName(value: String) = when (value) {
        SettingsManager.TAB_MODE_COMPACT -> "Compact"
        else -> "Bottom"
    }
}
