package com.estsoft.muvigram.ui.library.videolibrary;

import com.estsoft.muvigram.model.Video;
import com.estsoft.muvigram.ui.base.MvpView;

import java.util.List;

/**
 * Created by Administrator on 2017-01-06.
 */

public interface VideoLibraryMvpView extends MvpView {
  // show videos
  void showVideos(List<Video> videos);
  void showVideosEmpty();
  void showError();
}
