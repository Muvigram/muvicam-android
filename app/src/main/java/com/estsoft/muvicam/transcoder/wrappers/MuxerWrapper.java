package com.estsoft.muvicam.transcoder.wrappers;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by estsoft on 2016-12-06.
 */

// NOTE 원본에 비어있는 Audio or Video Buffer 채워줘야함

public class MuxerWrapper {
    private static final int BUFFER_SIZE = 64 * 1024; // I have no idea whether this value is appropriate or not...
    private static final long MICROSECS_PER_SEC = 1000000;

    private MediaMuxer mMuxer;
    private MediaFormat mVideoFormat;
    private MediaFormat mAudioFormat;

    private ByteBuffer mBuffer;
    private List<SampleInfo> mSampleInfoList;

    private int mVideoTrack;
    private int mAudioTrack;
    private boolean mMuxerStarted;

    private long mVideoPresentationTimeUs;
    private long mVideoLastPresentationTimeUs = 0;

    // TODO default time gap fix with user input
    private long mVideoDefaultTimeGap = 33333;
    private long mVideoTimeGapThreshold = mVideoDefaultTimeGap * 5;
    private long mAudioPresentationTimeUs;
    private long mAudioLastPresentationTimeUs = 0;

    private long mAudioDefaultTimeGap = 21333 ;
    private long mAudioTimeGapThreshold = mAudioDefaultTimeGap * 5;

    public MuxerWrapper(MediaMuxer mMuxer, int mode ) {
        this.mMuxer = mMuxer;
        mSampleInfoList = new ArrayList<>();
    }

    public void setOutputFormat(SampleType type, MediaFormat format) {
        switch ( type ) {
            case VIDEO :
                mVideoFormat = format;
                break;
            case AUDIO :
                mAudioFormat = format;
                break;
            default :
                throw new AssertionError();
        }
        onSetOutputFormat();
    }

    public void setVideoParams( int frameRate ) {
        mVideoDefaultTimeGap = MICROSECS_PER_SEC / frameRate;
        mVideoTimeGapThreshold = mVideoDefaultTimeGap * 2;
        Timber.d("setVideoParams: %ld", mVideoDefaultTimeGap);
    }
    public void setAudioParams( int sampleRate ) {
//        mAudioDefaultTimeGap = MICROSECS_PER_SEC / sampleRate;
//        mAudioTimeGapThreshold = mAudioDefaultTimeGap * 2;
//        Log.d(TAG, "setAudioParams: " + mAudioDefaultTimeGap);
    }

    private void onSetOutputFormat() {
        if ( mMuxerStarted ) return;
        if ( !isFormatSetup() ) return;
        mMuxer.start();
        mMuxerStarted = true;

        if (mBuffer == null) mBuffer = ByteBuffer.allocate(0);
        mBuffer.flip();
        Timber.v("Output format determined, writing %d samples / %d bytes to muxer.",
            mSampleInfoList.size(), mBuffer.limit());
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int offset = 0;
        for (SampleInfo sampleInfo : mSampleInfoList) {
            sampleInfo.writeToBufferInfo(bufferInfo, offset);
            mMuxer.writeSampleData( getTrackIndex( sampleInfo.mSampleType ), mBuffer, bufferInfo );
            offset += sampleInfo.mSize;
        }
        mSampleInfoList.clear();
        mBuffer = null;
    }

    private boolean isFormatSetup() {

        if (mVideoFormat == null || mAudioFormat == null) return false;
        mVideoTrack = mMuxer.addTrack(mVideoFormat);
        Timber.d("Added track #%d with %s to muxer.", mVideoTrack, mVideoFormat.getString(MediaFormat.KEY_MIME));
        mAudioTrack = mMuxer.addTrack(mAudioFormat);
        Timber.d("Added track #%d with %s to muxer.", mAudioTrack, mAudioFormat.getString(MediaFormat.KEY_MIME));
        return true;

//        switch ( CURRENT_MODE ) {
//            case MediaEditor.VIDEO_ONLY :
//                if (mVideoFormat == null) return false;
//                mVideoTrack = mMuxer.addTrack(mVideoFormat);
//                Log.d(TAG, "Added track #" + mVideoTrack + " with " + mVideoFormat.getString(MediaFormat.KEY_MIME) + " to muxer");
//                break;
//            case MediaEditor.AUDIO_ONLY :
//                if (mAudioFormat == null) return false;
//                mAudioTrack = mMuxer.addTrack(mAudioFormat);
//                Log.v(TAG, "Added track #" + mAudioTrack + " with " + mAudioFormat.getString(MediaFormat.KEY_MIME) + " to muxer");
//                break;
//            case MediaEditor.NORMAL :
//                if (mVideoFormat == null || mAudioFormat == null) return false;
//                mVideoTrack = mMuxer.addTrack(mVideoFormat);
//                Log.d(TAG, "Added track #" + mVideoTrack + " with " + mVideoFormat.getString(MediaFormat.KEY_MIME) + " to muxer");
//                mAudioTrack = mMuxer.addTrack(mAudioFormat);
//                Log.v(TAG, "Added track #" + mAudioTrack + " with " + mAudioFormat.getString(MediaFormat.KEY_MIME) + " to muxer");
//                break;
//            default :
//                throw new IllegalStateException( "not video or audio mode selected! " );
//                if (mVideoFormat == null || mAudioFormat == null) return false;
//                mVideoTrack = mMuxer.addTrack(mVideoFormat);
//                Log.d(TAG, "Added track #" + mVideoTrack + " with " + mVideoFormat.getString(MediaFormat.KEY_MIME) + " to muxer");
//                mAudioTrack = mMuxer.addTrack(mAudioFormat);
//                Log.v(TAG, "Added track #" + mAudioTrack + " with " + mAudioFormat.getString(MediaFormat.KEY_MIME) + " to muxer");
//                break;
//        }
//        return true;
    }

