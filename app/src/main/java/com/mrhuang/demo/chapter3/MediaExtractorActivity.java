package com.mrhuang.demo.chapter3;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.mrhuang.demo.BaseActivity;
import com.mrhuang.demo.R;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MediaExtractorActivity extends BaseActivity implements SurfaceHolder.Callback {

    String fileName = "VID_20181120_182318.mp4";

    String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath();

    SurfaceView surfaceView;

    VideoPlayer videoPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_extractor);
    }

    @Override
    protected void getViews() {
        surfaceView = findViewById(R.id.surfaceView);

    }

    @Override
    protected void viewCreated() {

        String filePath = path + "/Camera/" + fileName;
        videoPlayer = new VideoPlayer();
        videoPlayer.setVideoPath(filePath);
        surfaceView.getHolder().addCallback(this);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        videoPlayer.setSurface(holder.getSurface());
        videoPlayer.play();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        videoPlayer.setSurface(holder.getSurface());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        videoPlayer.stop();
    }
}
