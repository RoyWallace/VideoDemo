package com.mrhuang.demo.chapter2

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import com.mrhuang.demo.BaseTask
import java.io.*

class AudioTrackTask : BaseTask<Void?, Long?, String?>() {
    private var filePath: String? = null
    private val rateInHz = 44100
    var channel = AudioFormat.CHANNEL_OUT_MONO
    var audioEncoding = AudioFormat.ENCODING_PCM_16BIT
    private var audioTrack: AudioTrack? = null
    fun play(filePath: String?) {
        this.filePath = filePath
        execute()
    }

    val isPlaying: Boolean
        get() = audioTrack!!.playState == AudioTrack.PLAYSTATE_PLAYING

    fun stop() {
        audioTrack!!.stop()
    }

    override fun doInBackground(vararg params: Void?): String? {
        val file = File(filePath)
        if (file.exists()) {
            try {
                val minBufferSize = AudioTrack.getMinBufferSize(rateInHz, channel, audioEncoding)
                val bytes = ByteArray(minBufferSize)
                audioTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    AudioTrack.Builder()
                            .setAudioFormat(AudioFormat.Builder()
                                    .setEncoding(audioEncoding)
                                    .setSampleRate(rateInHz)
                                    .setChannelMask(channel)
                                    .build())
                            .setBufferSizeInBytes(minBufferSize)
                            .build()
                } else {
                    AudioTrack(AudioManager.STREAM_MUSIC, rateInHz, channel,
                            audioEncoding, minBufferSize, AudioTrack.MODE_STREAM)
                }
                audioTrack!!.play()
                val `is`: InputStream = FileInputStream(filePath)
                var read: Int
                var current: Long = 0
                while (`is`.read(bytes).also { read = it } > 0) {
                    current += read.toLong()
                    audioTrack!!.write(bytes, 0, bytes.size)
                    publishProgress(current)
                }
                `is`.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
        }
        return null
    }
}