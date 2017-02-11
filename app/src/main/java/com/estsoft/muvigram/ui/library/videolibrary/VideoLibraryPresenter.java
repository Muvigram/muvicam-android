package com.estsoft.muvigram.ui.library.videolibrary;

import com.estsoft.muvigram.data.DataManager;
import com.estsoft.muvigram.model.Video;
import com.estsoft.muvigram.ui.base.BasePresenter;
import com.estsoft.muvigram.ui.library.videolibrary.injection.VideoLibraryScope;
import com.estsoft.muvigram.util.RxUtil;

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
              Timber.w(e, "m/loadVideos There was an error loading the videos.");
              getMvpView().showError();
            },
            () -> {
              if (stock.isEmpty()) {
                getMvpView().showVideosEmpty();
              }
            }
        );
  }
}
