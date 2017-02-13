package com.estsoft.muvigram.ui.home.camera;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.estsoft.muvigram.R;
import com.estsoft.muvigram.model.Music;
import com.estsoft.muvigram.ui.home.HomeActivity;
import com.estsoft.muvigram.util.DialogFactory;
import com.estsoft.muvigram.util.MusicPlayer;
import com.estsoft.muvigram.util.RxUtil;
import com.estsoft.muvigram.util.UnitConversionUtil;
import com.estsoft.muvigram.util.WaveformView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by jaylim on 12/17/2016.
 */

public class MusicCutFragment extends Fragment {

  private static final String ARG_MUSIC = "MusicCutFragment.arg_music";
  private static final String ARG_OFFSET = "MusicCutFragment.arg_offset";

  public static MusicCutFragment newInstance(Music music, int offset) {
    MusicCutFragment fragment = new MusicCutFragment();
    Bundle args = new Bundle();
    args.putParcelable(ARG_MUSIC, music);
    args.putInt(ARG_OFFSET, offset);
    fragment.setArguments(args);
    return fragment;
  }

  Unbinder mUnbinder;

  @BindView(R.id.music_cut_item_thumbnail) ImageView mThumbnail;
  @BindView(R.id.music_cut_item_title)     TextView mTitle;
  @BindView(R.id.music_cut_waveform)       WaveformView mWaveformView;


  @OnClick(R.id.music_cut_ok_button)
  public void _completeMusicCut() {
    FragmentManager pcfm = mParentFragment.getChildFragmentManager();
    Fragment fragment = pcfm.findFragmentById(R.id.camera_container_music_cut);

    pcfm.beginTransaction()
        .remove(fragment)
        .commit();
    mParentFragment.requestUiChange(CameraFragment.UI_LOGIC_FINISH_CUT_MUSIC);
    mParentFragment.changeOffset(mOffset);
  }

  @OnClick(R.id.music_cut_cancel_button)
  public void _cancelMusicCut() {
    FragmentManager pcfm = mParentFragment.getChildFragmentManager();
    Fragment fragment = pcfm.findFragmentById(R.id.camera_container_music_cut);

    pcfm.beginTransaction()
        .remove(fragment)
        .commit();
    mParentFragment.requestUiChange(CameraFragment.UI_LOGIC_FINISH_CUT_MUSIC);
  }

  // Millisecond
  private Music mMusic;
  private int mOffset;

  private CameraFragment mParentFragment;
  private MusicPlayer mMusicPlayer;


  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mParentFragment = ((CameraFragment) getParentFragment());
    HomeActivity.get(mParentFragment).setCuttingVideo(true);

    mMusicPlayer = new MusicPlayer(getActivity(), "silence_15_sec.mp3");
    mMusicPlayer.openPlayer();
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_home_music_cut, container, false);
    mUnbinder = ButterKnife.bind(this, view);
    return view;
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mMusic = getArguments().getParcelable(ARG_MUSIC);
    mOffset = getArguments().getInt(ARG_OFFSET);

    if (mMusic == null) {
      DialogFactory.createGenericErrorDialog(
          getActivity(),
          getString(R.string.music_cut_error_loading)
      ).show();
      return;
    }

    mMusicPlayer.setMusic(mMusic.uri());

    // set music profile
    if (mMusic.thumbnail() != null) {
      mThumbnail.setImageBitmap(mMusic.thumbnail());
    } else {
      mThumbnail.setImageResource(R.drawable.music_item_no_album_art);
    }
    mTitle.setText(mMusic.title());
    
    mWaveformView.setSoundFile(mMusic.uri(), UnitConversionUtil.millisecToSec(mOffset));
    mWaveformView.setWaveformListener(mWaveformListener);
    mWaveformView.setOnPreparedListener(this::startMusic);
  }

  @Override
  public void onResume() {
    super.onResume();
    Timber.v("MP.isPlaying : %b, WV.isPrepared : %b", mMusicPlayer.isPlaying(), mWaveformView.isOnPrepared());
    if (!mMusicPlayer.isPlaying() &&
        mWaveformView.isOnPrepared()) {
      startMusic();
    }
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
    HomeActivity.get(mParentFragment).setCuttingVideo(false);
    super.onDestroy();
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
    mMusicPlayer.setOffset(mOffset);
    RxUtil.unsubscribe(mSubscription);

    mMusicPlayer.startPlayer();
    mSubscription = mMusicPlayer.startSubscribePlayer()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.newThread())
        .map(UnitConversionUtil::millisecToSec)
        .filter(sec -> {
          if (mWaveformView != null && mWaveformView.isValidRunningAt(sec)) {
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
            () -> {
              RxUtil.unsubscribe(mSubscription);
              mMusicPlayer.pausePlayer();
            }
        );
  }

}
