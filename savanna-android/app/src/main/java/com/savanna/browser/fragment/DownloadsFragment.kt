package com.savanna.browser.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.savanna.browser.MainActivity
import com.savanna.browser.R
import com.savanna.browser.adapter.DownloadAdapter

class DownloadsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var adapter: DownloadAdapter
    private val handler = Handler(Looper.getMainLooper())
    private val pollInterval = 1000L  // refresh progress every 1 second

    private val pollRunnable = object : Runnable {
        override fun run() {
            refreshList()
            handler.postDelayed(this, pollInterval)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_downloads, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as MainActivity
        view.setBackgroundColor(activity.themeManager.bgColor)
        val dm      = activity.downloadManager

        recyclerView = view.findViewById(R.id.downloads_recycler)
        emptyState   = view.findViewById(R.id.empty_state)
        val btnClose: ImageView = view.findViewById(R.id.btn_close_downloads)

        adapter = DownloadAdapter(
            items         = dm.getAll(),
            onOpenClick   = { item -> dm.openFile(item) },
            onDeleteClick = { item ->
                dm.remove(item.id)
                refreshList()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter        = adapter

        btnClose.setOnClickListener { activity.closeOverlay() }

        updateEmptyState()
    }

    override fun onResume() {
        super.onResume()
        handler.post(pollRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(pollRunnable)
    }

    private fun refreshList() {
        if (!isAdded) return
        val activity = activity as? MainActivity ?: return
        val items    = activity.downloadManager.getAll()
        adapter.updateItems(items)
        updateEmptyState()
    }

    private fun updateEmptyState() {
        val isEmpty = adapter.itemCount == 0
        emptyState.visibility   = if (isEmpty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isEmpty) View.GONE   else View.VISIBLE
    }
}
