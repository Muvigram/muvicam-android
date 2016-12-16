package com.estsoft.muvicam.ui.home.music;

import java.util.List;

import com.estsoft.muvicam.model.Music;
import com.estsoft.muvicam.ui.base.MvpView;

/**
 * Created by jaylim on 12/12/2016.
 */

public interface MusicMvpView extends MvpView {
  void showMusics(List<Music> musics);

  void showMusicsEmpty();

  void showError();

}
