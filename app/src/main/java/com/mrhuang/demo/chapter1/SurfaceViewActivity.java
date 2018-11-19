package com.mrhuang.demo.chapter1;

import android.os.Bundle;
import android.view.SurfaceView;

import com.mrhuang.demo.BaseActivity;
import com.mrhuang.demo.R;

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
    }
}
