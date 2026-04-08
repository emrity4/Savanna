package com.savanna.browser.model

data class Tab(
    val id: String = java.util.UUID.randomUUID().toString(),
    var url: String = "",
    var title: String = "New Tab",
    var isActive: Boolean = false,
    var favicon: String? = null,
    var isLoading: Boolean = false,
    var progress: Int = 0,
    var canGoBack: Boolean = false,
    var canGoForward: Boolean = false
)
