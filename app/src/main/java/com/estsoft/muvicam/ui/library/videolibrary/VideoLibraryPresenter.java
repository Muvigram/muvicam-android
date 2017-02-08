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
 *
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
    List<Video> stock = new ArrayList<>();

    checkViewAttached();
    RxUtil.unsubscribe(mSubscription);
    mSubscription = mDataManager.getVideos()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe(
            videos -> {
              stock.addAll(videos);
              getMvpView().showVideos(stock);
            },
            e -> {
              Timber.e(e, "There was an error loading the videos.");
              getMvpView().showError();
            },
            () -> {
              if (stock.isEmpty()) {
                getMvpView().showVideosEmpty();
              }
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
    getMvpView().releaseVideo(mSelectedVideos);
    removeVideo(video);
  }

  public List<Video> getVideos() {
    return mSelectedVideos;
  }

  private static final int MAX_SELECTION = 5;
  private List<Video> mSelectedVideos = new ArrayList<>();

  private void pushVideo(Video item) {
    if (mSelectedVideos.size() == MAX_SELECTION) {
      Timber.i("Selected video array is full.");
      return;
    }
    item.setSelectionOrder(mSelectedVideos.size());
    mSelectedVideos.add(item);
  }

  private Video removeVideo(Video item) {
    if (mSelectedVideos.size() == 0) {
      Timber.i("Selected video array is empty.");
      return null;
    }
    int i = mSelectedVideos.indexOf(item);
    return i == -1 ? null : mSelectedVideos.remove(i);
  }
}
