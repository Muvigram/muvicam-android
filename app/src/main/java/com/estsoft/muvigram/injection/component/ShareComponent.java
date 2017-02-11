package com.estsoft.muvigram.injection.component;

import com.estsoft.muvigram.injection.module.ShareModule;
import com.estsoft.muvigram.injection.scope.ShareScope;
import com.estsoft.muvigram.ui.share.ShareActivity;
import com.estsoft.muvigram.ui.share.injection.ShareMediaComponent;
import com.estsoft.muvigram.ui.share.injection.ShareMediaModule;

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
