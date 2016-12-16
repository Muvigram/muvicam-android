package com.estsoft.muvicam.data;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.estsoft.muvicam.data.local.MusicService;
import com.estsoft.muvicam.model.Music;
import rx.Observable;

/**
 * Created by jaylim on 12/13/2016.
 */

@Singleton
public class DataManager {

  MusicService mMusicService;

  @Inject
  public DataManager(MusicService musicService) {
    mMusicService = musicService;
  }

  public Observable<Music> getMusics() {
    return mMusicService.getMusics();
  }

}
