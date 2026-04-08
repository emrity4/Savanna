package com.savanna.browser.model

data class TrackerInfo(
    val domain: String,
    val category: String,
    val blockedCount: Int = 0,
    val lastBlocked: Long = System.currentTimeMillis()
)

data class PrivacyReport(
    val totalTrackersBlocked: Int = 0,
    val trackersByCategory: Map<String, Int> = emptyMap(),
    val topTrackerDomains: List<TrackerInfo> = emptyList(),
    val sitesVisited: Int = 0
)
