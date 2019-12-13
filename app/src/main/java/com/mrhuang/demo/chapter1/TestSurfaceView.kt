package com.mrhuang.demo.chapter1

import android.content.Context
import android.graphics.*
import android.provider.Settings
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.mrhuang.demo.R

class TestSurfaceView : SurfaceView, SurfaceHolder.Callback {
    private var bitmap: Bitmap? = null
    private lateinit var paint: Paint
    private var stop = false

    constructor(context: Context?) : super(context) {
        initBitmap()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initBitmap()
    }

    private fun initBitmap() {
        paint = Paint()
        paint.textSize = 40f
        paint.color = Color.GREEN
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) { //直接调用线程加载Bitmap，刷新UI
        val thread = Thread(Runnable {
            var canvas: Canvas? = null
            try {
                bitmap = BitmapFactory.decodeResource(resources, R.drawable.aomei)
                canvas = holder.lockCanvas()
                canvas.drawColor(Color.BLACK) //背景
                bitmap?.run { canvas.drawBitmap(this, 0f, 0f, null) }
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        })
        thread.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stop = true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        bitmap?.recycle()
    }
}