package com.estsoft.muvigram.ui.library.musiclibrary;

import android.support.annotation.Nullable;

import com.estsoft.muvigram.data.DataManager;
import com.estsoft.muvigram.model.Music;
import com.estsoft.muvigram.ui.base.BasePresenter;
import com.estsoft.muvigram.ui.library.musiclibrary.injection.MusicLibraryScope;
import com.estsoft.muvigram.util.rx.RxUtil;

import java.util.ArrayList;
import java.util.List;

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
    RxUtil.unsubscribe(mSubscription);
    if (mSubscription != null) {
      mSubscription = null;
    }
    super.detachView();
  }

  public void loadMusics(@Nullable CharSequence text) {
    List<Music> stock = new ArrayList<>();
    checkViewAttached();
    RxUtil.unsubscribe(mSubscription);
    mSubscription = mDataManager.getMusics(text)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            musics -> {
              stock.addAll(musics);
              getMvpView().showMusics(stock);
            },
            e -> {
              Timber.w(e, "m/loadMusics There was an error loading the music");
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
