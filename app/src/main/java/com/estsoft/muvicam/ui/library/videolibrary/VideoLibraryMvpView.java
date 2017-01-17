package com.estsoft.muvicam.ui.library.videolibrary;

import com.estsoft.muvicam.model.Video;
import com.estsoft.muvicam.ui.base.MvpView;

import java.util.List;

/**
 * Created by Administrator on 2017-01-06.
 */

public interface VideoLibraryMvpView extends MvpView {
    // show videos
    void showVideos(List<Video> videos);
    void showVideosEmpty();
    void showError();

    // selection
    void selectVideo(Video[] videos);
    void releaseVideo(Video[] videos);
}
