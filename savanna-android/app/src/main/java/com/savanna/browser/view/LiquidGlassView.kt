package com.savanna.browser.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View

class LiquidGlassView : GLSurfaceView {

    var opacity: Float = 0.85f
    var cornerRadius: Float = 30f

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
        if (capturing || parent !is View || width <= 0 || height <= 0) return
        capturing = true
        visibility = View.INVISIBLE
        postDelayed({
            try {
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val c = Canvas(bmp)
                c.save()
                c.translate(-left.toFloat(), -top.toFloat())
                (parent as? View)?.draw(c)
                c.restore()
                glRenderer.updateBackground(bmp)
            } catch (_: Exception) {}
            visibility = View.VISIBLE
            capturing = false
        }, 16)
    }

    fun updateParams(opacity: Float = this.opacity, radius: Float = this.cornerRadius) {
        this.opacity = opacity
        this.cornerRadius = radius
    }
}
