package com.mrhuang.demo.chapter2

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.mrhuang.demo.BaseTask
import com.mrhuang.demo.chapter2.PcmToWav.makePCMFileToWAVFile
import java.io.*

class RecorderTask(private var filePath: String) : BaseTask<Void?, Long?, String?>() {
    private var audioRecord: AudioRecord? = null
    private var minBufferSize = 0
    private var rateInHz = 44100
    private var channel = AudioFormat.CHANNEL_IN_MONO
    private var audioEncoding = AudioFormat.ENCODING_PCM_16BIT
    @JvmField
    var isRecording = false
    fun startRecording() {
        execute()
    }

    override fun doInBackground(vararg values: Void?): String? {
        minBufferSize = AudioRecord.getMinBufferSize(rateInHz, channel, audioEncoding)
        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, rateInHz, channel, audioEncoding, minBufferSize)
        audioRecord!!.startRecording()
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
                val outputStream: OutputStream = FileOutputStream(file)
                val bufferedOutputStream = BufferedOutputStream(outputStream)
                val bytes = ByteArray(minBufferSize)
                isRecording = true
                var current: Long = 0
                while (isRecording) {
                    val bufferResult = audioRecord!!.read(bytes, 0, minBufferSize)
                    if (bufferResult > 0) {
                        current += bufferResult.toLong()
                        bufferedOutputStream.write(bytes)
                        publishProgress(current)
                    }
                }
                audioRecord!!.stop()
                audioRecord!!.release()
                bufferedOutputStream.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            makePCMFileToWAVFile(filePath, filePath.substring(0, filePath.lastIndexOf(".") + 1) + "wav", false)
        } else {
            Log.i("record", "创建文件失败，文件不存在")
        }
        return null
    }

    fun stopRecord() {
        isRecording = false
    }

}