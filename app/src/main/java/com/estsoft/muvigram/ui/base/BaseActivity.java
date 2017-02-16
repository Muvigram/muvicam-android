package com.estsoft.muvigram.ui.base;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.estsoft.muvigram.MuvigramApplication;
import com.estsoft.muvigram.injection.component.ActivityComponent;
import com.estsoft.muvigram.injection.module.ActivityModule;
import com.jakewharton.rxbinding.view.RxView;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by jaylim on 12/12/2016.
 */

public abstract class BaseActivity extends AppCompatActivity {

  ActivityComponent mActivityComponent;

  protected ActivityComponent getActivityComponent() {
    return mActivityComponent;
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    // Set fullscreen mode
    setupFullscreen();
    setUpDecorView();

    super.onCreate(savedInstanceState);

    mActivityComponent = MuvigramApplication.get(this)
        .getApplicationComponent()
        .plus(new ActivityModule(this));
  }

  @Override
  protected void onResume() {
    super.onResume();
    mDecorView.setSystemUiVisibility(DEFAULT_UI_SETTING);
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  public void setupFullscreen() {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(
        WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN
    );
  }

  View mDecorView;
  private final static int DEFAULT_UI_SETTING =
      View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
      | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
      | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
      | View.SYSTEM_UI_FLAG_FULLSCREEN
      | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

  public void setUpDecorView() {
    mDecorView = getWindow().getDecorView();
    RxView.systemUiVisibilityChanges(mDecorView)
        .observeOn(Schedulers.computation())
        .map(visibility -> {
          Timber.v("onSystemUiVisibilityChange %08x / %08x", visibility, DEFAULT_UI_SETTING);
          return DEFAULT_UI_SETTING ^ visibility;
        })
        .filter(differ -> differ != 0)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(differ -> {
          new Thread(hideDecorView).start();
        });
  }

  Runnable hideDecorView = new Runnable() {
    @Override
    public void run() {
      try {
        Thread.sleep(3000);
        new Handler(getMainLooper()).post(
            () -> mDecorView.setSystemUiVisibility(DEFAULT_UI_SETTING)
        );
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  };

  // HANDLER ////////////////////////////////////////////////////////////////////////

}