    private long calculatePresentationTime ( SampleType type, long receivedTimeUs ) {
        long tmpUs;
        if ( type == SampleType.VIDEO ) {
            // NOTE When another media inserted
            if ( Math.abs( receivedTimeUs - mVideoLastPresentationTimeUs ) > mVideoTimeGapThreshold ) {
                tmpUs = mVideoPresentationTimeUs + mVideoDefaultTimeGap;
                Timber.d("writeSampleData: -------------------------------------------------------- added default Time gap to Video" );

            }
            else {
                tmpUs = mVideoPresentationTimeUs + Math.abs( receivedTimeUs - mVideoLastPresentationTimeUs);
            }
            mVideoLastPresentationTimeUs = receivedTimeUs;
            mVideoPresentationTimeUs = tmpUs;
            return tmpUs;
        } else {
            // NOTE When another media inserted
            if ( Math.abs( receivedTimeUs - mAudioLastPresentationTimeUs ) > mAudioTimeGapThreshold ) {
                tmpUs = mAudioPresentationTimeUs + mAudioDefaultTimeGap;
                Timber.d("writeSampleData: -------------------------------------------------------- added default Time gap to Audio" );
            }
            else {
                tmpUs = mAudioPresentationTimeUs + Math.abs( receivedTimeUs - mAudioLastPresentationTimeUs );
            }
            mAudioLastPresentationTimeUs = receivedTimeUs;
            mAudioPresentationTimeUs = tmpUs;
            return tmpUs;
        }

    }

    public long writeSampleData(SampleType type, ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo ) {

        long presentationTimeUs = calculatePresentationTime( type, bufferInfo.presentationTimeUs );
        Timber.v("writeSampleData: %s ... %ld ... source ? %ld / %d / %d",
            type.toString(), presentationTimeUs, bufferInfo.presentationTimeUs, byteBuffer.remaining(), byteBuffer.capacity());
        bufferInfo.set(bufferInfo.offset, bufferInfo.size, presentationTimeUs, bufferInfo.flags);
        if (mMuxerStarted) {
            mMuxer.writeSampleData( getTrackIndex(type), byteBuffer, bufferInfo );
            return presentationTimeUs ;
        }
        byteBuffer.limit( bufferInfo.offset + bufferInfo.size );
        byteBuffer.position(bufferInfo.offset);
        if ( mBuffer == null ) mBuffer = ByteBuffer.allocate(BUFFER_SIZE).order(ByteOrder.nativeOrder());
        mBuffer.put( byteBuffer );
        mSampleInfoList.add(new SampleInfo( type, bufferInfo.size, bufferInfo ));
        return presentationTimeUs;

    }

    private int getTrackIndex( SampleType type ) {
        switch ( type ) {
            case VIDEO : return mVideoTrack;
            case AUDIO : return mAudioTrack;
            default : throw new AssertionError();
        }
    }

    public void setOrientation( int rotation ) {
        mMuxer.setOrientationHint( rotation );
    }


    public void stop() {
        Timber.v("stop: %s", mMuxerStarted ? "true" : "false");
        mMuxer.stop();
        mMuxerStarted = false;
    }

    public void release() {
        if (mMuxerStarted) stop();
        mMuxer.release();
    }

    public boolean isStopped() {
        return !mMuxerStarted;
    }


    public long getVideoPresentationTimeUs() {
        return mVideoPresentationTimeUs;
    }

    public long getAudioPresentationTimeUs() {
        return mAudioPresentationTimeUs;
    }


    public enum SampleType { VIDEO, AUDIO }

    private static class SampleInfo {
        private final SampleType mSampleType;
        private final int mSize;
        private final long mPresentationTimeUs;
        private final int mFlags;

        private SampleInfo(SampleType sampleType, int size, MediaCodec.BufferInfo info) {
            mSampleType = sampleType;
            mSize = size;
            mPresentationTimeUs = info.presentationTimeUs;
            mFlags = info.flags;
        }

        private void writeToBufferInfo(MediaCodec.BufferInfo info, int offset) {
            info.set(offset, mSize, mPresentationTimeUs, mFlags);
        }
    }

}
