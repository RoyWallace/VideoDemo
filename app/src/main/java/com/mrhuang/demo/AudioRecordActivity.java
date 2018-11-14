package com.mrhuang.demo;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

public class AudioRecordActivity extends BaseActivity implements View.OnClickListener {

    Button start, stop, play;
    SeekBar seekBar;

    AudioRecord audioRecord;

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

        seekBar = findViewById(R.id.seekBar);
    }

    @Override
    protected void viewCreated() {
        start.setOnClickListener(this);
        stop.setOnClickListener(this);
        play.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.start) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    audioRecord.startRecording();
                }
            });
        } else if (v.getId() == R.id.stop) {
            audioRecord.stop();
        } else if (v.getId() == R.id.play) {

        }
    }
}
