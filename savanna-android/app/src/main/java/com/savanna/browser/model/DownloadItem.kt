package com.savanna.browser.model

import android.app.DownloadManager

data class DownloadItem(
    val id: Long,
    val title: String,
    val url: String,
    val status: Int,
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val mimeType: String?,
    val localUri: String?,
    val speedBps: Long = 0L        // bytes per second — computed by AppDownloadManager
) {
    val progress: Int
        get() = if (totalBytes > 0) ((bytesDownloaded * 100) / totalBytes).toInt() else -1

    val isComplete: Boolean get() = status == DownloadManager.STATUS_SUCCESSFUL
    val isFailed: Boolean   get() = status == DownloadManager.STATUS_FAILED
    val isPending: Boolean  get() = status == DownloadManager.STATUS_PENDING || status == DownloadManager.STATUS_RUNNING

    val sizeLabel: String
        get() = formatBytes(totalBytes)

    val speedLabel: String
        get() = if (speedBps > 0) "${formatBytes(speedBps)}/s" else ""

    val statusLabel: String
        get() = when (status) {
            DownloadManager.STATUS_SUCCESSFUL -> "Complete · $sizeLabel"
            DownloadManager.STATUS_FAILED     -> "Failed"
            DownloadManager.STATUS_PAUSED     -> "Paused"
            DownloadManager.STATUS_PENDING    -> "Waiting…"
            DownloadManager.STATUS_RUNNING    -> buildString {
                if (progress >= 0) append("$progress%")
                if (sizeLabel.isNotBlank()) append(" · $sizeLabel")
                if (speedLabel.isNotBlank()) append("  $speedLabel")
                if (isEmpty()) append("Downloading…")
            }
            else -> "Unknown"
        }

    companion object {
        fun formatBytes(bytes: Long): String = when {
            bytes <= 0      -> ""
            bytes < 1024L   -> "${bytes} B"
            bytes < 1048576L -> "${"%.1f".format(bytes / 1024f)} KB"
            bytes < 1073741824L -> "${"%.1f".format(bytes / 1048576f)} MB"
            else            -> "${"%.2f".format(bytes / 1073741824f)} GB"
        }
    }
}
