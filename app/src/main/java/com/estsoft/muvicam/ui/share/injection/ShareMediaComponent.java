package com.estsoft.muvicam.ui.share.injection;

import com.estsoft.muvicam.injection.component.ActivityComponent;
import com.estsoft.muvicam.ui.share.ShareFragment;

import dagger.Component;
import dagger.Subcomponent;

/**
 * Created by estsoft on 2017-01-19.
 */

@ShareMediaScope
@Subcomponent ( modules = ShareMediaModule.class )
public interface ShareMediaComponent {
    /* Subcomponent */

  /* Dependency objects extended by constructor injections */


  /* Dependency objects provided from modules and dependencies */


    /* Field injection */
    void inject(ShareFragment shareFragment);

}
