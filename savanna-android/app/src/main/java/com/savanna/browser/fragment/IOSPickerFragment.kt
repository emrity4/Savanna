package com.savanna.browser.fragment

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.savanna.browser.R
import java.util.Calendar

class IOSDatePickerFragment(
    private val onDateSelected: (year: Int, month: Int, day: Int) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val cal = Calendar.getInstance()

        val monthPicker = NumberPicker(requireContext()).apply {
            minValue = 1; maxValue = 12
            value = cal.get(Calendar.MONTH) + 1
            displayedValues = arrayOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        }
        val dayPicker = NumberPicker(requireContext()).apply {
            minValue = 1; maxValue = 31
            value = cal.get(Calendar.DAY_OF_MONTH)
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        }
        val yearPicker = NumberPicker(requireContext()).apply {
            minValue = 1970; maxValue = 2037
            value = cal.get(Calendar.YEAR)
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        }

        val row = LinearLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            setPadding(24, 16, 24, 16)
            addView(monthPicker, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(dayPicker, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(yearPicker, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        }

        val done = TextView(requireContext()).apply {
            text = "Done"
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 16)
            setOnClickListener {
                onDateSelected(yearPicker.value, monthPicker.value - 1, dayPicker.value)
                dismiss()
            }
        }

        val root = FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(Color.parseColor("#FF1C1C1E"))
            addView(row)
            addView(done, FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
            ).apply { topMargin = 220 })
        }

        return object : Dialog(requireContext(), R.style.GlassBottomSheetDialog) {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(root)
                window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                window?.setGravity(Gravity.BOTTOM)
                window?.setBackgroundDrawableResource(android.R.color.transparent)
            }
        }
    }
}

class IOSTimePickerFragment(
    private val onTimeSelected: (hour: Int, minute: Int) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val cal = Calendar.getInstance()

        val hourPicker = NumberPicker(requireContext()).apply {
            minValue = 1; maxValue = 12
            value = when {
                cal.get(Calendar.HOUR_OF_DAY) == 0 -> 12
                cal.get(Calendar.HOUR_OF_DAY) > 12 -> cal.get(Calendar.HOUR_OF_DAY) - 12
                else -> cal.get(Calendar.HOUR_OF_DAY)
            }
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        }
        val minutePicker = NumberPicker(requireContext()).apply {
            minValue = 0; maxValue = 59
            value = cal.get(Calendar.MINUTE)
            setFormatter { String.format("%02d", it) }
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        }
        val amPmPicker = NumberPicker(requireContext()).apply {
            minValue = 0; maxValue = 1
            displayedValues = arrayOf("AM", "PM")
            value = if (cal.get(Calendar.AM_PM) == Calendar.AM) 0 else 1
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        }

        val row = LinearLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            setPadding(24, 16, 24, 16)
            addView(hourPicker, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(minutePicker, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(amPmPicker, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        }

        val done = TextView(requireContext()).apply {
            text = "Done"
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 16)
            setOnClickListener {
                var h = hourPicker.value
                if (amPmPicker.value == 1) { if (h != 12) h += 12 }
                else { if (h == 12) h = 0 }
                onTimeSelected(h, minutePicker.value)
                dismiss()
            }
        }

        val root = FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(Color.parseColor("#FF1C1C1E"))
            addView(row)
            addView(done, FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
            ).apply { topMargin = 220 })
        }

        return object : Dialog(requireContext(), R.style.GlassBottomSheetDialog) {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(root)
                window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                window?.setGravity(Gravity.BOTTOM)
                window?.setBackgroundDrawableResource(android.R.color.transparent)
            }
        }
    }
}
