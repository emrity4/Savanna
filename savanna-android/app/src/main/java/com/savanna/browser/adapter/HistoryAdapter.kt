package com.savanna.browser.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.savanna.browser.R
import com.savanna.browser.model.HistoryItem
import com.savanna.browser.util.UrlUtils

class HistoryAdapter(
    private var items: List<HistoryItem>,
    private val onItemClick: (HistoryItem) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private val domainColors = listOf(
        0xFFFF453A.toInt(), 0xFFFF9F0A.toInt(), 0xFF30D158.toInt(), 0xFF0A84FF.toInt(),
        0xFF5E5CE6.toInt(), 0xFFBF5AF2.toInt(), 0xFFFF375F.toInt(), 0xFF64D2FF.toInt(),
        0xFFFFD60A.toInt(), 0xFF32D74B.toInt(), 0xFFFF6961.toInt(), 0xFF5AC8FA.toInt(),
        0xFFFF6B35.toInt(), 0xFF34C759.toInt(), 0xFF007AFF.toInt(), 0xFFAF52DE.toInt()
    )

    private fun colorForDomain(domain: String): Int {
        var hash = 0
        domain.forEach { hash = it.code + ((hash shl 5) - hash) }
        return domainColors[Math.abs(hash) % domainColors.size]
    }

    private fun extractDomain(url: String): String {
        return try {
            val host = java.net.URI(url).host ?: url
            host.removePrefix("www.")
        } catch (_: Exception) { url }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val favicon: TextView = view.findViewById(R.id.history_favicon)
        val title: TextView   = view.findViewById(R.id.history_title)
        val url: TextView     = view.findViewById(R.id.history_url)
        val time: TextView    = view.findViewById(R.id.history_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item   = items[position]
        val domain = extractDomain(item.url)
        val color  = colorForDomain(domain)
        val initial = if (item.title.isNotBlank()) item.title[0].uppercaseChar().toString()
                      else if (domain.isNotBlank()) domain[0].uppercaseChar().toString()
                      else "?"

        // Set colored rounded-rect background for the favicon icon
        val bg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = holder.favicon.context.resources.displayMetrics.density * 8f
            setColor(color)
        }
        holder.favicon.background = bg
        holder.favicon.text = initial

        holder.title.text = item.title.ifBlank { domain }
        holder.url.text   = UrlUtils.formatUrl(item.url)
        holder.time.text  = UrlUtils.timeAgo(item.timestamp)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<HistoryItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
