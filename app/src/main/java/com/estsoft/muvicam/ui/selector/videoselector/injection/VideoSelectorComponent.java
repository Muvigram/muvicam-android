package com.estsoft.muvicam.ui.selector.videoselector.injection;

import com.estsoft.muvicam.ui.selector.videoselector.VideoSelectorFragment;

import dagger.Subcomponent;

/**
 * Created by estsoft on 2017-02-02.
 */

@VideoSelectorScope
@Subcomponent( modules = VideoSelectorModule.class )
public interface VideoSelectorComponent {

    void inject(VideoSelectorFragment videoSelectorFragment);
}
