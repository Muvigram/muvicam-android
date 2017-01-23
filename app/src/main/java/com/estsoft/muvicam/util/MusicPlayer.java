package com.estsoft.muvicam.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.annotation.Nullable;

import com.estsoft.muvicam.model.Music;

import java.io.IOException;

import rx.Observable;
import timber.log.Timber;

/**
 * Customized music player. This class
 *  1. uses {@link MediaPlayer} to playing music.
 *  2. is designed to
 *    1) set and play music
 *    2) seek to a specific point
 *    3) to publish a current position as a {@link Observable} object.
 *
 * ** Caution **
 * There are some additional dependencies which are badly designed.
 * Additional dependencies //TODO - Resolve following unnecessary dependencies.
 *  1. Model object {@link Music}.
 *  2. silence_15_sec.mp3 file in assets folder.
 *
 * Created by jaylim on 21/01/2017.
 */

public class MusicPlayer {

  private Context mContext;

  public MusicPlayer(Context context) {
    mContext = context;
  }

  /* Music player */
  private Music mMusic = null;
  private MediaPlayer mPlayer;
  private int mMusicOffset = 0;

  public void updateMusic(@Nullable Music music) {
    // TODO - background
    stopPlayer();
    mMusic = music;
    mMusicOffset = 0;
    setUpPlayer();
  }

  public void cutMusic(int musicOffset) {
    pausePlayer();
    mMusicOffset = musicOffset;
    mPlayer.seekTo(mMusicOffset);
  }

  public void openPlayer() {
    if (mPlayer == null) { // Construct new player and set listener
      mPlayer = new MediaPlayer();
    } else { // Reset player so that becomes
      mPlayer.reset();
    }
  }

  public void closePlayer() {
    mPlayer.release();
    if (mPlayer != null) {
      mPlayer = null;
    }
  }

  public void setUpPlayer() {
    mPlayer.setOnPreparedListener(mp -> {
      mPlayer = mp;
      mPlayer.seekTo(mMusicOffset);
    });
    try {
      if (mMusic == null) {
        AssetFileDescriptor afd = mContext.getResources().getAssets().openFd("silence_15_sec.mp3");
        mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
      } else {
        mPlayer.setDataSource(mContext, mMusic.uri());
      }
      mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
      mPlayer.prepareAsync();
    } catch (IOException e) {
      Timber.e(e, "prepare() failed");
    }
  }

  public void startPlayer() {
    if (mPlayer != null && !mPlayer.isPlaying()) {
      mPlayer.start();
    }
  }

  public void pausePlayer() {
    if (mPlayer != null && mPlayer.isPlaying()) {
      mPlayer.pause();
    }
  }

  public void stopPlayer() {
    if (mPlayer != null) {
      mPlayer.stop();
      mPlayer.reset();
    }
  }

  public void rewindPlayer(int millisec) {
    pausePlayer();
    mPlayer.seekTo(millisec);
  }

  private boolean isStopped = false;

  public Observable<Integer> startSubscribePlayer() {
    isStopped = false;
    return Observable.create(subscriber -> {
      while (!isStopped) {
        subscriber.onNext(mPlayer.getCurrentPosition());
        try {
          Thread.sleep(50);
        } catch (InterruptedException e) {
          subscriber.onError(e);
          e.printStackTrace();
        }
      }
      subscriber.onCompleted();
    });
  }

  public void stopSubscribePlayer() {
    isStopped = true;
  }
}
