package com.estsoft.muvigram.ui.share.injection;

import com.estsoft.muvigram.ui.share.ShareFragment;

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
