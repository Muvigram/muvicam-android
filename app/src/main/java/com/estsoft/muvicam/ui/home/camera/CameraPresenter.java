package com.estsoft.muvicam.ui.home.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.ActivityCompat;

import javax.inject.Inject;

import com.estsoft.muvicam.data.DataManager;
import com.estsoft.muvicam.injection.qualifier.ActivityContext;
import com.estsoft.muvicam.ui.base.BasePresenter;
import com.estsoft.muvicam.ui.home.HomeActivity;
import com.estsoft.muvicam.ui.home.camera.injection.CameraScope;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by jaylim on 12/12/2016.
 */

// @CameraScope
public class CameraPresenter extends BasePresenter<CameraMvpView> {

  // @Inject
  public CameraPresenter() {}

}
