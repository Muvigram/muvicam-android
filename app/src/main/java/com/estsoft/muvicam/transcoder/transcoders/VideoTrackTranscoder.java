package com.estsoft.muvicam.transcoder.transcoders;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;


import com.estsoft.muvicam.transcoder.eglsurface.InputSurface;
import com.estsoft.muvicam.transcoder.eglsurface.OutputSurface;
import com.estsoft.muvicam.transcoder.utils.MediaFormatExtraInfo;
import com.estsoft.muvicam.transcoder.utils.TranscodeUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by estsoft on 2016-12-07.
 */

public class VideoTrackTranscoder implements TrackTranscoder {
    private static final String TAG = "VideoTrackTranscoder";
    private static final boolean VERBOSE = false;
    private static final int DRAIN_STATE_NONE = 0;
    private static final int DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY = 1;
    private static final int DRAIN_STATE_CONSUMED = 2;

    private final MediaExtractor mExtractor;
    private final MediaFormat mOutputFormat;
    private final BufferListener mBufferListener;
    private final int mTrackIndex;
    private MediaFormat mActualOutputFormat;
    private MediaCodec mEncoder;
    private MediaCodec mDecoder;
    private MediaCodec.BufferInfo mBufferInfo;
    private InputSurface mEncoderInputSurfaceWrapper;
    private OutputSurface mDecoderOutputSurfaceWrapper;
    private ByteBuffer[] mEncoderOutputBuffers;
    private ByteBuffer[] mDecoderInputBuffers;
    private long mWrittenPresentationTimeUs;
    private long mExtractedPresentationTimeUs;
    private long mEncodeStartPresentationTimeUs;
    private boolean isEncoderStarted;
    private boolean isDecoderStarted;
    private boolean sawEncoderEOS;
    private boolean sawDecoderEOS;
    private boolean sawExtractorEOS;
    private boolean forceExtractingStop;
    private boolean mEncodePermitted;

    public VideoTrackTranscoder(MediaExtractor extractor, MediaFormat outFormat, BufferListener bufferListener, int trackIndex ) {
        this.mExtractor = extractor;
        this.mOutputFormat = outFormat;
        this.mBufferListener = bufferListener;
        this.mTrackIndex = trackIndex;
        this.mBufferInfo = new MediaCodec.BufferInfo();
    }

    @Override
    public void setup() {
        mExtractor.selectTrack( mTrackIndex );
        try {
            mEncoder = MediaCodec.createEncoderByType( mOutputFormat.getString( MediaFormat.KEY_MIME ) );
        } catch ( IOException e ) {
            throw new IllegalStateException( e );
        }
        Log.d(TAG, "setup: " + mOutputFormat);
        mEncoder.configure(mOutputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE );
        // making EGLContext using encoder's input surface
        mEncoderInputSurfaceWrapper = new InputSurface( mEncoder.createInputSurface() );
        // attach an EGL rendering context to EGL surfaces
        mEncoderInputSurfaceWrapper.makeCurrent();
        mEncoder.start();
        isEncoderStarted = true;
        mEncoderOutputBuffers = mEncoder.getOutputBuffers();

        MediaFormat inputFormat = mExtractor.getTrackFormat( mTrackIndex );
        if (inputFormat.containsKey( MediaFormatExtraInfo.KEY_ROTATION_DEGREES )) {
            // Decoded video is rotated automatically in Android 5.0 lollipop.
            // Turn off here because we don't want to encode rotated one.
            // refer: https://android.googlesource.com/platform/frameworks/av/+blame/lollipop-release/media/libstagefright/Utils.cpp
            inputFormat.setInteger( MediaFormatExtraInfo.KEY_ROTATION_DEGREES, 0 );
        }
        // OutputSurface uses the EGL context created by InputSurface
        mDecoderOutputSurfaceWrapper = new OutputSurface();
        try {
            mDecoder = MediaCodec.createDecoderByType( inputFormat.getString( MediaFormat.KEY_MIME ) );
        } catch ( IOException e ) {
            throw new IllegalStateException( e );
        }
        mDecoder.configure( inputFormat, mDecoderOutputSurfaceWrapper.getSurface(), null, 0 );
        mDecoder.start();
        isDecoderStarted = true;
        mDecoderInputBuffers = mDecoder.getInputBuffers();
    }

