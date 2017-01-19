package com.estsoft.muvicam.ui.library.musiclibrary.injection;

import com.estsoft.muvicam.ui.library.musiclibrary.MusicLibraryFragment;

import dagger.Subcomponent;

/**
 * Created by jaylim on 10/01/2017.
 */

@MusicLibraryScope
@Subcomponent(modules = MusicLibraryModule.class)
public interface MusicLibraryComponent {
  /* Subcomponent */

  /* Dependency objects extended by constructor injections */


  /* Dependency objects provided from modules and dependencies */


  /* Field injection */
  void inject(MusicLibraryFragment musicLibraryFragment);
}
