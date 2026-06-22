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
import com.savanna.browser.adapter.SiteSettingsAdapter

class SiteSettingsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var adapter: SiteSettingsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_site_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        view.setBackgroundColor(activity.themeManager.activePreset.bgColor)

        recyclerView = view.findViewById(R.id.site_list)
        emptyText = view.findViewById(R.id.empty_text)
        view.findViewById<ImageView>(R.id.btn_close_site_settings).setOnClickListener { activity.closeOverlay() }

        val pm = activity.sitePermissionsManager
        adapter = SiteSettingsAdapter(pm.getAll(), pm) {
            adapter.updateItems(pm.getAll())
            updateEmpty()
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        updateEmpty()
    }

    private fun updateEmpty() {
        val empty = adapter.itemCount == 0
        emptyText.visibility = if (empty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (empty) View.GONE else View.VISIBLE
    }
}
