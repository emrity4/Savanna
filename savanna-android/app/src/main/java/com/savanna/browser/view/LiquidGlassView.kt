package com.savanna.browser.view

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.PixelCopy
import android.view.View

class LiquidGlassView : GLSurfaceView {

    var opacity: Float = 0.85f
    var cornerRadius: Float = 30f
    var viewToHideDuringCapture: View? = null

    private val glRenderer: LiquidGlassRenderer
    private var capturing = false

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setEGLContextClientVersion(2)
        setEGLConfigChooser(8, 8, 8, 8, 0, 0)
        glRenderer = LiquidGlassRenderer(this)
        setRenderer(glRenderer)
        renderMode = RENDERMODE_CONTINUOUSLY
        setZOrderOnTop(false)
    }

    fun snap() {
        if (capturing || width <= 0 || height <= 0) return
        capturing = true
        visibility = View.INVISIBLE
        viewToHideDuringCapture?.visibility = View.INVISIBLE
        postDelayed({
            try {
                val act = context as? Activity ?: run {
                    viewToHideDuringCapture?.visibility = View.VISIBLE
                    visibility = View.VISIBLE; capturing = false; return@postDelayed
                }
                val loc = IntArray(2)
                getLocationInWindow(loc)
                val rect = Rect(loc[0], loc[1], loc[0] + width, loc[1] + height)
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                PixelCopy.request(act.window, rect, bmp, { status ->
                    if (status == PixelCopy.SUCCESS) {
                        glRenderer.updateBackground(bmp)
                    } else {
                        bmp.recycle()
                    }
                    viewToHideDuringCapture?.visibility = View.VISIBLE
                    visibility = View.VISIBLE
                    capturing = false
                }, Handler(Looper.getMainLooper()))
            } catch (_: Exception) {
                viewToHideDuringCapture?.visibility = View.VISIBLE
                visibility = View.VISIBLE
                capturing = false
            }
        }, 50)
    }

    fun updateParams(opacity: Float = this.opacity, radius: Float = this.cornerRadius) {
        this.opacity = opacity
        this.cornerRadius = radius
    }
}
