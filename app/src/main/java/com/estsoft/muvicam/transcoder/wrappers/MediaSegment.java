package com.estsoft.muvicam.transcoder.wrappers;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.util.Log;


import com.estsoft.muvicam.transcoder.transcoders.AudioTrackTranscoder;
import com.estsoft.muvicam.transcoder.transcoders.BufferListener;
import com.estsoft.muvicam.transcoder.transcoders.VideoTrackTranscoder;
import com.estsoft.muvicam.transcoder.utils.TranscodeUtils;

import java.io.IOException;

/**
 * Created by estsoft on 2016-12-08.
 */

public class MediaSegment {

    private static final String TAG = "MediaSegment";
    private static final boolean VERBOSE = false;
    public int CURRENT_MODE = MediaEditor.NORMAL;

    private final MediaExtractor mExtractor;
    private final long mStartTimeUs;
    private final long mEndTimeUs;

    private MediaTarget mTarget;
    private MuxerWrapper mMuxer;
    private BufferListener mBufferListener;
    private String mInputFilePath;
    private VideoTrackTranscoder mVideoTranscoder;
    private AudioTrackTranscoder mAudioTranscoder;
    private MediaFormat mVideoInputFormat;
    private MediaFormat mAudioInputFormat;
    private long mMediaDurationUs;
    private long mVideoStartedTimeUs = -1;
    private long mAudioStartedTimeUs = -1;
    private int mVideoTrackIndex;
    private int mAudioTrackIndex;
    private int mOrginRotaion;
    private int mAudioVolume;
    private boolean mTranscdoerStoppedSignal;
    private boolean pFrameSeeking;
    private boolean mVideoEncodingStarted;
    private boolean mAudioEncodeingStarted;
    private boolean encodingStarted;

    public MediaSegment(MediaTarget target, String mediaFilePath, MuxerWrapper muxerWrapper, BufferListener bufferListener,
                        long startTimeUs, long endTimeUs, int audioVolume ) {
        this.mTarget = target;
        mInputFilePath = mediaFilePath;
        this.mExtractor = new MediaExtractor();
        try {
            mExtractor.setDataSource(mInputFilePath);
        } catch ( IOException e ) {
            throw new IllegalStateException( e );
        }

        this.mMuxer = muxerWrapper;
        this.mBufferListener = bufferListener;

        this.mStartTimeUs = startTimeUs < 0 ? 0 : startTimeUs;
        this.mEndTimeUs = endTimeUs < 0 ?
                mExtractor.getTrackFormat( mExtractor.getTrackCount() - 1 ).getLong( MediaFormat.KEY_DURATION ) : endTimeUs;
        this.mAudioVolume = audioVolume;
    }

    public void prepare( int mode, boolean isPFrameSeek ) {
        CURRENT_MODE = mode;
        setupMetadata();
        if ( mode == MediaEditor.VIDEO_ONLY || mode == MediaEditor.NORMAL ) setupVideoTranscoder();
        if ( mode == MediaEditor.AUDIO_ONLY || mode == MediaEditor.NORMAL ) setupAuidoTranscoder();

        // DOING
        // NOTE Seek mechanism
        if ( CURRENT_MODE != MediaEditor.AUDIO_ONLY ) {
            if (isPFrameSeek) P_FrameTranscodeSeeking();
            else I_FrameTranscodeSeeking();
        } else {
            mExtractor.seekTo( mStartTimeUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC );
            while ( mExtractor.getSampleTime() < mStartTimeUs ) {
                mExtractor.advance();
            }
            permitEncode();
        }

    }

    public boolean checkFinished() {
        switch ( CURRENT_MODE ) {
            case MediaEditor.VIDEO_ONLY : return mVideoTranscoder.isFinished();
            case MediaEditor.AUDIO_ONLY : return mAudioTranscoder.isFinished();
            case MediaEditor.NORMAL : return mVideoTranscoder.isFinished() && mAudioTranscoder.isFinished();
            default :
                throw new IllegalStateException( "not video or audio mode selected! " );
//                return mVideoTranscoder.isFinished() && mAudioTranscoder.isFinished();
        }
    }

    private void permitEncode() {
        if ( CURRENT_MODE != MediaEditor.AUDIO_ONLY ) {
            mVideoTranscoder.encodeStart();
            mVideoStartedTimeUs = mVideoTranscoder.getExtractedPresentationTimeUs();
            Log.d(TAG, "permitEncode: Video encode starts at " + mVideoStartedTimeUs +  " / " + mExtractor.getSampleTrackIndex());
        }
        if ( CURRENT_MODE != MediaEditor.VIDEO_ONLY ) {
            mAudioTranscoder.encodeStart();
            mAudioStartedTimeUs = mAudioTranscoder.getExtractedPresentationTimeUs();
            Log.d(TAG, "permitEncode: Audio encode starts at " + mAudioStartedTimeUs +  " / " + mExtractor.getSampleTrackIndex());
        }
    }

