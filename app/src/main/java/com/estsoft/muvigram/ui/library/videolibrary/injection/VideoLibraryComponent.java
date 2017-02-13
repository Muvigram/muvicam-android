package com.estsoft.muvigram.ui.library.videolibrary.injection;

import com.estsoft.muvigram.ui.library.videolibrary.VideoLibraryFragment;

import dagger.Subcomponent;

/**
 * Created by jaylim on 10/01/2017.
 */

@VideoLibraryScope
@Subcomponent(modules = VideoLibraryModule.class)
public interface VideoLibraryComponent {
  /* Subcomponent */

  /* Dependency objects extended by constructor injections */


  /* Dependency objects provided from modules and dependencies */


  /* Field injection */
  void inject(VideoLibraryFragment videoSelectorFragment);
}
