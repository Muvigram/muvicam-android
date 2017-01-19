package com.estsoft.muvicam.ui.share;

import com.estsoft.muvicam.ui.base.BasePresenter;
import com.estsoft.muvicam.ui.share.injection.ShareScope;

import javax.inject.Inject;

/**
 * Created by estsoft on 2017-01-19.
 */

@ShareScope
public class SharePresenter extends BasePresenter<ShareMvpView>{

    @Inject
    public SharePresenter() {}

    @Override
    public void detachView() {
        super.detachView();
    }


}
