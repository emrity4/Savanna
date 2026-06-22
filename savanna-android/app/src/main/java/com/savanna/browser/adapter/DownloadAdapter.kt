package com.savanna.browser.adapter

import android.app.DownloadManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.savanna.browser.R
import com.savanna.browser.model.DownloadItem

class DownloadAdapter(
    private var items: List<DownloadItem>,
    private val onOpenClick: (DownloadItem) -> Unit,
    private val onDeleteClick: (DownloadItem) -> Unit
) : RecyclerView.Adapter<DownloadAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView       = view.findViewById(R.id.download_icon)
        val title: TextView       = view.findViewById(R.id.download_title)
        val statusText: TextView  = view.findViewById(R.id.download_status)
        val progress: ProgressBar = view.findViewById(R.id.download_progress)
        val btnOpen: ImageView    = view.findViewById(R.id.btn_open_download)
        val btnDelete: ImageView  = view.findViewById(R.id.btn_delete_download)
        val sizeText: TextView    = view.findViewById(R.id.download_size)
        val dateText: TextView    = view.findViewById(R.id.download_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_download, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.title.text      = item.title
        holder.statusText.text = item.statusLabel   // includes speed e.g. "47% · 12.3 MB  1.2 MB/s"
        holder.sizeText.text   = item.sizeLabel
        val sdf = java.text.SimpleDateFormat("MMM dd, h:mm a", java.util.Locale.getDefault())
        holder.dateText.text   = sdf.format(java.util.Date(item.timestamp))

        when {
            item.isComplete -> {
                holder.icon.setImageResource(R.drawable.ic_download_done)
                holder.icon.alpha          = 1f
                holder.progress.visibility = View.GONE
                holder.btnOpen.visibility  = View.VISIBLE
            }
            item.isFailed -> {
                holder.icon.setImageResource(R.drawable.ic_download)
                holder.icon.alpha          = 0.35f
                holder.progress.visibility = View.GONE
                holder.btnOpen.visibility  = View.GONE
            }
            item.isPending -> {
                holder.icon.setImageResource(R.drawable.ic_download)
                holder.icon.alpha = 1f
                if (item.progress >= 0) {
                    holder.progress.isIndeterminate = false
                    holder.progress.progress        = item.progress
                } else {
                    holder.progress.isIndeterminate = true
                }
                holder.progress.visibility = View.VISIBLE
                holder.btnOpen.visibility  = View.GONE
            }
            else -> {
                holder.icon.setImageResource(R.drawable.ic_download)
                holder.icon.alpha          = 0.5f
                holder.progress.visibility = View.GONE
                holder.btnOpen.visibility  = View.GONE
            }
        }

        holder.btnOpen.setOnClickListener   { onOpenClick(item) }
        holder.btnDelete.setOnClickListener { onDeleteClick(item) }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<DownloadItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
