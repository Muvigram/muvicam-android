package com.estsoft.muvicam.transcoder.noneencode;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by estsoft on 2016-12-07.
 */

public class AudioTrackTranscoder implements TrackTranscoder {
    private static final String TAG = "AudioTrackTranscoder";
    private static final boolean VERBOSE = false;
    private static final int DRAIN_STATE_NONE = 0;
    private static final int DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY = 1;
    private static final int DRAIN_STATE_CONSUMED = 2;

    private AudioChannel mAudioChannel;

    private final MediaExtractor mExtractor;
    private final MediaFormat mOutputFormat;
    private final BufferListener mBufferListener;
    private final int mTrackIndex;
    private MediaFormat mActualOutputFormat;
    private MediaCodec mEncoder;
    private MediaCodec mDecoder;
    private MediaCodec.BufferInfo mBufferInfo;
    private ByteBuffer[] mEncoderOutputBuffers;
    private ByteBuffer[] mDecoderInputBuffers;
    private long mWrittenPresentationTimeUs;
    private long mExtractedPresentationTimeUs;
    private long mEncodeStartPresentationTimeUs;
    private float mVolume;
    private boolean isEncoderStarted;
    private boolean isDecoderStarted;
    private boolean sawEncoderEOS;
    private boolean sawDecoderEOS;
    private boolean sawExtractorEOS;
    private boolean forceExtractingStop;
    private boolean mEncodePermitted;

    public AudioTrackTranscoder(MediaExtractor extractor, MediaFormat outFormat, BufferListener bufferListener, int trackIndex, int volume ) {
        this.mExtractor = extractor;
        this.mOutputFormat = outFormat;
        this.mBufferListener = bufferListener;
        this.mTrackIndex = trackIndex;
        this.mVolume = Math.abs(volume) >= 100 ? 1.0f : Math.abs(volume / 100f);
        this.mBufferInfo = new MediaCodec.BufferInfo();
    }

    @Override
    public void setup() {
        mExtractor.selectTrack( mTrackIndex );
        try {
            mEncoder = MediaCodec.createEncoderByType( mOutputFormat.getString(MediaFormat.KEY_MIME) );
        } catch ( IOException e ){
            throw new IllegalStateException( e );
        }
        mEncoder.configure( mOutputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE );
        mEncoder.start();
        isEncoderStarted = true;
        mEncoderOutputBuffers = mEncoder.getOutputBuffers();

        MediaFormat inputFormat = mExtractor.getTrackFormat( mTrackIndex );
        try {
            mDecoder = MediaCodec.createDecoderByType( inputFormat.getString( MediaFormat.KEY_MIME ) );
        } catch ( IOException e ) {
            throw new IllegalStateException( e );
        }
        mDecoder.configure( inputFormat, null, null, 0 );
        mDecoder.start();
        isDecoderStarted = true;
        mDecoderInputBuffers = mDecoder.getInputBuffers();

        mAudioChannel = new AudioChannel( mOutputFormat, mVolume );

    }

    @Override
    public MediaFormat getDeterminedFormat() {
        return mActualOutputFormat;
    }

