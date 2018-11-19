package com.mrhuang.demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

public class AudioRecordActivity extends BaseActivity implements View.OnClickListener, RecordListFragment.ItemClickListener {

    Button start, stop, play;
    SeekBar seekBar;

    TextView currentTime, totalTime;

    AudioRecord audioRecord;

    int rateInHz = 44100;
    int channel = AudioFormat.CHANNEL_IN_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    boolean isRecording;


    AudioTrack audioTrack;


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


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startRecord();
                    }
                }).start();
            }

        } else if (v.getId() == R.id.stop) {
            isRecording = false;
        } else if (v.getId() == R.id.play) {
            fragment.show(getSupportFragmentManager(), "bottom");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startRecord();
                    }
                }).start();
            } else {
                Toast.makeText(this, "你就给这点权限，我很难帮你办事！", Toast.LENGTH_SHORT).show();
            }
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
//                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
//                DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);

                byte[] bytes = new byte[bufferSize];
                audioRecord.startRecording();

                isRecording = true;

                while (isRecording) {
                    int bufferResult = audioRecord.read(bytes, 0, bufferSize);
                    if (bufferResult > 0) {
                        outputStream.write(bytes);
                    }
                }

                audioRecord.stop();
                audioRecord.release();
                outputStream.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            PcmToWav.makePCMFileToWAVFile(filePath, filePath.substring(0, filePath.lastIndexOf(".") + 1) + "wav", false);

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
//                InputStream inputStream = new FileInputStream(filePath);
//                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
//                DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);
//                int i = 0;
//                while (dataInputStream.read() > 0) {
//                    music[i] = dataInputStream.readShort();
//                    i++;
//                }
//                dataInputStream.close();


                int minBufferSize = AudioTrack.getMinBufferSize(rateInHz, AudioFormat.CHANNEL_OUT_MONO, audioEncoding);
                int length = minBufferSize + 1024;
                byte[] bytes = new byte[length];

                if (Build.VERSION.SDK_INT >= 23) {
                    audioTrack = new AudioTrack.Builder()
                            .setAudioFormat(new AudioFormat.Builder()
                                    .setEncoding(audioEncoding)
                                    .setSampleRate(rateInHz)
                                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                    .build())
                            .setBufferSizeInBytes(minBufferSize)
                            .build();
                } else {
                    audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, rateInHz,
                            AudioFormat.CHANNEL_OUT_MONO, audioEncoding,
                            minBufferSize, AudioTrack.MODE_STREAM);
                }

                InputStream is = new FileInputStream(filePath);


                //实测length参数很重要，太大或者大小都有可能导致异常：play() called on uninitialized AudioTrack
                //int length = (int) audioFile.length();

                int read;
                while ((read = is.read(bytes)) > 0) {
                    audioTrack.write(bytes, 0, read);
                }
                audioTrack.play();
                is.close();
//                audioTrack.write(music, 0, musicLength);
//                audioTrack.stop();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

        }
    }

    long fileLength = 0;

    @Override
    public void onRecordClick(File file) {
        fragment.dismiss();

        seekBar.setMax(100);
        fileLength = file.length();
        currentTime.setText("0");

        long duration = getPcmDuration(rateInHz, 16, 1, fileLength);
        String durationString = duration / 1000 + ":" + duration % 1000;
        totalTime.setText(durationString);

        new PlayTask().execute(file.getAbsolutePath());
    }


    public class PlayTask extends AsyncTask<String, Long, Void> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... path) {
            String filePath = path[0];
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
                                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                        .build())
                                .setBufferSizeInBytes(minBufferSize)
                                .build();
                    } else {
                        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, rateInHz,
                                AudioFormat.CHANNEL_OUT_MONO, audioEncoding,
                                minBufferSize, AudioTrack.MODE_STREAM);
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

        @Override
        protected void onProgressUpdate(Long... values) {
            int percent = (int) (values[0] * 100 / fileLength);
            seekBar.setProgress(percent);

            long current = getPcmDuration(rateInHz, 16, 1, values[0]);
            String durationString = current / 1000 + ":" + current % 1000;
            currentTime.setText(durationString);
        }

        @Override
        protected void onPostExecute(Void aVoid) {

        }
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
}
