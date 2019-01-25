package com.mrhuang.demo.opengl.chapter2;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.mrhuang.demo.R;

public class GLVideoActivity extends AppCompatActivity {

    GLSurfaceView glSurfaceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glvideo);

        glSurfaceView = findViewById(R.id.glSurfaceView);
        glSurfaceView.setEGLContextClientVersion(2);
        GLVideoRenderer renderer = new GLVideoRenderer(this, "/storage/emulated/0/Download/VID_20181014_145854.mp4");
        renderer.setRenderCallback(glSurfaceView::requestRender);

        glSurfaceView.setRenderer(renderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onResume();
    }
}
