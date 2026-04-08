package com.savanna.browser

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.savanna.browser.fragment.*
import com.savanna.browser.manager.*

class MainActivity : AppCompatActivity() {

    lateinit var tabManager: TabManager
    lateinit var historyManager: HistoryManager
    lateinit var bookmarkManager: BookmarkManager
    lateinit var settingsManager: SettingsManager
    lateinit var trackerBlocker: TrackerBlocker

    private var currentBrowserFragment: BrowserFragment? = null
    private var isOverlayShowing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tabManager = TabManager()
        historyManager = HistoryManager(this)
        bookmarkManager = BookmarkManager(this)
        settingsManager = SettingsManager(this)
        trackerBlocker = TrackerBlocker(this)

        val initialUrl = intent?.data?.toString() ?: ""
        val tab = tabManager.createTab(url = initialUrl, title = "New Tab")
        showBrowserForTab(tab.id, initialUrl)
    }

    private fun showBrowserForTab(tabId: String, url: String = "") {
        val fragment = BrowserFragment.newInstance(tabId, url)
        currentBrowserFragment = fragment
        isOverlayShowing = false

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            .replace(R.id.fragment_container, fragment, "browser_$tabId")
            .commit()
    }

    fun showTabSwitcher() {
        isOverlayShowing = true
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_bottom, R.anim.fade_out,
                R.anim.fade_in, R.anim.slide_out_bottom)
            .replace(R.id.fragment_container, TabSwitcherFragment(), "tab_switcher")
            .addToBackStack("tab_switcher")
            .commit()
    }

    fun showHistory() {
        isOverlayShowing = true
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_bottom, R.anim.fade_out,
                R.anim.fade_in, R.anim.slide_out_bottom)
            .replace(R.id.fragment_container, HistoryFragment(), "history")
            .addToBackStack("history")
            .commit()
    }

    fun showBookmarks() {
        isOverlayShowing = true
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_bottom, R.anim.fade_out,
                R.anim.fade_in, R.anim.slide_out_bottom)
            .replace(R.id.fragment_container, BookmarksFragment(), "bookmarks")
            .addToBackStack("bookmarks")
            .commit()
    }

    fun showSettings() {
        isOverlayShowing = true
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                R.anim.slide_in_right, R.anim.slide_out_left)
            .replace(R.id.fragment_container, SettingsFragment(), "settings")
            .addToBackStack("settings")
            .commit()
    }

    fun showPrivacyReport() {
        isOverlayShowing = true
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_bottom, R.anim.fade_out,
                R.anim.fade_in, R.anim.slide_out_bottom)
            .replace(R.id.fragment_container, PrivacyReportFragment(), "privacy_report")
            .addToBackStack("privacy_report")
            .commit()
    }

    fun closeOverlay() {
        isOverlayShowing = false
        supportFragmentManager.popBackStack()
    }

    fun switchToTab(tabId: String) {
        tabManager.switchToTab(tabId)
        val tab = tabManager.getTabById(tabId)
        if (isOverlayShowing) {
            supportFragmentManager.popBackStack()
        }
        showBrowserForTab(tabId, tab?.url ?: "")
    }

    fun closeTab(tabId: String) {
        val resultTab = tabManager.closeTab(tabId)
        resultTab?.let {
            if (isOverlayShowing) return
            showBrowserForTab(it.id, it.url)
        }
    }

    fun createNewTab(url: String = "", title: String = "New Tab") {
        val tab = tabManager.createTab(url = url, title = title)
        if (isOverlayShowing) {
            supportFragmentManager.popBackStack()
        }
        showBrowserForTab(tab.id, url)
    }

    fun navigateToUrl(url: String) {
        if (isOverlayShowing) {
            supportFragmentManager.popBackStack()
        }
        val activeTab = tabManager.getActiveTab()
        if (activeTab != null) {
            showBrowserForTab(activeTab.id, url)
        } else {
            createNewTab(url = url)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (isOverlayShowing) {
            closeOverlay()
            return
        }

        val browserFragment = currentBrowserFragment
        if (browserFragment != null && browserFragment.canGoBack()) {
            browserFragment.goBack()
            return
        }

        super.onBackPressed()
    }

    override fun onDestroy() {
        if (settingsManager.clearOnExit) {
            historyManager.clearHistory()
            android.webkit.CookieManager.getInstance().removeAllCookies(null)
            android.webkit.WebStorage.getInstance().deleteAllData()
        }
        super.onDestroy()
    }
}
