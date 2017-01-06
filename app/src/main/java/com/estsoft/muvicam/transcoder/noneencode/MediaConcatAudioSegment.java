package com.estsoft.muvicam.transcoder.noneencode;

import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import com.estsoft.muvigram.transcoder.utils.TranscodeUtils;

import java.io.IOException;

/**
 * Created by estsoft on 2017-01-04.
 */

public class MediaConcatAudioSegment {
    private static final String TAG = "MediaConcatAudioSegment";
    public static final String AAC_MIME_TYPE = "audio/mp4a-latm";

    private AudioTrackTranscoder mAudioTranscoder;
    private MediaExtractor mExtractor;
    private MediaFormat mAudioFormat;
    private BufferListener mBufferListener;
    private String mInputFilePath;
    private long mStartOffsetUs;
    private long mActualFirstExtractedTimeUs = -1251;
    private int mAudioVolume;
    private int mAudioTrackIndex;

    public MediaConcatAudioSegment(BufferListener bufferListener, String inputFilePath, long startOffsetUs, int audioVolume) {
        this.mBufferListener = bufferListener;
        this.mInputFilePath = inputFilePath;
        this.mExtractor = new MediaExtractor();
        try {
            mExtractor.setDataSource(mInputFilePath);
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
        this.mStartOffsetUs = startOffsetUs;
        this.mAudioVolume = audioVolume;
        setupAudioTranscoder();
    }

    public void prepare() {
        mExtractor.seekTo( mStartOffsetUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        mExtractor.advance();
        Log.d(TAG, "prepare: " + mExtractor.getSampleTime());
        mActualFirstExtractedTimeUs = mExtractor.getSampleTime();
//        mExtractor.seekTo( mStartOffsetUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
//        mExtractor.advance();
        mAudioTranscoder.encodeStart();
    }

    public boolean stepOnce() {
        boolean busy = mAudioTranscoder.stepPipeline();
        return busy;
    }

    public void release() {
        mExtractor.release();
        mAudioTranscoder.release();
    }

    private void setupAudioTranscoder() {
        MediaFormat inputFormat = TranscodeUtils.getFirstTrack( mExtractor, TranscodeUtils.MODE_AUDIO );
        mAudioFormat = MediaFormat.createAudioFormat( AAC_MIME_TYPE,
                inputFormat.getInteger( MediaFormat.KEY_SAMPLE_RATE ),
                inputFormat.getInteger( MediaFormat.KEY_CHANNEL_COUNT));
        String bitrate = inputFormat.getString( MediaFormat.KEY_BIT_RATE );
        if (bitrate == null) bitrate = String.valueOf( 128 * 1000 );
        mAudioFormat.setInteger( MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC );
        mAudioFormat.setInteger( MediaFormat.KEY_BIT_RATE, Integer.parseInt(bitrate) );
        mAudioTrackIndex = TranscodeUtils.getFirstTrackIndex(mExtractor, TranscodeUtils.MODE_AUDIO);
        mAudioTranscoder = new AudioTrackTranscoder( mExtractor, mAudioFormat, mBufferListener, mAudioTrackIndex, mAudioVolume );
        mAudioTranscoder.setup();
    }

    public MediaFormat getAudioFormat() {
        return mAudioFormat;
    }
    public long getExtractedTimeUs() {
        return mAudioTranscoder.getExtractedPresentationTimeUs();
    }
    public long getActualFirstExtractedTimeUs(){
        return mActualFirstExtractedTimeUs;
    }
    public long getAudioCurrentWrittenTimeUs() {
        return mAudioTranscoder.getWrittenPresentationTimeUs();
    }
    public boolean isFinished() {
        return mAudioTranscoder.isFinished();
    }

}
