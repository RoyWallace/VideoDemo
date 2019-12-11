package com.mrhuang.demo.chapter2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.mrhuang.demo.BaseActivity;
import com.mrhuang.demo.BaseTask;
import com.mrhuang.demo.R;

import java.io.File;

public class AudioRecordActivity extends BaseActivity implements View.OnClickListener, RecordListFragment.ItemClickListener {

    Button start, play;
    SeekBar seekBar;

    TextView recordTextView, currentTime, totalTime;

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
        play = findViewById(R.id.play);

        recordTextView = findViewById(R.id.recordTextView);

        currentTime = findViewById(R.id.currentTime);
        totalTime = findViewById(R.id.totalTime);

        seekBar = findViewById(R.id.seekBar);
    }

    @Override
    protected void viewCreated() {
        start.setOnClickListener(this);
        play.setOnClickListener(this);

        fragment = new RecordListFragment();
        fragment.setItemClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.start) {
            if (recorderTask != null && recorderTask.isRecording) {
                recorderTask.stopRecord();
                start.setText("start");
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.RECORD_AUDIO,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                } else {
                    startRecording();
                }
            }
        } else if (v.getId() == R.id.play) {
            if (audioTrackTask != null && audioTrackTask.isPlaying()) {
                audioTrackTask.stop();
                play.setText("play");
            } else {
                fragment.show(getSupportFragmentManager(), "bottom");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording();
            } else {
                Toast.makeText(this, "你就给这点权限，我很难帮你办事！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void startRecording() {
        String filePath = getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/record" + System.currentTimeMillis() + ".pcm";
        recorderTask = new RecorderTask(filePath);
        recorderTask.setProgressListener(new BaseTask.ProgressListener<Long>() {
            @Override
            public void onProgressUpdate(Long[] values) {
                long current = getPcmDuration(44100, 16, 1, values[0]);
                String durationString = String.format("%02d:%03d", current / 1000, current % 1000);
                recordTextView.setText(durationString);
            }
        });
        recorderTask.startRecording();
        start.setText("stop");
    }

    long fileLength = 0;

    @Override
    public void onRecordClick(File file) {
        fragment.dismiss();

        seekBar.setMax(100);
        fileLength = file.length();
        currentTime.setText("00:000");

        long duration = getPcmDuration(44100, 16, 1, fileLength);
        String durationString = duration / 1000 + ":" + duration % 1000;
        totalTime.setText(durationString);


        audioTrackTask = new AudioTrackTask();
        audioTrackTask.setProgressListener(new BaseTask.ProgressListener<Long>() {
            @Override
            public void onProgressUpdate(Long[] values) {
                int percent = (int) (values[0] * 100 / fileLength);
                seekBar.setProgress(percent);

                long current = getPcmDuration(44100, 16, 1, values[0]);
                String durationString = current / 1000 + ":" + current % 1000;
                currentTime.setText(durationString);
            }
        });
        audioTrackTask.setCompleteListener(new BaseTask.CompleteListener() {
            @Override
            public void onComplete(Object o) {
                play.setText("play");
            }
        });
        audioTrackTask.play(file.getPath());
        play.setText("stop");
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
