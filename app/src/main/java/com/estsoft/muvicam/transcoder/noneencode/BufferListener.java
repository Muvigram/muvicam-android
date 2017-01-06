package com.estsoft.muvicam.transcoder.noneencode;

import android.media.MediaCodec;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

/**
 * Created by estsoft on 2017-01-03.
 */

public interface BufferListener {

    void onBufferAvailable(BufferType type, ByteBuffer buffer, MediaCodec.BufferInfo bufferInfo);
    void onOutputFormat(BufferType type, MediaFormat format);

    enum BufferType {
        VIDEO,
        AUDIO,
    }
}
