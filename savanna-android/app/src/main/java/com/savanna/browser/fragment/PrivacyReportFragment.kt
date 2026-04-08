package com.savanna.browser.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.savanna.browser.MainActivity
import com.savanna.browser.R

class PrivacyReportFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_privacy_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as MainActivity
        val report = activity.trackerBlocker.getPrivacyReport()

        val btnClose: ImageView = view.findViewById(R.id.btn_close_privacy)
        val totalCount: TextView = view.findViewById(R.id.total_blocked_count)
        val categoriesContainer: LinearLayout = view.findViewById(R.id.categories_container)
        val topTrackersContainer: LinearLayout = view.findViewById(R.id.top_trackers_container)

        totalCount.text = report.totalTrackersBlocked.toString()

        if (report.trackersByCategory.isEmpty()) {
            addEmptyRow(categoriesContainer, "No trackers blocked yet")
        } else {
            report.trackersByCategory.forEach { (category, count) ->
                addCategoryRow(categoriesContainer, category.replaceFirstChar { it.uppercase() }, count)
            }
        }

        if (report.topTrackerDomains.isEmpty()) {
            addEmptyRow(topTrackersContainer, "No tracker domains recorded")
        } else {
            report.topTrackerDomains.forEach { tracker ->
                addTrackerRow(topTrackersContainer, tracker.domain, tracker.blockedCount)
            }
        }

        btnClose.setOnClickListener {
            activity.closeOverlay()
        }
    }

    private fun addCategoryRow(container: LinearLayout, name: String, count: Int) {
        val row = layoutInflater.inflate(android.R.layout.simple_list_item_2, container, false)
        row.setPadding(dp(16), dp(12), dp(16), dp(12))

        val text1 = row.findViewById<TextView>(android.R.id.text1)
        val text2 = row.findViewById<TextView>(android.R.id.text2)

        text1.text = name
        text1.setTextColor(resources.getColor(R.color.text_primary, null))
        text1.textSize = 15f

        text2.text = "$count blocked"
        text2.setTextColor(resources.getColor(R.color.purple_primary, null))
        text2.textSize = 13f

        container.addView(row)
    }

    private fun addTrackerRow(container: LinearLayout, domain: String, count: Int) {
        val row = layoutInflater.inflate(android.R.layout.simple_list_item_2, container, false)
        row.setPadding(dp(16), dp(12), dp(16), dp(12))

        val text1 = row.findViewById<TextView>(android.R.id.text1)
        val text2 = row.findViewById<TextView>(android.R.id.text2)

        text1.text = domain
        text1.setTextColor(resources.getColor(R.color.text_primary, null))
        text1.textSize = 14f

        text2.text = "$count requests blocked"
        text2.setTextColor(resources.getColor(R.color.text_secondary, null))
        text2.textSize = 12f

        container.addView(row)
    }

    private fun addEmptyRow(container: LinearLayout, message: String) {
        val textView = TextView(requireContext()).apply {
            text = message
            setTextColor(resources.getColor(R.color.text_tertiary, null))
            textSize = 14f
            setPadding(dp(16), dp(20), dp(16), dp(20))
            gravity = android.view.Gravity.CENTER
        }
        container.addView(textView)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
