package com.mrhuang.demo.chapter2;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.mrhuang.demo.BaseTask;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class RecorderTask extends BaseTask<Void, Long, String> {

    AudioRecord audioRecord;

    int minBufferSize;

    int rateInHz = 44100;
    int channel = AudioFormat.CHANNEL_IN_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    boolean isRecording = false;

    String filePath;

    public RecorderTask(String filePath) {
        this.filePath = filePath;
    }

    void startRecording() {
        execute();
    }

    @Override
    protected String doInBackground(Void... values) {

        minBufferSize = AudioRecord.getMinBufferSize(rateInHz, channel, audioEncoding);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, rateInHz, channel, audioEncoding, minBufferSize);

        audioRecord.startRecording();


        File file = new File(filePath);

        //删除已存在的文件
        if (file.exists()) {
            file.delete();
        }

        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (file.exists()) {
            try {
                OutputStream outputStream = new FileOutputStream(file);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

                byte[] bytes = new byte[minBufferSize];
                isRecording = true;
                long current = 0;
                while (isRecording) {
                    int bufferResult = audioRecord.read(bytes, 0, minBufferSize);
                    if (bufferResult > 0) {
                        current += bufferResult;
                        bufferedOutputStream.write(bytes);
                        publishProgress(current);
                    }
                }

                audioRecord.stop();
                audioRecord.release();
                bufferedOutputStream.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            PcmToWav.makePCMFileToWAVFile(filePath, filePath.substring(0, filePath.lastIndexOf(".") + 1) + "wav", false);

        } else {
            Log.i("record", "创建文件失败，文件不存在");
        }

        return null;
    }

    void stopRecord() {
        isRecording = false;
    }
}