package com.mrhuang.demo;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.Buffer;

public class AudioRecordActivity extends BaseActivity implements View.OnClickListener {

    Button start, stop, play;
    SeekBar seekBar;

    AudioRecord audioRecord;

    int rateInHz = 16000;
    int channel = AudioFormat.CHANNEL_IN_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    boolean isRecording;


    AudioTrack audioTrack;

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
                    startRecord();
                }
            });
        } else if (v.getId() == R.id.stop) {
            isRecording = false;
        } else if (v.getId() == R.id.play) {
            playRecord("");
        }
    }


    public void startRecord() {

        String filePath = getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/record" + System.currentTimeMillis() + ".pcm";
        File file = new File(filePath);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (file.exists()) {


            int bufferSize = AudioRecord.getMinBufferSize(rateInHz, channel, audioEncoding);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, rateInHz, channel, audioEncoding, bufferSize);

            try {
                OutputStream outputStream = new FileOutputStream(file);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
                DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);

                byte[] buffer = new byte[bufferSize];
                audioRecord.startRecording();

                isRecording = true;

                while (isRecording) {
                    int bufferResult = audioRecord.read(buffer, 0, bufferSize);
                    for (int i = 0; i < bufferResult; i++) {
                        dataOutputStream.write(buffer[i]);
                    }
                }

                audioRecord.stop();
                dataOutputStream.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("record", "创建文件失败，文件不存在");
        }
    }


    void playRecord(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            int musicLength = (int) (file.length() / 2);
            short[] music = new short[musicLength];

            try {
                InputStream inputStream = new FileInputStream(filePath);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);
                int i = 0;
                while (dataInputStream.read() > 0) {
                    music[i] = dataInputStream.readShort();
                    i++;
                }
                dataInputStream.close();

                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, rateInHz, channel, audioEncoding, musicLength * 2, AudioTrack.MODE_STREAM);
                audioTrack.play();
                audioTrack.write(music, 0, musicLength);
                audioTrack.stop();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

        }
    }
}
