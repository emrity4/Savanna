package com.savanna.browser.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.savanna.browser.MainActivity
import com.savanna.browser.R
import com.savanna.browser.adapter.BookmarkAdapter

class BookmarksFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BookmarkAdapter
    private lateinit var emptyText: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bookmarks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as MainActivity
        view.setBackgroundColor(activity.themeManager.bgColor)

        recyclerView = view.findViewById(R.id.bookmarks_recycler)
        emptyText = view.findViewById(R.id.empty_text)
        val btnClose: ImageView = view.findViewById(R.id.btn_close_bookmarks)

        adapter = BookmarkAdapter(
            bookmarks = activity.bookmarkManager.allBookmarks,
            onItemClick = { bookmark ->
                activity.navigateToUrl(bookmark.url)
            },
            onDeleteClick = { bookmark ->
                activity.bookmarkManager.removeBookmark(bookmark.id)
                adapter.updateBookmarks(activity.bookmarkManager.allBookmarks)
                updateEmptyState()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        updateEmptyState()

        btnClose.setOnClickListener {
            activity.closeOverlay()
        }
    }

    private fun updateEmptyState() {
        val isEmpty = adapter.itemCount == 0
        emptyText.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}
