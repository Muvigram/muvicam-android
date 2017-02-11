package com.estsoft.muvigram.transcoder.wrappers;

import android.content.res.AssetFileDescriptor;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;


import com.estsoft.muvigram.transcoder.transcoders.BufferListener;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by estsoft on 2016-12-21.
 */

public
class MediaEditor implements MediaTranscoder {
    public static final int NORMAL = -12;
    public static final int MUTE_AND_ADD_MUSIC = -13;
    public static final int ADD_MUSIC = -14;
    private int progressInterval;

    private final int CURRENT_MODE;
    private final int outputContainer = MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4;
    private final MuxerWrapper mMuxer;
    private final String mOutputFilePath;
    private final MediaTarget mTarget;
    private final List<MediaSegment> mSegmentLists;

    private final ProgressListener mListener;

    private MediaSegment mMusicSegment;
    private long mTotalEstimatedDuration;
    private long mSegmentTargetDuration;
    private boolean musicSegmentAdded;

    public MediaEditor(String outputPath, int transcodeMode, ProgressListener progressListener ) {
        this.mOutputFilePath = outputPath;
        this.CURRENT_MODE = transcodeMode;
        this.mTarget = new MediaTarget();
        this.mSegmentLists = new ArrayList<>();

        MediaMuxer muxer;
        try {
            muxer = new MediaMuxer( this.mOutputFilePath, outputContainer );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
        this.mMuxer = new MuxerWrapper( muxer, CURRENT_MODE );
        this.mListener = progressListener;
    }

    @Override
    public void initVideoTarget(int interval, int frameRate, int bitrate, int rotation, int width, int height ) {
        mTarget.initVideoTarget( interval, frameRate, bitrate, width, height );
        mMuxer.setOrientation( rotation );
        mMuxer.setVideoParams( frameRate );
    }
    @Override
    public void initAudioTarget( int sampleRate, int channelCount, int bitrate ) {
        mTarget.initAudioTarget( sampleRate, channelCount, bitrate );
        mMuxer.setAudioParams( sampleRate );
    }


    @Override
    public void addSegment(String inputFilePath, long startTimeUs, long endTimeUs, int audioVolume  ) {
        if ( musicSegmentAdded ) throw new IllegalStateException( "music segment can be added after all segments added " );
        if ( !(endTimeUs < 0) && startTimeUs >= endTimeUs) throw new IllegalStateException( "start can't be later than end " );

        int mode = MediaSegment.NORMAL;
        if ( CURRENT_MODE == MUTE_AND_ADD_MUSIC ) mode = MediaSegment.VIDEO_ONLY;
        MediaSegment segment = new MediaSegment( mTarget, inputFilePath, mBufferListener,
                startTimeUs, endTimeUs, audioVolume, mode );
        // NOTE check startTime over total duration
        if ( segment.getStartTimeUs() < segment.getEndTimeUs() ) {
            mSegmentLists.add(segment);
            mTotalEstimatedDuration += segment.getEndTimeUs() - segment.getStartTimeUs();
            Timber.d("addSegment: Adding segment ... %s/%ld/%ld", inputFilePath, segment.getStartTimeUs(), segment.getEndTimeUs() );
        } else {
            Timber.d("addSegment: Skipping segment ... %s/%ld/%ld", inputFilePath, segment.getStartTimeUs(), segment.getEndTimeUs());
        }
    }

    @Override
    public void addLogoSegment(AssetFileDescriptor inputFile, long startTimeUs, long endTimeUs, int audioVolume) {
        if ( musicSegmentAdded ) throw new IllegalStateException( "music segment can be added after all segments added " );
        if ( !(endTimeUs < 0) && startTimeUs >= endTimeUs) throw new IllegalStateException( "start can't be later than end " );

        int mode = MediaSegment.NORMAL;
        if ( CURRENT_MODE == MUTE_AND_ADD_MUSIC ) mode = MediaSegment.VIDEO_ONLY;
        MediaSegment segment = new MediaSegment( mTarget, inputFile, mBufferListener,
                startTimeUs, endTimeUs, audioVolume, mode );
        // NOTE check startTime over total duration
        if ( segment.getStartTimeUs() < segment.getEndTimeUs() ) {
            mSegmentLists.add(segment);
//            mTotalEstimatedDuration += segment.getEndTimeUs() - segment.getStartTimeUs();
            Timber.d("addSegment: Adding segment as logo ... %s/%ld/%ld", inputFile.toString(), segment.getStartTimeUs(), segment.getEndTimeUs() );
        } else {
            Timber.d("addSegment: Skipping segment ... %s/%ld/%ld", inputFile.toString(), segment.getStartTimeUs(), segment.getEndTimeUs() );
        }
    }

    @Override
    public void addMusicSegment(String inputFilePath, long offset, int audioVolume ) {
        if ( CURRENT_MODE == NORMAL ) throw new IllegalStateException( "to add MusicSegment, mode should be ADD_MUSIC or MUTE_AND_ADD_MUSIC " );

        musicSegmentAdded = true;
//        mMusicSegment = new MediaSegment( mTarget, inputFilePath, mBufferListener,
//                offset,-1, audioVolume, MediaSegment.AUDIO_ONLY);
        mMusicSegment = new MediaSegment( mTarget, inputFilePath, mBufferListener,
                offset, mTotalEstimatedDuration + offset, audioVolume, MediaSegment.AUDIO_ONLY);
        Timber.d("addMusicSegment: %ld", mTotalEstimatedDuration);
    }
    @Override
    public void startWork() {
        if ( mListener != null ) callListener( ProgressListener.START );
        if ( mMusicSegment != null ) mMusicSegment.prepare();

        boolean segmentStepped;
        long videoSyncBufferTimeUs = 0;
        long audioSyncBufferTimeUs = 0;
        for ( MediaSegment segment : mSegmentLists ) {
            Timber.v("start: Start of new segment");
            segment.prepare();
            segment.setSmallSync( videoSyncBufferTimeUs, audioSyncBufferTimeUs );

            long segmentDuration = segment.getEndTimeUs() - segment.getStartTimeUs();
            mSegmentTargetDuration += segmentDuration;

            while ( !segment.checkFinished() ) {
                segmentStepped = segment.stepOnce();
                if ( !segmentStepped ) sleepWhile( 20 );
                    // TODO segment.isAudioEncodingStarted? when ADD_MUSIC MODE
                else if ( mMusicSegment != null && segment.isVideoEncodingStarted() ) {
                    musicSegmentStepping( segment.getVideoCurrentWrittenTimeUs() + mSegmentTargetDuration - segment.getEndTimeUs());
                }
                if (mListener != null) callListener( ProgressListener.PROGRESS );
                Timber.v("stepOnce: ");

            }

            videoSyncBufferTimeUs = (mMuxer.getVideoPresentationTimeUs() - mSegmentTargetDuration);
            audioSyncBufferTimeUs = (mMuxer.getAudioPresentationTimeUs() - mSegmentTargetDuration);

            Timber.d("start: End of this segment ... target Duration is %ld", mSegmentTargetDuration );
            segment.release();
        }

        // NOTE this method order is important
        if (mMusicSegment != null) flushMusicSegment();
        release();
        if (mListener != null) callListener( ProgressListener.COMPLETE );
    }

    public void release() {
        if (mMusicSegment != null) mMusicSegment.release();
        if (!mMuxer.isStopped()) mMuxer.stop();
        mMuxer.release();
    }

    private void musicSegmentStepping( long totalProcessed ) {
        long musicProcessed = mMusicSegment.getAudioCurrentWrittenTimeUs() - mMusicSegment.getStartTimeUs();
        long musicExtracted;
        while ( !mMusicSegment.checkFinished()
                && (!mMusicSegment.isAudioEncodingStarted() || totalProcessed >= musicProcessed) ) {
            boolean stepped = mMusicSegment.stepOnce();
            if (!stepped) sleepWhile( 20 );
            musicProcessed = mMusicSegment.getAudioCurrentWrittenTimeUs() - mMusicSegment.getStartTimeUs();
            musicExtracted = mMusicSegment.getAudioCurrentExtractedTimeUs() - mMusicSegment.getStartTimeUs();
            // NOTE for safety
            if ( musicExtracted >= mTotalEstimatedDuration )  mMusicSegment.forceStop();
        }
    }
    private void flushMusicSegment() {
        while ( !mMusicSegment.checkFinished() ) {
            Timber.v("flushMusicSegment: FLUSHING");
            boolean stepped = mMusicSegment.stepOnce();
            if (!stepped) sleepWhile( 20 );
        }
    }

    private void sleepWhile( long sleepUs ) {
        try { Thread.sleep( sleepUs ); }
        catch ( Exception e ) { throw new RuntimeException( e ); }
    }

    private void callListener( int mode ) {
        if ( mListener == null ) return;
        switch ( mode ) {
            case ProgressListener.START :
                mListener.onStart( mTotalEstimatedDuration );
                break;
            case ProgressListener.PROGRESS :
                if ( ++ progressInterval < ProgressListener.PROGRESS_INTERVAL ) return;
                progressInterval %= ProgressListener.PROGRESS_INTERVAL;
                mListener.onProgress( mMuxer.getVideoPresentationTimeUs(), (int) (mMuxer.getVideoPresentationTimeUs() * 100 / mTotalEstimatedDuration) );
                break;
            case ProgressListener.COMPLETE :
                mListener.onComplete( mMuxer.getVideoPresentationTimeUs() );
                break;
            case ProgressListener.ERROR :
                mListener.onError( new Exception( "Exception Occurred" ) );
                break;
            default :
                throw new IllegalStateException( "check listener mode" );
        }
    }

    private final BufferListener mBufferListener = new BufferListener() {
        @Override
        public void onBufferAvailable(BufferType type, ByteBuffer buffer, MediaCodec.BufferInfo bufferInfo) {
            mMuxer.writeSampleData(
                    type == BufferType.VIDEO ? MuxerWrapper.SampleType.VIDEO : MuxerWrapper.SampleType.AUDIO,
                    buffer,
                    bufferInfo );
        }

        @Override
        public void onOutputFormat(BufferListener.BufferType type, MediaFormat format) {
            mMuxer.setOutputFormat(
                    type == BufferType.VIDEO ? MuxerWrapper.SampleType.VIDEO : MuxerWrapper.SampleType.AUDIO,
                    format);
        }
    };
}
