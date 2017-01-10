package com.estsoft.muvicam.ui.library.musiclibrary;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.EditorVideo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MusicLibraryFragment extends Fragment {
  public static final String SELECTED_VIDEO = "musicselector.MusicLibraryFragment.selected_video";

  //선택된 video list (수정필요!)
  private static ArrayList<EditorVideo> selectedVideos = new ArrayList<>();

  public static MusicLibraryFragment newInstance(List<EditorVideo> videos) {
    MusicLibraryFragment fragment = new MusicLibraryFragment();
    Bundle args = new Bundle();
    args.putParcelableArrayList(MusicLibraryFragment.SELECTED_VIDEO,
        (ArrayList<EditorVideo>) videos);
    fragment.setArguments(args);
    return fragment;
  }

  Unbinder mUnbinder;

  @BindView(R.id.library_music_search_bar_skip) TextView mSkipButton;
  @BindView(R.id.library_music_search_bar_back) TextView mBackButton;
  @BindView(R.id.library_music_search_bar_edit_text) EditText mSearchTextBar;
  @BindView(R.id.library_music_search_bar) RecyclerView mRecyclerView;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Bundle args = getArguments();
    //선택된 video list 받는 곳 (수정필요!)
    if (args != null) {
      selectedVideos = args.getParcelableArrayList(SELECTED_VIDEO);
    }
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
  }

  @Override
  public void onDestroyView() {
    mUnbinder.unbind();
    super.onDestroyView();
  }
}
