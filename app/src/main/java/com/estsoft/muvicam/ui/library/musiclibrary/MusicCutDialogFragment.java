package com.estsoft.muvicam.ui.library.musiclibrary;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.Music;
import com.estsoft.muvicam.ui.home.camera.WaveformView;
import com.estsoft.muvicam.ui.library.LibraryActivity;
import com.estsoft.muvicam.util.MusicPlayer;
import com.estsoft.muvicam.util.RxUtil;
import com.estsoft.muvicam.util.UnitConversionUtil;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by jaylim on 21/01/2017.
 */

public class MusicCutDialogFragment extends DialogFragment {

  private static final String ARG_MUSIC = "librarymusic.MusicCutDialogFragment.arg_music";
  private static final String ARG_OFFSET = "librarymusic.MusicCutDialogFragment.arg_offset";

  public static MusicCutDialogFragment newInstance(Music music) {
    return newInstance(music, 0);
  }

  public static MusicCutDialogFragment newInstance(Music music, int offset) {
    MusicCutDialogFragment fragment = new MusicCutDialogFragment();
    Bundle args = new Bundle();
    args.putParcelable(ARG_MUSIC, music);
    args.putInt(ARG_OFFSET, offset);
    fragment.setArguments(args);
    return fragment;
  }

  Unbinder mUnbinder;

  @BindView(R.id.music_dialog_item_thumbnail) ImageView mThumbnail;
  @BindView(R.id.music_dialog_item_artist) TextView mArtist;
  @BindView(R.id.music_dialog_item_title) TextView mTitle;
  @BindView(R.id.music_dialog_cut_waveform) WaveformView mWaveformView;

  private Music mMusic;
  private int mOffset;

  private MusicPlayer mMusicPlayer;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mMusicPlayer = new MusicPlayer(getActivity(), "silence_15_sec.mp3");
    mMusicPlayer.openPlayer();
  }

  @Override
  public void onPause() {
    mMusicPlayer.stopSubscribePlayer();
    super.onPause();
  }

  @Override
  public void onDestroyView() {
    mUnbinder.unbind();
    super.onDestroyView();
  }

  @Override
  public void onDestroy() {
    mMusicPlayer.closePlayer();
    super.onDestroy();
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

    mMusic = getArguments().getParcelable(ARG_MUSIC);
    mOffset = getArguments().getInt(ARG_OFFSET);

    LayoutInflater inflater = getActivity().getLayoutInflater();
    View view = inflater.inflate(R.layout.dialog_music_cut, null);
    mUnbinder = ButterKnife.bind(this, view);

    if (mMusic == null) {
      return builder.setMessage(getString(R.string.music_cut_error_loading))
          .setPositiveButton(android.R.string.ok, (dialog, id) -> dialog.dismiss())
          .create();
    }

    mMusicPlayer.setMusic(mMusic.uri());

    // set music profile
    if (mMusic.thumbnail() != null) {
      mThumbnail.setImageBitmap(mMusic.thumbnail());
    } else {
      mThumbnail.setImageResource(R.drawable.music_item_no_album_art);
    }
    mTitle.setText(mMusic.title());
    mArtist.setText(mMusic.artist());

    // set waveform
    mWaveformView.setSoundFile(mMusic.uri(), UnitConversionUtil.millisecToSec(mOffset));
    mWaveformView.setWaveformListener(mWaveformListener);
    mWaveformView.setOnPreparedListener(this::startMusic);

    builder.setView(view)
        .setPositiveButton(android.R.string.ok, (dialog, id) -> {
          LibraryActivity.get(this).completeSelection(mMusic.uri().toString(), mOffset, 15);
        })
        .setNegativeButton(android.R.string.cancel, (dialog, id) -> dismiss());

    return builder.create();
  }

  Subscription mSubscription;

  public WaveformView.WaveformListener mWaveformListener = new WaveformView.WaveformListener() {
    private float mXStart;

    @Override
    public void waveformTouchStart(float x) {
      mMusicPlayer.stopSubscribePlayer();
      mXStart = x;
    }

    @Override
    public void waveformTouchMove(float x) {
      mWaveformView.moveOffset(mXStart - x);
      mXStart = x;
    }

    @Override
    public void waveformTouchEnd() {
      startMusic();
    }
  };

  private void startMusic() {
    mOffset = UnitConversionUtil.secToMillisec(mWaveformView.fixOffset());
    Timber.e("mOffset : %d", mOffset);
    mMusicPlayer.setOffset(mOffset);
    RxUtil.unsubscribe(mSubscription);

    mMusicPlayer.startPlayer();
    mSubscription = mMusicPlayer.startSubscribePlayer()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.newThread())
        .map(UnitConversionUtil::millisecToSec)
        .filter(currentSec -> {
          if (mWaveformView.isValidRunningAt(currentSec)) {
            return true;
          } else {
            mMusicPlayer.pausePlayer();
            mMusicPlayer.stopSubscribePlayer();
            return false;
          }
        })
        .subscribe(
            mWaveformView::updateUi,
            Throwable::printStackTrace,
            () -> RxUtil.unsubscribe(mSubscription)
        );
  }


}
