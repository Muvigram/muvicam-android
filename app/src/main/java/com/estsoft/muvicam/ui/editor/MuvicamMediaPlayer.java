package com.estsoft.muvicam.ui.editor;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by estsoft on 2017-01-11.
 */

public class MuvicamMediaPlayer extends MediaPlayer {
    private static final String TAG = "MuvicamMediaPlayer";
    private static final boolean VERBOSE = true;
    private static final boolean asyncFrameInfoExtracting = true;

    private final String VIDEO = "video/";
    private final int MICRO_TO_MILLI = 1000;

    private MediaExtractor mExtractor;
    private ExecutorService mThreadExecutor;

    private String mDataSource;
    private FileDescriptor mDataSourceD;
    private List<Long> IFrameMarkers;

    private boolean IFrameInfoPrepared;

    @Override
    public void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        this.mDataSource = path;
        super.setDataSource(path);
    }


    @Override
    public void setDataSource(FileDescriptor fd) throws IOException, IllegalArgumentException, IllegalStateException {
        this.mDataSourceD = fd;
        super.setDataSource(fd);
    }

    @Override
    public void prepare() throws IOException, IllegalStateException {
        if (!IFrameInfoPrepared) {
            if (asyncFrameInfoExtracting) {
                IFrameInfoPrepared = true;
                mThreadExecutor = Executors.newSingleThreadExecutor();
                mThreadExecutor.submit(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        setupIFrameInformation();
                        return null;
                    }
                });
            } else {
                IFrameInfoPrepared = true;
                setupIFrameInformation();
            }
        }
        super.prepare();
    }

    @Override
    public void seekTo(int i) throws IllegalStateException {
        if (VERBOSE) Log.d(TAG, "seekTo: requested ... " + i * MICRO_TO_MILLI );
        long userSeek = getClosestPositionInIFrames( i );
        if (VERBOSE) Log.d(TAG, "seekTo: resulted ... " + userSeek);
        long ceilingMilliSec = (long)Math.ceil( userSeek / (double) MICRO_TO_MILLI );
        if (VERBOSE) Log.d(TAG, "seekTo: roundedMilliSec ... " + ceilingMilliSec);
        super.seekTo( (int) ceilingMilliSec);
    }

    private long getClosestPositionInIFrames( int requestMS ) {
        long requestedUs = requestMS * MICRO_TO_MILLI;
        if (IFrameMarkers == null) return 0;
        if ( requestedUs <= 0 ) return 0;
        if ( requestedUs >= IFrameMarkers.get( IFrameMarkers.size() -1 ))
            return IFrameMarkers.get( IFrameMarkers.size() - 1);

        int position ;
        for ( position = 0 ; position < IFrameMarkers.size(); position ++ ) {
            if ( requestedUs < IFrameMarkers.get( position ) ) break;
        }
        long result;
        long left = requestedUs - IFrameMarkers.get(position - 1);
        long right = IFrameMarkers.get( position ) - requestedUs;
        result = left > right ? IFrameMarkers.get(position) : IFrameMarkers.get( position - 1 );
        return result;
    }

    private void setupIFrameInformation() throws IOException {
        long startTime = System.currentTimeMillis();
        IFrameMarkers = new ArrayList<>();
        mExtractor = new MediaExtractor();
        if (mDataSource == null) {
            mExtractor.setDataSource(mDataSourceD);
        } else {

            mExtractor.setDataSource(mDataSource);
        }
        mExtractor.selectTrack( getVideoTrack() );

        while ( mExtractor.getSampleTime() >= 0 ) {
            if ( mExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC) {
                IFrameMarkers.add( (mExtractor.getSampleTime()) );
                if (VERBOSE) Log.d(TAG, "setupIFrameInformation: " + mExtractor.getSampleTime());
            }
            mExtractor.advance();
        }
        long delayedTime = System.currentTimeMillis() - startTime;
        Log.e(TAG, getClass().getSimpleName() + ": I-Frame Time extracting ended in \t" + ((float)delayedTime / MICRO_TO_MILLI) + " seconds.");
        Log.e(TAG, getClass().getSimpleName() + ": I-Frame Time total List Size is \t" + IFrameMarkers.size() );
        mExtractor.release();
    }
    private int getVideoTrack() throws IllegalStateException {
        int trackCount = mExtractor.getTrackCount();
        for (int i = 0; i < trackCount; i ++) {
            MediaFormat format = mExtractor.getTrackFormat( i );
            if (format.getString( MediaFormat.KEY_MIME ).startsWith(VIDEO)) {
                return i;
            }
        }
        throw new IllegalStateException( "No Video Track in " + mDataSource );
    }

}

