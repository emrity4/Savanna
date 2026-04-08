package com.savanna.browser.util

import android.util.Patterns

object UrlUtils {
    fun smartUrlProcess(input: String): Pair<Boolean, String> {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return Pair(false, "")

        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return Pair(true, trimmed)
        }

        if (Patterns.WEB_URL.matcher(trimmed).matches() && trimmed.contains(".")) {
            return Pair(true, "https://$trimmed")
        }

        return Pair(false, trimmed)
    }

    fun extractDomain(url: String): String {
        return try {
            val uri = java.net.URI(url)
            uri.host?.removePrefix("www.") ?: url
        } catch (_: Exception) {
            url
        }
    }

    fun formatUrl(url: String): String {
        return try {
            val uri = java.net.URI(url)
            uri.host?.removePrefix("www.") ?: url
        } catch (_: Exception) {
            url
        }
    }

    fun timeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            days < 30 -> "${days / 7}w ago"
            else -> "${days / 30}mo ago"
        }
    }
}
