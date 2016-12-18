package com.estsoft.muvicam.ui.home.camera;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.estsoft.muvicam.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import timber.log.Timber;

/**
 * Created by jaylim on 12/17/2016.
 */

public class MusicCutFragment extends Fragment {

  public static MusicCutFragment newInstance() {
    return new MusicCutFragment();
  }

  private int mOffset = 30000;

  Unbinder mUnbinder;

  @BindView(R.id.music_cut_complete_button)
  ImageButton mCompleteButton;

  @OnClick(R.id.music_cut_complete_button)
  public void _completeMusicCut() {
    // TODO update music seek
    Timber.e("Music_Cut");
    ((CameraFragment) getParentFragment()).cutMusic(mOffset);
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_music_cut, container, false);
    mUnbinder = ButterKnife.bind(this, view);
    return view;
  }

  @Override
  public void onDestroyView() {
    mUnbinder.unbind();
    super.onDestroyView();
  }
}
