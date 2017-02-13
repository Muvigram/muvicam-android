package com.estsoft.muvigram.transcoder.noneencode;

import android.content.res.AssetFileDescriptor;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import com.estsoft.muvigram.transcoder.transcoders.BufferListener;
import com.estsoft.muvigram.transcoder.utils.TranscodeUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

import timber.log.Timber;

/**
 * Created by estsoft on 2017-01-03.
 */

public class MediaConcatSegment {
    public static final int VIDEO_ONLY = -11;
    public static final int NORMAL = -12;
    private int currentMode;

    private MediaExtractor mExtractor;
    private BufferListener mBufferListener;
    private ByteBuffer mVideoBuffer = ByteBuffer.allocateDirect( 6230016 );
    private ByteBuffer mAudioBuffer = ByteBuffer.allocateDirect( 4086 );
    private MediaCodec.BufferInfo mVideoBufferInfo = new MediaCodec.BufferInfo();
    private MediaCodec.BufferInfo mAudioBufferInfo = new MediaCodec.BufferInfo();
    private String mInputFilePath;
    private AssetFileDescriptor mInputFile;
    private long mStartTimeUs;
    private long mActualFirstExtractedTimeUs;
    private long mEndTimeUs;
    private long mShorestDurationUs;
    private int mAudioVolume;
    private int mVideoTrackIndex;
    private int mAudioTrackIndex;
    private boolean isFinished;

    public long getmShorestDurationUs() { return mShorestDurationUs; }

    public MediaConcatSegment(BufferListener bufferListener, AssetFileDescriptor inputFile,
                              long startTimeUs, long endTimeUs, int audioVolume, int mode) {
        this.mBufferListener = bufferListener;
        this.mInputFile = inputFile;
        this.mExtractor = new MediaExtractor();
        Timber.v("SharePresenter: %s", mInputFile.toString());
        try {
            mExtractor.setDataSource( mInputFile.getFileDescriptor(), mInputFile.getStartOffset(), mInputFile.getLength() );
        } catch (IOException e ) {
            throw new RuntimeException( e );
        }
        mShorestDurationUs = getShortestDuration();
        this.mStartTimeUs = startTimeUs < 0 ? 0 : startTimeUs > mShorestDurationUs ? mShorestDurationUs : startTimeUs;
        this.mEndTimeUs = endTimeUs < 0 ? mShorestDurationUs : endTimeUs > mShorestDurationUs ? mShorestDurationUs : endTimeUs;
        this.mAudioVolume = audioVolume;
        this.currentMode = mode;
    }

    public MediaConcatSegment(BufferListener bufferListener, String inputFilePath,
                              long startTimeUs, long endTimeUs, int audioVolume, int mode) {
        this.mBufferListener = bufferListener;
        this.mInputFilePath = inputFilePath;
        this.mExtractor = new MediaExtractor();
        try {
            mExtractor.setDataSource( mInputFilePath );
        } catch (IOException e ) {
            throw new RuntimeException( e );
        }
        mShorestDurationUs = getShortestDuration();
        this.mStartTimeUs = startTimeUs < 0 ? 0 : startTimeUs > mShorestDurationUs ? mShorestDurationUs : startTimeUs;
        this.mEndTimeUs = endTimeUs < 0 ? mShorestDurationUs : endTimeUs > mShorestDurationUs ? mShorestDurationUs : endTimeUs;
        this.mAudioVolume = audioVolume;
        this.currentMode = mode;
    }

    public void prepare() {
        if ( currentMode == VIDEO_ONLY ) {
            mVideoTrackIndex = TranscodeUtils.getFirstTrackIndex( mExtractor, TranscodeUtils.MODE_VIDEO );
            mExtractor.selectTrack( mVideoTrackIndex );
        } else {
            mVideoTrackIndex = TranscodeUtils.getFirstTrackIndex( mExtractor, TranscodeUtils.MODE_VIDEO );
            mExtractor.selectTrack( mVideoTrackIndex );
            mAudioTrackIndex = TranscodeUtils.getFirstTrackIndex( mExtractor, TranscodeUtils.MODE_AUDIO );
            mExtractor.selectTrack( mAudioTrackIndex );
        }
        // NOTE seeking start time
        mExtractor.seekTo( mStartTimeUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC );
        mActualFirstExtractedTimeUs = mExtractor.getSampleTime();
        Timber.v("prepare: Extractor seek ... %d", mExtractor.getSampleTime());
    }

    private boolean stepVideo() {
        if (isFinished) return false;
        if ( mExtractor.getSampleTrackIndex() != mVideoTrackIndex ) return false;
        long presentationTimeUs = mExtractor.getSampleTime();
        int size = mExtractor.readSampleData( mVideoBuffer, 0 );
        boolean isKeyFrame = (mExtractor.getSampleFlags() & MediaExtractor.SAMPLE_FLAG_SYNC) != 0;
        mVideoBufferInfo.set(0, size, presentationTimeUs - mActualFirstExtractedTimeUs, isKeyFrame ? MediaCodec.BUFFER_FLAG_SYNC_FRAME : 0);
        mExtractor.advance();
        mBufferListener.onBufferAvailable(BufferListener.BufferType.VIDEO, mVideoBuffer, mVideoBufferInfo);
        if ( mExtractor.getSampleTrackIndex() < 0 ) isFinished = true;
        if ( mExtractor.getSampleTime() > mEndTimeUs ) isFinished = true;
        return true;
    }
    private boolean stepAudio() {
        if (isFinished) return false;
        if (mExtractor.getSampleTrackIndex() != mAudioTrackIndex ) return false;
        long presentationTimeUs = mExtractor.getSampleTime();
        int size = mExtractor.readSampleData( mAudioBuffer, 0);
        boolean isKeyFrame = (mExtractor.getSampleFlags() & MediaExtractor.SAMPLE_FLAG_SYNC) != 0;
        mAudioBufferInfo.set(0, size, presentationTimeUs - mActualFirstExtractedTimeUs, isKeyFrame ? MediaCodec.BUFFER_FLAG_SYNC_FRAME : 0);
        mExtractor.advance();
        mBufferListener.onBufferAvailable(BufferListener.BufferType.AUDIO, mAudioBuffer, mAudioBufferInfo);
        if ( mExtractor.getSampleTrackIndex() < 0 ) isFinished = true;
        if ( mExtractor.getSampleTime() > mEndTimeUs ) isFinished = true;
        return true;
    }
    public boolean stepOnce() {
        boolean busy = false;
        switch ( currentMode ) {
            case VIDEO_ONLY :
                stepVideo(); break;
            case NORMAL :
                stepVideo();
                stepAudio();
                break;
        }
        return busy;
    }

    private long getShortestDuration() {
        long shortestTimeUs = mExtractor.getTrackFormat( 0 ).getLong( MediaFormat.KEY_DURATION );
        for ( int i = 1; i < mExtractor.getTrackCount(); i ++ ) {
            long time = mExtractor.getTrackFormat( i ).getLong( MediaFormat.KEY_DURATION );
            if ( time < shortestTimeUs ) shortestTimeUs = time;
        }
        return shortestTimeUs;
    }
    public void release() {
        mExtractor.release();
    }

    public long getEndTimeUs() {
        return mEndTimeUs;
    }

    public long getStartTimeUs() {
        return mStartTimeUs;
    }

    public boolean isFinished() {
        return isFinished;
    }
}
