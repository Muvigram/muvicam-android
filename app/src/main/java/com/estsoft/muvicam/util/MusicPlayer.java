package com.estsoft.muvicam.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.estsoft.muvicam.ui.home.camera.CameraFragment;

import java.io.IOException;

import rx.Observable;
import timber.log.Timber;

/**
 * Customized music player. This class
 *  1. uses {@link MediaPlayer} to playing music.
 *  2. is designed to
 *    1) setup media player synchronously and asynchronously.
 *    2) set and play music.
 *    3) seek to a specific point.
 *    4) to publish a current position as a {@link Observable} object.
 *
 * Created by jaylim on 21/01/2017.
 */

public class MusicPlayer {

  public interface OnCompleteSetupListener {
    void onCompleteSetup(MusicPlayer musicPlayer);
  }

  private OnCompleteSetupListener mOnCompleteSetupListener;

  public void setOnCompleteSetupListener(OnCompleteSetupListener listener) {
    mOnCompleteSetupListener = listener;
  }

  /* Field */
  private final Context mContext;
  private final String mDefaultAsset;

  private MediaPlayer mPlayer;
  private Uri mMusicUri = null;
  private int mOffset = 0;

  /* Constructor with no default asset */
  public MusicPlayer(Context context) {
    this(context, (Uri) null);
  }

  public MusicPlayer(Context context, Uri musicUri) {
    this.mContext = context;
    this.mMusicUri = musicUri;
    this.mDefaultAsset = null;
  }

  /* Constructor with default asset */
  public MusicPlayer(Context context, @NonNull String defaultAsset) {
    this(context, null, defaultAsset);
  }

  public MusicPlayer(Context context, Uri musicUri, @NonNull String defaultAsset) {
    this.mContext = context;
    this.mMusicUri = musicUri;
    this.mDefaultAsset = defaultAsset;
  }

  public void setMusicAsync(@Nullable Uri musicUri) {
    stopPlayer();
    mMusicUri = musicUri;
    mOffset = 0;
    setUpPlayerAsync();
  }


  public void setMusic(@Nullable Uri musicUri) {
    stopPlayer();
    mMusicUri = musicUri;
    mOffset = 0;
    setUpPlayer();
  }

  public void setOffset(int offset) {
    pausePlayer();
    mOffset = offset;
    mPlayer.seekTo(mOffset);
  }

  public int getAbsolutePosition() {
    return mPlayer.getCurrentPosition();
  }

  public int getOffset() {
    return mOffset;
  }

  public int getRelativePosition() {
    return mPlayer.getCurrentPosition() - mOffset;
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
      mPlayer.seekTo(mOffset);
    });

    setDataSource();
    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

    mPlayer.prepareAsync();
  }

  public void setUpPlayerAsync() {
    mPlayer.setOnPreparedListener(mp -> {
      mPlayer = mp;
      mPlayer.seekTo(mOffset);

      mOnCompleteSetupListener.onCompleteSetup(this);
    });

    setDataSource();
    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

    mPlayer.prepareAsync();
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

  private void setDataSource() {
    try {
      if (mMusicUri != null) {
        mPlayer.setDataSource(mContext, mMusicUri);
      } else if (mDefaultAsset != null) {
        AssetFileDescriptor afd = mContext.getResources().getAssets().openFd(mDefaultAsset);
        mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
      } else {
        throw new RuntimeException("There is neither music nor default asset.");
      }
    } catch (IOException e) {
      Timber.e(e, "setDataSource failed");
    }
  }
}
