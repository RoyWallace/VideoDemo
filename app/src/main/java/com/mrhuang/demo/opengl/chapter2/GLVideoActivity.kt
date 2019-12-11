package com.mrhuang.demo.opengl.chapter2

import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mrhuang.demo.R
import kotlinx.android.synthetic.main.activity_glvideo.*

class GLVideoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_glvideo)
        glSurfaceView.setEGLContextClientVersion(2)
        val renderer = GLVideoRenderer(this, "/storage/emulated/0/Download/VID_20181014_145854.mp4")
        renderer.setRenderCallback { glSurfaceView.requestRender() }
        glSurfaceView.setRenderer(renderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView.onResume()
    }
}