    @Override
    public MediaFormat getDeterminedFormat() {  return mActualOutputFormat; }

    @Override
    public boolean stepPipeline() {
        boolean busy = false;

        int status;
        while (drainEncoder(0) != DRAIN_STATE_NONE) busy = true;
        do {
            status = drainDecoder(0);
            if (status != DRAIN_STATE_NONE) busy = true;
            // NOTE: not repeating to keep from deadlock when encoder is full.
        } while (status == DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY);
        while (drainExtractor(0) != DRAIN_STATE_NONE) busy = true;

        return busy;
    }

    @Override
    public long getWrittenPresentationTimeUs() {
        return mWrittenPresentationTimeUs;
    }

    @Override
    public boolean isFinished() {
        return sawEncoderEOS;
    }
    public boolean isExtractingFinished() { return sawExtractorEOS; }

    @Override
    public void release() {
        if (mDecoderOutputSurfaceWrapper != null) {
            mDecoderOutputSurfaceWrapper.release();
            mDecoderOutputSurfaceWrapper = null;
        }
        if (mEncoderInputSurfaceWrapper != null) {
            mEncoderInputSurfaceWrapper.release();
            mEncoderInputSurfaceWrapper = null;
        }
        if (mDecoder != null) {
            if (isDecoderStarted) mDecoder.stop();
            mDecoder.release();
            mDecoder = null;
        }
        if (mEncoder != null) {
            if (isEncoderStarted) mEncoder.stop();
            mEncoder.release();
            mEncoder = null;
        }
    }

    private int drainExtractor( long timeoutUs ) {
        if (sawExtractorEOS && mExtractor.getSampleTrackIndex() == mTrackIndex ) mExtractor.advance();
        if ( sawExtractorEOS ) return DRAIN_STATE_NONE;
        int track = mExtractor.getSampleTrackIndex();
        if ( track >= 0 && track != mTrackIndex ) return DRAIN_STATE_NONE;
        int index = mDecoder.dequeueInputBuffer( timeoutUs );
        if ( index < 0 ) return DRAIN_STATE_NONE;
        if ( track < 0 || forceExtractingStop) {
            Log.d(TAG, "permitEncode: forceStop! VIDEO " + mExtractedPresentationTimeUs + " / " + mExtractor.getSampleTrackIndex());
            if (VERBOSE) Log.d(TAG, "permitEncode: END OF EXTRACTING " + mExtractedPresentationTimeUs);
            sawExtractorEOS = true;
            mDecoder.queueInputBuffer( index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM );
            return DRAIN_STATE_NONE;
        }
        int sampleSize = mExtractor.readSampleData( mDecoderInputBuffers[index], 0 );
        boolean isKeyFrame = (mExtractor.getSampleFlags() & MediaExtractor.SAMPLE_FLAG_SYNC) != 0;
        mDecoder.queueInputBuffer(index, 0, sampleSize, mExtractor.getSampleTime(), isKeyFrame ? MediaCodec.BUFFER_FLAG_SYNC_FRAME : 0);
        mExtractedPresentationTimeUs = mExtractor.getSampleTime();
        mExtractor.advance();
        return DRAIN_STATE_CONSUMED;
    }

