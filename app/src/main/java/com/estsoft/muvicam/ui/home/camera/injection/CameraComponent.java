package com.estsoft.muvicam.ui.home.camera.injection;

import dagger.Subcomponent;
import com.estsoft.muvicam.ui.home.camera.CameraFragment;

/**
 * Created by jaylim on 12/12/2016.
 */

@CameraScope
@Subcomponent(modules = CameraModule.class)
public interface CameraComponent {

  /* Subcomponent */


  /* Dependency objects extended by constructor injections */


  /* Dependency objects provided from modules and dependencies */


  /* Field injection */
  void inject(CameraFragment cameraFragment);
}
