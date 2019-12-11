package com.mrhuang.demo.chapter3

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import java.nio.ByteBuffer

class VideoCliper {
    private var mMediaExtractor: MediaExtractor? = null
    private var mMediaFormat: MediaFormat? = null
    private var mMediaMuxer: MediaMuxer? = null
    private var mime: String? = null
    fun clipVideo(url: String, clipPoint: Long, clipDuration: Long): Boolean {
        var videoTrackIndex = -1
        var audioTrackIndex = -1
        var videoMaxInputSize = 0
        var audioMaxInputSize = 0
        var sourceVTrack = 0
        var sourceATrack = 0
        var videoDuration: Long
        var audioDuration: Long
        Log.d(TAG, ">>　url : $url")
        //创建分离器
        mMediaExtractor = MediaExtractor()
        try { //设置文件路径
            mMediaExtractor!!.setDataSource(url)
            //创建合成器
            mMediaMuxer = MediaMuxer(url.substring(0, url.lastIndexOf(".")) + "_output.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        } catch (e: Exception) {
            Log.e(TAG, "error path" + e.message)
        }
        //获取每个轨道的信息
        for (i in 0 until mMediaExtractor!!.trackCount) {
            try {
                mMediaFormat = mMediaExtractor!!.getTrackFormat(i)
                mime = mMediaFormat!!.getString(MediaFormat.KEY_MIME)
                if (mime!!.startsWith("video/")) {
                    sourceVTrack = i
                    val width = mMediaFormat!!.getInteger(MediaFormat.KEY_WIDTH)
                    val height = mMediaFormat!!.getInteger(MediaFormat.KEY_HEIGHT)
                    videoMaxInputSize = mMediaFormat!!.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
                    videoDuration = mMediaFormat!!.getLong(MediaFormat.KEY_DURATION)
                    //检测剪辑点和剪辑时长是否正确
                    if (clipPoint >= videoDuration) {
                        Log.e(TAG, "clip point is error!")
                        return false
                    }
                    if (clipDuration != 0L && clipDuration + clipPoint >= videoDuration) {
                        Log.e(TAG, "clip duration is error!")
                        return false
                    }
                    Log.d(TAG, "width and height is " + width + " " + height
                            + ";maxInputSize is " + videoMaxInputSize
                            + ";duration is " + videoDuration
                    )
                    //向合成器添加视频轨
                    videoTrackIndex = mMediaMuxer!!.addTrack(mMediaFormat!!)
                } else if (mime!!.startsWith("audio/")) {
                    sourceATrack = i
                    val sampleRate = mMediaFormat!!.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                    val channelCount = mMediaFormat!!.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                    audioMaxInputSize = mMediaFormat!!.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
                    audioDuration = mMediaFormat!!.getLong(MediaFormat.KEY_DURATION)
                    Log.d(TAG, "sampleRate is " + sampleRate
                            + ";channelCount is " + channelCount
                            + ";audioMaxInputSize is " + audioMaxInputSize
                            + ";audioDuration is " + audioDuration
                    )
                    //添加音轨
                    audioTrackIndex = mMediaMuxer!!.addTrack(mMediaFormat!!)
                }
                Log.d(TAG, "file mime is $mime")
            } catch (e: Exception) {
                Log.e(TAG, " read error " + e.message)
            }
        }
        //分配缓冲
        val inputBuffer = ByteBuffer.allocate(videoMaxInputSize)
        //根据官方文档的解释MediaMuxer的start一定要在addTrack之后
        mMediaMuxer!!.start()
        //视频处理部分
        mMediaExtractor!!.selectTrack(sourceVTrack)
        val videoInfo = MediaCodec.BufferInfo()
        videoInfo.presentationTimeUs = 0
        var videoSampleTime: Long
        //获取源视频相邻帧之间的时间间隔。(1)
        run {
            mMediaExtractor!!.readSampleData(inputBuffer, 0)
            //skip first I frame
            if (mMediaExtractor!!.sampleFlags == MediaExtractor.SAMPLE_FLAG_SYNC) mMediaExtractor!!.advance()
            mMediaExtractor!!.readSampleData(inputBuffer, 0)
            val firstVideoPTS = mMediaExtractor!!.sampleTime
            mMediaExtractor!!.advance()
            mMediaExtractor!!.readSampleData(inputBuffer, 0)
            val SecondVideoPTS = mMediaExtractor!!.sampleTime
            videoSampleTime = Math.abs(SecondVideoPTS - firstVideoPTS)
            Log.d(TAG, "videoSampleTime is $videoSampleTime")
        }
        //选择起点
        mMediaExtractor!!.seekTo(clipPoint, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
        while (true) {
            val sampleSize = mMediaExtractor!!.readSampleData(inputBuffer, 0)
            if (sampleSize < 0) { //这里一定要释放选择的轨道，不然另一个轨道就无法选中了
                mMediaExtractor!!.unselectTrack(sourceVTrack)
                break
            }
            val trackIndex = mMediaExtractor!!.sampleTrackIndex
            //获取时间戳
            val presentationTimeUs = mMediaExtractor!!.sampleTime
            //获取帧类型，只能识别是否为I帧
            val sampleFlag = mMediaExtractor!!.sampleFlags
            Log.d(TAG, "trackIndex is " + trackIndex
                    + ";presentationTimeUs is " + presentationTimeUs
                    + ";sampleFlag is " + sampleFlag
                    + ";sampleSize is " + sampleSize)
            //剪辑时间到了就跳出
            if (clipDuration != 0L && presentationTimeUs > clipPoint + clipDuration) {
                mMediaExtractor!!.unselectTrack(sourceVTrack)
                break
            }
            mMediaExtractor!!.advance()
            videoInfo.offset = 0
            videoInfo.size = sampleSize
            videoInfo.flags = sampleFlag
            mMediaMuxer!!.writeSampleData(videoTrackIndex, inputBuffer, videoInfo)
            videoInfo.presentationTimeUs += videoSampleTime //presentationTimeUs;
        }
        //音频部分
        mMediaExtractor!!.selectTrack(sourceATrack)
        val audioInfo = MediaCodec.BufferInfo()
        audioInfo.presentationTimeUs = 0
        var audioSampleTime: Long
        //获取音频帧时长
        run {
            mMediaExtractor!!.readSampleData(inputBuffer, 0)
            //skip first sample
            if (mMediaExtractor!!.sampleTime == 0L) mMediaExtractor!!.advance()
            mMediaExtractor!!.readSampleData(inputBuffer, 0)
            val firstAudioPTS = mMediaExtractor!!.sampleTime
            mMediaExtractor!!.advance()
            mMediaExtractor!!.readSampleData(inputBuffer, 0)
            val SecondAudioPTS = mMediaExtractor!!.sampleTime
            audioSampleTime = Math.abs(SecondAudioPTS - firstAudioPTS)
            Log.d(TAG, "AudioSampleTime is $audioSampleTime")
        }
        mMediaExtractor!!.seekTo(clipPoint, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
        while (true) {
            val sampleSize = mMediaExtractor!!.readSampleData(inputBuffer, 0)
            if (sampleSize < 0) {
                mMediaExtractor!!.unselectTrack(sourceATrack)
                break
            }
            val trackIndex = mMediaExtractor!!.sampleTrackIndex
            val presentationTimeUs = mMediaExtractor!!.sampleTime
            Log.d(TAG, "trackIndex is " + trackIndex
                    + ";presentationTimeUs is " + presentationTimeUs)
            if (clipDuration != 0L && presentationTimeUs > clipPoint + clipDuration) {
                mMediaExtractor!!.unselectTrack(sourceATrack)
                break
            }
            mMediaExtractor!!.advance()
            audioInfo.offset = 0
            audioInfo.size = sampleSize
            mMediaMuxer!!.writeSampleData(audioTrackIndex, inputBuffer, audioInfo)
            audioInfo.presentationTimeUs += audioSampleTime //presentationTimeUs;
        }
        //全部写完后释放MediaMuxer和MediaExtractor
        mMediaMuxer!!.stop()
        mMediaMuxer!!.release()
        mMediaExtractor!!.release()
        mMediaExtractor = null
        return true
    }

    companion object {
        private const val TAG = "VideoClip"
    }
}