package com.estsoft.muvicam.data.local;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import com.estsoft.muvicam.model.Video;
import com.estsoft.muvicam.util.CursorObservable;

import rx.Observable;
import timber.log.Timber;


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

  public void initColumnIndex(Cursor cursor) {
    mIdColumn = cursor.getColumnIndex(MediaStore.Video.Media._ID);
    mPathColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
    mHeightColumn = cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT);
    mWidthColumn = cursor.getColumnIndex(MediaStore.Video.Media.WIDTH);
    mDurationColumn = cursor.getColumnIndex(MediaStore.Video.Media.DURATION);
  }

  public Observable<Video> getVideos() {

    Cursor videoCursor = mContext.getContentResolver().query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        null, null, null, null
    );
    initColumnIndex(videoCursor);

    if (videoCursor == null || !videoCursor.moveToFirst()) {
      return null;
    }

    return CursorObservable.create(videoCursor, false)
        .filter(this::isValid)
        .map(cursor -> Video.builder()
            .setUri(getUri(cursor))
            .setDuration(getDuration(cursor))
            .setWidth(getWidth(cursor))
            .setHeight(getHeight(cursor))
            .setThumbnail(getThumbnail(cursor))
            .build())
        .doOnCompleted(videoCursor::close);
  }

  private boolean isValid(Cursor cursor) {
    return Integer.parseInt(cursor.getString(mDurationColumn)) < 180000;
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

  public Bitmap getThumbnail(Cursor cursor) {
    long id = cursor.getLong(mIdColumn);
    Timber.e("PATH : %s", id);
    Bitmap bmp = MediaStore.Video.Thumbnails.getThumbnail(
        mContext.getContentResolver(),
        id, MediaStore.Video.Thumbnails.MINI_KIND,
        null);

    return bmp;
  }



}