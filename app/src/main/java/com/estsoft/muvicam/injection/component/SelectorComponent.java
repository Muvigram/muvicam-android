package com.estsoft.muvicam.injection.component;

import com.estsoft.muvicam.injection.module.SelectorModule;
import com.estsoft.muvicam.injection.scope.SelectorScope;
import com.estsoft.muvicam.ui.selector.SelectorActivity;
import com.estsoft.muvicam.ui.selector.injection.VideoSelectorComponent;
import com.estsoft.muvicam.ui.selector.injection.VideoSelectorModule;

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