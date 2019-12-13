package com.mrhuang.demo.chapter3

import android.media.*
import android.os.Build
import android.util.Log
import android.view.Surface
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class VideoPlayer {
    private var filePath: String? = null
    private var surface: Surface? = null
    private var duration: Long = 0
    private var playing = false
    var width = 0
    var height = 0

    var videoSizeCallBack: ((Int, Int) -> Unit)? = null

    fun setVideoPath(filePath: String?) {
        this.filePath = filePath
    }

    fun setSurface(surface: Surface?) {
        this.surface = surface
    }

    fun play() {
        playing = true
//        DecodeThread().start()
//        audioDecodeThread().start()

        GlobalScope.launch {
            val videoTask = async {
                videoDecode()
            }
            val audioTask = async {
                audioDecode()
            }
            videoTask.await()
            audioTask.await()
        }
    }

    fun stop() {
        playing = false
    }

    internal inner class DecodeThread : Thread() {
        override fun run() {
            videoDecode()
        }
    }

    internal inner class audioDecodeThread : Thread() {
        override fun run() {
            audioDecode()
        }
    }

    fun videoDecode() {
        val mediaExtractor = MediaExtractor()
        var mediaCodec: MediaCodec? = null
        try {
            mediaExtractor.setDataSource(filePath!!)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        for (i in 0 until mediaExtractor.trackCount) {
            val mediaFormat = mediaExtractor.getTrackFormat(i)
            val mime = mediaFormat.getString(MediaFormat.KEY_MIME)
            if (mime.startsWith(KEY_VIDEO)) { //匹配视频对应的轨道
                mediaExtractor.selectTrack(i) //选择视频对应的轨道
                //获取视频总时长
                duration = mediaFormat.getLong(MediaFormat.KEY_DURATION)
                width = mediaFormat.getInteger(MediaFormat.KEY_WIDTH)
                height = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT)
                videoSizeCallBack?.invoke(width, height)
                try {
                    mediaCodec = MediaCodec.createDecoderByType(mime)
                    mediaCodec.configure(mediaFormat, surface, null, 0) //flag=1的时候为encode
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                break
            }
        }
        if (mediaCodec == null) {
            return
        }
        mediaCodec.start()
        val videoBufferInfo = MediaCodec.BufferInfo()
        val inputBuffers = mediaCodec.inputBuffers
        val outputBuffers = mediaCodec.inputBuffers
        var readEnd = false
        var lastSampleTime: Long = 0
        while (playing) {
            if (!readEnd) {
                readEnd = putBufferToCoder(mediaExtractor, mediaCodec, inputBuffers)
            }
            val outputBufferIndex = mediaCodec.dequeueOutputBuffer(videoBufferInfo, TIMEOUT_US)
            when (outputBufferIndex) {
                MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> Log.v(TAG, "format changed")
                MediaCodec.INFO_TRY_AGAIN_LATER -> {
                    Log.v(TAG, "视频解码当前帧超时")
                    try { // wait 10ms
                        Thread.sleep(10)
                    } catch (e: InterruptedException) {
                    }
                }
                MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED ->  //                    outputBuffers = mediaCodec.getOutputBuffers();
                    Log.v(TAG, "output buffers changed")
                else -> {
                    //直接渲染到Surface时使用不到outputBuffer
//                    ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
//延时操作 获取两个视频帧之间的时间间隔 让线程休眠对应的毫秒数，以保证视频播放速度正常。
                    val currentSampleTime = videoBufferInfo.presentationTimeUs
                    if (lastSampleTime > 0) {
                        val distant = (currentSampleTime - lastSampleTime) / 1000
                        Log.i(TAG, "distant: $distant")
                        if (distant > 0) {
                            try {
                                Thread.sleep(distant)
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }
                        }
                    }
                    lastSampleTime = currentSampleTime
                    //渲染 释放缓冲区
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mediaCodec.releaseOutputBuffer(outputBufferIndex, mediaExtractor.sampleTime)
                    } else {
                        mediaCodec.releaseOutputBuffer(outputBufferIndex, true)
                    }
                }
            }
            if (videoBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                Log.v(TAG, "buffer stream end")
                break
            }
        }

    }

    fun audioDecode() {
        val mediaExtractor = MediaExtractor()
        var mediaCodec: MediaCodec? = null
        var audioTrack: AudioTrack? = null
        try {
            mediaExtractor.setDataSource(filePath!!)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        for (i in 0 until mediaExtractor.trackCount) {
            val mediaFormat = mediaExtractor.getTrackFormat(i)
            val mime = mediaFormat.getString(MediaFormat.KEY_MIME)
            if (mime.startsWith(KEY_AUDIO)) {
                mediaExtractor.selectTrack(i)
                val audioEncoding = AudioFormat.ENCODING_PCM_16BIT
                val rateInHz = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                val channelCount = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT) //不能直接获取KEY_CHANNEL_MASK，所以只能获取声道数量然后再做处理
                val channel = if (channelCount == 1) AudioFormat.CHANNEL_OUT_MONO else AudioFormat.CHANNEL_OUT_STEREO
                val minBufferSize = AudioTrack.getMinBufferSize(rateInHz, channel, audioEncoding)
                val maxInputSize = mediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
                val audioInputBufferSize = if (minBufferSize > 0) minBufferSize * 4 else maxInputSize
                audioTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    AudioTrack.Builder()
                            .setAudioFormat(AudioFormat.Builder()
                                    .setEncoding(audioEncoding)
                                    .setSampleRate(rateInHz)
                                    .setChannelMask(channel)
                                    .build())
                            .setBufferSizeInBytes(audioInputBufferSize)
                            .build()
                } else {
                    AudioTrack(AudioManager.STREAM_MUSIC, rateInHz, channel,
                            audioEncoding, audioInputBufferSize, AudioTrack.MODE_STREAM)
                }
                try {
                    mediaCodec = MediaCodec.createDecoderByType(mime)
                    mediaCodec.configure(mediaFormat, null, null, 0)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                break
            }
        }
        if (mediaCodec == null) {
            return
        }
        audioTrack!!.play()
        mediaCodec.start()
        val bufferInfo = MediaCodec.BufferInfo()
        val inputBuffers = mediaCodec.inputBuffers
        var outputBuffers = mediaCodec.outputBuffers
        var readEnd = false
        val timeDistant: Long = 0
        while (playing) {
            if (!readEnd) {
                readEnd = putBufferToCoder(mediaExtractor, mediaCodec, inputBuffers)
            }
            val outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_US)
            when (outputBufferIndex) {
                MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> Log.v(TAG, "format changed")
                MediaCodec.INFO_TRY_AGAIN_LATER -> Log.v(TAG, "音频解码当前帧超时")
                MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
                    outputBuffers = mediaCodec.outputBuffers
                    Log.v(TAG, "output buffers changed")
                }
                else -> {
                    val outputBuffer = outputBuffers[outputBufferIndex] //1. 视频可以直接显示在Surface上，音频需要获取pcm所在的ByteBuffer
                    //
                    val tempBuffer = ByteArray(outputBuffer.limit())
                    outputBuffer.position(0)
                    outputBuffer[tempBuffer, 0, outputBuffer.limit()] //2.将保存在ByteBuffer的数据，转到临时的tempBuffer字节数组中去
                    outputBuffer.clear()
                    if (bufferInfo.size > 0) {
                        audioTrack.write(tempBuffer, 0, bufferInfo.size)
                    }
                    //延时操作
//如果缓冲区里的可展示时间>当前视频播放的进度，就休眠一下
//sleepRender(videoBufferInfo, startMs);
//渲染
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                        mediaCodec.releaseOutputBuffer(outputBufferIndex, mediaExtractor.getSampleTime());
//                    } else {
                    mediaCodec.releaseOutputBuffer(outputBufferIndex, true)
                }
            }
            if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                Log.v(TAG, "buffer stream end")
                break
            }
        }
        mediaExtractor.release()
        mediaCodec.stop()
        mediaCodec.release()
        audioTrack.stop()
        audioTrack.release()
    }

    /**
     * 将缓冲区传递至解码器
     *
     * @param extractor
     * @param decoder
     * @param inputBuffers
     * @return 如果到了文件末尾，返回true;否则返回false
     */
    private fun putBufferToCoder(extractor: MediaExtractor, decoder: MediaCodec, inputBuffers: Array<ByteBuffer>): Boolean {
        var isMediaEnd = false
        val inputBufferIndex = decoder.dequeueInputBuffer(TIMEOUT_US)
        if (inputBufferIndex >= 0) {
            val inputBuffer = inputBuffers[inputBufferIndex]
            val sampleSize = extractor.readSampleData(inputBuffer, 0)
            if (sampleSize < 0) {
                decoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                isMediaEnd = true
            } else {
                decoder.queueInputBuffer(inputBufferIndex, 0, sampleSize, extractor.sampleTime, 0)
                extractor.advance()
            }
        }
        return isMediaEnd
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun getSamplesForChannel(codec: MediaCodec, bufferId: Int, channelIx: Int): ShortArray? {
        val outputBuffer = codec.getOutputBuffer(bufferId)
        val format = codec.getOutputFormat(bufferId)
        val numChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        val samples = outputBuffer!!.order(ByteOrder.nativeOrder()).asShortBuffer()
        if (channelIx < 0 || channelIx >= numChannels) {
            return null
        }
        val res = ShortArray(samples.remaining() / numChannels)
        for (i in res.indices) {
            res[i] = samples[i * numChannels + channelIx]
        }
        return res
    }

    companion object {
        private const val TAG = "hwtPlay"
        private const val KEY_VIDEO = "video/"
        private const val KEY_AUDIO = "audio/"
        private const val TIMEOUT_US: Long = 0
    }
}