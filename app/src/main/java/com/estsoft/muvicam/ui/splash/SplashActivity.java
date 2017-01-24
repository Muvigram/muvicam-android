package com.estsoft.muvicam.ui.splash;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.view.Window;
import android.view.WindowManager;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.ui.base.BaseActivity;
import com.estsoft.muvicam.ui.home.HomeActivity;
import com.estsoft.muvicam.ui.home.camera.temp.PermissionManager;

import timber.log.Timber;

/**
 * Created by jaylim on 24/01/2017.
 */

public class SplashActivity extends BaseActivity {

  private PermissionManager mPermissionManager;
  final String PERMISSION_DIALOG = "permissionDialog";

  private boolean isRequestIgnored = false;

  @Override
  public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
    // Set fullscreen mode
    setupFullscreen();
    super.onCreate(savedInstanceState, persistentState);

    setContentView(R.layout.activity_splash);

  }

  @Override
  protected void onStart() {
    super.onStart();

  }

  @Override
  protected void onResume() {
    super.onResume();

    // Runtime Permission
    getPermissionManager();
    requestPermission();
  }

  public void getPermissionManager() {

    final int REQUEST_VIDEO_PERMISSIONS = 1;

    final String[] VIDEO_PERMISSIONS = {
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    mPermissionManager = new PermissionManager(this, VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS);
  }


  public void requestPermission() {
    if (!mPermissionManager.hasPermissionsGranted()) {
      if (mPermissionManager.shouldShowRequestPermissionsRationale()) {
        mPermissionManager
            .createConfirmationDialog("This application needs permission for camera, audio, and storage access.")
            .show(getSupportFragmentManager(), PERMISSION_DIALOG);

      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (isRequestIgnored) {
          mPermissionManager
              .createDetailSettingDialog(getString(R.string.splash_request_permission_denied))
              .show(getSupportFragmentManager(), PERMISSION_DIALOG);
        } else {
          requestPermissions(mPermissionManager.getPermissions(), mPermissionManager.getRequestCode());
        }
      } else {
        mPermissionManager
            .createDetailSettingDialog(getString(R.string.splash_request_permission_denied))
            .show(getSupportFragmentManager(), PERMISSION_DIALOG);
      }

    } else { // All permissions has already been granted.
      if (isRequestIgnored) {
        mPermissionManager
            .createDetailSettingDialog(getString(R.string.splash_request_permission_denied))
            .show(getSupportFragmentManager(), PERMISSION_DIALOG);
      }
      startActivity(HomeActivity.newIntent(this));
    }
  }



  @Override
  public void onRequestPermissionsResult(int requestCode,
                                         @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {

    Timber.e("%d\n", requestCode);
    for (String s : permissions) {
      Timber.e("%s\n", s);
    }
    for (int i : grantResults) {
      Timber.e("%d\n", i);
    }

    if (requestCode == mPermissionManager.getRequestCode() &&
        grantResults.length == mPermissionManager.getPermissions().length) {

      for (int result : grantResults) {
        if (result != PackageManager.PERMISSION_GRANTED) {
          isRequestIgnored = true;
          return;
        }
      }

      startActivity(HomeActivity.newIntent(this));

    }

  }

  public void setupFullscreen() {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(
        WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN
    );
  }


}
