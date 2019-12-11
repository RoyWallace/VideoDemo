package com.mrhuang.demo.chapter1

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.mrhuang.demo.R

class TestImageView : AppCompatImageView {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.aomei)
        if (bitmap != null) {
            val drawable: Drawable = BitmapDrawable(resources, bitmap)
            //            drawable.setBounds(0, 0, getWidth(), getHeight());
            drawable.setBounds(0, 0, bitmap.width, bitmap.height)
            drawable.draw(canvas)
            //            canvas.drawBitmap(bitmap, 0, 0, null);
        } else {
        }
        bitmap?.recycle()
    }
}