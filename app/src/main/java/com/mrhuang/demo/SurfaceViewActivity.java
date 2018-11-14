package com.mrhuang.demo;

import android.os.Bundle;
import android.view.SurfaceView;

public class SurfaceViewActivity extends BaseActivity {

    SurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface_view);
    }


    @Override
    protected void getViews() {
        surfaceView = findViewById(R.id.surfaceView);
    }

    @Override
    protected void viewCreated() {

//        Canvas canvas = surfaceView.getHolder().lockCanvas();
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher);
////        Bitmap bitmap1 = ImageDecoder.decodeBitmap()
//        canvas.drawBitmap(bitmap,0,0,null);
//        surfaceView.draw(canvas);
    }
}
