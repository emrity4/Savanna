package com.savanna.browser.fragment

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.savanna.browser.R
import java.util.*

class IOSDatePickerFragment(
    private val onDateTimeSelected: (year: Int, month: Int, day: Int, hour: Int, minute: Int) -> Unit
) : DialogFragment() {

    private var displayYear = 0
    private var displayMonth = 0
    private var selYear = 0
    private var selMonth = 0
    private var selDay = 0
    private var selHour = 0
    private var selMinute = 0

    private lateinit var headerText: TextView
    private lateinit var grid: LinearLayout
    private lateinit var timeLabel: TextView

    private fun Int.dp() = (this * resources.displayMetrics.density).toInt()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        displayYear = c.get(Calendar.YEAR)
        displayMonth = c.get(Calendar.MONTH)
        selYear = displayYear; selMonth = displayMonth
        selDay = c.get(Calendar.DAY_OF_MONTH)
        selHour = c.get(Calendar.HOUR_OF_DAY); selMinute = c.get(Calendar.MINUTE)

        headerText = TextView(requireContext()).apply {
            setTextColor(Color.WHITE)
            textSize = 17f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        updateHeader()

        val prevBtn = TextView(requireContext()).apply {
            text = "\u25C0"; textSize = 15f
            setTextColor(Color.parseColor("#4693FF"))
            setPadding(8.dp(), 4.dp(), 8.dp(), 4.dp())
            setOnClickListener { displayMonth--; if (displayMonth < 0) { displayMonth = 11; displayYear-- }; rebuild(); updateHeader() }
        }
        val nextBtn = TextView(requireContext()).apply {
            text = "\u25B6"; textSize = 21f
            setTextColor(Color.WHITE)
            setPadding(8.dp(), 4.dp(), 8.dp(), 4.dp())
            setOnClickListener { displayMonth++; if (displayMonth > 11) { displayMonth = 0; displayYear++ }; rebuild(); updateHeader() }
        }

        val headerRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(headerText, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(prevBtn)
            addView(nextBtn)
        }

        // weekday labels
        val wdLabels = arrayOf("MON","TUE","WED","THU","FRI","SAT","SUN")
        val wdRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        for (d in wdLabels) {
            wdRow.addView(TextView(requireContext()).apply {
                text = d; textSize = 13f
                setTextColor(Color.parseColor("#4DFFFFFF"))
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, 16.dp(), 1f)
            })
        }

        grid = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
        }

        // time section
        timeLabel = TextView(requireContext()).apply {
            text = "Time"
            setTextColor(Color.WHITE); textSize = 18f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        val timePill = TextView(requireContext()).apply {
            val h = if (selHour == 0) 12 else if (selHour > 12) selHour - 12 else selHour
            val am = if (selHour < 12) "AM" else "PM"
            text = "${h}:${"%02d".format(selMinute)} $am"
            setTextColor(Color.WHITE); textSize = 17f
            gravity = Gravity.CENTER
            setPadding(12.dp(), 6.dp(), 12.dp(), 6.dp())
            setOnClickListener { showTimePicker() }
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE; cornerRadius = 8f * resources.displayMetrics.density
                setColor(Color.parseColor("#10FFFFFF"))
            }
            background = bg
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 36.dp())
        }

        val timeRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(timeLabel, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(timePill)
        }

        // reset + done buttons
        val resetBtn = TextView(requireContext()).apply {
            text = "Reset"
            setTextColor(Color.WHITE); textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, 12.dp(), 12.dp(), 0)
            setOnClickListener {
                val now = Calendar.getInstance()
                displayYear = now.get(Calendar.YEAR); displayMonth = now.get(Calendar.MONTH)
                selYear = displayYear; selMonth = displayMonth
                selDay = now.get(Calendar.DAY_OF_MONTH)
                selHour = now.get(Calendar.HOUR_OF_DAY); selMinute = now.get(Calendar.MINUTE)
                updateHeader(); rebuild()
                timePill.text = formatTime()
            }
        }

        val doneBtn = FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(48.dp(), 48.dp())
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#5199FD"))
            }
            background = bg
            setOnClickListener {
                onDateTimeSelected(selYear, selMonth, selDay, selHour, selMinute)
                dismiss()
            }
            addView(TextView(requireContext()).apply {
                text = "\u2713"; textSize = 22f
                setTextColor(Color.BLACK)
                gravity = Gravity.CENTER
                layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            })
        }

        val btnRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(resetBtn, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(doneBtn)
        }

        val content = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24.dp(), 24.dp(), 24.dp(), 12.dp())
            addView(headerRow)
            addView(wdRow, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = 16.dp() })
            addView(grid, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = 8.dp() })
            addView(timeRow, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = 12.dp() })
            addView(btnRow, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = 4.dp() })
        }

        val container = FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(353.dp(), ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundResource(R.drawable.date_picker)
            addView(content)
        }

        rebuild()

        return object : Dialog(requireContext(), R.style.GlassBottomSheetDialog) {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(container)
                window?.setLayout(353.dp(), ViewGroup.LayoutParams.WRAP_CONTENT)
                window?.setGravity(Gravity.CENTER)
                window?.setBackgroundDrawableResource(android.R.color.transparent)
                window?.setDimAmount(0.28f)
            }
        }
    }

    private fun updateHeader() {
        val names = arrayOf("January","February","March","April","May","June","July","August","September","October","November","December")
        headerText.text = "${names[displayMonth]} $displayYear"
    }

    private fun rebuild() {
        grid.removeAllViews()
        val cal = Calendar.getInstance()
        cal.set(displayYear, displayMonth, 1)
        val firstDow = (cal.get(Calendar.DAY_OF_WEEK) + 6) % 7
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        var dayNum = 1
        for (row in 0 until 6) {
            val r = LinearLayout(requireContext()).apply { orientation = LinearLayout.HORIZONTAL }
            for (col in 0 until 7) {
                var show = false; var n = 0
                if (row == 0 && col >= firstDow) { show = true; n = dayNum; dayNum++ }
                else if (row > 0 && dayNum <= daysInMonth) { show = true; n = dayNum; dayNum++ }
                val tv = TextView(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(0, 42.dp(), 1f)
                    gravity = Gravity.CENTER
                    textSize = 20f
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    if (show) {
                        text = n.toString()
                        if (n == selDay && displayYear == selYear && displayMonth == selMonth) {
                            val bg = GradientDrawable().apply {
                                shape = GradientDrawable.OVAL; setColor(Color.parseColor("#5199FD"))
                            }
                            setTextColor(Color.WHITE)
                            background = bg
                        } else {
                            setTextColor(Color.WHITE)
                        }
                        setOnClickListener {
                            selDay = n; selMonth = displayMonth; selYear = displayYear
                            rebuild()
                        }
                    } else {
                        setTextColor(Color.TRANSPARENT)
                    }
                }
                r.addView(tv)
            }
            grid.addView(r, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 42.dp()))
            if (dayNum > daysInMonth) break
        }
    }

    private fun formatTime(): String {
        val h = if (selHour == 0) 12 else if (selHour > 12) selHour - 12 else selHour
        val am = if (selHour < 12) "AM" else "PM"
        return "${h}:${"%02d".format(selMinute)} $am"
    }

    private fun showTimePicker() {
        val hourPicker = NumberPicker(requireContext()).apply {
            minValue = 1; maxValue = 12
            value = when { selHour == 0 -> 12; selHour > 12 -> selHour - 12; else -> selHour }
            displayedValues = (1..12).map { "%02d".format(it) }.toTypedArray()
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        }
        val minutePicker = NumberPicker(requireContext()).apply {
            minValue = 0; maxValue = 59; value = selMinute
            setFormatter { String.format("%02d", it) }
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        }
        val amPmPicker = NumberPicker(requireContext()).apply {
            minValue = 0; maxValue = 1
            displayedValues = arrayOf("AM", "PM")
            value = if (selHour < 12) 0 else 1
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        }
        val pickerRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(24.dp(), 48.dp(), 24.dp(), 48.dp())
            addView(hourPicker, LinearLayout.LayoutParams(0, 172.dp(), 1f))
            addView(minutePicker, LinearLayout.LayoutParams(0, 172.dp(), 1f))
            addView(amPmPicker, LinearLayout.LayoutParams(0, 172.dp(), 1f))
        }

        val selectPill = FrameLayout(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(308.dp(), 34.dp(), Gravity.CENTER)
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE; cornerRadius = 17.dp().toFloat()
                setColor(Color.parseColor("#14FFFFFF"))
            }
            background = bg
            isClickable = false; isFocusable = false
        }

        val resetBtn = TextView(requireContext()).apply {
            text = "Reset"; textSize = 18f
            setTextColor(Color.WHITE); gravity = Gravity.CENTER
            setPadding(12.dp(), 3.dp(), 12.dp(), 3.dp())
            setOnClickListener {
                val now = Calendar.getInstance()
                selHour = now.get(Calendar.HOUR_OF_DAY); selMinute = now.get(Calendar.MINUTE)
                hourPicker.value = when { selHour == 0 -> 12; selHour > 12 -> selHour - 12; else -> selHour }
                minutePicker.value = selMinute
                amPmPicker.value = if (selHour < 12) 0 else 1
            }
        }

        val doneBtn = FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(48.dp(), 48.dp())
            val bg = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(Color.parseColor("#5199FD")) }
            background = bg
            setOnClickListener {
                var h = hourPicker.value
                if (amPmPicker.value == 1) { if (h != 12) h += 12 } else { if (h == 12) h = 0 }
                selHour = h; selMinute = minutePicker.value; dismiss()
            }
            addView(TextView(requireContext()).apply {
                text = "\u2713"; textSize = 22f; setTextColor(Color.BLACK)
                gravity = Gravity.CENTER
                layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            })
        }

        val btnRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(26.dp(), 0, 26.dp(), 0)
            addView(resetBtn, LinearLayout.LayoutParams(0, 48.dp(), 1f))
            addView(doneBtn)
        }

        val root = FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(344.dp(), 298.dp())
            addView(pickerRow)
            addView(selectPill)
            addView(btnRow, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.BOTTOM; bottomMargin = 12.dp()
            })
        }

        val container = FrameLayout(requireContext()).apply {
            setBackgroundResource(R.drawable.wheel_time)
            addView(root)
        }

        object : Dialog(requireContext(), R.style.GlassBottomSheetDialog) {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(container)
                window?.setLayout(344.dp(), 298.dp())
                window?.setGravity(Gravity.CENTER)
                window?.setBackgroundDrawableResource(android.R.color.transparent)
                window?.setDimAmount(0.28f)
                setOnDismissListener { timeLabel.text = formatTime() }
            }
        }.show()
    }
}
