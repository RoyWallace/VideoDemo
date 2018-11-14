package com.mrhuang.demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

public class TestImageView extends android.support.v7.widget.AppCompatImageView {

    public TestImageView(Context context) {
        super(context);
    }

    public TestImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.aomei);

        if (bitmap != null) {
            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
//            drawable.setBounds(0, 0, getWidth(), getHeight());
            drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
            drawable.draw(canvas);
//            canvas.drawBitmap(bitmap, 0, 0, null);
        } else {

        }
        bitmap.recycle();
    }
}
