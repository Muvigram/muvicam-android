package com.estsoft.muvigram.ui.selector.videoselector.legacy;

import com.estsoft.muvigram.ui.base.BasePresenter;
import com.estsoft.muvigram.ui.base.MvpView;

/**
 * Created by Administrator on 2017-01-06.
 */

public interface LVideoSelectorView extends MvpView {

    void setPresent(BasePresenter basePresenter);
    void setupVideoSelectorAdapter();
}
