package com.savanna.browser.view

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class LiquidGlassRenderer(private val view: LiquidGlassView) : GLSurfaceView.Renderer {

    private var program = 0
    private var textureId = 0
    private var vbo = IntArray(2)
    private var ibo = IntArray(1)

    private var background: Bitmap? = null
    private var needsUpload = false

    private var uTextureLoc      = 0
    private var uResolutionLoc   = 0
    private var uTimeLoc         = 0
    private var uOpacityLoc      = 0
    private var uBlurLoc         = 0
    private var uDisplacementLoc = 0
    private var uSaturationLoc   = 0
    private var uAberrationLoc   = 0
    private var uCornerRadiusLoc = 0
    private var aPositionLoc     = 0
    private var aTexCoordLoc     = 0

    private val quadVerts = floatArrayOf(
        -1f, -1f, 0f, 1f,
         1f, -1f, 1f, 1f,
         1f,  1f, 1f, 0f,
        -1f,  1f, 0f, 0f
    )
    private val quadIdx = shortArrayOf(0, 1, 2, 0, 2, 3)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        program = createProgram()
        if (program == 0) return

        aPositionLoc = GLES20.glGetAttribLocation(program, "aPosition")
        aTexCoordLoc = GLES20.glGetAttribLocation(program, "aTexCoord")
        uTextureLoc      = GLES20.glGetUniformLocation(program, "uTexture")
        uResolutionLoc   = GLES20.glGetUniformLocation(program, "uResolution")
        uTimeLoc         = GLES20.glGetUniformLocation(program, "uTime")
        uOpacityLoc      = GLES20.glGetUniformLocation(program, "uOpacity")
        uBlurLoc         = GLES20.glGetUniformLocation(program, "uBlur")
        uDisplacementLoc = GLES20.glGetUniformLocation(program, "uDisplacement")
        uSaturationLoc   = GLES20.glGetUniformLocation(program, "uSaturation")
        uAberrationLoc   = GLES20.glGetUniformLocation(program, "uAberration")
        uCornerRadiusLoc = GLES20.glGetUniformLocation(program, "uCornerRadius")

        val bb = ByteBuffer.allocateDirect(quadVerts.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().put(quadVerts)
        bb.position(0)
        val ib = ByteBuffer.allocateDirect(quadIdx.size * 2)
            .order(ByteOrder.nativeOrder()).asShortBuffer().put(quadIdx)
        ib.position(0)

        GLES20.glGenBuffers(2, vbo, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0])
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, quadVerts.size * 4, bb, GLES20.GL_STATIC_DRAW)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

        GLES20.glGenBuffers(1, ibo, 0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0])
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, quadIdx.size * 2, ib, GLES20.GL_STATIC_DRAW)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)

        val texIds = IntArray(1)
        GLES20.glGenTextures(1, texIds, 0)
        textureId = texIds[0]
    }

    override fun onSurfaceChanged(gl: GL10?, w: Int, h: Int) {
        GLES20.glViewport(0, 0, w, h)
    }

    override fun onDrawFrame(gl: GL10?) {
        if (needsUpload && background != null) {
            uploadTexture(background!!)
            needsUpload = false
        }
        if (program == 0) return

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glUseProgram(program)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(uTextureLoc, 0)

        GLES20.glUniform2f(uResolutionLoc, view.width.toFloat(), view.height.toFloat())
        GLES20.glUniform1f(uTimeLoc, System.nanoTime() / 1e9f)
        GLES20.glUniform1f(uOpacityLoc, view.opacity)
        GLES20.glUniform1f(uBlurLoc, 0.2f)
        GLES20.glUniform1f(uDisplacementLoc, 86f)
        GLES20.glUniform1f(uSaturationLoc, 1.3f)
        GLES20.glUniform1f(uAberrationLoc, 2f)
        GLES20.glUniform1f(uCornerRadiusLoc, view.cornerRadius)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0])
        GLES20.glEnableVertexAttribArray(aPositionLoc)
        GLES20.glVertexAttribPointer(aPositionLoc, 2, GLES20.GL_FLOAT, false, 16, 0)
        GLES20.glEnableVertexAttribArray(aTexCoordLoc)
        GLES20.glVertexAttribPointer(aTexCoordLoc, 2, GLES20.GL_FLOAT, false, 16, 8)

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0])
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, 0)

        GLES20.glDisableVertexAttribArray(aPositionLoc)
        GLES20.glDisableVertexAttribArray(aTexCoordLoc)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
    }

    fun updateBackground(bmp: Bitmap) {
        background = bmp
        needsUpload = true
    }

    private fun uploadTexture(bmp: Bitmap) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0)
    }

    private fun createProgram(): Int {
        val vSrc = readRaw(com.savanna.browser.R.raw.passthrough_vert)
        val fSrc = readRaw(com.savanna.browser.R.raw.liquid_glass_frag)
        if (vSrc == null || fSrc == null) return 0

        val vs = compileShader(GLES20.GL_VERTEX_SHADER, vSrc) ?: return 0
        val fs = compileShader(GLES20.GL_FRAGMENT_SHADER, fSrc) ?: return 0

        val prog = GLES20.glCreateProgram()
        GLES20.glAttachShader(prog, vs)
        GLES20.glAttachShader(prog, fs)
        GLES20.glLinkProgram(prog)
        GLES20.glDeleteShader(vs)
        GLES20.glDeleteShader(fs)

        val status = IntArray(1)
        GLES20.glGetProgramiv(prog, GLES20.GL_LINK_STATUS, status, 0)
        if (status[0] == 0) {
            GLES20.glDeleteProgram(prog)
            return 0
        }
        return prog
    }

    private fun compileShader(type: Int, src: String): Int? {
        val s = GLES20.glCreateShader(type)
        GLES20.glShaderSource(s, src)
        GLES20.glCompileShader(s)
        val status = IntArray(1)
        GLES20.glGetShaderiv(s, GLES20.GL_COMPILE_STATUS, status, 0)
        return if (status[0] == 0) { GLES20.glDeleteShader(s); null } else s
    }

    private fun readRaw(id: Int): String? {
        return try {
            view.context.resources.openRawResource(id).bufferedReader().use { it.readText() }
        } catch (_: Exception) { null }
    }
}
