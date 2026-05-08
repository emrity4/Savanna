package com.savanna.browser

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/** Shows a toast when the system DownloadManager finishes a file. */
class DownloadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return
        Toast.makeText(context, "Download complete", Toast.LENGTH_SHORT).show()
    }
}
