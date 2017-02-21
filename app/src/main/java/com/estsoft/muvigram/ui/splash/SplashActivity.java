package com.estsoft.muvigram.ui.splash;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.estsoft.muvigram.R;
import com.estsoft.muvigram.ui.base.BaseActivity;
import com.estsoft.muvigram.ui.home.HomeActivity;

/**
 * Created by jaylim on 24/01/2017.
 */

public class SplashActivity extends BaseActivity {

  private PermissionManager mPermissionManager;
  final String PERMISSION_DIALOG = "permissionDialog";

  private static final int SPLASH_TIME_OUT = 1000;

  private boolean isRequestIgnored = false;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);

  }

  @Override
  protected void onResume() {
    super.onResume();

    // Runtime Permission
    setupPermissionManager();
    requestPermission();
  }

  public void setupPermissionManager() {

    final int REQUEST_VIDEO_PERMISSIONS = 1;

    final String[] VIDEO_PERMISSIONS = {
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.INTERNET
    };

    mPermissionManager = new PermissionManager(this, VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS);
  }


  public void requestPermission() {
    if (!mPermissionManager.hasPermissionsGranted()) {
      if (mPermissionManager.shouldShowRequestPermissionsRationale()) {
        // A detailed explanation of one or more permission requests is required.
        mPermissionManager
            .createDetailDialog(getString(R.string.splash_request_permission_detail))
            .show(getSupportFragmentManager(), PERMISSION_DIALOG);

      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // No need detailed explanation. Version M or higher required.
        if (isRequestIgnored) {
          mPermissionManager
              .createSettingDialog(getString(R.string.splash_request_permission_denied))
              .show(getSupportFragmentManager(), PERMISSION_DIALOG);
        } else {
          requestPermissions(mPermissionManager.getPermissions(), mPermissionManager.getRequestCode());
        }
      } else {
        mPermissionManager
            .createSettingDialog(getString(R.string.splash_request_permission_denied))
            .show(getSupportFragmentManager(), PERMISSION_DIALOG);
      }

    } else {
      startMainActivity();
    }
  }

  public void startMainActivity() {
    new Thread(startMainActivity).start();
  }

  Runnable startMainActivity = () -> {
    try {
      Thread.sleep(SPLASH_TIME_OUT);
      startActivity(HomeActivity.getIntent(SplashActivity.this));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  };

  @Override
  public void onRequestPermissionsResult(int requestCode,
                                         @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {

    if (requestCode == mPermissionManager.getRequestCode() &&
        grantResults.length == mPermissionManager.getPermissions().length) {
      for (int result : grantResults) {
        if (result != PackageManager.PERMISSION_GRANTED) {
          isRequestIgnored = true;
          return;
        }
      }
      startMainActivity();
    }
  }
}
