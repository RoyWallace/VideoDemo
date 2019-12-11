package com.mrhuang.demo.chapter2

import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * 统一的wav文件的header
 */
class WaveHeader {
    private val fileID = charArrayOf('R', 'I', 'F', 'F')
    var fileLength = 0
    var wavTag = charArrayOf('W', 'A', 'V', 'E')
    var FmtHdrID = charArrayOf('f', 'm', 't', ' ')
    var FmtHdrLength = 0
    var FormatTag: Short = 0
    var Channels: Short = 0
    var SamplesPerSec = 0
    var AvgBytesPerSec = 0
    var BlockAlign: Short = 0
    var BitsPerSample: Short = 0
    var DataHdrID = charArrayOf('d', 'a', 't', 'a')
    var DataHdrLength = 0
    @get:Throws(IOException::class)
    val header: ByteArray
        get() {
            val bos = ByteArrayOutputStream()
            WriteChar(bos, fileID)
            WriteInt(bos, fileLength)
            WriteChar(bos, wavTag)
            WriteChar(bos, FmtHdrID)
            WriteInt(bos, FmtHdrLength)
            WriteShort(bos, FormatTag.toInt())
            WriteShort(bos, Channels.toInt())
            WriteInt(bos, SamplesPerSec)
            WriteInt(bos, AvgBytesPerSec)
            WriteShort(bos, BlockAlign.toInt())
            WriteShort(bos, BitsPerSample.toInt())
            WriteChar(bos, DataHdrID)
            WriteInt(bos, DataHdrLength)
            bos.flush()
            val r = bos.toByteArray()
            bos.close()
            return r
        }

    @Throws(IOException::class)
    fun WriteShort(bos: ByteArrayOutputStream, s: Int) {
        val myByte = ByteArray(2)
        myByte[1] = (s shl 16 shr 24).toByte()
        myByte[0] = (s shl 24 shr 24).toByte()
        bos.write(myByte)
    }

    @Throws(IOException::class)
    private fun WriteInt(bos: ByteArrayOutputStream, n: Int) {
        val buf = ByteArray(4)
        buf[3] = (n shr 24).toByte()
        buf[2] = (n shl 8 shr 24).toByte()
        buf[1] = (n shl 16 shr 24).toByte()
        buf[0] = (n shl 24 shr 24).toByte()
        bos.write(buf)
    }

    private fun WriteChar(bos: ByteArrayOutputStream, id: CharArray) {
        for (i in id.indices) {
            val c = id[i]
            bos.write(c.toInt())
        }
    }
}