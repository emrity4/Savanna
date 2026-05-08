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

    /** Enqueue a new download; returns the download ID. */
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

    /** Query all downloads tracked by this app (all entries in system DM). */
    fun getAll(): List<DownloadItem> {
        val items = mutableListOf<DownloadItem>()
        val query = DownloadManager.Query()
        val cursor = system.query(query)
        cursor?.use {
            while (it.moveToNext()) {
                fun str(col: String): String? =
                    try { it.getString(it.getColumnIndexOrThrow(col)) } catch (_: Exception) { null }
                fun lng(col: String): Long =
                    try { it.getLong(it.getColumnIndexOrThrow(col)) } catch (_: Exception) { 0L }
                fun int_(col: String): Int =
                    try { it.getInt(it.getColumnIndexOrThrow(col)) } catch (_: Exception) { 0 }

                items.add(
                    DownloadItem(
                        id              = lng(DownloadManager.COLUMN_ID),
                        title           = str(DownloadManager.COLUMN_TITLE) ?: "Download",
                        url             = str(DownloadManager.COLUMN_URI) ?: "",
                        status          = int_(DownloadManager.COLUMN_STATUS),
                        bytesDownloaded = lng(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR),
                        totalBytes      = lng(DownloadManager.COLUMN_TOTAL_SIZE_BYTES),
                        mimeType        = str(DownloadManager.COLUMN_MEDIA_TYPE),
                        localUri        = str(DownloadManager.COLUMN_LOCAL_URI)
                    )
                )
            }
        }
        return items.sortedByDescending { it.id }
    }

    /** Open a completed file with the system viewer. */
    fun openFile(item: DownloadItem) {
        val uri = system.getUriForDownloadedFile(item.id) ?: return
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, item.mimeType ?: "*/*")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        try { context.startActivity(intent) } catch (_: Exception) { }
    }

    /** Remove a download entry (and its file if complete). */
    fun remove(id: Long) = system.remove(id)

    val downloadCount: Int
        get() = getAll().count { it.isPending }
}
