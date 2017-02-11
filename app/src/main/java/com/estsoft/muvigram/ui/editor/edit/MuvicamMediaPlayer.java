package com.estsoft.muvigram.ui.editor.edit;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import timber.log.Timber;

/**
 * Created by estsoft on 2017-01-11.
 */

public class MuvicamMediaPlayer extends MediaPlayer {
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
        Timber.v("seekTo: requested ... %d", i * MICRO_TO_MILLI );
        long userSeek = getClosestPositionInIFrames( i );
        Timber.v("seekTo: resulted ... %d", userSeek);
        long ceilingMilliSec = (long)Math.ceil( userSeek / (double) MICRO_TO_MILLI );
        Timber.v("seekTo: roundedMilliSec ... %d", ceilingMilliSec);
        Timber.d("seekTo: seetTest1 request ... %d", i);
        Timber.d("seekTo: seekTest1 %d", ceilingMilliSec);
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
                Timber.v("setupIFrameInformation: %d", mExtractor.getSampleTime());
            }
            mExtractor.advance();
        }
        long delayedTime = System.currentTimeMillis() - startTime;
        Timber.d("%s: I-Frame Time extracting ended in %f seconds.",getClass().getSimpleName(), ((float)delayedTime / MICRO_TO_MILLI));
        Timber.d("%s: I-Frame Time total List Size is %d",getClass().getSimpleName(), IFrameMarkers.size() );
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

