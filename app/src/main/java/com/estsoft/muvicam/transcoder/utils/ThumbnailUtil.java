package com.estsoft.muvicam.transcoder.utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.provider.MediaStore;
import android.util.Log;

import com.estsoft.muvicam.transcoder.transcoders.VideoTrackDecoder;

import java.io.IOException;
import java.util.List;

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
    private Activity mAct;
    private long mIntervalUs;
    private int mWidth;
    private int mHeight;
    private boolean isStarted;
    private boolean isIFrameMode;

    private BitmapHandlerImpl mBitmapListener;
    private UserBitmapListener userBitmapListener;

    public ThumbnailUtil(UserBitmapListener userBitmapListener, Activity act, boolean iFrameExtractingMode) {
        this.userBitmapListener = userBitmapListener;
        this.isIFrameMode = iFrameExtractingMode;
        this.mAct = act;
        Log.d(TAG, "ThumbnailUtil: " + mAct.toString());
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
        void onComplete();
        void onError(Exception e);
    }

    private class BitmapHandlerImpl implements VideoTrackDecoder.BitmapListener {

        @Override
        public void onBitmapSupply(Bitmap bitmap, long presentationTime, boolean isEnd ) {
            mAct.runOnUiThread(() -> {
                    userBitmapListener.onBitmapNext( bitmap, presentationTime, isEnd );
            });
        }

        @Override
        public void onComplete() {
            if (VERBOSE) Log.d(TAG, "onComplete: End of Task");
            userBitmapListener.onComplete();
            mDecoder.release();
        }
    }

    //code From Inkiu
    public static void getThumbnails(List<String> videoPaths, Context context, VideoMetaDataListener listener) {

        int count = 0;

        String[] projection = new String[]{
                MediaStore.MediaColumns._ID,
                MediaStore.Video.VideoColumns.DURATION,
                MediaStore.Video.VideoColumns.HEIGHT,
                MediaStore.Video.VideoColumns.WIDTH,
                MediaStore.Video.VideoColumns.RESOLUTION};
        String selection = MediaStore.MediaColumns.DATA + "=?";

        for (String path : videoPaths) {
            if (!path.endsWith("mp4")) listener.onError(new IOException("file is not mp4."));
            int imageId = -1;
            long duration = -1;
            int width = -1;
            int height = -1;
            String[] selectArgs = new String[]{path};
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    selectArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                imageId = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION));
                height = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.VideoColumns.HEIGHT));
                width =  cursor.getInt(cursor.getColumnIndex(MediaStore.Video.VideoColumns.WIDTH));
            } else {
                listener.onError(new IOException("query failed."));
            }
            cursor.close();

            Bitmap thumbnail = MediaStore.Video.Thumbnails.getThumbnail(
                    context.getContentResolver(),
                    imageId,
                    MediaStore.Images.Thumbnails.MINI_KIND,
                    null);

            VideoMetaData metaData = new VideoMetaData(thumbnail, width, height, duration, (int) duration / 1000, path, count++);
            listener.onProgress(metaData);
        }
        listener.onComplete();
    }

    //code From Inkiu
    public static class VideoMetaData {
        public Bitmap thumbnailBitmap;
        public String videoPath;
        public long durationMs;
        public int durationSec;
        public int position;
        public int width;
        public int height;

        private VideoMetaData(Bitmap thumbnailBitmap, int width, int height, long durationMs, int durationSec, String videoPath, int position) {
            this.thumbnailBitmap = thumbnailBitmap;
            this.width = width;
            this.height = height;
            this.durationMs = durationMs;
            this.durationSec = durationSec;
            this.videoPath = videoPath;
            this.position = position;
        }
    }
    //code From Inkiu
    public interface VideoMetaDataListener {
        void onProgress(VideoMetaData data);

        void onComplete();

        void onError(Exception e);
    }

}
