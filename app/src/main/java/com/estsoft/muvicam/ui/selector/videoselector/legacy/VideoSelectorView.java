package com.estsoft.muvicam.ui.selector.videoselector.legacy;

import com.estsoft.muvicam.ui.base.BasePresenter;
import com.estsoft.muvicam.ui.base.MvpView;

/**
 * Created by Administrator on 2017-01-06.
 */

public interface VideoSelectorView extends MvpView {

    void setPresent(BasePresenter basePresenter);
    void setupVideoSelectorAdapter();
}
