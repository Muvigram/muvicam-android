package com.estsoft.muvicam.ui.share.injection;

import com.estsoft.muvicam.injection.component.ActivityComponent;
import com.estsoft.muvicam.ui.share.ShareFragment;

import dagger.Component;

/**
 * Created by estsoft on 2017-01-19.
 */

@ShareScope
@Component ( dependencies = ActivityComponent.class, modules = ShareModule.class )
public interface ShareComponent {
    /* Subcomponent */

  /* Dependency objects extended by constructor injections */


  /* Dependency objects provided from modules and dependencies */


    /* Field injection */
    void inject(ShareFragment shareFragment);

}
