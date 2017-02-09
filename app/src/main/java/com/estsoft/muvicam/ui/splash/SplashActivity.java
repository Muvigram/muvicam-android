package com.estsoft.muvicam.ui.splash;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.ui.base.BaseActivity;
import com.estsoft.muvicam.ui.home.HomeActivity;

import timber.log.Timber;

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
    //ImageView logoImage = (ImageView) findViewById(R.id.splash_logo_image);
    //logoImage.setBackgroundResource(R.drawable.splash_w);

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
        Manifest.permission.WRITE_EXTERNAL_STORAGE
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
    final Activity activity = this;
    new Handler().postDelayed(() -> {
      startActivity(HomeActivity.newIntent(SplashActivity.this));
    }, SPLASH_TIME_OUT);
  }

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
