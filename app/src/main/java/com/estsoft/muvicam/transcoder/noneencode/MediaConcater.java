package com.estsoft.muvicam.transcoder.noneencode;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import com.estsoft.muvicam.transcoder.transcoders.BufferListener;
import com.estsoft.muvicam.transcoder.utils.TranscodeUtils;
import com.estsoft.muvicam.transcoder.wrappers.MediaTranscoder;
import com.estsoft.muvicam.transcoder.wrappers.ProgressListener;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by estsoft on 2017-01-03.
 */

public class MediaConcater implements MediaTranscoder {
    private static final String TAG = "MediaConcater";
    public static final String AAC_MIME_TYPE = "audio/mp4a-latm";
    public static final int NORMAL = -98;
    public static final int MUTE_AND_ADD_MUSIC = -99;
    private int currentMode = NORMAL;

    private MediaMuxer mMuxer;
    private MediaFormat mOutVideoFormat;
    private MediaFormat mOutAudioFormat;
    private List<MediaConcatSegment> mSegments = new ArrayList<>();
    private MediaConcatAudioSegment mMusicSegment;
    private String mOutputFilePath;
    private long mTotalEstimatedDuration;
    private long mSegmentFinishedDuration;
    private int mMuxerVideoIndex;
    private int mMuxerAudioIndex;
    private boolean mMuxerStarted;

    private ProgressListener mListener;

