package com.estsoft.muvicam.data.local;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;

import com.estsoft.muvicam.model.Music;
import com.estsoft.muvicam.util.CursorObservable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Data service that finds musics from local drive and then provides them to presenter.
 * This data service will be enclosed in {@link com.estsoft.muvicam.data.DataManager},
 * which is the interface for presenter to make their data-based businesses.
 * <p/>
 * Created by jaylim on 12/13/2016.
 */
public class MusicService {

  private Context mContext;

  public MusicService(Context context) {
    mContext = context;
  }

  private int mPathColumn;
  private int mTitleColumn;
  private int mArtistColumn;
  private int mDurationColumn;

  public void initColumnIndex(Cursor cursor) {
    mPathColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
    mTitleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
    mArtistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
    mDurationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
  }

  public Observable<Music> getMusics() {

    Cursor musicCursor = mContext.getContentResolver().query(
        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        null, null, null, null
    );
    initColumnIndex(musicCursor);

    if (musicCursor == null || !musicCursor.moveToFirst()) {
      return null;
    }

    return CursorObservable.create(musicCursor, false)
        .onBackpressureBuffer(128) // for "MissingBackPressureException"
        .filter(this::isEnoughDuration)
        .map(cursor -> Music.builder()
            .setUri(getUri(cursor))
            .setTitle(getTitle(cursor))
            .setArtist(getArtist(cursor))
            .setThumbnail(getThumbnail(cursor))
            .build())
        .doOnCompleted(musicCursor::close);
  }

  public boolean isEnoughDuration(Cursor cursor) {
    return Long.parseLong(cursor.getString(mDurationColumn)) > 15000L;
  }

  public Uri getUri(Cursor cursor) {
    return Uri.parse(cursor.getString(mPathColumn));
  }

  public String getTitle(Cursor cursor) {
    return cursor.getString(mTitleColumn);
  }

  public String getArtist(Cursor cursor) {
    return cursor.getString(mArtistColumn);
  }

  public Bitmap getThumbnail(Cursor cursor) {
    MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
    metaRetriever.setDataSource(cursor.getString(mPathColumn));
    byte[] art = metaRetriever.getEmbeddedPicture();
    if (art == null) {
      return null;
    }
    return BitmapFactory.decodeByteArray(art, 0, art.length);
  }
}
