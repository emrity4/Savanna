package com.savanna.browser.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.savanna.browser.R
import com.savanna.browser.model.Bookmark
import com.savanna.browser.util.UrlUtils

class BookmarkAdapter(
    private var bookmarks: List<Bookmark>,
    private val onItemClick: (Bookmark) -> Unit,
    private val onDeleteClick: (Bookmark) -> Unit
) : RecyclerView.Adapter<BookmarkAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.bookmark_title)
        val url: TextView = view.findViewById(R.id.bookmark_url)
        val deleteBtn: ImageView = view.findViewById(R.id.btn_delete_bookmark)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bookmark, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bookmark = bookmarks[position]
        holder.title.text = bookmark.title
        holder.url.text = UrlUtils.formatUrl(bookmark.url)
        holder.itemView.setOnClickListener { onItemClick(bookmark) }
        holder.deleteBtn.setOnClickListener { onDeleteClick(bookmark) }
    }

    override fun getItemCount() = bookmarks.size

    fun updateBookmarks(newBookmarks: List<Bookmark>) {
        bookmarks = newBookmarks
        notifyDataSetChanged()
    }
}
