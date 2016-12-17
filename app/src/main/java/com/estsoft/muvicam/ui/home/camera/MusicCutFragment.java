package com.estsoft.muvicam.ui.home.camera;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.estsoft.muvicam.R;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by jaylim on 12/17/2016.
 */

public class MusicCutFragment extends Fragment {

  public static MusicCutFragment newInstance() {
    return new MusicCutFragment();
  }

  Unbinder mUnbinder;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_music_cut, container, false);
    mUnbinder = ButterKnife.bind(this, view);
    return inflater.inflate(R.layout.fragment_music_cut, container, false);
  }

  @Override
  public void onDestroyView() {
    mUnbinder.unbind();
    super.onDestroyView();
  }
}