    @Override
    public boolean stepPipeline() {
        boolean busy = false;
        int status;
        while( drainEncoder(0) != DRAIN_STATE_NONE ) busy = true;
        do {
            status = drainDecoder(0);
            if (status != DRAIN_STATE_NONE) busy = true;
            // NOTE: not repeating to keep from deadlock when encoder is full.
        } while (status == DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY);

        while ( mAudioChannel.feedEncoder(mDecoder, mEncoder, 0) ) busy = true;
        while ( drainExtractor(0) != DRAIN_STATE_NONE ) busy = true;

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

    private int drainExtractor( long timeoutUs ){
        if (sawExtractorEOS && mExtractor.getSampleTrackIndex() == mTrackIndex ) mExtractor.advance();
        if (sawExtractorEOS) return DRAIN_STATE_NONE;
        int track = mExtractor.getSampleTrackIndex();
        if ( track >= 0 && track != mTrackIndex ) return DRAIN_STATE_NONE;

        final int index = mDecoder.dequeueInputBuffer( timeoutUs );
        if ( index < 0 ) return DRAIN_STATE_NONE;
        if ( track < 0 || forceExtractingStop) {
            Log.d(TAG, "permitEncode: forceStop! AUDIO " + mExtractedPresentationTimeUs + " / " + mExtractor.getSampleTrackIndex());
            if (VERBOSE) Log.d(TAG, "drainExtractor: END OF EXTRACTING");
            sawExtractorEOS = true;
            mDecoder.queueInputBuffer( index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM );
            return DRAIN_STATE_NONE;
        }

        final int sampleSize = mExtractor.readSampleData( mDecoderInputBuffers[index], 0 );
        final boolean isKeyFrame = (mExtractor.getSampleFlags() & MediaExtractor.SAMPLE_FLAG_SYNC) != 0;
        mDecoder.queueInputBuffer( index, 0, sampleSize, mExtractor.getSampleTime(), isKeyFrame ? MediaCodec.BUFFER_FLAG_SYNC_FRAME : 0);
        mExtractedPresentationTimeUs = mExtractor.getSampleTime();
        mExtractor.advance();
        return DRAIN_STATE_CONSUMED;
    }

    private int drainDecoder( long timeoutUs ) {
        if (sawDecoderEOS) return DRAIN_STATE_NONE;
        int index = mDecoder.dequeueOutputBuffer( mBufferInfo, timeoutUs );
        switch ( index ) {
            case MediaCodec.INFO_TRY_AGAIN_LATER :
                return DRAIN_STATE_NONE;
            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED :
                mAudioChannel.setActualDecodeFormat(mDecoder.getOutputFormat());
            case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED :
                return DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY;
        }
        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            sawDecoderEOS = true;
            mAudioChannel.drainDecoderBufferAndQueue( mDecoder, AudioChannel.DECODER_END_OF_STREAM , 0);
        } else {
            if ( mEncodePermitted && mBufferInfo.presentationTimeUs > mEncodeStartPresentationTimeUs ) {
                mAudioChannel.drainDecoderBufferAndQueue(mDecoder, index, mBufferInfo.presentationTimeUs);
            } else {
                mDecoder.releaseOutputBuffer( index, false );
            }
        }

        return DRAIN_STATE_CONSUMED;
    }

    private int drainEncoder( long timeoutUs ) {
        if (sawEncoderEOS) return DRAIN_STATE_NONE;
        int index = mEncoder.dequeueOutputBuffer( mBufferInfo, timeoutUs );
        switch ( index ) {
            case MediaCodec.INFO_TRY_AGAIN_LATER :
                return DRAIN_STATE_NONE;
            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED :
                if (mActualOutputFormat != null) throw new RuntimeException("Video output format changed twice.");
                mActualOutputFormat = mEncoder.getOutputFormat();
                // TODO - resampling and confirm
                mBufferListener.onOutputFormat( BufferListener.BufferType.AUDIO, mActualOutputFormat );
//                TranscodeUtils.printInformationOf( mActualOutputFormat );
                return DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY;
            case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED :
                mEncoderOutputBuffers = mEncoder.getOutputBuffers();
                return DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY;
        }
        if (mActualOutputFormat == null) throw new RuntimeException("Could not determine actual output format.");
        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            if (VERBOSE)  Log.d(TAG, "drainEncoder: END OF TASK ... saw Encoder EOS");
            sawEncoderEOS = true;
            Log.d(TAG, "drainEncoder: END OF TASK ... saw Encoder EOS");
            mBufferInfo.set( 0, 0, 0,mBufferInfo.flags );
        }
        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            // SPS or PPS, which should be passed by MediaFormat.
            mEncoder.releaseOutputBuffer(index, false);
            return DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY;
        }
        mWrittenPresentationTimeUs = mBufferInfo.presentationTimeUs > 0 ? mBufferInfo.presentationTimeUs : mWrittenPresentationTimeUs;
        mBufferListener.onBufferAvailable( BufferListener.BufferType.AUDIO, mEncoderOutputBuffers[index], mBufferInfo );
        mEncoder.releaseOutputBuffer( index, false );
        if (VERBOSE) Log.d(TAG, "drainEncoder:  _____________________________________________________ " + "AUDIO DRAIN_STATE_CONSUMED");
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
        Log.d(TAG, "permitEncode: AUDIO Start at " + mEncodeStartPresentationTimeUs);
        this.mEncodePermitted = true;
    }
}
