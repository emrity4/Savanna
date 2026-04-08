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
import com.savanna.browser.MainActivity
import com.savanna.browser.R
import com.savanna.browser.manager.SettingsManager

class SettingsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as MainActivity
        val settings = activity.settingsManager

        val btnClose: ImageView = view.findViewById(R.id.btn_close_settings)
        val searchEngineValue: TextView = view.findViewById(R.id.search_engine_value)
        val tabModeValue: TextView = view.findViewById(R.id.tab_mode_value)
        val switchJs: MaterialSwitch = view.findViewById(R.id.switch_javascript)
        val switchTrackers: MaterialSwitch = view.findViewById(R.id.switch_block_trackers)
        val switchAds: MaterialSwitch = view.findViewById(R.id.switch_block_ads)
        val switchPopups: MaterialSwitch = view.findViewById(R.id.switch_block_popups)
        val switchDnt: MaterialSwitch = view.findViewById(R.id.switch_do_not_track)
        val switchClearOnExit: MaterialSwitch = view.findViewById(R.id.switch_clear_on_exit)

        searchEngineValue.text = getSearchEngineName(settings.searchEngine)
        tabModeValue.text = getTabModeName(settings.tabMode)
        switchJs.isChecked = settings.javascriptEnabled
        switchTrackers.isChecked = settings.blockTrackers
        switchAds.isChecked = settings.blockAds
        switchPopups.isChecked = settings.blockPopups
        switchDnt.isChecked = settings.doNotTrack
        switchClearOnExit.isChecked = settings.clearOnExit

        view.findViewById<View>(R.id.setting_search_engine).setOnClickListener {
            showSearchEngineDialog(settings, searchEngineValue)
        }

        view.findViewById<View>(R.id.setting_tab_mode).setOnClickListener {
            showTabModeDialog(settings, tabModeValue)
        }

        switchJs.setOnCheckedChangeListener { _, isChecked ->
            settings.javascriptEnabled = isChecked
        }

        switchTrackers.setOnCheckedChangeListener { _, isChecked ->
            settings.blockTrackers = isChecked
        }

        switchAds.setOnCheckedChangeListener { _, isChecked ->
            settings.blockAds = isChecked
        }

        switchPopups.setOnCheckedChangeListener { _, isChecked ->
            settings.blockPopups = isChecked
        }

        switchDnt.setOnCheckedChangeListener { _, isChecked ->
            settings.doNotTrack = isChecked
        }

        switchClearOnExit.setOnCheckedChangeListener { _, isChecked ->
            settings.clearOnExit = isChecked
        }

        btnClose.setOnClickListener {
            activity.closeOverlay()
        }
    }

    private fun showSearchEngineDialog(settings: SettingsManager, valueView: TextView) {
        val options = arrayOf("Google", "DuckDuckGo", "Bing")
        val values = arrayOf(SettingsManager.SEARCH_GOOGLE, SettingsManager.SEARCH_DUCKDUCKGO, SettingsManager.SEARCH_BING)
        val currentIndex = values.indexOf(settings.searchEngine).coerceAtLeast(0)

        AlertDialog.Builder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
            .setTitle("Search Engine")
            .setSingleChoiceItems(options, currentIndex) { dialog, which ->
                settings.searchEngine = values[which]
                valueView.text = options[which]
                dialog.dismiss()
            }
            .show()
    }

    private fun showTabModeDialog(settings: SettingsManager, valueView: TextView) {
        val options = arrayOf("Bottom", "Compact")
        val values = arrayOf(SettingsManager.TAB_MODE_BOTTOM, SettingsManager.TAB_MODE_COMPACT)
        val currentIndex = values.indexOf(settings.tabMode).coerceAtLeast(0)

        AlertDialog.Builder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
            .setTitle("Tab Mode")
            .setSingleChoiceItems(options, currentIndex) { dialog, which ->
                settings.tabMode = values[which]
                valueView.text = options[which]
                dialog.dismiss()
            }
            .show()
    }

    private fun getSearchEngineName(value: String): String {
        return when (value) {
            SettingsManager.SEARCH_DUCKDUCKGO -> "DuckDuckGo"
            SettingsManager.SEARCH_BING -> "Bing"
            else -> "Google"
        }
    }

    private fun getTabModeName(value: String): String {
        return when (value) {
            SettingsManager.TAB_MODE_COMPACT -> "Compact"
            else -> "Bottom"
        }
    }
}
