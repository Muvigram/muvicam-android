package com.estsoft.muvicam.transcoder.wrappers;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import com.estsoft.muvicam.transcoder.transcoders.BufferListener;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by estsoft on 2016-12-08.
 */

// NOTE Since this can't resampling, Track Audio should NOT be encoding. i.e) VIDEO_ONLY
// NOTE Just encode Music segment.

public class MediaEditor {
    private static final String TAG = "MediaEditor";
    public static final int VIDEO_ONLY = -10;
    public static final int AUDIO_ONLY = -11;
    public static final int NORMAL = -12;
    public static int CURRENT_MODE = NORMAL;

    public static final boolean pFrameSeek = true;

    private final MuxerWrapper mMuxer;
    private final String mOutputFilePath;
    private final int outputContainer = MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4;

    private final MediaTarget mTarget;
    private final List<MediaSegment> mSegmentLists;
    private MediaSegment mMusicSegment;

    public MediaEditor(String outputPath, int TranscodeMode ) {
        this.mOutputFilePath = outputPath;

        MediaMuxer muxer;
        try {
            muxer = new MediaMuxer(this.mOutputFilePath, outputContainer);
        } catch ( IOException e ) {
            throw new IllegalStateException( e );
        }

        CURRENT_MODE = TranscodeMode;
        mTarget = new MediaTarget();

        mMuxer = new MuxerWrapper( muxer, CURRENT_MODE );
        mSegmentLists = new ArrayList<>();
    }

    public void initVideoTarget(int interval, int frameRate, int bitrate, int rotation, int width, int height ) {
        mTarget.initVideoTarget( interval, frameRate, bitrate, rotation, width, height, false );
        mMuxer.setOrientation( rotation );
        mMuxer.setVideoParams( frameRate );
    }
    public void initAudioTarget( int sampleRate, int channelCount, int bitrate ) {
        mTarget.initAudioTarget( sampleRate, channelCount, bitrate );
        mMuxer.setAudioParams( sampleRate );
    }
    public void addSegment(String inputFilePath, long startTimeUs, long endTimeUs, int audioVolume  ){
        if ( !(endTimeUs < 0) && startTimeUs >= endTimeUs) throw new IllegalStateException( " start can't be later than end " );
        MediaSegment segment = new MediaSegment( mTarget, inputFilePath, mMuxer, mBufferListener, startTimeUs, endTimeUs, audioVolume );
        mSegmentLists.add( segment );
    }
    public void setMusicSegment(String musicPath, long offset, int volume ) {
        mMusicSegment = new MediaSegment( mTarget, musicPath, mMuxer, mBufferListener, offset, -1, volume );
        mMuxer.setMode( NORMAL );
    }

    public void start() {

        if ( mMusicSegment != null ) mMusicSegment.prepare( AUDIO_ONLY, pFrameSeek );

        for ( MediaSegment segment  : mSegmentLists ) {
            segment.prepare( CURRENT_MODE, pFrameSeek );

            Log.d(TAG, "start: START OF NEW SEGMENT" );
            while ( !segment.checkFinished() ) {
//                if ( segment.isTranscdoerStoppedSignal() ) break;
                boolean stepped = segment.stepOnce();
                Log.d(TAG, "start: " + segment.getExtractor().getSampleTime() + " / " + segment.getExtractor().getSampleTrackIndex());
                if (segment.getVideoCurrentPresentationTimeUs() > segment.getEndTimeUs() ) {
                    segment.forceStopVideo();
                    if (segment.getExtractor().getSampleTrackIndex() == segment.getVideoTrackIndex()) segment.getExtractor().advance();
                }
                if (segment.getAudioCurrentPresentationTimeUs() > segment.getEndTimeUs() ) {
                    segment.forceStopAudio();
                    if (segment.getExtractor().getSampleTrackIndex() == segment.getAudioTrackIndex()) segment.getExtractor().advance();
                }

//                if (segment.getExtractor().getSampleTime() > segment.getEndTimeUs()
//                        || segment.isTranscdoerStoppedSignal() ) {
//                    segment.setTranscdoerStoppedSignal( true );
//                    segment.release();
//                    segment.getExtractor().advance();
//                }

                if (!stepped) {
                    try {    Thread.sleep(20);  }
                    catch ( Exception e ) { e.printStackTrace(); }
                } else {
                    if ( mMusicSegment != null && segment.isEncodeStarted()) {
                        // NOTE should it be avg of video and audio?
                        long mediaProcessedTime = segment.getVideoCurrentPresentationTimeUs() - segment.getStartTimeUs();
                        long musicProcessedTime = mMusicSegment.getAudioCurrentPresentationTimeUs() - mMusicSegment.getStartTimeUs();
                        while (mediaProcessedTime >= musicProcessedTime) {
                            mMusicSegment.stepOnce();
                            musicProcessedTime = mMusicSegment.getAudioCurrentPresentationTimeUs() - mMusicSegment.getStartTimeUs();
                            Log.d(TAG, "start: " + mediaProcessedTime + " / MUSIC!  " + musicProcessedTime);
                        }
                    }
                }
            }
            segment.release();
            Log.d(TAG, "start: END OF SEGMENTS");
        }
        if ( mMusicSegment != null ) {
            mMusicSegment.setTranscdoerStoppedSignal(true);
            mMusicSegment.forceStop();
            mMusicSegment.getExtractor().advance();
            mMusicSegment.release();
        }
        stop();
        release();
    }
    public void stop() {
        if (!mMuxer.isStopped()) mMuxer.stop();
    }
    public void release() {
        if (!mMuxer.isStopped()) mMuxer.stop();
        mMuxer.release();
    }

    // TODO Audio from Video Mix with MP3 Buffer. - in AudioChannel X _X
    private final BufferListener mBufferListener = new BufferListener() {
        @Override
        public void onBufferAvailable(BufferType type, ByteBuffer buffer, MediaCodec.BufferInfo bufferInfo) {
            mMuxer.writeSampleData(
                    type == BufferType.VIDEO ? MuxerWrapper.SampleType.VIDEO : MuxerWrapper.SampleType.AUDIO,
                    buffer,
                    bufferInfo );
        }

        @Override
        public void onOutputFormat(BufferType type, MediaFormat format) {
            mMuxer.setOutputFormat(
                    type == BufferListener.BufferType.VIDEO ? MuxerWrapper.SampleType.VIDEO : MuxerWrapper.SampleType.AUDIO,
                    format);
        }
    };

}