    private void permitVideoEncode() {
        if ( CURRENT_MODE != MediaEditor.AUDIO_ONLY ) {
            mVideoTranscoder.encodeStart();
            mVideoStartedTimeUs = mVideoTranscoder.getExtractedPresentationTimeUs();
            Log.d(TAG, "permitEncode: Video encode starts at " + mVideoStartedTimeUs +  " / " + mExtractor.getSampleTrackIndex());
        }
    }

    private void permitAudioEncode() {
        if ( CURRENT_MODE != MediaEditor.VIDEO_ONLY ) {
            mAudioTranscoder.encodeStart();
            mAudioStartedTimeUs = mAudioTranscoder.getExtractedPresentationTimeUs();
            Log.d(TAG, "permitEncode: Audio encode starts at " + mAudioStartedTimeUs +  " / " + mExtractor.getSampleTrackIndex());
        }
    }

    public boolean stepOnce() {
        if ( pFrameSeeking && mExtractor.getSampleTime() > mStartTimeUs && !encodingStarted ) {
            Log.d(TAG, "stepOnce: START OF ENCODING IN THIS SEGMENT   " + mExtractor.getSampleTime());
            encodingStarted = true;
            permitEncode();
        }

//        if ( pFrameSeeking ) {
//
//            Log.d(TAG, "drainExtractor: " + mExtractor.getSampleTime() + " / " + mExtractor.getSampleTrackIndex());
//
//            if ( !mAudioEncodeingStarted &&
//                    mExtractor.getSampleTrackIndex() == mAudioTrackIndex && mExtractor.getSampleTime() > mStartTimeUs ) {
//                mAudioEncodeingStarted = true;
//                permitAudioEncode();
//            }
//            if ( !mVideoEncodingStarted &&
//                    mExtractor.getSampleTrackIndex() == mVideoTrackIndex && mExtractor.getSampleTime() > mStartTimeUs ) {
//                mVideoEncodingStarted = true;
//                permitVideoEncode();
//            }
//
//        }

        switch ( CURRENT_MODE ) {
            case MediaEditor.VIDEO_ONLY : return mVideoTranscoder.stepPipeline();
            case MediaEditor.AUDIO_ONLY : return mAudioTranscoder.stepPipeline();
            case MediaEditor.NORMAL : {
                boolean video = mVideoTranscoder.stepPipeline();
                boolean audio = mAudioTranscoder.stepPipeline();
                Log.d(TAG, "stepOnce: " + mExtractor.getSampleTime() + " / " + mExtractor.getSampleTrackIndex());
                return video || audio;
            }
            default :
                throw new IllegalStateException( "not video or audio mode selected! " );
//                return mVideoTranscoder.stepPipeline() || mAudioTranscoder.stepPipeline();
        }
    }
    public void forceStopVideo() {
        if ( CURRENT_MODE != MediaEditor.AUDIO_ONLY ) mVideoTranscoder.forceStop();
    }
    public void forceStopAudio() {
        if (CURRENT_MODE != MediaEditor.VIDEO_ONLY) mAudioTranscoder.forceStop();
    }
    public void forceStop() {
        switch ( CURRENT_MODE ) {
            case MediaEditor.VIDEO_ONLY : mVideoTranscoder.forceStop(); return;
            case MediaEditor.AUDIO_ONLY : mAudioTranscoder.forceStop(); return;
            case MediaEditor.NORMAL : mVideoTranscoder.forceStop(); mAudioTranscoder.forceStop(); return;
            default :
                throw new IllegalStateException( "not video or audio mode selected! " );
//                mVideoTranscoder.forceStop(); mAudioTranscoder.forceStop(); return;
        }
    }
    private long getVideoIFrameInterval() {
        long first, second;
        long current = 0;
        long step = 33333;
        while ( mExtractor.getSampleTrackIndex() != mVideoTrackIndex ) {
            mExtractor.seekTo( current, MediaExtractor.SEEK_TO_NEXT_SYNC );
            current += step;
        }
        first = mExtractor.getSampleTime();
        mExtractor.advance();
        while (mExtractor.getSampleTrackIndex() != mVideoTrackIndex ) {
            mExtractor.seekTo( current, MediaExtractor.SEEK_TO_NEXT_SYNC);
            current += step;
        }
        second = mExtractor.getSampleTime();
        mExtractor.seekTo( 0 , MediaExtractor.SEEK_TO_CLOSEST_SYNC );
        return second - first;
    }

    public void P_FrameTranscodeSeeking() {
        pFrameSeeking = true;
        // NOTE when try Sync to prefer time,
        // NOTE extractor just extract Video or Audio Sample. until it advanced about 1~3 seconds
        // NOTE might Extractor has seeking bugs.
        mExtractor.seekTo( mStartTimeUs - getVideoIFrameInterval(), MediaExtractor.SEEK_TO_PREVIOUS_SYNC );
        Log.d(TAG, "P_FrameTranscodeSeeking: " + mExtractor.getSampleTime());
    }

