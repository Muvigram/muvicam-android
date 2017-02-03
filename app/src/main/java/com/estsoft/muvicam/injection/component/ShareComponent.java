package com.estsoft.muvicam.injection.component;

import com.estsoft.muvicam.injection.module.ShareModule;
import com.estsoft.muvicam.injection.scope.ShareScope;
import com.estsoft.muvicam.ui.share.ShareActivity;
import com.estsoft.muvicam.ui.share.injection.ShareMediaComponent;
import com.estsoft.muvicam.ui.share.injection.ShareMediaModule;

import dagger.Component;

/**
 * Created by estsoft on 2017-02-03.
 */

@ShareScope
@Component(dependencies = ActivityComponent.class, modules = ShareModule.class)
public interface ShareComponent {

    ShareMediaComponent plus( ShareMediaModule shareMediaModule );
    void inject(ShareActivity shareActivity);

}