    private int drainDecoder( long timeoutUs ) {
        if ( sawDecoderEOS ) return DRAIN_STATE_NONE;
        int index = mDecoder.dequeueOutputBuffer( mBufferInfo, timeoutUs );
        switch ( index ) {
            case MediaCodec.INFO_TRY_AGAIN_LATER :
                return DRAIN_STATE_NONE;
            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED :
            case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED :
                return DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY;
        }
        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            // Since we are using surface, call this method to stop submitting data to the codec
            mEncoder.signalEndOfInputStream();
            sawDecoderEOS = true;
            mBufferInfo.size = 0;
        }
        boolean doRender = mBufferInfo.size > 0;
        // NOTE: doRender will block if buffer (of encoder) is full.
        // Refer: http://bigflake.com/mediacodec/CameraToMpegTest.java.txt
        mDecoder.releaseOutputBuffer( index, doRender );
        if ( doRender ) {
            mDecoderOutputSurfaceWrapper.awaitNewImage();
            mDecoderOutputSurfaceWrapper.drawImage( false );
//            if ( mEncodePermitted ) {
            if ( mEncodePermitted && mBufferInfo.presentationTimeUs >= mEncodeStartPresentationTimeUs ) {
                mEncoderInputSurfaceWrapper.setPresentationTime(mBufferInfo.presentationTimeUs * 1000);
                mEncoderInputSurfaceWrapper.swapBuffers();
            }
        }
        return DRAIN_STATE_CONSUMED;
    }

    private int drainEncoder( long timeoutUs ) {
        if ( sawEncoderEOS ) return DRAIN_STATE_NONE;
        int index = mEncoder.dequeueOutputBuffer( mBufferInfo, timeoutUs );
        switch ( index ) {
            case MediaCodec.INFO_TRY_AGAIN_LATER:
                return DRAIN_STATE_NONE;
            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                if (mActualOutputFormat != null)
                    throw new RuntimeException("Video output format changed twice.");
                mActualOutputFormat = mEncoder.getOutputFormat();
                // TODO
                mBufferListener.onOutputFormat( BufferListener.BufferType.VIDEO, mActualOutputFormat );
//                mMuxerWrapper.setOutputFormat(MuxerWrapper.SampleType.VIDEO, mActualOutputFormat);
                TranscodeUtils.printInformationOf( mActualOutputFormat );
                return DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY;
            case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                mEncoderOutputBuffers = mEncoder.getOutputBuffers();
                return DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY;
        }
        if (mActualOutputFormat == null) throw new RuntimeException( "Could not determine actual output format." );
        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            if (VERBOSE)  Log.d(TAG, "drainEncoder: END OF TASK ... saw Encoder EOS");
            sawEncoderEOS = true;
            mBufferInfo.set( 0, 0, 0,mBufferInfo.flags );
        }
        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            // SPS or PPS, which should be passed by MediaFormat.
            mEncoder.releaseOutputBuffer(index, false);
            return DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY;
        }
        mWrittenPresentationTimeUs = mBufferInfo.presentationTimeUs > 0 ? mBufferInfo.presentationTimeUs : mWrittenPresentationTimeUs;
        mBufferListener.onBufferAvailable( BufferListener.BufferType.VIDEO, mEncoderOutputBuffers[index], mBufferInfo );
//        mMuxerWrapper.writeSampleData( MuxerWrapper.SampleType.VIDEO, mEncoderOutputBuffers[index], mBufferInfo );
        mEncoder.releaseOutputBuffer( index, false );
        // Since runPipeline wait for only DRAIN_STATE_NONE, timeoutUs must not be negative.
        if (VERBOSE) Log.d(TAG, "drainEncoder:  _____________________________________________________ " + "VIDEO DRAIN_STATE_CONSUMED");
        return DRAIN_STATE_CONSUMED;
    }



    @Override
    public void forceStop () {
        this.forceExtractingStop = true;
    }
    @Override
    public long getExtractedPresentationTimeUs() {
        return mExtractedPresentationTimeUs;
    }
    public void encodeStart() {
        this.mEncodeStartPresentationTimeUs = mExtractedPresentationTimeUs;
        Log.d(TAG, "permitEncode: VIDEO Start at " + mEncodeStartPresentationTimeUs);
        this.mEncodePermitted = true;
    }
}
