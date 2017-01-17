package com.estsoft.muvicam.data.local;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;

import com.estsoft.muvicam.model.Video;
import com.estsoft.muvicam.util.CursorObservable;

import rx.Observable;

/**
 * Created by jaylim on 12/01/2017.
 */

public class VideoService {

  private Context mContext;

  public VideoService(Context context) {
    mContext = context;
  }
  private int mPathColumn;
  private int mHeightColumn;
  private int mWidthColumn;
  private int mDurationColumn;

  public void initColumnIndex(Cursor cursor) {
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
        .retry() // TODO - verify the effect.
        .map(cursor -> Video.builder()
            .setUri(getUri(cursor))
            .setDuration(getDuration(cursor))
            .setWidth(getWidth(cursor))
            .setHeight(getHeight(cursor))
            .setThumbnail(getThumbnail(cursor))
            .build());
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
    String path = cursor.getString(mPathColumn);
    return ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MICRO_KIND);
  }

}
