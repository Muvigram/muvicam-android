package com.estsoft.muvicam.ui.home.music;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import butterknife.Unbinder;
import com.estsoft.muvicam.model.Music;
import com.estsoft.muvicam.R;
import com.estsoft.muvicam.ui.home.HomeActivity;
import com.estsoft.muvicam.ui.home.camera.CameraFragment;
import com.estsoft.muvicam.ui.home.music.injection.MusicComponent;
import com.estsoft.muvicam.ui.home.music.injection.MusicModule;
import com.estsoft.muvicam.util.DialogFactory;

/**
 * Created by jaylim on 12/12/2016.
 */

public class MusicFragment extends Fragment implements MusicMvpView {

  MusicComponent mMusicComponent;

  public static MusicFragment newInstance() {
    return new MusicFragment();
  }

  public static MusicFragment get(Fragment fragment) {
    return (MusicFragment) fragment.getParentFragment();
  }

  @BindView(R.id.music_search_bar_edit_text) EditText mSearchTextBar;
  @BindView(R.id.music_search_recyclerview) RecyclerView mRecyclerView;

  @OnTextChanged(R.id.music_search_bar_edit_text)
  public void searchBarSearchRequested(CharSequence text) {
    mPresenter.loadMusics(text);
  }


  @OnFocusChange(R.id.music_search_bar_edit_text)
  public void searchBarOnFocus(View v, boolean hasFocus) {
    if(hasFocus) {
      ((EditText) v).setHint("");
    } else {
      ((EditText) v).setHint(getString(R.string.music_search_hint));
    }
  }

  @Inject MusicSearchAdapter mAdapter;
  @Inject MusicPresenter mPresenter;

  Unbinder mUnbinder;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    preventKeyboardPopup();
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_music, container, false);
    mUnbinder = ButterKnife.bind(this, view);
    return view;
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mMusicComponent = HomeActivity.get(this).getComponent().plus(new MusicModule());
    mMusicComponent.inject(this);
    mPresenter.attachView(this);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    mRecyclerView.setAdapter(mAdapter);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

    mPresenter.loadMusics(null);
  }

  @Override
  public void onDestroyView() {
    mUnbinder.unbind();
    if (mUnbinder != null) {
      mUnbinder = null;
    }
    super.onDestroyView();
  }

  @Override
  public void onDestroy() {
    mPresenter.detachView();
    if (mPresenter != null) {
      mPresenter = null;
    }
    if (mMusicComponent != null) {
      mMusicComponent = null;
    }
    super.onDestroy();
  }

  @Override
  public void showMusics(List<Music> musics) {
    mAdapter.setMusics(musics);
    mAdapter.notifyDataSetChanged();
  }

  @Override
  public void showMusicsEmpty() {
    mAdapter.setMusics(Collections.emptyList());
    mAdapter.notifyDataSetChanged();
    Toast.makeText(
        getActivity(),
        R.string.music_search_empty_musics,
        Toast.LENGTH_LONG
    ).show();
  }

  @Override
  public void showError() {
    DialogFactory.createGenericErrorDialog(
        getActivity(),
        getString(R.string.music_search_error_loading)
    ).show();
  }

  public void preventKeyboardPopup() {
    this.getActivity().getWindow().setSoftInputMode(
        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
  }

}
