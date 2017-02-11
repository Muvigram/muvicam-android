package com.estsoft.muvigram.transcoder.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import com.estsoft.muvigram.transcoder.transcoders.VideoTrackDecoder;

import timber.log.Timber;

/**
 * Created by estsoft on 2016-12-09.
 */

public class ThumbnailUtil {
    private static final long US_WEIGHT = 1000000;

    private VideoTrackDecoder mDecoder;
    private MediaExtractor mExtractor;
    private Activity mAct;
    private long mIntervalUs;
    private int mWidth;
    private int mHeight;
    private boolean isStarted;
    private boolean isIFrameMode;

    private boolean threadKiller;

    private BitmapHandlerImpl mBitmapListener;
    private UserBitmapListener userBitmapListener;

    public ThumbnailUtil(UserBitmapListener userBitmapListener, Activity act, boolean iFrameExtractingMode) {
        this.userBitmapListener = userBitmapListener;
        this.isIFrameMode = iFrameExtractingMode;
        this.mAct = act;
        Timber.d("ThumbnailUtil: %s", mAct.toString());
    }

    public synchronized void release() {
        threadKiller = true;
    }


//    public void extract(final String filePath, final double intervalSec, final int width, final int height ) {
//        if (isStarted) throw new IllegalStateException( "Already started!" );
//        isStarted = true;
//        extractingStart(filePath, intervalSec, width, height);
//    }

    public void extractFromNewThread(final String filePath, final double intervalSec, final int width, final int height ) {
        if (isStarted) throw new IllegalStateException( "Already started!" );
        isStarted = true;
        new Thread( () -> {
                extractingStart(filePath, intervalSec, width, height);
        }).start();
    }


    public void extractingStart(final String filePath, final double intervalSec, final int width, final int height ) {
        mExtractor = new MediaExtractor();
        try {
            mExtractor.setDataSource(filePath);
        } catch ( Exception e ) {
            userBitmapListener.onError( e );
        }
        mIntervalUs = (long)(intervalSec * US_WEIGHT);
        mWidth = width;
        mHeight = height;
        setup();
        //start of extracting
        runPipeline();
    }

    private void runPipeline(){
        while (!mDecoder.isFinished() && !threadKiller) {
            boolean stepped = mDecoder.stepPipeline();
            if (!stepped) {
                try {
                    Thread.sleep(20);
                } catch ( Exception e ) { e.printStackTrace(); }
            }
        }
    }

    private void setup() {
        mBitmapListener = new BitmapHandlerImpl();

        MediaFormat format = MediaFormat.createVideoFormat( "video/avc", mWidth, mHeight );
        format.setInteger( MediaFormat.KEY_BIT_RATE, 2000000 );
        format.setInteger( MediaFormat.KEY_FRAME_RATE, 30);
        format.setInteger( MediaFormat.KEY_I_FRAME_INTERVAL, 5);
        format.setInteger( MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        int tmpVideoTrackIndex = TranscodeUtils.getFirstTrackIndex(mExtractor, TranscodeUtils.MODE_VIDEO );
        mDecoder = new VideoTrackDecoder( mExtractor, tmpVideoTrackIndex, format, mIntervalUs, mBitmapListener, isIFrameMode);
        mDecoder.setup();
    }


    public interface UserBitmapListener {
        void onBitmapNext(Bitmap bitmap, long presentationTimeUs, boolean isLast);
        void onComplete( long totalUs );
        void onError(Exception e);
    }

    private class BitmapHandlerImpl implements VideoTrackDecoder.BitmapListener {

        @Override
        public void onBitmapSupply(Bitmap bitmap, long presentationTime, boolean isEnd ) {
            if (threadKiller) return;
                mAct.runOnUiThread(() -> {
                    userBitmapListener.onBitmapNext(bitmap, presentationTime, isEnd);
                });
        }

        @Override
        public void onComplete( long totalUs ) {
            if (threadKiller) return;
            Timber.v("onComplete: End of Task");
            userBitmapListener.onComplete( totalUs );
            mDecoder.release();
        }
    }

}
