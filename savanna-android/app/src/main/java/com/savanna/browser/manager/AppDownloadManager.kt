package com.savanna.browser.manager

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import com.savanna.browser.model.DownloadItem

class AppDownloadManager(private val context: Context) {

    private val system: DownloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    // Speed tracking: id → (bytesAtLastPoll, timeAtLastPoll)
    private val speedTracker = mutableMapOf<Long, Pair<Long, Long>>()

    fun enqueue(
        url: String,
        userAgent: String,
        contentDisposition: String?,
        mimeType: String?
    ): Long {
        val filename = URLUtil.guessFileName(url, contentDisposition, mimeType)
        val resolvedMime = mimeType?.takeIf { it.isNotBlank() }
            ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                filename.substringAfterLast('.', "")
            ) ?: "application/octet-stream"

        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(filename)
            .setDescription("Downloading…")
            .setMimeType(resolvedMime)
            .setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
            .addRequestHeader("User-Agent", userAgent)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        return system.enqueue(request)
    }

    fun getAll(): List<DownloadItem> {
        val now   = System.currentTimeMillis()
        val items = mutableListOf<DownloadItem>()

        val cursor = system.query(DownloadManager.Query())
        cursor?.use {
            while (it.moveToNext()) {
                fun str(col: String): String? =
                    try { it.getString(it.getColumnIndexOrThrow(col)) } catch (_: Exception) { null }
                fun lng(col: String): Long =
                    try { it.getLong(it.getColumnIndexOrThrow(col)) } catch (_: Exception) { 0L }
                fun int_(col: String): Int =
                    try { it.getInt(it.getColumnIndexOrThrow(col)) } catch (_: Exception) { 0 }

                val id              = lng(DownloadManager.COLUMN_ID)
                val bytesDownloaded = lng(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                val status          = int_(DownloadManager.COLUMN_STATUS)

                // ── Compute speed ──────────────────────────────────────────
                val speedBps: Long = if (status == DownloadManager.STATUS_RUNNING) {
                    val prev = speedTracker[id]
                    speedTracker[id] = bytesDownloaded to now
                    if (prev != null) {
                        val dBytes = bytesDownloaded - prev.first
                        val dMs    = now - prev.second
                        if (dMs > 200L && dBytes >= 0L) (dBytes * 1000L) / dMs else 0L
                    } else 0L
                } else {
                    speedTracker.remove(id)
                    0L
                }

                items.add(
                    DownloadItem(
                        id              = id,
                        title           = str(DownloadManager.COLUMN_TITLE) ?: "Download",
                        url             = str(DownloadManager.COLUMN_URI) ?: "",
                        status          = status,
                        bytesDownloaded = bytesDownloaded,
                        totalBytes      = lng(DownloadManager.COLUMN_TOTAL_SIZE_BYTES),
                        mimeType        = str(DownloadManager.COLUMN_MEDIA_TYPE),
                        localUri        = str(DownloadManager.COLUMN_LOCAL_URI),
                        speedBps        = speedBps,
                        timestamp       = lng(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP)
                    )
                )
            }
        }
        return items.sortedByDescending { it.id }
    }

    fun openFile(item: DownloadItem) {
        val uri = system.getUriForDownloadedFile(item.id) ?: return
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, item.mimeType ?: "*/*")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        try { context.startActivity(intent) } catch (_: Exception) { }
    }

    fun remove(id: Long) {
        speedTracker.remove(id)
        system.remove(id)
    }

    val downloadCount: Int
        get() = getAll().count { it.isPending }
}
