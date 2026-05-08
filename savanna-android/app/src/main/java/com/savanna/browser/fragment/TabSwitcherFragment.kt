package com.savanna.browser.fragment

import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.savanna.browser.MainActivity
import com.savanna.browser.R
import com.savanna.browser.adapter.TabAdapter

class TabSwitcherFragment : BottomSheetDialogFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TabAdapter

    // Use our custom glass theme so the sheet background is transparent
    override fun getTheme() = R.style.GlassBottomSheetDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): android.app.Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        // API 31+: blur the browser content behind the sheet glass panel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dialog.window?.apply {
                addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                attributes = attributes.also { it.blurBehindRadius = 22 }
            }
        }

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_tab_switcher, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as MainActivity

        recyclerView = view.findViewById(R.id.tabs_recycler)
        val btnNewTab: ImageView = view.findViewById(R.id.btn_new_tab)
        val btnClose: ImageView  = view.findViewById(R.id.btn_close_tab_switcher)

        adapter = TabAdapter(
            tabs       = activity.tabManager.allTabs,
            onTabClick = { tab -> activity.switchToTab(tab.id); dismiss() },
            onCloseClick = { tab ->
                activity.closeTab(tab.id)
                val remaining = activity.tabManager.allTabs
                if (remaining.isEmpty()) { dismiss() }
                else adapter.updateTabs(remaining)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        btnNewTab.setOnClickListener { activity.createNewTab(); dismiss() }
        btnClose.setOnClickListener  { dismiss() }
    }

    override fun onStart() {
        super.onStart()

        val bottomSheet = (dialog as? BottomSheetDialog)
            ?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?: return

        // Transparent so our glass drawable shows
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)

        val screenH = Resources.getSystem().displayMetrics.heightPixels

        BottomSheetBehavior.from(bottomSheet).apply {
            peekHeight      = (screenH * 0.58).toInt()   // starts ~60% up
            isHideable      = true                        // drag down to dismiss
            skipCollapsed   = false
            state           = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        (activity as? MainActivity)?.isOverlayShowing = false
    }
}
