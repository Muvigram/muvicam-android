package com.estsoft.muvigram.ui.library.musiclibrary;

import com.estsoft.muvigram.model.Music;
import com.estsoft.muvigram.ui.base.MvpView;

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
