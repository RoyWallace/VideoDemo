package com.mrhuang.demo.opengl.chapter1

import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mrhuang.demo.R
import kotlinx.android.synthetic.main.activity_triangle.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class TriangleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_triangle)
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setRenderer(TriangleRenderer(this))
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

    inner class MyRenderer : GLSurfaceView.Renderer {
        override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {}
        override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {}
        override fun onDrawFrame(gl: GL10) {}
    }
}