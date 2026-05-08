package com.savanna.browser

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.savanna.browser.fragment.*
import com.savanna.browser.manager.*

class MainActivity : AppCompatActivity() {

    lateinit var tabManager: TabManager
    lateinit var historyManager: HistoryManager
    lateinit var bookmarkManager: BookmarkManager
    lateinit var settingsManager: SettingsManager
    lateinit var trackerBlocker: TrackerBlocker
    lateinit var downloadManager: AppDownloadManager
    lateinit var themeManager: ThemeManager

    private var currentBrowserFragment: BrowserFragment? = null
    private var isOverlayShowing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        themeManager    = ThemeManager(this)
        themeManager.applyToWindow(window)   // apply background + status/nav bar color

        setContentView(R.layout.activity_main)

        tabManager      = TabManager()
        historyManager  = HistoryManager(this)
        bookmarkManager = BookmarkManager(this)
        settingsManager = SettingsManager(this)
        trackerBlocker  = TrackerBlocker(this)
        downloadManager = AppDownloadManager(this)

        val initialUrl = intent?.data?.toString() ?: ""
        val tab = tabManager.createTab(url = initialUrl, title = "New Tab")
        showBrowserForTab(tab.id, initialUrl)
    }

    private fun showBrowserForTab(tabId: String, url: String = "") {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        val fragment = BrowserFragment.newInstance(tabId, url)
        currentBrowserFragment = fragment
        isOverlayShowing = false

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            .replace(R.id.fragment_container, fragment, "browser")
            .commit()
    }

    private fun showOverlayFragment(
        fragment: androidx.fragment.app.Fragment, tag: String,
        enterAnim: Int = R.anim.slide_in_bottom,
        exitAnim: Int  = R.anim.slide_out_bottom
    ) {
        if (isOverlayShowing) return
        isOverlayShowing = true
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(enterAnim, R.anim.fade_out, R.anim.fade_in, exitAnim)
            .add(R.id.fragment_container, fragment, tag)
            .addToBackStack(tag)
            .commit()
    }

    fun showTabSwitcher()   = showOverlayFragment(TabSwitcherFragment(),   "tab_switcher")
    fun showHistory()       = showOverlayFragment(HistoryFragment(),        "history")
    fun showBookmarks()     = showOverlayFragment(BookmarksFragment(),      "bookmarks")
    fun showPrivacyReport() = showOverlayFragment(PrivacyReportFragment(),  "privacy_report")
    fun showDownloads()     = showOverlayFragment(DownloadsFragment(),      "downloads")
    fun showTheme()         = showOverlayFragment(ThemeFragment(),          "theme",
                                  R.anim.slide_in_right, R.anim.slide_out_left)
    fun showSettings()      = showOverlayFragment(SettingsFragment(),       "settings",
                                  R.anim.slide_in_right, R.anim.slide_out_left)

    fun closeOverlay() {
        isOverlayShowing = false
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        }
    }

    fun switchToTab(tabId: String) {
        tabManager.switchToTab(tabId)
        val tab = tabManager.getTabById(tabId) ?: return
        showBrowserForTab(tabId, tab.url)
    }

    fun closeTab(tabId: String) {
        val resultTab = tabManager.closeTab(tabId)
        resultTab?.let { showBrowserForTab(it.id, it.url) }
    }

    fun createNewTab(url: String = "") {
        val tab = tabManager.createTab(url = url, title = "New Tab")
        showBrowserForTab(tab.id, url)
    }

    fun navigateToUrl(url: String) {
        val activeTab = tabManager.getActiveTab()
        if (activeTab != null) showBrowserForTab(activeTab.id, url)
        else createNewTab(url = url)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (isOverlayShowing) { closeOverlay(); return }
        val browserFragment = currentBrowserFragment
        if (browserFragment != null && browserFragment.canGoBack()) {
            browserFragment.goBack(); return
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
