package com.estsoft.muvicam.injection.component;

import com.estsoft.muvicam.injection.module.LibraryModule;
import com.estsoft.muvicam.injection.scope.LibraryScope;
import com.estsoft.muvicam.ui.library.LibraryActivity;
import com.estsoft.muvicam.ui.library.musiclibrary.injection.MusicLibraryComponent;
import com.estsoft.muvicam.ui.library.musiclibrary.injection.MusicLibraryModule;

import dagger.Component;

/**
 *
 * Created by jaylim on 10/01/2017.
 */

@LibraryScope
@Component(dependencies = ActivityComponent.class, modules = LibraryModule.class)
public interface LibraryComponent {
  /* Subcomponent */
  MusicLibraryComponent plus(MusicLibraryModule musicLibraryModule);

  /* Dependency objects extended by constructor injections */


  /* Dependency objects provided from modules and dependencies */


  /* Field injection */
  void inject(LibraryActivity libraryActivity);


}
