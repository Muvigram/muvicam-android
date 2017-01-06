package com.estsoft.muvicam.transcoder.audiosurface;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import com.estsoft.muvicam.transcoder.audioresampler.Resampler;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by estsoft on 2016-12-08.
 */

public class AudioChannel {
    private static final String TAG = "AudioChannel";
    private static final boolean VERBOSE = false;

    private static class AudioSample {
        int bufferIndex;
        long presentationTimeUs;
        ShortBuffer data;
    }

    public static final int DECODER_END_OF_STREAM = -123;
    private static final int BYTES_PER_SHORT = 2;
    private static final long MICROSECS_PER_SEC = 1000000;

    private final Queue<AudioSample> mFilledSamples;
    private final Queue<AudioSample> mBufferedSamples;
    private AudioSample mOverFlowSample = new AudioSample();

    private final MediaFormat mEncodeFormat;
    private MediaFormat mActualDecodeFormat;

    private AudioRemixer mRemixer;
    private float mVolume;

    private int mInputSampleRate;
    private int mInputChannelCount;
    private int mOutputChannelCount;

    private Resampler resampler;


    public AudioChannel(MediaFormat mEncodeFormat, float volume) {
        this.mEncodeFormat = mEncodeFormat;
        this.mVolume = volume;
//        this.resampler = new Resampler(false, 44100, 44100);
        mFilledSamples = new ArrayDeque<>();
        mBufferedSamples = new ArrayDeque<>();
    }

