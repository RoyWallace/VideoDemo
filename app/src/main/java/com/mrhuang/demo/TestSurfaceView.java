package com.mrhuang.demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class TestSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    Bitmap bitmap;

    Paint paint;

    boolean stop;

    public TestSurfaceView(Context context) {
        super(context);
        initBitmap();
    }

    public TestSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initBitmap();
    }


    void initBitmap() {
        paint = new Paint();
        paint.setTextSize(40);
        paint.setColor(Color.GREEN);

        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                int i = 0;

//                while (i < 100) {
//                    if (stop) {
//                        return;
//                    }
                    i++;
                    Canvas canvas = null;
                    try {
                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.aomei);
                        canvas = holder.lockCanvas();
//                        canvas.drawText("" + i, i * 40, 100, paint);
                        if (canvas != null) {
                            canvas.drawColor(Color.BLACK);//背景
                            canvas.drawBitmap(bitmap, 0, 0, null);
                        }
//                        draw(canvas);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
                    } finally {
                        if (canvas != null) {
                            holder.unlockCanvasAndPost(canvas);
                        }
                    }
//                }
            }
        });

        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stop = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }
}