    public void I_FrameTranscodeSeeking() {
        pFrameSeeking = false;
        permitEncode();
        // NOTE when try Sync to prefer time,
        // NOTE extractor just extract Video or Audio Sample. until it advanced about 1~3 seconds
        // NOTE might Extractor has seeking bugs.
        long frameInterval = getVideoIFrameInterval();
        if (VERBOSE) Log.d(TAG, "prevSeeking: " + frameInterval);
        long buffStartTimeUs = mStartTimeUs - frameInterval;
        if (buffStartTimeUs <= 0) buffStartTimeUs = 0;
        mExtractor.seekTo( buffStartTimeUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC );
        while ( mExtractor.getSampleFlags() != MediaExtractor.SAMPLE_FLAG_SYNC
                || mExtractor.getSampleTrackIndex() != mVideoTrackIndex
                || mExtractor.getSampleTime() < mStartTimeUs - frameInterval) {
            mExtractor.advance();
        }
        if (VERBOSE) Log.d(TAG, "prevSeeking: start seek ... " + mExtractor.getSampleTime());
    }

//    public void start() {
//        if (VERBOSE)  Log.d(TAG, "start:  ======================================= START OF NEW SEGMENT");
//        I_FrameTranscodeSeeking();
//
//        while( !checkFinished() ) {
//            if (VERBOSE) Log.d(TAG, "start: " + mExtractor.getSampleTime() + " / "+ mExtractor.getSampleTrackIndex());
//            boolean stepped = stepOnce();
//            // NOTE there's some trouble extractor's advance going back!?
//            if ( mExtractor.getSampleTime() > mEndTimeUs || mTranscdoerStoppedSignal ) {
//                mTranscdoerStoppedSignal = true;
//                forceStop();
//                mExtractor.advance();
//            }
//
//            if (!stepped) {
//                try {
//                    Thread.sleep(20);
//                } catch ( Exception e ) { e.printStackTrace(); }
//
//            }
//        }
//
//        if (VERBOSE) Log.d(TAG, "start: RELEASEING");
//        if (mVideoTranscoder != null) mVideoTranscoder.release();
//        if (mAudioTranscoder != null) mAudioTranscoder.release();
//    }

    public void release() {
        if (mVideoTranscoder != null) mVideoTranscoder.release();
        if (mAudioTranscoder != null) mAudioTranscoder.release();
    }

    private void setupMetadata() {
        if ( CURRENT_MODE == MediaEditor.AUDIO_ONLY ) return;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource( mInputFilePath );
        mOrginRotaion = Integer.parseInt( retriever.extractMetadata( MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION ));
        mMediaDurationUs = Long.parseLong( retriever.extractMetadata( MediaMetadataRetriever.METADATA_KEY_DURATION ));
    }

    private void setupVideoTranscoder() {
        mVideoInputFormat = TranscodeUtils.getFirstTrack( mExtractor, TranscodeUtils.MODE_VIDEO );
        mVideoTrackIndex = TranscodeUtils.getFirstTrackIndex( mExtractor, TranscodeUtils.MODE_VIDEO );
        mVideoTranscoder = new VideoTrackTranscoder( mExtractor, mTarget.videoOutputFormat, mBufferListener, mVideoTrackIndex, false );
        mVideoTranscoder.setup();
    }

    private void setupAuidoTranscoder() {
        mAudioInputFormat = TranscodeUtils.getFirstTrack( mExtractor, TranscodeUtils.MODE_AUDIO );
        mAudioTrackIndex = TranscodeUtils.getFirstTrackIndex( mExtractor, TranscodeUtils.MODE_AUDIO );
        mAudioTranscoder = new AudioTrackTranscoder( mExtractor, mTarget.audioOutputFormat, mBufferListener, mAudioTrackIndex, mAudioVolume );
        mAudioTranscoder.setup();
    }


    public MediaExtractor getExtractor() {
        return mExtractor;
    }

    public long getStartTimeUs() {
        return mStartTimeUs;
    }

    public long getEndTimeUs() {
        return mEndTimeUs;
    }

    public boolean isTranscdoerStoppedSignal() {
        return mTranscdoerStoppedSignal;
    }

    public void setTranscdoerStoppedSignal(boolean transcdoerStoppedSignal) {
        this.mTranscdoerStoppedSignal = transcdoerStoppedSignal;
    }

    public long getVideoStartedTimeUs() {
        return mVideoStartedTimeUs;
    }

    public long getAuidoStartedTimeUs() {
        return mAudioStartedTimeUs;
    }

    public long getVideoCurrentPresentationTimeUs () {
        return mVideoTranscoder.getExtractedPresentationTimeUs();
    }
    public long getAudioCurrentPresentationTimeUs () {
        return mAudioTranscoder.getExtractedPresentationTimeUs();
    }

    public boolean isEncodeStarted() {
        return encodingStarted;
    }

    public int getVideoTrackIndex() {
        return mVideoTrackIndex;
    }

    public int getAudioTrackIndex() {
        return mAudioTrackIndex;
    }
}
