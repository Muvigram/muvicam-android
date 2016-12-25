package com.estsoft.muvicam.ui.home.camera;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.Music;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import timber.log.Timber;

/**
 * Created by jaylim on 12/17/2016.
 */

public class MusicCutFragment extends Fragment {

  private static final String ARG_MUSIC = "MusicCutFragment.arg_music";
  private static final String ARG_OFFSET = "MusicCutFragment.arg_offset";

  public static MusicCutFragment newInstance(Uri uri, int offset) {
    MusicCutFragment fragment = new MusicCutFragment();
    Bundle args = new Bundle();
    args.putParcelable(ARG_MUSIC, uri);
    args.putInt(ARG_OFFSET, offset);
    fragment.setArguments(args);
    return fragment;
  }

  // Millisecond
  private int mOffset;

  Unbinder mUnbinder;

  @BindView(R.id.music_cut_complete_button)
  ImageButton mCompleteButton;

  @BindView(R.id.music_cut_waveform)
  WaveformView mWaveformView;

  @OnClick(R.id.music_cut_complete_button)
  public void _completeMusicCut() {
    CameraFragment parentFragment = ((CameraFragment) getParentFragment());
    Timber.e("Music_Cut : %d", mOffset);

    FragmentManager pcfm = parentFragment.getChildFragmentManager();
    Fragment fragment = pcfm.findFragmentById(R.id.camera_container_music_cut);

    pcfm.beginTransaction()
        .remove(fragment)
        .commit();
    parentFragment.requestUiChange(CameraFragment.UI_LOGIC_FINISH_CUT_MUSIC);

    parentFragment.cutMusic(mOffset);
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_music_cut, container, false);
    mUnbinder = ButterKnife.bind(this, view);
    return view;
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    Uri uri = getArguments().getParcelable(ARG_MUSIC);
    mOffset = getArguments().getInt(ARG_OFFSET);
    mWaveformView.setSoundFile(uri, mOffset / 1000.0f);
    mWaveformView.setListener(mWaveformListener);
  }

  @Override
  public void onDestroyView() {
    mUnbinder.unbind();
    super.onDestroyView();
  }

  public WaveformView.WaveformListener mWaveformListener = new WaveformView.WaveformListener() {
    private float mXStart;

    @Override
    public void waveformTouchStart(float x) {
      mXStart = x;
    }

    @Override
    public void waveformTouchMove(float x) {
      mWaveformView.moveOffset(mXStart - x);
      mXStart = x;
    }

    @Override
    public void waveformTouchEnd() {
      mOffset = (int) (1000 * mWaveformView.fixOffset());
    }
  };
}
