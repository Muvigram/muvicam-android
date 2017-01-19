package com.estsoft.muvicam.ui.library.musiclibrary;

import android.support.annotation.Nullable;

import com.estsoft.muvicam.data.DataManager;
import com.estsoft.muvicam.model.Music;
import com.estsoft.muvicam.ui.base.BasePresenter;
import com.estsoft.muvicam.ui.library.musiclibrary.injection.MusicLibraryScope;
import com.estsoft.muvicam.util.RxUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * MusicLibraryPresenter.
 * Created by jaylim on 10/01/2017.
 */

@MusicLibraryScope
public class MusicLibraryPresenter extends BasePresenter<MusicLibraryMvpView> {

  private DataManager mDataManager;
  private Subscription mSubscription;

  @Inject
  public MusicLibraryPresenter(DataManager dataManager) {
    mDataManager = dataManager;
  }

  @Override
  public void attachView(MusicLibraryMvpView mvpView) {
    super.attachView(mvpView);
  }

  @Override
  public void detachView() {
    super.detachView();
  }

  public void loadMusics(@Nullable CharSequence text) {
    List<Music> stock = new ArrayList<>();
    checkViewAttached();
    RxUtil.unsubscribe(mSubscription);
    mSubscription = mDataManager.getMusics(text)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe(
            musics -> {
              stock.addAll(musics);
              getMvpView().showMusics(stock);
            },
            e -> {
              Timber.e(e, "There was an error loading the music");
              getMvpView().showError();
            },
            () -> {
              if (stock.isEmpty()) {
                getMvpView().showMusicsEmpty();
              }
            }
        );
  }
}
