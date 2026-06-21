package com.savanna.browser.manager

import com.savanna.browser.model.Tab

class TabManager {
    private val tabs = mutableListOf<Tab>()
    private var activeTabId: String? = null

    val allTabs: List<Tab> get() = tabs.toList()
    val tabCount: Int get() = tabs.size

    fun getActiveTab(): Tab? = tabs.find { it.id == activeTabId }

    fun createTab(url: String = "", title: String = "New Tab"): Tab {
        val tab = Tab(url = url, title = title)
        tabs.forEach { it.isActive = false }
        tab.isActive = true
        tabs.add(tab)
        activeTabId = tab.id
        return tab
    }

    fun switchToTab(tabId: String): Tab? {
        tabs.forEach { it.isActive = (it.id == tabId) }
        activeTabId = tabId
        return getActiveTab()
    }

    fun closeTab(tabId: String): Tab? {
        val index = tabs.indexOfFirst { it.id == tabId }
        if (index == -1) return getActiveTab()

        tabs.removeAt(index)

        if (tabs.isEmpty()) {
            return createTab()
        }

        if (tabId == activeTabId) {
            val newIndex = if (index >= tabs.size) tabs.size - 1 else index
            return switchToTab(tabs[newIndex].id)
        }

        return getActiveTab()
    }

    fun updateTab(tabId: String, url: String? = null, title: String? = null,
                  isLoading: Boolean? = null, progress: Int? = null,
                  canGoBack: Boolean? = null, canGoForward: Boolean? = null) {
        tabs.find { it.id == tabId }?.apply {
            val prevUrl = this.url
            url?.let { this.url = it }
            title?.let { this.title = it }
            isLoading?.let { this.isLoading = it }
            progress?.let { this.progress = it }
            canGoBack?.let { this.canGoBack = it }
            canGoForward?.let { this.canGoForward = it }
            if (url != null && url != prevUrl && url.startsWith("http")) {
                if (prevUrl.isNotBlank() && prevUrl.startsWith("http")) {
                    pageHistory.add(prevUrl)
                }
                forwardStack.clear()
            }
        }
    }

    fun getBackHistory(tabId: String): List<String> {
        val tab = getTabById(tabId) ?: return emptyList()
        return tab.pageHistory.toList().reversed()
    }

    fun goBackInHistory(tabId: String): String? {
        val tab = getTabById(tabId) ?: return null
        if (tab.pageHistory.isEmpty()) return null
        val prevUrl = tab.pageHistory.removeAt(tab.pageHistory.size - 1)
        tab.forwardStack.add(tab.url)
        tab.url = prevUrl
        return prevUrl
    }

    fun getTabById(tabId: String): Tab? = tabs.find { it.id == tabId }
}
