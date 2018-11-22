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

    public int width;

    public int height;

    public void setVideoSizeCallBack(VideoSizeCallBack videoSizeCallBack) {
        this.videoSizeCallBack = videoSizeCallBack;
    }

    VideoSizeCallBack videoSizeCallBack;

    public void setVideoPath(String filePath) {
        this.filePath = filePath;
    }

    public void setSurface(Surface surface) {
        this.surface = surface;
    }

    public void play() {
        playing = true;
        new DecodeThread().start();
        new audioDecodeThread().start();
    }

    public void stop() {
        playing = false;
    }

    class DecodeThread extends Thread {
        @Override
        public void run() {
            videoDecode();
        }
    }

    class audioDecodeThread extends Thread {
        @Override
        public void run() {
            audioDecode();
        }
    }

    void videoDecode() {

        MediaExtractor mediaExtractor = new MediaExtractor();
        MediaCodec mediaCodec = null;

        try {
            mediaExtractor.setDataSource(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
            MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);


            if (mime.startsWith(KEY_VIDEO)) {//匹配视频对应的轨道
                mediaExtractor.selectTrack(i);//选择视频对应的轨道

                //获取视频总时长
                duration = mediaFormat.getLong(MediaFormat.KEY_DURATION);
                width = mediaFormat.getInteger(MediaFormat.KEY_WIDTH);
                height = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT);

                videoSizeCallBack.callback(width, height);

                try {
                    mediaCodec = MediaCodec.createDecoderByType(mime);
                    mediaCodec.configure(mediaFormat, surface, null, 0);//flag=1的时候为encode
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
            }
        }

        if (mediaCodec == null) {
            return;
        }

        mediaCodec.start();

        MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();


        boolean readEnd = false;

        while (playing) {

            if (!readEnd) {
                readEnd = putBufferToCoder(mediaExtractor, mediaCodec, inputBuffers);
            }

            int outputBufferIndex = mediaCodec.dequeueOutputBuffer(videoBufferInfo, TIMEOUT_US);
            switch (outputBufferIndex) {
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    Log.v(TAG, "format changed");
                    break;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    Log.v(TAG, "视频解码当前帧超时");
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
                    mediaCodec.releaseOutputBuffer(outputBufferIndex, true);
                    break;
            }

            if ((videoBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                Log.v(TAG, "buffer stream end");
                break;
            }
        }

        mediaCodec.stop();
        mediaCodec.release();
        mediaExtractor.release();
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

                int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
                int rateInHz = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                int channelCount = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);//不能直接获取KEY_CHANNEL_MASK，所以只能获取声道数量然后再做处理

                int channel = channelCount == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO;

                int minBufferSize = AudioTrack.getMinBufferSize(rateInHz, channel, audioEncoding);
                int maxInputSize = mediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
                int audioInputBufferSize = minBufferSize > 0 ? minBufferSize * 4 : maxInputSize;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    audioTrack = new AudioTrack.Builder()
                            .setAudioFormat(new AudioFormat.Builder()
                                    .setEncoding(audioEncoding)
                                    .setSampleRate(rateInHz)
                                    .setChannelMask(channel)
                                    .build())
                            .setBufferSizeInBytes(audioInputBufferSize)
                            .build();
                } else {
                    audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, rateInHz, channel,
                            audioEncoding, audioInputBufferSize, AudioTrack.MODE_STREAM);
                }

                try {
                    mediaCodec = MediaCodec.createDecoderByType(mime);
                    mediaCodec.configure(mediaFormat, null, null, 0);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;

            }
        }

        if (mediaCodec == null) {
            return;
        }

        audioTrack.play();
        mediaCodec.start();

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
        ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();

        boolean readEnd = false;
        while (playing) {

            if (!readEnd) {
                readEnd = putBufferToCoder(mediaExtractor, mediaCodec, inputBuffers);
            }

            int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_US);
            switch (outputBufferIndex) {
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    Log.v(TAG, "format changed");
                    break;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    Log.v(TAG, "音频解码当前帧超时");
                    break;
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    outputBuffers = mediaCodec.getOutputBuffers();
                    Log.v(TAG, "output buffers changed");
                    break;
                default:
                    ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];//1. 视频可以直接显示在Surface上，音频需要获取pcm所在的ByteBuffer
                    byte[] tempBuffer = new byte[outputBuffer.limit()];
                    outputBuffer.position(0);
                    outputBuffer.get(tempBuffer, 0, outputBuffer.limit());      //2.将保存在ByteBuffer的数据，转到临时的tempBuffer字节数组中去
                    outputBuffer.clear();
                    if (bufferInfo.size > 0) {
                        audioTrack.write(tempBuffer, 0, bufferInfo.size);
                    }
                    //延时操作
                    //如果缓冲区里的可展示时间>当前视频播放的进度，就休眠一下
                    //sleepRender(videoBufferInfo, startMs);
                    //渲染
                    mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                    break;
            }

            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                Log.v(TAG, "buffer stream end");
                break;
            }

        }

        mediaExtractor.release();
        mediaCodec.stop();
        mediaCodec.release();
        audioTrack.stop();
        audioTrack.release();

    }

    /**
     * 将缓冲区传递至解码器
     *
     * @param extractor
     * @param decoder
     * @param inputBuffers
     * @return 如果到了文件末尾，返回true;否则返回false
     */
    private boolean putBufferToCoder(MediaExtractor extractor, MediaCodec decoder, ByteBuffer[] inputBuffers) {
        boolean isMediaEnd = false;
        int inputBufferIndex = decoder.dequeueInputBuffer(TIMEOUT_US);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            int sampleSize = extractor.readSampleData(inputBuffer, 0);
            if (sampleSize < 0) {
                decoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                isMediaEnd = true;
            } else {
                decoder.queueInputBuffer(inputBufferIndex, 0, sampleSize, extractor.getSampleTime(), 0);
                extractor.advance();
            }
        }
        return isMediaEnd;
    }

    public interface VideoSizeCallBack {
        public void callback(int width, int height);
    }

}
