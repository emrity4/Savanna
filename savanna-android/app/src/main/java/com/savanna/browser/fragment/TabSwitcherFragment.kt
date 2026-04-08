package com.savanna.browser.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.savanna.browser.MainActivity
import com.savanna.browser.R
import com.savanna.browser.adapter.TabAdapter

class TabSwitcherFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TabAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tab_switcher, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as MainActivity

        recyclerView = view.findViewById(R.id.tabs_recycler)
        val btnNewTab: ImageView = view.findViewById(R.id.btn_new_tab)
        val btnClose: ImageView = view.findViewById(R.id.btn_close_tab_switcher)

        adapter = TabAdapter(
            tabs = activity.tabManager.allTabs,
            onTabClick = { tab ->
                activity.switchToTab(tab.id)
            },
            onCloseClick = { tab ->
                activity.closeTab(tab.id)
                adapter.updateTabs(activity.tabManager.allTabs)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        btnNewTab.setOnClickListener {
            activity.createNewTab()
        }

        btnClose.setOnClickListener {
            activity.closeOverlay()
        }
    }
}
