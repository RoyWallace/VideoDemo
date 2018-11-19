package com.mrhuang.demo.chapter2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mrhuang.demo.BaseActivity;
import com.mrhuang.demo.BaseTask;
import com.mrhuang.demo.R;

import java.io.File;

public class AudioRecordActivity extends BaseActivity implements View.OnClickListener, RecordListFragment.ItemClickListener, BaseTask.ProgressListener<Long> {

    Button start, stop, play;
    SeekBar seekBar;

    TextView currentTime, totalTime;

    RecorderTask recorderTask;
    AudioTrackTask audioTrackTask;

    RecordListFragment fragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_record);
    }

    @Override
    protected void getViews() {
        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        play = findViewById(R.id.play);

        currentTime = findViewById(R.id.currentTime);
        totalTime = findViewById(R.id.totalTime);

        seekBar = findViewById(R.id.seekBar);
    }

    @Override
    protected void viewCreated() {
        start.setOnClickListener(this);
        stop.setOnClickListener(this);
        play.setOnClickListener(this);

        fragment = new RecordListFragment();
        fragment.setItemClickListener(this);

        String filePath = getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/record" + System.currentTimeMillis() + ".pcm";
        recorderTask = new RecorderTask(filePath);
        audioTrackTask = new AudioTrackTask(filePath);
        audioTrackTask.setProgressListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.start) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            } else {
                recorderTask.startRecording();
            }

        } else if (v.getId() == R.id.stop) {
            recorderTask.stopRecord();
        } else if (v.getId() == R.id.play) {
            fragment.show(getSupportFragmentManager(), "bottom");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recorderTask.startRecording();
            } else {
                Toast.makeText(this, "你就给这点权限，我很难帮你办事！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    long fileLength = 0;

    @Override
    public void onRecordClick(File file) {
        fragment.dismiss();

        seekBar.setMax(100);
        fileLength = file.length();
        currentTime.setText("0");

        long duration = getPcmDuration(44100, 16, 1, fileLength);
        String durationString = duration / 1000 + ":" + duration % 1000;
        totalTime.setText(durationString);

        audioTrackTask.play();
    }

    @Override
    public void onProgressUpdate(Long[] values) {
        int percent = (int) (values[0] * 100 / fileLength);
        seekBar.setProgress(percent);

        long current = getPcmDuration(44100, 16, 1, values[0]);
        String durationString = current / 1000 + ":" + current % 1000;
        currentTime.setText(durationString);
    }

    /**
     * 数据量Byte=
     * 采样频率Hz
     * ×（采样位数/8）
     * × 声道数
     * × 时间s
     */

    long getPcmDuration(int rateInHz, int rateNumber, int channels, long fileLength) {
        return ((fileLength * 1024 * 1000) / (rateInHz * rateNumber / 8 * channels)) / 1024;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioTrackTask.setProgressListener(null);
    }
}
