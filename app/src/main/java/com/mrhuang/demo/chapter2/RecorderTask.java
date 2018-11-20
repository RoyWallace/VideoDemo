package com.mrhuang.demo.chapter2

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log

import com.mrhuang.demo.BaseTask

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class RecorderTask(internal var filePath: String) : BaseTask<Void, Void, String>() {

    internal var audioRecord: AudioRecord

    internal var minBufferSize: Int = 0

    internal var rateInHz = 44100
    internal var channel = AudioFormat.CHANNEL_IN_MONO
    internal var audioEncoding = AudioFormat.ENCODING_PCM_16BIT

    internal var isRecording = false

    internal fun startRecording() {
        execute()
    }

    override fun doInBackground(vararg values: Void): String? {

        minBufferSize = AudioRecord.getMinBufferSize(rateInHz, channel, audioEncoding)
        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, rateInHz, channel, audioEncoding, minBufferSize)

        audioRecord.startRecording()


        val file = File(filePath)

        //删除已存在的文件
        if (file.exists()) {
            file.delete()
        }

        try {
            file.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (file.exists()) {
            try {
                val outputStream = FileOutputStream(file)
                val bufferedOutputStream = BufferedOutputStream(outputStream)

                val bytes = ByteArray(minBufferSize)
                isRecording = true
                val current = 0
                while (isRecording) {
                    val bufferResult = audioRecord.read(bytes, 0, minBufferSize)
                    if (bufferResult > 0) {
                        bufferedOutputStream.write(bytes)
                        publishProgress()
                    }
                }

                audioRecord.stop()
                audioRecord.release()
                bufferedOutputStream.close()

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            PcmToWav.makePCMFileToWAVFile(filePath, filePath.substring(0, filePath.lastIndexOf(".") + 1) + "wav", false)

        } else {
            Log.i("record", "创建文件失败，文件不存在")
        }

        return null
    }

    internal fun stopRecord() {
        isRecording = false
    }
}