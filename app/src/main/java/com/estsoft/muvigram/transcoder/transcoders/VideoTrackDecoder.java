package com.estsoft.muvigram.transcoder.transcoders;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.opengl.GLES20;

import com.estsoft.muvigram.transcoder.eglsurface.OutputSurface;

import java.io.IOException;
import java.nio.ByteBuffer;

import timber.log.Timber;

/**
 * Created by estsoft on 2016-12-09.
 */

public class VideoTrackDecoder implements TrackTranscoder {
    private static final int DRAIN_STATE_NONE = 0;
    private static final int DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY = 1;
    private static final int DRAIN_STATE_CONSUMED = 2;

    private boolean I_FRAME_EXTRACTING = true;

    private final MediaExtractor mExtractor;
    private final int mTrackIndex;
    private MediaCodec mDecoder;
    private MediaCodec.BufferInfo mBufferInfo;
    private MediaFormat mOutputFormat;
    private OutputSurface mDecoderOutputSurfaceWrapper;
    private ByteBuffer[] mDecoderInputBuffers;
    private long mTotalDuration;
    private long mCurrentPositionUs;
    private long mFrameIntervalUs;
    private long mExtractedPresentationTimeUs;
    private boolean isDecoderStarted;
    private boolean sawDecoderEOS;
    private boolean sawExtractorEOS;
    private boolean forceStop;

    private BitmapListener mBitmapListener;

    private int mWidth;
    private int mHeight;

    private Matrix flipMatrix = new Matrix();

    public VideoTrackDecoder(MediaExtractor extractor, int trackIndex, MediaFormat outputFormat, long frameIntervalUs, BitmapListener listener, boolean extractingMode) {
        this.mExtractor = extractor;
        this.mTrackIndex = trackIndex;
        this.mTotalDuration = mExtractor.getTrackFormat( mTrackIndex ).getLong( MediaFormat.KEY_DURATION );
        Timber.v("VideoTrackDecoder: %ld", mTotalDuration);
        this.mOutputFormat = outputFormat;
        this.mFrameIntervalUs = frameIntervalUs;
        this.mBitmapListener = listener;
        this.mBufferInfo = new MediaCodec.BufferInfo();
        this.I_FRAME_EXTRACTING = extractingMode;
    }

    @Override
    public void setup( ) {
        mExtractor.selectTrack( mTrackIndex );
        MediaFormat inputFormat = mExtractor.getTrackFormat( mTrackIndex );
        mWidth = mOutputFormat.getInteger(MediaFormat.KEY_WIDTH);
        mHeight = mOutputFormat.getInteger(MediaFormat.KEY_HEIGHT);
        mDecoderOutputSurfaceWrapper = new OutputSurface( mWidth, mHeight );
        try {
            mDecoder = MediaCodec.createDecoderByType( inputFormat.getString( MediaFormat.KEY_MIME ) );
        } catch (IOException e) {
            throw new IllegalStateException( e );
        }
        mDecoder.configure( inputFormat, mDecoderOutputSurfaceWrapper.getSurface(), null, 0 );
        mDecoder.start();
        isDecoderStarted = false;
        mDecoderInputBuffers = mDecoder.getInputBuffers();

        mCurrentPositionUs = 0;

        flipMatrix.preScale(1, -1);

    }

    @Override
    public MediaFormat getDeterminedFormat() {
        return mOutputFormat;
    }

    @Override
    public boolean stepPipeline() {
        boolean busy = false;
        int status;
        do {
            status = drainDecoder(0);
            if (status != DRAIN_STATE_NONE) busy = true;
        } while (status == DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY);
        while (drainExtractor(0) != DRAIN_STATE_NONE) busy = true;

        return busy;
    }

    @Override
    public long getExtractedPresentationTimeUs() {
        return mExtractedPresentationTimeUs;
    }

    @Override
    public long getWrittenPresentationTimeUs() {
        return 0;
    }

    @Override
    public boolean isFinished() {
        return sawDecoderEOS;
    }

