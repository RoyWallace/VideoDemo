package com.mrhuang.demo.chapter3;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.mrhuang.demo.BaseActivity;
import com.mrhuang.demo.R;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MediaExtractorActivity extends BaseActivity implements SurfaceHolder.Callback {

    String fileName = "VID_20181031_200530.mp4";

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

        videoPlayer = new VideoPlayer();
        String filePath = path + "/Camera/" + fileName;
        videoPlayer.setVideoPath(filePath);
        surfaceView.getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        videoPlayer.setSurface(holder.getSurface());
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        videoPlayer.setSurface(holder.getSurface());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        videoPlayer.stop();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                videoPlayer.play();
            } else {
                Toast.makeText(this, "你就给这点权限，我很难帮你办事！", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    void startPlay(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        } else {
            videoPlayer.play();
        }

    }
}
