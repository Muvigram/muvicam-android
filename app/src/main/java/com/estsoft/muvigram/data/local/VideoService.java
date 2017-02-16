package com.estsoft.muvigram.data.local;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import com.estsoft.muvigram.model.Video;
import com.estsoft.muvigram.util.CursorObservable;

import rx.Observable;


/**
 *
 * Created by jaylim on 12/01/2017.
 */

public class VideoService {

  private Context mContext;

  public VideoService(Context context) {
    mContext = context;

  }

  private int mIdColumn;
  private int mPathColumn;
  private int mHeightColumn;
  private int mWidthColumn;
  private int mDurationColumn;
  private int mResolutionColumn;

  public void initColumnIndex(Cursor cursor) {
    mIdColumn = cursor.getColumnIndex(MediaStore.Video.Media._ID);
    mPathColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
    mHeightColumn = cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT);
    mWidthColumn = cursor.getColumnIndex(MediaStore.Video.Media.WIDTH);
    mDurationColumn = cursor.getColumnIndex(MediaStore.Video.Media.DURATION);
    mResolutionColumn = cursor.getColumnIndex(MediaStore.Video.Media.RESOLUTION);
  }

  public Observable<Video> getVideos() {

    Cursor videoCursor = mContext.getContentResolver().query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        null, null, null,
        MediaStore.Video.Media._ID + " DESC"
    );

    initColumnIndex(videoCursor);

    return CursorObservable.create(videoCursor)
        .filter(this::isSupported)
        .map(cursor -> Video.builder()
            .setId(getId(cursor))
            .setUri(getUri(cursor))
            .setDuration(getDuration(cursor))
            .setWidth(getWidth(cursor))
            .setHeight(getHeight(cursor))
            .build());
  }

  private boolean isSupported(Cursor cursor) {
    return isSupportedFormat(cursor) &&
           isSupportedLength(cursor) &&
           isSupportedResolution(cursor);
  }

  private boolean isSupportedFormat(Cursor cursor) {
    return cursor.getString(mPathColumn).endsWith(".mp4");
  }

  private boolean isSupportedLength(Cursor cursor) {
    int duration = Integer.parseInt(cursor.getString(mDurationColumn));
    return duration >= 1000 && duration < 180000;
  }

  private boolean isSupportedResolution(Cursor cursor) {
    String resolution = cursor.getString(mResolutionColumn);
    String[] dimen = resolution.split("x");
    int x = Integer.parseInt(dimen[0]);
    int y = Integer.parseInt(dimen[1]);

    return Math.min(x, y) <= 1080;
  }

  public long getId(Cursor cursor) {
    return cursor.getLong(mIdColumn);
  }

  public Uri getUri(Cursor cursor) {
    return Uri.parse(cursor.getString(mPathColumn));
  }

  public int getDuration(Cursor cursor) {
    return cursor.getInt(mDurationColumn);
  }

  public int getWidth(Cursor cursor) {
    return cursor.getInt(mWidthColumn);
  }

  public int getHeight(Cursor cursor) {
    return cursor.getInt(mHeightColumn);
  }

}