    public void setActualDecodeFormat( final MediaFormat decodeFormat ) {
        mActualDecodeFormat = decodeFormat;
        mInputSampleRate = mActualDecodeFormat.getInteger( MediaFormat.KEY_SAMPLE_RATE );
        if (mInputSampleRate != mEncodeFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)) {
            throw new UnsupportedOperationException("Audio sample rate conversion not supported yet." + " ||| source : " + mInputSampleRate + " / target : " + mEncodeFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE));
        }
        mInputChannelCount = mActualDecodeFormat.getInteger( MediaFormat.KEY_CHANNEL_COUNT );
        mOutputChannelCount = mEncodeFormat.getInteger( MediaFormat.KEY_CHANNEL_COUNT );

        if (mInputChannelCount != 1 && mInputChannelCount != 2) {
            throw new UnsupportedOperationException("Input channel count (" + mInputChannelCount + ") not supported.");
        }

        if (mOutputChannelCount != 1 && mOutputChannelCount != 2) {
            throw new UnsupportedOperationException("Output channel count (" + mOutputChannelCount + ") not supported.");
        }

        if ( mInputChannelCount > mOutputChannelCount ) {
            if (VERBOSE) Log.d(TAG, "setActualDecodeFormat: DOWNMIX");
            mRemixer = AudioRemixer.DOWNMIX;
        } else if ( mInputChannelCount < mOutputChannelCount ) {
            if (VERBOSE) Log.d(TAG, "setActualDecodeFormat: UPMIX");
            mRemixer = AudioRemixer.UPMIX;
        } else {
            if (VERBOSE) Log.d(TAG, "setActualDecodeFormat: PASSTHROUGH");
            mRemixer = AudioRemixer.PASSTHROUGH;
        }
    }

    public void drainDecoderBufferAndQueue(final MediaCodec decoder, final int bufferIndex, final long presentationTimeUs ) {
        if (mActualDecodeFormat == null) throw new RuntimeException("Buffer received before format!");

        final ByteBuffer data = bufferIndex == DECODER_END_OF_STREAM ?
                null : decoder.getOutputBuffer( bufferIndex );
        AudioSample sample = new AudioSample();
        sample.bufferIndex = bufferIndex;
        sample.presentationTimeUs = presentationTimeUs;
        if (VERBOSE) Log.d(TAG, "drainDecoderBufferAndQueue: index ... " + bufferIndex  + " / time ... " + presentationTimeUs + " ???? " + DECODER_END_OF_STREAM);
        sample.data = data == null ? null : data.asShortBuffer();

        if (mOverFlowSample.data == null){
            mOverFlowSample.data = ByteBuffer
                    .allocateDirect(data.capacity())
                    .order(ByteOrder.nativeOrder())
                    .asShortBuffer();
            mOverFlowSample.data.clear().flip();
            Log.d(TAG, "remix: start : " + mOverFlowSample.data.position() + " / " + mOverFlowSample.data.limit());
        }

        mFilledSamples.add( sample );
        if (VERBOSE) Log.d(TAG, "drainDecoderBufferAndQueue: inqueue ... " + mFilledSamples.size());
    }

    public boolean throwDecoder( final MediaCodec decoder ) {
        if (mFilledSamples.isEmpty()) return false;
        final AudioSample inSample = mFilledSamples.poll();
        decoder.releaseOutputBuffer( inSample.bufferIndex, false );
        return true;
    }

    public boolean feedEncoder(final MediaCodec decoder, final MediaCodec encoder, long timeoutUs ) {
        final boolean hasOverFlow = mOverFlowSample.data != null && mOverFlowSample.data.hasRemaining();

        if (mFilledSamples.isEmpty() && !hasOverFlow) return false;

        final int index = encoder.dequeueInputBuffer(timeoutUs);
        if (VERBOSE) Log.d(TAG, "feedEncoder: encoder input index ... " + index);
        if (index < 0) return false;


        final ShortBuffer outBuffer = encoder.getInputBuffer( index ).asShortBuffer();
        if (hasOverFlow) {
            final long presentationTimeUs = drainOverFlow( outBuffer );
            encoder.queueInputBuffer( index, 0, outBuffer.position() * BYTES_PER_SHORT, presentationTimeUs, 0 );
            return true;
        }

        final AudioSample inSample = mFilledSamples.poll();
        if (VERBOSE) Log.d(TAG, "drainDecoderBufferAndQueue: dequeue ... " + mFilledSamples.size());
        if (inSample.bufferIndex == DECODER_END_OF_STREAM) {
            encoder.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            return false;
        }

//        final FloatBuffer floatBuffer = encoder.getInputBuffer(index).asFloatBuffer();
//        final FloatBuffer tmpBuffer = ByteBuffer.allocate( floatBuffer.capacity() ).asFloatBuffer();
//        resampler.process(44100, floatBuffer, false, tmpBuffer);
//        ByteBuffer tmpReceiver = ByteBuffer.allocate( floatBuffer.capacity() * 4 );
//        tmpReceiver.asFloatBuffer().put( tmpBuffer );
//        byte[] array = tmpReceiver.array();
//        final ShortBuffer outBuffer = ByteBuffer.wrap(array).asShortBuffer();
//        final ShortBuffer outBuffer = encoder.getInputBuffer(index).asShortBuffer();

        final long presentationTimeUs = remix(inSample, outBuffer);
        encoder.queueInputBuffer(index, 0, outBuffer.position() * BYTES_PER_SHORT, presentationTimeUs, 0);

        decoder.releaseOutputBuffer(inSample.bufferIndex, false);
        return true;

    }

    float timePerSample = -1;
    private long sampleCountToDurationUs(final int sampleCount,
                                                final int sampleRate,
                                                final int channelCount,
                                                final boolean fromSample) {
        if ( timePerSample < 0 ) {
            timePerSample = ((float)MICROSECS_PER_SEC / sampleRate);
            if (VERBOSE) Log.d(TAG, "sampleCountToDurationUs: " + timePerSample);
        }

        return (long)(sampleCount * timePerSample );
//        return (sampleCount / (MICROSECS_PER_SEC / sampleRate ) / channelCount );
    }

    private long drainOverFlow( final ShortBuffer outBuffer ) {
        final ShortBuffer overFlowBuffer = mOverFlowSample.data;
        final int overFlowLimit = overFlowBuffer.limit();
        final int overFlowSize = overFlowBuffer.remaining();

//        final long beginPresentationTimeUs = mOverFlowSample.presentationTimeUs +
//                sampleCountToDurationUs( overFlowBuffer.remaining(), mInputSampleRate, mOutputChannelCount, false );
        final long beginPresentationTimeUs = mOverFlowSample.presentationTimeUs;

        outBuffer.clear();
        if ( overFlowSize > outBuffer.capacity() ) overFlowBuffer.limit( outBuffer.capacity() );
        // TODO remix
        outBuffer.put( overFlowBuffer );
        if (overFlowSize >= outBuffer.capacity()) overFlowBuffer.clear().limit(0);
        else overFlowBuffer.limit( overFlowLimit );

        return beginPresentationTimeUs;

    }


    private long remix ( final AudioSample inSample, final ShortBuffer outBuffer) {
        final ShortBuffer inBuffer = inSample.data;
        final ShortBuffer overFlow = mOverFlowSample.data;
        inBuffer.clear();
        outBuffer.clear();
        // NOTE if outBuffer is overflowing
        if ( inBuffer.remaining() > outBuffer.remaining() ) {
            inBuffer.limit( outBuffer.capacity() );
            mRemixer.remix( inBuffer, outBuffer, mVolume );
            inBuffer.limit( inBuffer.capacity() );

            final long consumedDurationUs =
                    sampleCountToDurationUs( inBuffer.position() , mInputSampleRate, mInputChannelCount, true );

            overFlow.position( 0 );
            overFlow.limit( inBuffer.remaining() );
            mRemixer.remix( inBuffer, overFlow , mVolume );
            overFlow.flip();
            mOverFlowSample.presentationTimeUs = inSample.presentationTimeUs + consumedDurationUs;
        } else {
            mRemixer.remix(inBuffer, outBuffer, mVolume);
        }

        return inSample.presentationTimeUs;
    }

}
