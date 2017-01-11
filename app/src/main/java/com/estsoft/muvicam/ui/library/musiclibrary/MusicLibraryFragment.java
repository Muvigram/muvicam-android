package com.estsoft.muvicam.ui.library.musiclibrary;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.Music;
import com.estsoft.muvicam.ui.library.LibraryActivity;
import com.estsoft.muvicam.ui.library.musiclibrary.injection.MusicLibraryComponent;
import com.estsoft.muvicam.ui.library.musiclibrary.injection.MusicLibraryModule;
import com.estsoft.muvicam.util.DialogFactory;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import butterknife.Unbinder;

public class MusicLibraryFragment extends Fragment implements MusicLibraryMvpView {

  MusicLibraryComponent mMusicLibraryComponent;

  public static MusicLibraryFragment newInstance() {
    return new MusicLibraryFragment();
  }

  Unbinder mUnbinder;

  @BindView(R.id.library_music_search_bar_skip) TextView mSkipButton;
  @BindView(R.id.library_music_search_bar_back) TextView mBackButton;
  @BindView(R.id.library_music_search_bar_edit_text) EditText mSearchTextBar;
  @BindView(R.id.library_music_search_bar) RecyclerView mRecyclerView;

  @OnFocusChange(R.id.library_music_search_bar_edit_text)
  public void searchBarOnFocus(View v, boolean hasFocus) {
    if(hasFocus) {
      ((EditText) v).setHint("");
    } else {
      ((EditText) v).setHint(getString(R.string.music_search_hint));
    }
  }

  @OnTextChanged(R.id.library_music_search_bar_edit_text)
  public void searchBarSearchRequested(CharSequence text) {
    mPresenter.loadMusics(text);
  }

  @OnClick(R.id.library_music_search_bar_skip)
  public void skipSelectMusic(View v) {
    LibraryActivity.get(this).completeSelection(null, 0, 15);
  }

  @Inject MusicLibraryAdapter mAdapter;
  @Inject MusicLibraryPresenter mPresenter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    preventKeyboardPopup();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_library_music, container, false);
    mUnbinder = ButterKnife.bind(this, view);
    return view;
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mMusicLibraryComponent = LibraryActivity.get(this)
        .getComponent().plus(new MusicLibraryModule());
    mMusicLibraryComponent.inject(this);
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
    if (mMusicLibraryComponent != null) {
      mMusicLibraryComponent = null;
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
