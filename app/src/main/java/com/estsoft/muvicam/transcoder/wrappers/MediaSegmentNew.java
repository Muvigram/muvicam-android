package com.estsoft.muvicam.transcoder.wrappers;

import android.content.res.AssetFileDescriptor;
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
 * Created by estsoft on 2016-12-21.
 */

public class MediaSegmentNew {

    private static final String TAG = "MediaSegment";
    private static final boolean VERBOSE = false;
    private static final long SEEK_STEP = 33333;
    public static final int VIDEO_ONLY = -9;
    public static final int AUDIO_ONLY = -10;
    public static final int NORMAL = -11;
    public int CURRENT_MODE = MediaEditorNew.NORMAL;

    private final MediaTarget mTarget;
    private final MediaExtractor mExtractor;
    private final BufferListener mBufferListener;
    private String mInputFilePath;
    private AssetFileDescriptor mInputFile;
    private final long mStartTimeUs;
    private final long mEndTimeUs;
    private final int mAudioVolume;

    private long mVideoSyncOffset;
    private long mAudioSyncOffset;

    private VideoTrackTranscoder mVideoTranscoder;
    private AudioTrackTranscoder mAudioTranscoder;
    private MediaFormat mVideoInputFormat;
    private MediaFormat mAudioInputFormat;
    private long mMediaDurationMs;
    private long mVideoCurrentExtractedUs = -1;
    private long mAudioCurrentExtractedUs = -1;
    private int mVideoTrackIndex;
    private int mAudioTrackIndex;
    private int mOrginRotaion;
    private boolean mVideoEncodingStarted;
    private boolean mVideoForceStoped;
    private boolean mAudioEncodingStarted;
    private boolean mAudioForceStoped;

