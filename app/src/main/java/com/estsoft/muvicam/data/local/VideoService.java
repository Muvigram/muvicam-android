package com.estsoft.muvicam.data.local;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;

import com.estsoft.muvicam.util.CursorObservable;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by estsoft on 2017-01-25.
 */

public class VideoService {
    private static final String TAG = "VideoService";
    private static final String ASC = " ASC";
    private static final String DESC = " DESC";

    private Context mContext;

    public VideoService( Context context ) {
        mContext = context;
    }

    private int mPathIndex;
    private int mImageIdIndex;
    private int mDurationIndex;
    private int mWidthIndex;
    private int mHeghtIndex;
    private int mResolutionIndex;

    private boolean stopped;

    private void initCursorIndex( Cursor cursor ) {
        mPathIndex = cursor.getColumnIndex( MediaStore.MediaColumns.DATA );
        mImageIdIndex = cursor.getColumnIndex( MediaStore.MediaColumns._ID );
        mDurationIndex = cursor.getColumnIndex( MediaStore.Video.VideoColumns.DURATION );
        mWidthIndex = cursor.getColumnIndex( MediaStore.Video.VideoColumns.WIDTH );
        mHeghtIndex = cursor.getColumnIndex( MediaStore.Video.VideoColumns.HEIGHT );
        mResolutionIndex = cursor.getColumnIndex( MediaStore.Video.VideoColumns.RESOLUTION );
    }

    public Observable<VideoMetaData> getVideos( boolean idOrderToDESC ) {

        String order;
        if (idOrderToDESC) order = DESC;
        else order = ASC;

            Cursor cursor = mContext.getContentResolver().query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    null, null, null,
                    MediaStore.Video.Media._ID + order
                    , null
            );

            initCursorIndex( cursor );
            if (cursor == null || !cursor.moveToFirst()) {
                return null;
            }
        return CursorObservable.create(cursor, true)
                .doOnUnsubscribe( this::stopExtracting )
                .doOnSubscribe( this::init )
                .doOnCompleted( this::init )
                .filter( this::isValid )
                .map(cursor1 -> getVideoMetadata(cursor1) );
    }

    private void init() {
        stopped = false;
    }
    private void stopExtracting() {
        stopped = true;
    }

    private VideoMetaData getVideoMetadata( Cursor cursor ) {
        if ( this.stopped ) return null;
        Log.d( TAG, "getVideoMetadata: extracted " + cursor.getString(mPathIndex) );
        return new VideoMetaData(
                getThumbnailBitmap( cursor.getInt(mImageIdIndex), mContext ),
                cursor.getInt(mWidthIndex),
                cursor.getInt(mHeghtIndex),
                cursor.getInt(mDurationIndex),
                cursor.getInt(mDurationIndex) / 1000,
                cursor.getString(mPathIndex),
                0
        );
    }

    private boolean isValid( Cursor cursor ) {
        String path = cursor.getString( mPathIndex );
        return path.endsWith(".mp4") && !path.contains("camera_video_test");
    }

    private Bitmap getThumbnailBitmap( int imageId, Context context ) {
        return MediaStore.Video.Thumbnails.getThumbnail(
                context.getContentResolver(),
                imageId,
                MediaStore.Images.Thumbnails.MINI_KIND,
                null);
    }

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

        @Override
        public String toString() {
            return videoPath + " - " + durationMs + " = " + durationSec + " - " + position + " " + width + " . " + height;
        }
    }

}
