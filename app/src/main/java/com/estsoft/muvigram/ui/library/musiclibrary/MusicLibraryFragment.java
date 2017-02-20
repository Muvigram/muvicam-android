package com.estsoft.muvigram.ui.library.musiclibrary;

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
import android.widget.Toast;

import com.estsoft.muvigram.R;
import com.estsoft.muvigram.model.Music;
import com.estsoft.muvigram.ui.library.LibraryActivity;
import com.estsoft.muvigram.ui.library.musiclibrary.injection.MusicLibraryComponent;
import com.estsoft.muvigram.ui.library.musiclibrary.injection.MusicLibraryModule;
import com.estsoft.muvigram.ui.library.videolibrary.VideoLibraryFragment;
import com.estsoft.muvigram.util.thumbnail.BitmapViewUtil;
import com.estsoft.muvigram.util.DialogFactory;

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

  @BindView(R.id.library_music_search_bar_edit_text) EditText mSearchTextBar;
  @BindView(R.id.library_music_search_recyclerview) RecyclerView mRecyclerView;

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
    if (text.equals(""))
      mSearchTextBar.clearFocus();
    mPresenter.loadMusics(text);
  }

  @OnClick(R.id.library_music_search_bar_back)
  public void backToPrevious(View v) {
    LibraryActivity.get(this).navigate(VideoLibraryFragment.class);
  }

  @OnClick(R.id.library_music_search_bar_skip)
  public void skipSelectMusic(View v) {
    // LibraryActivity.get(this).completeSelection(null, 0, 15000);
    DialogFactory
        .createSimpleOkErrorDialog(getActivity(),
        R.string.library_music_skip_dialog_title,
        R.string.library_music_skip_dialog_desc)
        .show();
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

  @Inject
  public void registerFragment() {
    mAdapter.register(this);
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
    // BitmapViewUtil.clearViewGroup(mRecyclerView);
    super.onDestroyView();
  }

  @Override
  public void onDestroy() {
    mPresenter.detachView();
    if (mPresenter != null) {
      mPresenter = null;
    }
    if (mAdapter != null) {
      mAdapter.clearMusics();
      mAdapter.deregister();
      mAdapter = null;
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
        R.string.music_search_error_loading
    ).show();
  }


  @Override
  public void showMusicCutDialog(Music music) {
    getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

    MusicCutDialogFragment dialog = MusicCutDialogFragment.newInstance(music);
    dialog.setOnPreparedListener(() -> {
      getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    });
    dialog.show(getFragmentManager(), MusicCutDialogFragment.TAG);
  }

  public void preventKeyboardPopup() {
    this.getActivity().getWindow().setSoftInputMode(
        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
  }

  public MusicLibraryPresenter getPresenter() {
    return mPresenter;
  }
}
