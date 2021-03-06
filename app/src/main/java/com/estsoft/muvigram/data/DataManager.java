package com.estsoft.muvigram.data;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.estsoft.muvigram.data.local.MusicService;
import com.estsoft.muvigram.data.local.VideoService;
import com.estsoft.muvigram.model.Music;
import com.estsoft.muvigram.model.Video;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import rx.Observable;

/**
 * Created by jaylim on 12/13/2016.
 */

@Singleton
public class DataManager {

  private MusicService mMusicService;
  private VideoService mVideoService;

  @Inject
  public DataManager(MusicService musicService, VideoService videoService) {
    mMusicService = musicService;
    mVideoService = videoService;
  }

  // TODO - Which one is fast?

  public Observable<List<Music>> getMusics(@Nullable CharSequence text) {
    String str = text == null ? "" : text.toString();
    String[] tokens = str.split("[ \\t\\n\\u000B\\f\\r]+");

    return mMusicService.getMusics()
        .filter(music -> filterOutMusics(music, tokens)) // Filtering
        .buffer(500, TimeUnit.MILLISECONDS);
  }

  public Observable<List<Video>> getVideos() {
    return mVideoService.getVideos()
        .buffer(200, TimeUnit.MILLISECONDS);
  }

  private static boolean filterOutMusics(Music music, String[] tokens) {
    String title = music.title().toLowerCase(Locale.US);
    String artist = music.artist().toLowerCase(Locale.US);
    for (int i = 0; i < tokens.length; i++) {
      String token = tokens[i].toLowerCase(Locale.US);
      if (title.contains(token) || artist.contains(token)) {
        return true;
      }
    }
    return false;
  }
}
