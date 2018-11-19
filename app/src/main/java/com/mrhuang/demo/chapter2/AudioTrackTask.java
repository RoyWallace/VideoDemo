package com.mrhuang.demo.chapter2;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;

import com.mrhuang.demo.BaseTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class AudioTrackTask extends BaseTask<Void, Long, String> {

    private String filePath;

    private int rateInHz = 44100;

    int channel = AudioFormat.CHANNEL_IN_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    private AudioTrack audioTrack;

    public AudioTrackTask(String filePath) {
        this.filePath = filePath;
    }


    public void play() {
        execute();
    }

    @Override
    protected String doInBackground(Void... voids) {
        File file = new File(filePath);
        if (file.exists()) {
            try {
                int minBufferSize = AudioTrack.getMinBufferSize(rateInHz, AudioFormat.CHANNEL_OUT_MONO, audioEncoding);
                byte[] bytes = new byte[minBufferSize];

                if (Build.VERSION.SDK_INT >= 23) {
                    audioTrack = new AudioTrack.Builder()
                            .setAudioFormat(new AudioFormat.Builder()
                                    .setEncoding(audioEncoding)
                                    .setSampleRate(rateInHz)
                                    .setChannelMask(channel)
                                    .build())
                            .setBufferSizeInBytes(minBufferSize)
                            .build();
                } else {
                    audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, rateInHz, channel,
                            audioEncoding, minBufferSize, AudioTrack.MODE_STREAM);
                }

                audioTrack.play();

                InputStream is = new FileInputStream(filePath);

                //实测length参数很重要，太大或者大小都有可能导致异常：play() called on uninitialized AudioTrack
                //int length = (int) audioFile.length();

                int read;
                long current = 0;
                while ((read = is.read(bytes)) > 0) {
                    current += read;
                    audioTrack.write(bytes, 0, bytes.length);
                    publishProgress(current);
                }

                is.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {

        }
        return null;
    }
}
