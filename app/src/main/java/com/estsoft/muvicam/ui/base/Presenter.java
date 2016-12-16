package com.estsoft.muvicam.ui.base;

/**
 * Created by gangGongUi on 2016. 10. 8..
 */
public interface Presenter<V extends MvpView> {

    void attachView(V mvpView);

    void detachView();
}
