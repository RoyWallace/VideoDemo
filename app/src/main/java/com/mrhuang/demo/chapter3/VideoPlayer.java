package com.mrhuang.demo.chapter3;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoPlayer {

    private static final String TAG = "hwtPlay";

    private String filePath;

    private Surface surface;

    private static String KEY_VIDEO = "video/";

    private static String KEY_AUDIO = "audio/";

    private long duration;

    private static long TIMEOUT_US = 10000;

    private boolean playing = false;

    public void setVideoPath(String filePath) {
        this.filePath = filePath;
    }

    public void setSurface(Surface surface) {
        this.surface = surface;
    }

    public void play() {
        new DecodeThread().start();
    }

    public void stop() {
        playing = false;
    }

    class DecodeThread extends Thread {
        @Override
        public void run() {
            videoDecode();
            audioDecode();
        }
    }

    void videoDecode() {

        MediaExtractor videoExtractor = new MediaExtractor();
        MediaCodec videoCodec = null;

        try {
            videoExtractor.setDataSource(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < videoExtractor.getTrackCount(); i++) {
            MediaFormat mediaFormat = videoExtractor.getTrackFormat(i);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);


            if (mime.startsWith(KEY_VIDEO)) {//匹配视频对应的轨道
                videoExtractor.selectTrack(i);//选择视频对应的轨道

                //获取视频总时长
                duration = mediaFormat.getLong(MediaFormat.KEY_DURATION);

                try {
                    videoCodec = MediaCodec.createDecoderByType(mime);
                    videoCodec.configure(mediaFormat, surface, null, 0);//flag=1的时候为encode
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
            }
        }

        if (videoCodec == null) {
            return;
        }

        videoCodec.start();

        MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
        ByteBuffer[] inputBuffers = videoCodec.getInputBuffers();

        playing = true;
        while (playing) {
            int inputBufferIndex = videoCodec.dequeueInputBuffer(TIMEOUT_US);
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                int sampleSize = videoExtractor.readSampleData(inputBuffer, 0);

                if (sampleSize < 0) {
                    videoCodec.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    break;
                } else {
                    videoCodec.queueInputBuffer(inputBufferIndex, 0, sampleSize, videoExtractor.getSampleTime(), 0);
                    videoExtractor.advance();
                }
            }

            int outputBufferIndex = videoCodec.dequeueOutputBuffer(videoBufferInfo, TIMEOUT_US);
            switch (outputBufferIndex) {
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    Log.v(TAG, "format changed");
                    break;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    Log.v(TAG, "解码当前帧超时");
                    break;
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    //outputBuffers = videoCodec.getOutputBuffers();
                    Log.v(TAG, "output buffers changed");
                    break;
                default:
                    //直接渲染到Surface时使用不到outputBuffer
                    //ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                    //延时操作
                    //如果缓冲区里的可展示时间>当前视频播放的进度，就休眠一下
//                        sleepRender(videoBufferInfo, startMs);
                    //渲染
                    videoCodec.releaseOutputBuffer(outputBufferIndex, true);
                    break;
            }
        }

        videoCodec.stop();
        videoCodec.release();
        videoExtractor.release();
    }

    void audioDecode() {
        MediaExtractor mediaExtractor = new MediaExtractor();
        MediaCodec mediaCodec = null;
        AudioTrack audioTrack = null;

        try {
            mediaExtractor.setDataSource(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
            MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith(KEY_AUDIO)) {
                mediaExtractor.selectTrack(i);

                int audioEncoding = mediaFormat.getInteger(MediaFormat.KEY_PCM_ENCODING);
                int rateInHz = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                int channel = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_MASK);

                int minBufferSize = AudioTrack.getMinBufferSize(rateInHz, channel, audioEncoding);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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

                try {
                    mediaCodec = MediaCodec.createDecoderByType(mime);
                    mediaCodec.configure(mediaFormat, null, null, 0);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }

    }

}
