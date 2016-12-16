package com.estsoft.muvicam.ui.home.music;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import javax.inject.Inject;

import com.estsoft.muvicam.data.DataManager;
import com.estsoft.muvicam.model.Music;
import com.estsoft.muvicam.ui.base.BasePresenter;
import com.estsoft.muvicam.ui.home.music.injection.MusicScope;
import com.estsoft.muvicam.util.RxUtil;
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

  DataManager mDataManager;
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
    super.detachView();
    if(mSubscription != null) {
      mSubscription.unsubscribe();
    }
  }

  /* Business logic here */
  public void loadMusics() {

    List<Music> musics = new ArrayList<>();

    checkViewAttached();
    RxUtil.unsubscribe(mSubscription);
    mSubscription = mDataManager.getMusics()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe(
            music -> {
              musics.add(music);
              if (musics.size() % 10 == 0) {
                getMvpView().showMusics(musics);
              }
            },
            e -> {
              Timber.e(e, "There was an error loading the music");
              getMvpView().showError();
            },
            () -> {
              if (musics.isEmpty()) {
                getMvpView().showMusicsEmpty();
              } else {
                getMvpView().showMusics(musics);
              }
            }
        );
  }
}
