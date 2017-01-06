package com.estsoft.muvicam.transcoder.utils;

import android.graphics.Bitmap;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import com.estsoft.muvigram.transcoder.transcoders.VideoTrackDecoder;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by estsoft on 2016-12-09.
 */

public class ThumbnailUtil {
    private static final String TAG = "ThumbnailUser";
    private static final boolean VERBOSE = false;
    private static final long US_WEIGHT = 1000000;

    private VideoTrackDecoder mDecoder;
    private MediaExtractor mExtractor;
    private long mIntervalUs;
    private int mWidth;
    private int mHeight;
    private boolean isStarted;
    private boolean isIFrameMode;

    private BitmapHandlerImpl mBitmapListener;
    private PublishSubject<BitmapSignal> mPublishSubject;
    private Subscription mSubscription;
    private UserBitmapListener userBitmapListener;

    public ThumbnailUtil(UserBitmapListener userBitmapListener, boolean iFrameExtractingMode) {
        this.userBitmapListener = userBitmapListener;
        this.isIFrameMode = iFrameExtractingMode;
    }


    public void extract(final String filePath, final double intervalSec, final int width, final int height ) {
        if (isStarted) throw new IllegalStateException( "Already started!" );
        isStarted = true;
        extractingStart(filePath, intervalSec, width, height);
    }

    public void extractFromNewThread(final String filePath, final double intervalSec, final int width, final int height ) {
        if (isStarted) throw new IllegalStateException( "Already started!" );
        isStarted = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                extractingStart(filePath, intervalSec, width, height);
            }
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

        mSubscription = mPublishSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .subscribe(new Observer<BitmapSignal>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onNext(BitmapSignal signal) {
                        userBitmapListener.onBitmapNext( signal.bitmap, signal.presentationTimeUs, signal.lastOne );
                    }

                    @Override
                    public void onError(Throwable e) {
                        userBitmapListener.onError( new Exception( e ) );
                    }
                });
        //start of Subject
        runPipeline();
    }

    private void runPipeline(){
        while (!mDecoder.isFinished()) {
            boolean stepped = mDecoder.stepPipeline();
            if (!stepped) {
                try {
                    Thread.sleep(20);
                } catch ( Exception e ) { e.printStackTrace(); }
            }
        }
    }

    private void setup() {
        mPublishSubject = PublishSubject.create();
        mBitmapListener = new BitmapHandlerImpl( mPublishSubject );

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
        void onError(Exception e);
    }

    private class BitmapHandlerImpl implements VideoTrackDecoder.BitmapListener {

        private PublishSubject<BitmapSignal> subject;

        public BitmapHandlerImpl(PublishSubject<BitmapSignal> subject) {
            this.subject = subject;
        }

        @Override
        public void onBitmapSupply(Bitmap bitmap, long presentationTime, boolean isEnd ) {
            subject.onNext( new BitmapSignal(bitmap, presentationTime, isEnd ) );
        }

        @Override
        public void onComplete() {
            if (VERBOSE) Log.d(TAG, "onComplete: End of Task");
            mDecoder.release();
            subject.onCompleted();
            mSubscription.unsubscribe();
        }
    }

    private class BitmapSignal {
        public BitmapSignal(Bitmap bitmap, long presentationTimeUs, boolean lastOne) {
            this.bitmap = bitmap;
            this.lastOne = lastOne;
            this.presentationTimeUs = presentationTimeUs;
        }

        Bitmap bitmap;
        boolean lastOne;
        long presentationTimeUs;
    }

}
