package com.estsoft.muvicam.ui.library.videolibrary;

import com.estsoft.muvicam.data.DataManager;
import com.estsoft.muvicam.model.EditorVideo;
import com.estsoft.muvicam.model.Video;
import com.estsoft.muvicam.ui.base.BasePresenter;
import com.estsoft.muvicam.ui.library.videolibrary.injection.VideoLibraryScope;
import com.estsoft.muvicam.util.RxUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;


/**
 * Created by Administrator on 2017-01-05.
 */

@VideoLibraryScope
public class VideoLibraryPresenter extends BasePresenter<VideoLibraryMvpView> {

  private DataManager mDataManager;
  private Subscription mSubscription;

  @Inject
  public VideoLibraryPresenter(DataManager dataManager) {
    mDataManager = dataManager;
  }

  @Override
  public void attachView(VideoLibraryMvpView mvpView) {
    super.attachView(mvpView);
  }

  @Override
  public void detachView() {
    RxUtil.unsubscribe(mSubscription);
    if (mSubscription != null) {
      mSubscription = null;
    }
    super.detachView();
  }

  public void loadVideos() {
    checkViewAttached();
    RxUtil.unsubscribe(mSubscription);
    mSubscription = mDataManager.getVideos()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe(
            videos -> {
              if (videos.isEmpty()) {
                getMvpView().showVideosEmpty();
              } else {
                getMvpView().showVideos(videos);
              }
            },
            e -> {
              Timber.e(e, "There was an error loading the videos.");
              getMvpView().showError();
            }
        );
  }

  public void onItemSelected(Video video) {
    video.selected();
    pushVideo(video);
    getMvpView().selectVideo(mSelectedVideos);
  }

  public void onItemReleased(Video video) {
    video.released();
    removeVideo(video);
    getMvpView().releaseVideo(mSelectedVideos);
  }

  public List<EditorVideo> getVideos() {
    List<EditorVideo> editorVideoList = new ArrayList<>();

    for (Video video : mSelectedVideos) {
      EditorVideo editorVideo = new EditorVideo();
      editorVideo.setDurationMiliSec(video.duration());
      editorVideo.setVideoPath(video.uri().toString());
      editorVideoList.add(editorVideo);
    }

    return editorVideoList;
  }

  private static final int MAX_SELECTION = 5;
  private Video[] mSelectedVideos = new Video[MAX_SELECTION];

  private void pushVideo(Video item) {
    if (mSelectedVideos.length == MAX_SELECTION) {
      Timber.i("Selected video array is empty.");
      return;
    }
    item.setSelectionOrder(mSelectedVideos.length);
    mSelectedVideos[mSelectedVideos.length] = item;
  }

  private Video removeVideo(Video item) {
    if (mSelectedVideos.length == 0) {
      Timber.i("Selected video array is full.");
      return null;
    }

    int i; // 0, 1, 2, 3, 4

    //noinspection StatementWithEmptyBody
    for (i = 0; i < MAX_SELECTION && mSelectedVideos[i].compareTo(item) != 0; i++)
      ;
    Video temp = mSelectedVideos[i++];
    for (; i < MAX_SELECTION; i++) {
      mSelectedVideos[i].setSelectionOrder(i-1);
      mSelectedVideos[i-1] = mSelectedVideos[i];
    }

    return temp;
  }
}