    public MediaConcater(String outputPath, int mode, ProgressListener listener ) {
        this.mOutputFilePath = outputPath;
        try {
            this.mMuxer = new MediaMuxer(mOutputFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
        if ( !mOutputFilePath.endsWith(".mp4") ) mOutputFilePath += ".mp4";
        this.currentMode = mode;
        this.mListener = listener;
    }


    //NOTE startTimeUs is checked for I-Frame Sync before this
    @Override
    public void addSegment(String inputFilePath, long startTimeUs, long endTimeUs, int audioVolume ) {
        int mode = MediaConcatSegment.NORMAL;
        if ( currentMode == MUTE_AND_ADD_MUSIC ) mode = MediaConcatSegment.VIDEO_ONLY;
        MediaConcatSegment segment = new MediaConcatSegment( mBufferListener, inputFilePath, startTimeUs,
                endTimeUs, audioVolume, mode );
        mSegments.add( segment );
        mTotalEstimatedDuration += segment.getEndTimeUs() - segment.getStartTimeUs();
        Log.e(TAG, "addSegment: segment max duration ... " + segment.getmShorestDurationUs() );
        Log.e(TAG, "addSegment: total EstimatedDuration ... " + mTotalEstimatedDuration );
        setVideoTrackToMuxer( inputFilePath );
        if ( currentMode == NORMAL ) setAudioTrackToMuxer( inputFilePath );
    }

    @Override
    public void addMusicSegment(String inputFilePath, long offsetUs, int audioVolume ) {
        if ( currentMode == NORMAL ) throw new IllegalStateException( "to add MusicSegment, mode should be ADD_MUSIC or MUTE_AND_ADD_MUSIC " );
        mMusicSegment = new MediaConcatAudioSegment(mBufferListener, inputFilePath, offsetUs, audioVolume);
//        setTranscodeAudioTrackToMuxer( mMusicSegment.getAudioFormat() );
    }

    private void setVideoTrackToMuxer( String inputFilePath ) {
        if ( mOutVideoFormat == null ) {
            MediaExtractor extractor = new MediaExtractor();
            try { extractor.setDataSource(inputFilePath); }
            catch ( IOException e ) { throw new RuntimeException( e ); }
            mOutVideoFormat = TranscodeUtils.getFirstTrack( extractor, TranscodeUtils.MODE_VIDEO );
            mMuxerVideoIndex = mMuxer.addTrack( mOutVideoFormat );
            mMuxer.setOrientationHint( 90 );
        }
        if (!mMuxerStarted && mOutVideoFormat != null && mOutAudioFormat != null) {
            mMuxerStarted = true;
            mMuxer.start();
        }
    }
    private void setAudioTrackToMuxer( String inputFilePath ) {
        if ( mOutAudioFormat == null ) {
            MediaExtractor extractor = new MediaExtractor();
            try { extractor.setDataSource(inputFilePath); }
            catch ( IOException e ) { throw new RuntimeException( e ); }
            mOutAudioFormat = TranscodeUtils.getFirstTrack( extractor, TranscodeUtils.MODE_AUDIO );
            mMuxerAudioIndex = mMuxer.addTrack( mOutAudioFormat );
        }
        if (!mMuxerStarted && mOutVideoFormat != null && mOutAudioFormat != null) {
            mMuxerStarted = true;
            mMuxer.start();
        }
    }
    private void setTranscodeAudioTrackToMuxer( MediaFormat audioFormat ) {
        if ( mOutAudioFormat == null ) {
            mOutAudioFormat = audioFormat;
            mMuxerAudioIndex = mMuxer.addTrack( mOutAudioFormat );
        }
        if (mOutVideoFormat != null && mOutAudioFormat != null) {
            mMuxer.start();
        }
    }

    @Override
    public void startWork() {
        if (mListener != null) callListener( ProgressListener.START );
        if (mMusicSegment != null) mMusicSegment.prepare();

        for (MediaConcatSegment segment : mSegments) {
            segment.prepare();
            while ( !segment.isFinished() ) {
                if( mMusicSegment != null) mMusicSegment.stepOnce();
                segment.stepOnce();
                if (mListener != null) callListener( ProgressListener.PROGRESS );
            }
            mSegmentFinishedDuration += segment.getEndTimeUs() - segment.getStartTimeUs();
            Log.d(TAG, "onBufferAvailable: " + " _______________________________________ " +mSegmentFinishedDuration);
            segment.release();
            if (mMusicSegment != null) followMusicSegment();

        }
        if (mMusicSegment != null) flushMusicSegment();
        release();
        if (mListener != null) callListener( ProgressListener.COMPLETE );
    }
    public void release() {
        if (mMusicSegment != null) mMusicSegment.release();
        mMuxer.stop();
        mMuxer.release();
    }
    private void flushMusicSegment() {
        if (mMusicSegment.isFinished()) return;
        while ( mSegmentFinishedDuration > mMusicSegment.getAudioCurrentWrittenTimeUs() - mMusicSegment.getActualFirstExtractedTimeUs() ) {
            boolean stepped = mMusicSegment.stepOnce();
            if (mListener != null) callListener( ProgressListener.PROGRESS );
            if (!stepped) sleepWhile( 10 );
        }
    }
    private void followMusicSegment() {
        if (mMusicSegment.isFinished()) return;
        while( mSegmentFinishedDuration > mMusicSegment.getExtractedTimeUs() - mMusicSegment.getActualFirstExtractedTimeUs() ) {
            boolean stepped = mMusicSegment.stepOnce();
            if (!stepped) sleepWhile( 10 );
        }
    }
    private void sleepWhile( long sleepUs ) {
        try { Thread.sleep( sleepUs ); }
        catch ( Exception e ) { throw new RuntimeException( e ); }
    }

    private int progressInterval;
    private void callListener(int mode ) {
        if ( mListener == null ) return;
        long currentPresentation = (videoCurrent + audioCurrent) / 2;
        switch ( mode ) {
            case ProgressListener.START :
                mListener.onStart( mTotalEstimatedDuration );
                break;
            case ProgressListener.PROGRESS :
                if ( ++ progressInterval < ProgressListener.PROGRESS_INTERVAL ) return;
                progressInterval %= ProgressListener.PROGRESS_INTERVAL;
                mListener.onProgress( currentPresentation, (int) (currentPresentation * 100 / mTotalEstimatedDuration) );
                break;
            case ProgressListener.COMPLETE :
                mListener.onComplete( currentPresentation );
                break;
            case ProgressListener.ERROR :
                mListener.onError( new Exception( "Exception Occurred" ) );
                break;
            default :
                throw new IllegalStateException( "check listener mode" );
        }
    }

    private long videoCurrent;
    private long audioCurrent;
    BufferListener mBufferListener = new BufferListener() {
        @Override
        public void onBufferAvailable(BufferType type, ByteBuffer buffer, MediaCodec.BufferInfo bufferInfo) {
            if (currentMode == NORMAL) {
                bufferInfo.set(bufferInfo.offset, bufferInfo.size, bufferInfo.presentationTimeUs + mSegmentFinishedDuration, bufferInfo.flags);
            } else {
                if ( type == BufferType.VIDEO )
                    bufferInfo.set(bufferInfo.offset, bufferInfo.size, bufferInfo.presentationTimeUs + mSegmentFinishedDuration, bufferInfo.flags);
                else
                    bufferInfo.set(bufferInfo.offset, bufferInfo.size, bufferInfo.presentationTimeUs - mMusicSegment.getActualFirstExtractedTimeUs(), bufferInfo.flags);
            }
            if ( type == BufferType.VIDEO ) videoCurrent = bufferInfo.presentationTimeUs;
            else audioCurrent = bufferInfo.presentationTimeUs;
            Log.d( TAG, "onBufferAvailable: " + type
                    + " / " + bufferInfo.presentationTimeUs + " / " + bufferInfo.size
                    + (bufferInfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME ? " /  ** KEY" : " " + bufferInfo.flags ) );
            mMuxer.writeSampleData(
                    type == BufferType.VIDEO ? mMuxerVideoIndex : mMuxerAudioIndex,
                    buffer,
                    bufferInfo );
        }

        //NOTE MUSIC ONLY
        @Override
        public void onOutputFormat(BufferType type, MediaFormat format) {
            Log.d(TAG, "onOutputFormat: " + format.toString());
            mOutAudioFormat = format;
            mMuxerAudioIndex = mMuxer.addTrack( format );
            mMuxerStarted = true;
            mMuxer.start();
        }
    };

    @Override
    public void initVideoTarget(int interval, int frameRate, int bitrate, int rotation, int width, int height) {
    }

    @Override
    public void initAudioTarget(int sampleRate, int channelCount, int bitrate) {
    }
}
