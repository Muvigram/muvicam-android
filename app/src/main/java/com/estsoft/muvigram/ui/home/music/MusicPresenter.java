package com.estsoft.muvigram.ui.home.music;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.estsoft.muvigram.data.DataManager;
import com.estsoft.muvigram.model.Music;
import com.estsoft.muvigram.ui.base.BasePresenter;
import com.estsoft.muvigram.ui.home.music.injection.MusicScope;
import com.estsoft.muvigram.util.rx.RxUtil;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Music presenter.
 * Created by jaylim on 12/12/2016.
 */

@MusicScope
public class MusicPresenter extends BasePresenter<MusicMvpView> {

  private DataManager mDataManager;
  private Subscription mSubscription;

  @Inject
  public MusicPresenter(DataManager dataManager) {
    mDataManager = dataManager;
  }

  @Override
  public void attachView(MusicMvpView mvpView) {
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

  /* Business logic here */
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