    @Override
    public void release() {

        if (mDecoderOutputSurfaceWrapper != null) {
            mDecoderOutputSurfaceWrapper.release();
            mDecoderOutputSurfaceWrapper = null;
        }
        if (mDecoder != null) {
            if (isDecoderStarted) mDecoder.stop();
            mDecoder.release();
            mDecoder = null;
        }
    }
    int lastOneFlag = 0;
    int extractedCount = 0;
    private int drainExtractor( long timeoutUs ) {
        if (sawExtractorEOS) return DRAIN_STATE_NONE;
        int track = mExtractor.getSampleTrackIndex();
        if (track >= 0 && track != mTrackIndex) return DRAIN_STATE_NONE;

        int index = mDecoder.dequeueInputBuffer( timeoutUs );
        if ( index < 0 ) return DRAIN_STATE_NONE;
        // need to confirm
        if ( mExtractor.getSampleTime() < 0 || lastOneFlag >= 1 || forceStop ) {
            sawExtractorEOS = true;
            Timber.v("drainExtractor: Extractor ended %d", extractedCount);
            mDecoder.queueInputBuffer( index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM );
            return DRAIN_STATE_NONE;
        }
        int sampleSize = mExtractor.readSampleData( mDecoderInputBuffers[index], 0 );
        boolean isKeyFrame = (mExtractor.getSampleFlags() & MediaExtractor.SAMPLE_FLAG_SYNC) != 0;
        mDecoder.queueInputBuffer(index, 0, sampleSize, mExtractor.getSampleTime(), isKeyFrame ? MediaCodec.BUFFER_FLAG_SYNC_FRAME : 0);
        extractedCount ++;
        mCurrentPositionUs += mFrameIntervalUs;
        if (mCurrentPositionUs >= mTotalDuration) {
            mCurrentPositionUs = mTotalDuration;
            Timber.d("drainExtractor: %ld/%d", mCurrentPositionUs, mTotalDuration);
            lastOneFlag ++;
        }
        if ( I_FRAME_EXTRACTING ) {
            mExtractor.seekTo( mCurrentPositionUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        } else {
            // None I-Frame image is distorting
            mExtractor.seekTo( mCurrentPositionUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
            while (mExtractor.getSampleTime() < mCurrentPositionUs
//                    && mExtractor.getSampleTrackIndex() >= 0
                    && mExtractor.getSampleTime() < mTotalDuration - 100000) {
                mExtractor.advance();
                Timber.v("drainExtractor: %ld/%ld", mExtractor.getSampleTime(), mTotalDuration);
            }
            Timber.v("drainExtractor:-------------------------------------------------------------- %ld", mExtractor.getSampleTime());
        }
        return DRAIN_STATE_CONSUMED;
    }

    int decodedCount = 0;
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
            sawDecoderEOS = true;
            mBufferInfo.size = 0;
        }
        Bitmap flipBitmap;
        boolean doRender = mBufferInfo.size > 0;
        mDecoder.releaseOutputBuffer( index, doRender );
        if ( doRender) {
            decodedCount ++;
            mDecoderOutputSurfaceWrapper.awaitNewImage();
            mDecoderOutputSurfaceWrapper.drawImage( false );
            Timber.v("drainDecoder: drained Position ... %ld/ e:%d / d:%d",
                mBufferInfo.presentationTimeUs, extractedCount, decodedCount);
            ByteBuffer buffer = ByteBuffer.allocateDirect( 4 * mWidth * mHeight );
            buffer.rewind();
            GLES20.glReadPixels( 0, 0, mWidth, mHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);

            Bitmap bmp = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            buffer.position( 0 );
            bmp.copyPixelsFromBuffer(buffer);
            flipBitmap = Bitmap.createBitmap(bmp, 0, 0, mWidth, mHeight, flipMatrix, false);

            mBitmapListener.onBitmapSupply( flipBitmap, mBufferInfo.presentationTimeUs , false );
        }
        if ( sawDecoderEOS ) {
            sawDecoderEOS = true;
            mBitmapListener.onBitmapSupply( null, mTotalDuration, true);
            mBitmapListener.onComplete( mTotalDuration );
        }
        return DRAIN_STATE_CONSUMED;
    }


    public interface BitmapListener {
        void onBitmapSupply(Bitmap bitmap, long presentationTime, boolean isEnd);
        void onComplete( long totalUs );
    }

    @Override
    public void forceStop() {
        this.forceStop = true;
    }
}
