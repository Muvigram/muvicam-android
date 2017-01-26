package com.estsoft.muvicam.ui.library.musiclibrary;

import com.estsoft.muvicam.model.Music;
import com.estsoft.muvicam.ui.base.MvpView;

import java.util.List;

/**
 * Created by jaylim on 10/01/2017.
 */

public interface MusicLibraryMvpView extends MvpView {
  void showMusics(List<Music> musics);

  void showMusicsEmpty();

  void showError();

  void showMusicCutDialog(Music music);
}
