package com.savanna.browser.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.materialswitch.MaterialSwitch
import com.savanna.browser.R
import com.savanna.browser.manager.SitePermissionsManager
import com.savanna.browser.model.SitePermission

class SiteSettingsAdapter(
    private var items: List<SitePermission>,
    private val pm: SitePermissionsManager,
    private val onChanged: () -> Unit
) : RecyclerView.Adapter<SiteSettingsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val host: TextView = view.findViewById(R.id.site_host)
        val container: LinearLayout = view.findViewById(R.id.permissions_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_site_permission, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.host.text = item.host
        holder.container.removeAllViews()
        val density = holder.itemView.resources.displayMetrics.density

        fun addToggle(label: String, current: Int, onSet: (Int) -> Unit) {
            val row = LinearLayout(holder.itemView.context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, (42 * density).toInt()
                ).also { it.setMargins(0, (2 * density).toInt(), 0, 0) }
                gravity = android.view.Gravity.CENTER_VERTICAL
            }
            val tv = TextView(holder.itemView.context).apply {
                text = label
                setTextColor(0xFF8E8E93.toInt())
                textSize = 13f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val sw = MaterialSwitch(holder.itemView.context).apply {
                isChecked = current == SitePermission.ALLOW
                setOnCheckedChangeListener { _, v ->
                    onSet(if (v) SitePermission.ALLOW else SitePermission.ASK)
                    onChanged()
                }
            }
            row.addView(tv)
            row.addView(sw)
            holder.container.addView(row)
        }

        addToggle("Camera", item.camera) { v -> pm.set(item.host) { it.copy(camera = v) } }
        addToggle("Microphone", item.microphone) { v -> pm.set(item.host) { it.copy(microphone = v) } }
        addToggle("Location", item.location) { v -> pm.set(item.host) { it.copy(location = v) } }
        addToggle("Notifications", item.notifications) { v -> pm.set(item.host) { it.copy(notifications = v) } }
        addToggle("Clipboard", item.clipboard) { v -> pm.set(item.host) { it.copy(clipboard = v) } }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<SitePermission>) {
        items = newItems
        notifyDataSetChanged()
    }
}
