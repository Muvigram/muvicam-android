package com.estsoft.muvigram.injection.component;

import com.estsoft.muvigram.injection.module.SelectorModule;
import com.estsoft.muvigram.injection.scope.SelectorScope;
import com.estsoft.muvigram.ui.selector.SelectorActivity;
import com.estsoft.muvigram.ui.selector.videoselector.injection.VideoSelectorComponent;
import com.estsoft.muvigram.ui.selector.videoselector.injection.VideoSelectorModule;

import dagger.Component;

/**
 * Created by estsoft on 2017-02-02.
 */

@SelectorScope
@Component(dependencies = ActivityComponent.class, modules = SelectorModule.class)
public interface SelectorComponent {

    VideoSelectorComponent plus(VideoSelectorModule videoSelectorModule);

    void inject( SelectorActivity selectorActivity );

}