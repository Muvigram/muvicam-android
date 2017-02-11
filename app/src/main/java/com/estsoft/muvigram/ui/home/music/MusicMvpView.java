package com.estsoft.muvigram.ui.home.music;

import java.util.List;

import com.estsoft.muvigram.model.Music;
import com.estsoft.muvigram.ui.base.MvpView;

/**
 * Created by jaylim on 12/12/2016.
 */

public interface MusicMvpView extends MvpView {
  void showMusics(List<Music> musics);

  void showMusicsEmpty();

  void showError();

}
