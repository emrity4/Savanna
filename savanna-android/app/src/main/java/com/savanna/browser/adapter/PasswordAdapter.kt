package com.savanna.browser.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.savanna.browser.R
import com.savanna.browser.model.PasswordEntry

class PasswordAdapter(
    private var items: List<PasswordEntry>,
    private val onToggle: (PasswordEntry) -> Unit,
    private val onEdit: (PasswordEntry) -> Unit,
    private val onDelete: (PasswordEntry) -> Unit
) : RecyclerView.Adapter<PasswordAdapter.ViewHolder>() {

    private val visible = mutableSetOf<String>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val favicon: TextView = view.findViewById(R.id.password_favicon)
        val site: TextView = view.findViewById(R.id.password_site)
        val username: TextView = view.findViewById(R.id.password_username)
        val passwordValue: TextView = view.findViewById(R.id.password_value)
        val btnToggle: ImageView = view.findViewById(R.id.btn_toggle_password)
        val btnEdit: ImageView = view.findViewById(R.id.btn_edit_password)
        val btnDelete: ImageView = view.findViewById(R.id.btn_delete_password)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_password, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val domain = try { java.net.URI(item.url).host?.removePrefix("www.") ?: item.url } catch (_: Exception) { item.url }
        val initial = domain.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        val colors = listOf(0xFFFF453A.toInt(), 0xFFFF9F0A.toInt(), 0xFF30D158.toInt(), 0xFF0A84FF.toInt(),
            0xFF5E5CE6.toInt(), 0xFFBF5AF2.toInt(), 0xFF64D2FF.toInt())
        val color = colors[Math.abs(domain.hashCode()) % colors.size]
        val bg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 8f * holder.itemView.resources.displayMetrics.density
            setColor(color)
        }
        holder.favicon.background = bg
        holder.favicon.text = initial
        holder.site.text = domain
        holder.username.text = item.username

        val show = visible.contains(item.id)
        holder.passwordValue.text = if (show) item.password else "••••••••"
        holder.btnToggle.setImageResource(if (show) R.drawable.ic_lock else R.drawable.ic_lock)
        holder.btnToggle.alpha = if (show) 1f else 0.4f

        holder.btnToggle.setOnClickListener {
            if (show) visible.remove(item.id) else visible.add(item.id)
            notifyItemChanged(position)
        }
        holder.btnEdit.setOnClickListener { onEdit(item) }
        holder.btnDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<PasswordEntry>) {
        items = newItems
        notifyDataSetChanged()
    }
}
