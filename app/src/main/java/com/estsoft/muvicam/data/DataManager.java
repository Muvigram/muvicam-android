package com.estsoft.muvicam.data;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.estsoft.muvicam.data.local.MusicService;
import com.estsoft.muvicam.data.local.VideoService;
import com.estsoft.muvicam.model.Music;
import com.estsoft.muvicam.model.Video;

import java.util.List;

import rx.Observable;

/**
 * Created by jaylim on 12/13/2016.
 */

@Singleton
public class DataManager {

  MusicService mMusicService;
  VideoService mVideoService;

  @Inject
  public DataManager(MusicService musicService, VideoService videoService) {
    mMusicService = musicService;
    mVideoService = videoService;
  }

  // TODO - Which one is fast?

  public Observable<Music> getMusics() {
    return mMusicService.getMusics();
  }


  public Observable<List<Video>> getVideos() {
    return mVideoService.getVideos().toList();
  }
}
