package com.savanna.browser.manager

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.savanna.browser.model.TrackerInfo
import com.savanna.browser.model.PrivacyReport
import java.io.File

class TrackerBlocker(private val context: Context) {
    private val blockedTrackers = mutableListOf<TrackerInfo>()
    private val gson = Gson()
    private val fileName = "tracker_data.json"

    private val trackerDomains = mapOf(
        "analytics" to listOf(
            "google-analytics.com", "analytics.google.com", "googletagmanager.com",
            "hotjar.com", "mixpanel.com", "segment.com", "amplitude.com",
            "heap.io", "fullstory.com", "mouseflow.com", "crazyegg.com"
        ),
        "advertising" to listOf(
            "doubleclick.net", "googlesyndication.com", "googleadservices.com",
            "facebook.net", "fbcdn.net", "ads-twitter.com", "advertising.com",
            "adnxs.com", "criteo.com", "outbrain.com", "taboola.com",
            "amazon-adsystem.com", "moatads.com", "adsrvr.org"
        ),
        "social" to listOf(
            "facebook.com/tr", "connect.facebook.net", "platform.twitter.com",
            "platform.linkedin.com", "apis.google.com/js/plusone",
            "pinterest.com/js", "snap.licdn.com"
        ),
        "fingerprinting" to listOf(
            "fingerprintjs.com", "cdn.cookielaw.org", "bat.bing.com",
            "clarity.ms", "browser-update.org"
        )
    )

    init {
        loadFromFile()
    }

    fun shouldBlockUrl(url: String): Boolean {
        val domain = extractDomain(url)
        for ((category, domains) in trackerDomains) {
            if (domains.any { domain.contains(it) }) {
                recordBlockedTracker(domain, category)
                return true
            }
        }
        return false
    }

    private fun recordBlockedTracker(domain: String, category: String) {
        val existing = blockedTrackers.find { it.domain == domain }
        if (existing != null) {
            val index = blockedTrackers.indexOf(existing)
            blockedTrackers[index] = existing.copy(
                blockedCount = existing.blockedCount + 1,
                lastBlocked = System.currentTimeMillis()
            )
        } else {
            blockedTrackers.add(
                TrackerInfo(domain = domain, category = category, blockedCount = 1)
            )
        }
        saveToFile()
    }

    fun getPrivacyReport(): PrivacyReport {
        val totalBlocked = blockedTrackers.sumOf { it.blockedCount }
        val byCategory = blockedTrackers.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.blockedCount } }
        val topDomains = blockedTrackers.sortedByDescending { it.blockedCount }.take(10)

        return PrivacyReport(
            totalTrackersBlocked = totalBlocked,
            trackersByCategory = byCategory,
            topTrackerDomains = topDomains,
            sitesVisited = 0
        )
    }

    fun clearData() {
        blockedTrackers.clear()
        saveToFile()
    }

    private fun extractDomain(url: String): String {
        return try {
            java.net.URI(url).host ?: url
        } catch (_: Exception) {
            url
        }
    }

    private fun saveToFile() {
        try {
            val file = File(context.filesDir, fileName)
            file.writeText(gson.toJson(blockedTrackers))
        } catch (_: Exception) { }
    }

    private fun loadFromFile() {
        try {
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                val type = object : TypeToken<List<TrackerInfo>>() {}.type
                val items: List<TrackerInfo> = gson.fromJson(file.readText(), type)
                blockedTrackers.addAll(items)
            }
        } catch (_: Exception) { }
    }
}
