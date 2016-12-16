package com.estsoft.muvicam.ui.home.music.injection;

import dagger.Subcomponent;
import com.estsoft.muvicam.ui.home.music.MusicFragment;

/**
 * Created by jaylim on 12/12/2016.
 */
@MusicScope
@Subcomponent(modules =  MusicModule.class)
public interface MusicComponent {
    /* Subcomponent */

  /* Dependency objects extended by constructor injections */


  /* Dependency objects provided from modules and dependencies */


  /* Field injection */
  void inject(MusicFragment musicFragment);

}
