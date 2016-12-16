package com.estsoft.muvicam.injection.component;

import com.estsoft.muvicam.injection.component.ActivityComponent;

import dagger.Component;
import com.estsoft.muvicam.injection.module.HomeModule;
import com.estsoft.muvicam.injection.scope.HomeScope;
import com.estsoft.muvicam.ui.home.HomeActivity;
import com.estsoft.muvicam.ui.home.camera.injection.CameraComponent;
import com.estsoft.muvicam.ui.home.camera.injection.CameraModule;
import com.estsoft.muvicam.ui.home.music.injection.MusicComponent;
import com.estsoft.muvicam.ui.home.music.injection.MusicModule;

/**
 * Created by jaylim on 12/12/2016.
 */

@HomeScope
@Component(dependencies = ActivityComponent.class, modules = HomeModule.class)
public interface HomeComponent {
  /* Subcomponent */
  CameraComponent plus(CameraModule cameraModule);
  MusicComponent plus(MusicModule musicModule);

  /* Dependency objects extended by constructor injections */


  /* Dependency objects provided from modules and dependencies */


  /* Field injection */
  void inject(HomeActivity homeActivity);
}
