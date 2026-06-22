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

    fun formatTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val sdf = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
        if (diff < 86400000L) return java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
        if (diff < 604800000L) return sdf.format(java.util.Date(timestamp))
        return java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
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
