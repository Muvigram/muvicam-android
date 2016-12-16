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

    return CursorObservable.create(musicCursor)
        .filter(this::isEnoughDuration)
        .map(cursor -> {
          Uri uri = getUri(cursor);
          String title = getTitle(cursor);
          String artist = getArtist(cursor);
          Bitmap thumbnail = getThumbnail(cursor);
          return Music.builder()
              .setUri(uri)
              .setTitle(title)
              .setArtist(artist)
              .setThumbnail(thumbnail)
              .build();
        });
  }

  public boolean isEnoughDuration(Cursor cursor) {
    return Integer.parseInt(cursor.getString(mDurationColumn)) > 15000;
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
