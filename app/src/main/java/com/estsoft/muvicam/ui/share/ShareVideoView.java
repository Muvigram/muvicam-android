package com.estsoft.muvicam.ui.share;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.VideoView;

/**
 * Created by estsoft on 2017-01-23.
 */

public class ShareVideoView extends VideoView {
    private static final String TAG = "ShareVideoView";
    private final int WATCH_INTERVAL_STANDARD = 500;
    private final int WATCH_INTERVAL_BUSY = 50;
    private int WATCH_INTERVAL_MS = WATCH_INTERVAL_STANDARD;

    private MediaPlayer mPlayer;

    private int mPlayOffset;
    private int mPlayLimit;
    private Thread mRuntimeWatcher;
    private boolean mThreadRunning = false;

    @Override
    public void start() {
        super.start();
        if (mRuntimeWatcher != null) return;
        setupWatcher();
        mRuntimeWatcher.start();
    }

    @Override
    public void pause() {
        super.pause();
        if (isThreadAlive()) killThread();
        mRuntimeWatcher = null;
    }

    @Override
    public void stopPlayback() {
        super.stopPlayback();
        if (isThreadAlive()) killThread();
        mRuntimeWatcher = null;
    }

    public void setupReplay(int startTimeMs, int endTimeMs, MediaPlayer mp) {
        this.seekTo( startTimeMs );
        mPlayOffset = startTimeMs;
        mPlayLimit = endTimeMs;
        if ( mPlayer == null ) mPlayer = mp;
    }

    private void setupWatcher() {
        if ( !isThreadAlive() ) killThread();
        mRuntimeWatcher = null;
        mRuntimeWatcher = new Thread( () -> {
            makeThread();
           while( isThreadAlive() ) {
               sleepWhile( WATCH_INTERVAL_MS );
//               Log.d( TAG, "setupWatcher: " + Thread.currentThread().getName() + " / " + getCurrentPosition() + " / " + mPlayLimit );
               if ( getCurrentPosition() >= mPlayLimit ) {
                   seekTo( mPlayOffset );
                   WATCH_INTERVAL_MS = WATCH_INTERVAL_STANDARD;
               } else if ( getCurrentPosition() + WATCH_INTERVAL_STANDARD * 2 > mPlayLimit ){
                   WATCH_INTERVAL_MS = WATCH_INTERVAL_BUSY;
               }
           }
            killThread();
            Log.d(TAG, "setupWatcher: killed ... " + Thread.currentThread().getName() + " / "  + isThreadAlive() );
        });
    }

    private void sleepWhile(int ms ) {
        try {
            Thread.sleep( ms );
        } catch (InterruptedException e ) {
            e.printStackTrace();
        }
    }

    public synchronized boolean isThreadAlive() {
        return mThreadRunning;
    }
    private synchronized void killThread() {
        this.mThreadRunning = false;
    }
    private synchronized void makeThread() {
        this.mThreadRunning = true;
    }

    public ShareVideoView(Context context) {
        super(context);
    }

    public ShareVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShareVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ShareVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

}
