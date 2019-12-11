package com.mrhuang.demo.chapter1

import android.os.Bundle
import android.view.SurfaceView
import com.mrhuang.demo.BaseActivity
import com.mrhuang.demo.R

class SurfaceViewActivity : BaseActivity() {
    var surfaceView: SurfaceView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_surface_view)
    }

    override fun getViews() {
        surfaceView = findViewById(R.id.surfaceView)
    }

    override fun viewCreated() {}
}