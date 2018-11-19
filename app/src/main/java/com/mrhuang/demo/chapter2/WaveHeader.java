package com.mrhuang.demo.chapter2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 统一的wav文件的header
 */
public class WaveHeader {
    public final char fileID[] = {'R', 'I', 'F', 'F'};
    public int fileLength;
    public char wavTag[] = {'W', 'A', 'V', 'E'};
    public char FmtHdrID[] = {'f', 'm', 't', ' '};
    public int FmtHdrLength;
    public short FormatTag;
    public short Channels;
    public int SamplesPerSec;
    public int AvgBytesPerSec;
    public short BlockAlign;
    public short BitsPerSample;
    public char DataHdrID[] = {'d', 'a', 't', 'a'};
    public int DataHdrLength;

    public byte[] getHeader() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        WriteChar(bos, fileID);
        WriteInt(bos, fileLength);
        WriteChar(bos, wavTag);
        WriteChar(bos, FmtHdrID);
        WriteInt(bos, FmtHdrLength);
        WriteShort(bos, FormatTag);
        WriteShort(bos, Channels);
        WriteInt(bos, SamplesPerSec);
        WriteInt(bos, AvgBytesPerSec);
        WriteShort(bos, BlockAlign);
        WriteShort(bos, BitsPerSample);
        WriteChar(bos, DataHdrID);
        WriteInt(bos, DataHdrLength);
        bos.flush();
        byte[] r = bos.toByteArray();
        bos.close();
        return r;
    }

    private void WriteShort(ByteArrayOutputStream bos, int s) throws IOException {
        byte[] myByte = new byte[2];
        myByte[1] = (byte) ((s << 16) >> 24);
        myByte[0] = (byte) ((s << 24) >> 24);
        bos.write(myByte);
    }


    private void WriteInt(ByteArrayOutputStream bos, int n) throws IOException {
        byte[] buf = new byte[4];
        buf[3] = (byte) (n >> 24);
        buf[2] = (byte) ((n << 8) >> 24);
        buf[1] = (byte) ((n << 16) >> 24);
        buf[0] = (byte) ((n << 24) >> 24);
        bos.write(buf);
    }

    private void WriteChar(ByteArrayOutputStream bos, char[] id) {
        for (int i = 0; i < id.length; i++) {
            char c = id[i];
            bos.write(c);
        }
    }
}
