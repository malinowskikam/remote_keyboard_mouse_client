package com.kmalinowski.remote_keyboard_mouse_client

import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MouseRenderer(private val mouseHandler: MouseHandler) : GLSurfaceView.Renderer {
    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
    }

    override fun onSurfaceChanged(p0: GL10?, p1: Int, p2: Int) {
    }

    override fun onDrawFrame(p0: GL10?) {
        mouseHandler.handleGesture()
    }
}