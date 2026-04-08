package com.savanna.browser.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.savanna.browser.MainActivity
import com.savanna.browser.R
import com.savanna.browser.adapter.HistoryAdapter

class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private lateinit var emptyText: TextView
    private lateinit var searchField: EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as MainActivity

        recyclerView = view.findViewById(R.id.history_recycler)
        emptyText = view.findViewById(R.id.empty_text)
        searchField = view.findViewById(R.id.search_history)
        val btnClear: ImageView = view.findViewById(R.id.btn_clear_history)
        val btnClose: ImageView = view.findViewById(R.id.btn_close_history)

        adapter = HistoryAdapter(
            items = activity.historyManager.allHistory,
            onItemClick = { item ->
                activity.navigateToUrl(item.url)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        updateEmptyState()

        searchField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                if (query.isBlank()) {
                    adapter.updateItems(activity.historyManager.allHistory)
                } else {
                    adapter.updateItems(activity.historyManager.search(query))
                }
                updateEmptyState()
            }
        })

        btnClear.setOnClickListener {
            activity.historyManager.clearHistory()
            adapter.updateItems(emptyList())
            updateEmptyState()
        }

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
