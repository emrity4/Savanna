package com.savanna.browser.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.savanna.browser.R
import com.savanna.browser.model.Tab
import com.savanna.browser.util.UrlUtils

class TabAdapter(
    private var tabs: List<Tab>,
    private val onTabClick: (Tab) -> Unit,
    private val onCloseClick: (Tab) -> Unit
) : RecyclerView.Adapter<TabAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: View = view.findViewById(R.id.tab_card)
        val title: TextView = view.findViewById(R.id.tab_title)
        val url: TextView = view.findViewById(R.id.tab_url)
        val closeBtn: ImageView = view.findViewById(R.id.btn_close_tab)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tab, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tab = tabs[position]
        holder.title.text = tab.title.ifBlank { "New Tab" }
        holder.url.text = if (tab.url.isNotBlank()) UrlUtils.formatUrl(tab.url) else "about:blank"

        holder.card.setBackgroundResource(
            if (tab.isActive) R.drawable.tab_card_active else R.drawable.tab_card_background
        )

        holder.card.setOnClickListener { onTabClick(tab) }
        holder.closeBtn.setOnClickListener { onCloseClick(tab) }
    }

    override fun getItemCount() = tabs.size

    fun updateTabs(newTabs: List<Tab>) {
        tabs = newTabs
        notifyDataSetChanged()
    }
}
