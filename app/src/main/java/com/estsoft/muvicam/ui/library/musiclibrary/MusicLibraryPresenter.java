package com.estsoft.muvicam.ui.library.musiclibrary;

import android.support.annotation.Nullable;

import com.estsoft.muvicam.data.DataManager;
import com.estsoft.muvicam.model.Music;
import com.estsoft.muvicam.ui.base.BasePresenter;
import com.estsoft.muvicam.ui.library.musiclibrary.injection.MusicLibraryScope;
import com.estsoft.muvicam.util.RxUtil;

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
    super.detachView();
  }

  public void loadMusics(@Nullable CharSequence text) {
    List<Music> musics = new ArrayList<>();

    String str = text == null ? "" : text.toString();
    String[] tokens = str.split("[ \\t\\n\\u000B\\f\\r]+");

    checkViewAttached();
    RxUtil.unsubscribe(mSubscription);
    mSubscription = mDataManager.getMusics()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .filter(music -> filterOutMusics(music, tokens))
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

  private static boolean filterOutMusics(Music music, String[] tokens) {
    String title = music.title();
    String artist = music.artist();
    for (int i = 0; i < tokens.length; i++) {
      String token = tokens[i];
      if (title.contains(token) || artist.contains(token)) {
        return true;
      }
    }
    return false;
  }
}
