package com.estsoft.muvigram.ui.library.videolibrary;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.estsoft.muvigram.R;
import com.estsoft.muvigram.model.Video;
import com.estsoft.muvigram.ui.library.LibraryActivity;
import com.estsoft.muvigram.ui.library.musiclibrary.MusicLibraryFragment;
import com.estsoft.muvigram.ui.library.videolibrary.injection.VideoLibraryComponent;
import com.estsoft.muvigram.ui.library.videolibrary.injection.VideoLibraryModule;
import com.estsoft.muvigram.util.DialogFactory;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class VideoLibraryFragment extends Fragment implements VideoLibraryMvpView {

  public static VideoLibraryFragment newInstance() {
    return new VideoLibraryFragment();
  }

  VideoLibraryComponent mVideoLibraryComponent;

  @Inject VideoLibraryPresenter mPresenter;
  @Inject VideoLibraryAdapter   mAdapter;

  @Inject
  public void registerFragment(VideoLibraryAdapter adapter) {
    adapter.register(this);
  }

  Unbinder mUnbinder;

  @BindView(R.id.library_video_recyclerview) RecyclerView mRecyclerView;

  @OnClick(R.id.library_video_home_button)
  public void backToHome(View v) {
    getActivity().onBackPressed();
  }

  @OnClick(R.id.library_video_next_button)
  public void goToNext(View v) {
    List<Video> videos = mAdapter.getVideos();
    if (videos.size() <= 0) {
      DialogFactory.createSimpleOkErrorDialog(getActivity(),
          R.string.library_video_title_text, R.string.library_video_selection_is_empty).show();
      return;
    }
    LibraryActivity.get(this)
        .completeVideoSelection(videos)
        .navigate(MusicLibraryFragment.class);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate a layout and bind views on this fragment
    View view = inflater.inflate(R.layout.fragment_library_video, container, false);
    mUnbinder = ButterKnife.bind(this, view);

    return view;
  }

  //
  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    // Create a dagger component and inject dependencies
    mVideoLibraryComponent = LibraryActivity.get(this).getComponent()
        .plus(new VideoLibraryModule());
    mVideoLibraryComponent.inject(this);

    // Attach views
    mPresenter.attachView(this);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    // Set up recycler view with GridLayoutManager
    ((SimpleItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
    mRecyclerView.setAdapter(mAdapter);
    mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 4));

    // Load videos from local library
    mPresenter.loadVideos();
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
    if (mAdapter != null) {
      mAdapter.clearVideos();
      mAdapter.deregister();
      mAdapter = null;
    }
    if (mVideoLibraryComponent != null) {
      mVideoLibraryComponent = null;
    }
    super.onDestroy();
  }

  // Show videos
  @Override
  public void showVideos(List<Video> videos) {
    mAdapter.setVideos(videos);
    mAdapter.notifyDataSetChanged();
  }

  @Override
  public void showVideosEmpty() {
    mAdapter.setVideos(Collections.emptyList());
    mAdapter.notifyDataSetChanged();
    Toast.makeText(
        getActivity(),
        R.string.library_video_empty_videos,
        Toast.LENGTH_LONG
    ).show();
  }

  @Override
  public void showError() {
    DialogFactory.createGenericErrorDialog(
        getActivity(),
        getString(R.string.library_video_error_loading)
    ).show();
  }

}
