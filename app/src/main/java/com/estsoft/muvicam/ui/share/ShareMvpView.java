package com.estsoft.muvicam.ui.share;

import android.graphics.Bitmap;

import com.estsoft.muvicam.ui.base.MvpView;

/**
 * Created by estsoft on 2017-01-19.
 */

public interface ShareMvpView extends MvpView {

    void showToast( String msg );
    void showShareBottomSheet();
    void updateProgress( float progress, boolean isFinished );
    void holdFirstThumbnail(Bitmap bitmap );
    void videoSetAndStart( String videoPath, int durationMs );
}
