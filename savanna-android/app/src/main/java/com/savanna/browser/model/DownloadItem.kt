package com.savanna.browser.model

import android.app.DownloadManager

data class DownloadItem(
    val id: Long,
    val title: String,
    val url: String,
    val status: Int,         // DownloadManager.STATUS_*
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val mimeType: String?,
    val localUri: String?
) {
    val progress: Int get() = if (totalBytes > 0) ((bytesDownloaded * 100) / totalBytes).toInt() else -1
    val isComplete: Boolean get() = status == DownloadManager.STATUS_SUCCESSFUL
    val isFailed: Boolean  get() = status == DownloadManager.STATUS_FAILED
    val isPending: Boolean get() = status == DownloadManager.STATUS_PENDING || status == DownloadManager.STATUS_RUNNING
    val sizeLabel: String get() = when {
        totalBytes <= 0       -> ""
        totalBytes < 1024     -> "${totalBytes}B"
        totalBytes < 1048576  -> "${"%.1f".format(totalBytes / 1024f)}KB"
        else                  -> "${"%.1f".format(totalBytes / 1048576f)}MB"
    }
    val statusLabel: String get() = when (status) {
        DownloadManager.STATUS_SUCCESSFUL -> "Complete · $sizeLabel"
        DownloadManager.STATUS_FAILED     -> "Failed"
        DownloadManager.STATUS_PAUSED     -> "Paused"
        DownloadManager.STATUS_PENDING    -> "Waiting…"
        DownloadManager.STATUS_RUNNING    -> if (progress >= 0) "$progress% · $sizeLabel" else "Downloading…"
        else                              -> "Unknown"
    }
}