    public MediaSegmentNew(MediaTarget target, AssetFileDescriptor inputFile, BufferListener bufferListener,
                           long startTimeUs, long endTimeUs, int audioVolume, int transcodeMode ) {
        this.mTarget = target;
        this.mInputFile = inputFile;
        this.mBufferListener = bufferListener;
        this.mAudioVolume = audioVolume;
        this.mExtractor = new MediaExtractor();
        try {
            mExtractor.setDataSource( mInputFile.getFileDescriptor(), mInputFile.getStartOffset(), mInputFile.getLength() );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
        long shortestDuration = getShortestDuration();
        this.mStartTimeUs = startTimeUs < 0 ? 0 : startTimeUs > shortestDuration ? shortestDuration : startTimeUs;
        this.mEndTimeUs = endTimeUs < 0 ? shortestDuration : endTimeUs > shortestDuration ? shortestDuration : endTimeUs;
        Log.e(TAG, "MediaSegmentNew: " + mStartTimeUs + " / " + mEndTimeUs  + " / " + shortestDuration);
        this.CURRENT_MODE = transcodeMode;
    }


    public MediaSegmentNew(MediaTarget target, String mediaFilePath, BufferListener bufferListener,
                           long startTimeUs, long endTimeUs, int audioVolume, int transcodeMode ) {
        this.mTarget = target;
        this.mInputFilePath = mediaFilePath;
        this.mBufferListener = bufferListener;
        this.mAudioVolume = audioVolume;
        this.mExtractor = new MediaExtractor();
        try {
            mExtractor.setDataSource( mInputFilePath );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
        long shortestDuration = getShortestDuration();
        this.mStartTimeUs = startTimeUs < 0 ? 0 : startTimeUs > shortestDuration ? shortestDuration : startTimeUs;
        this.mEndTimeUs = endTimeUs < 0 ? shortestDuration : endTimeUs > shortestDuration ? shortestDuration : endTimeUs;
        Log.e(TAG, "MediaSegmentNew: " + mStartTimeUs + " / " + mEndTimeUs  + " / " + shortestDuration);
        this.CURRENT_MODE = transcodeMode;
        Log.d(TAG, "MediaSegmentNew: " + CURRENT_MODE);
    }

    public void prepare( ) {
        switch ( CURRENT_MODE ) {
            case VIDEO_ONLY :
                setupVideoTranscoder();
                break;
            case AUDIO_ONLY:
                setupAudioTranscoder();
                break;
            default :
                setupVideoTranscoder();
                setupAudioTranscoder();
        }

        // HINT P-Frame Seeking
        long interval = getVideoIFrameInterval();
        if ( interval < mStartTimeUs ) {
            if ( CURRENT_MODE == VIDEO_ONLY ) mExtractor.seekTo( mStartTimeUs - interval, MediaExtractor.SEEK_TO_PREVIOUS_SYNC );
            else if ( CURRENT_MODE == AUDIO_ONLY ) mExtractor.seekTo( mStartTimeUs - interval * 20, MediaExtractor.SEEK_TO_PREVIOUS_SYNC );
            else {
                do {
                    mExtractor.seekTo(mStartTimeUs - interval, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
                } while ( mExtractor.getSampleTrackIndex() != mVideoTrackIndex );
            }
        }
        Log.d(TAG, "prepare: Seek Time ... " + CURRENT_MODE + " / " + mExtractor.getSampleTime() + " / " + mExtractor.getSampleTrackIndex());
    }

    public void setSmallSync(long videoSyncBufferTimeUs, long audioSyncBufferTimeUs ) {
        mVideoSyncOffset = videoSyncBufferTimeUs;
        mAudioSyncOffset = audioSyncBufferTimeUs;
    }

    private boolean videoStepOnce() {
        // HINT checking encode Start
        if ( !mVideoEncodingStarted ) checkPermittingVideoEncode();
        // HINT checking over end time
        if ( mVideoEncodingStarted && !mVideoForceStoped ) checkForceStoppingVideo();
        // HINT when one transcoder is ended
//        if ( !mAudioForceStoped && mVideoTranscoder.isExtractingFinished() && mAudioCurrentExtractedUs > mVideoCurrentExtractedUs ) {
//            Log.d(TAG, "stepOnce : AUDIO ???? ");
//            mAudioTranscoder.forceStop();
//            mAudioForceStoped = true;
//        }
        boolean stepped = mVideoTranscoder.stepPipeline();
        mVideoCurrentExtractedUs = mVideoTranscoder.getExtractedPresentationTimeUs();
        return stepped;
    }
    private boolean audioStepOnce() {

        // HINT checking encode Start
        if ( !mAudioEncodingStarted ) checkPermittingAudioEncode();
        // HINT checking over end time
        if ( mAudioEncodingStarted && !mAudioForceStoped ) checkForceStoppingAudio();
        // HINT when one transcoder is ended
//        if ( !mVideoForceStoped && mAudioTranscoder.isExtractingFinished() && mVideoCurrentExtractedUs > mAudioCurrentExtractedUs ) {
//            Log.d(TAG, "stepOnce : VIDEO ???? ");
//            mVideoTranscoder.forceStop();
//            mVideoForceStoped = true;
//        }
        boolean stepped = mAudioTranscoder.stepPipeline();
        mAudioCurrentExtractedUs = mAudioTranscoder.getExtractedPresentationTimeUs();
        return stepped;
    }

    public boolean stepOnce() {
        switch ( CURRENT_MODE ) {
            case VIDEO_ONLY :
                return videoStepOnce();
            case AUDIO_ONLY:
                return audioStepOnce();
            default :
                boolean video = videoStepOnce();
                boolean audio = audioStepOnce();
                return video || audio;
        }
    }

    public boolean checkFinished() {
        boolean video = mVideoTranscoder == null || mVideoTranscoder.isFinished();
        boolean audio = mAudioTranscoder == null || mAudioTranscoder.isFinished();
        return video && audio;
    }

    public void forceStop() {
        if (mVideoTranscoder != null) mVideoTranscoder.forceStop();
        mVideoForceStoped = true;
        mAudioTranscoder.forceStop();
        if (mAudioTranscoder != null) mAudioForceStoped = true;
    }

    public void release() {
        mExtractor.release();
        if (mVideoTranscoder != null) mVideoTranscoder.release();
        if (mAudioTranscoder != null) mAudioTranscoder.release();
    }

    private long getShortestDuration() {
        long shortestTimeUs = mExtractor.getTrackFormat( 0 ).getLong( MediaFormat.KEY_DURATION );
        for ( int i = 1; i < mExtractor.getTrackCount(); i ++ ) {
            long time = mExtractor.getTrackFormat( i ).getLong( MediaFormat.KEY_DURATION );
            if ( time < shortestTimeUs ) shortestTimeUs = time;
        }
        return shortestTimeUs;
    }

    private void checkForceStoppingVideo() {
        if ( mVideoCurrentExtractedUs > mEndTimeUs ) {
            mVideoTranscoder.forceStop();
            mVideoForceStoped = true;
            Log.d(TAG, "checkTimeStamp: video extracting end at " + mVideoCurrentExtractedUs );
        }
    }
    private void checkForceStoppingAudio() {
        if (mAudioCurrentExtractedUs > mEndTimeUs ) {
            mAudioTranscoder.forceStop();
            mAudioForceStoped = true;
            Log.d(TAG, "checkTimeStamp: audio extracting end at " + mAudioCurrentExtractedUs );
        }
    }

    private void checkPermittingVideoEncode() {
        if ( mVideoCurrentExtractedUs > ( mStartTimeUs + mVideoSyncOffset - 2)) {
            mVideoTranscoder.encodeStart();
            mVideoEncodingStarted = true;
            Log.d(TAG, "checkTimeStamp: video encode start at " + mVideoCurrentExtractedUs );
        }
    }

    private void checkPermittingAudioEncode() {
        if ( mAudioCurrentExtractedUs > ( mStartTimeUs + mAudioSyncOffset - 2 ) ) {
            mAudioTranscoder.encodeStart();
            mAudioEncodingStarted = true;
            Log.d(TAG, "checkTimeStamp: audio encode start at " + mAudioCurrentExtractedUs );
        }
    }

    private long getVideoIFrameInterval() {
        long first, second;
        long current = 0;
        long step = SEEK_STEP;
        while ( mExtractor.getSampleTrackIndex() != mVideoTrackIndex ) {
            mExtractor.seekTo( current, MediaExtractor.SEEK_TO_NEXT_SYNC );
            current += step;
        }
        first = mExtractor.getSampleTime();
        mExtractor.advance();
        while (mExtractor.getSampleTrackIndex() != mVideoTrackIndex ) {
            mExtractor.seekTo( current, MediaExtractor.SEEK_TO_NEXT_SYNC);
            current += step;
            // out of duration or no 2 more I-Frame
            if ( mExtractor.getSampleTime() < 0 ) break;
        }
        second = mExtractor.getSampleTime();
        if (second < 0 ) second = getShortestDuration();
        mExtractor.seekTo( 0 , MediaExtractor.SEEK_TO_CLOSEST_SYNC );
        return second - first;
    }

    private void setupVideoMetaDatas() {

    }
    private void setupVideoTranscoder() {

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        if (mInputFilePath == null ) {
            retriever.setDataSource( mInputFile.getFileDescriptor(), mInputFile.getStartOffset(), mInputFile.getLength() );
        } else {
            retriever.setDataSource(mInputFilePath);
        }
        mOrginRotaion = Integer.parseInt( retriever.extractMetadata( MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION ));
        mMediaDurationMs = Long.parseLong( retriever.extractMetadata( MediaMetadataRetriever.METADATA_KEY_DURATION ));

        mVideoInputFormat = TranscodeUtils.getFirstTrack( mExtractor, TranscodeUtils.MODE_VIDEO );
        mVideoTrackIndex = TranscodeUtils.getFirstTrackIndex( mExtractor, TranscodeUtils.MODE_VIDEO );
        mVideoTranscoder = new VideoTrackTranscoder( mExtractor, mTarget.videoOutputFormat, mBufferListener, mVideoTrackIndex );
        mVideoTranscoder.setup();
    }

    private void setupAudioTranscoder() {
        mAudioInputFormat = TranscodeUtils.getFirstTrack( mExtractor, TranscodeUtils.MODE_AUDIO );
        mAudioTrackIndex = TranscodeUtils.getFirstTrackIndex( mExtractor, TranscodeUtils.MODE_AUDIO );
        mAudioTranscoder = new AudioTrackTranscoder( mExtractor, mTarget.audioOutputFormat, mBufferListener, mAudioTrackIndex, mAudioVolume );
        mAudioTranscoder.setup();
    }

    public long getEndTimeUs() {
        return mEndTimeUs;
    }

    public long getStartTimeUs() {
        return mStartTimeUs;
    }

    public long getVideoCurrentExtractedTimeUs() {
        return mVideoTranscoder.getExtractedPresentationTimeUs();
    }
    public long getAudioCurrentExtractedTimeUs() {
        return mAudioTranscoder.getExtractedPresentationTimeUs();
    }

    public long getVideoCurrentWrittenTimeUs() {
        return mVideoTranscoder.getWrittenPresentationTimeUs();
    }
    public long getAudioCurrentWrittenTimeUs() {
        return mAudioTranscoder.getWrittenPresentationTimeUs();
    }

    public boolean isVideoEncodingStarted() {
        return mVideoEncodingStarted;
    }

    public boolean isAudioEncodingStarted() {
        return mAudioEncodingStarted;
    }

}
