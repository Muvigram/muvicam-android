package com.estsoft.muvicam.injection.module;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentManager;

import dagger.Module;
import dagger.Provides;
import com.estsoft.muvicam.injection.qualifier.ActivityContext;
import com.estsoft.muvicam.injection.qualifier.ApplicationContext;
import com.estsoft.muvicam.injection.scope.ActivityScope;
import com.estsoft.muvicam.ui.base.BaseActivity;
import com.estsoft.muvicam.util.RxEventBus;

import javax.inject.Singleton;

/**
 * Created by jaylim on 12/12/2016.
 */

@Module
public class ActivityModule {

  private final Activity mActivity;

  public ActivityModule(Activity activity) {
    mActivity = activity;
  }

  /* Android System */
  @Provides
  Activity provideActivity() {
    return mActivity;
  }

  @ActivityContext
  @Provides
  Context provideContext() {
    return mActivity;
  }

  @Provides
  FragmentManager provideFragmentManager() {
    return ((BaseActivity) mActivity).getSupportFragmentManager();
  }

  /* Explicit Functionality */
  @Provides
  @ActivityScope
  @ActivityContext
  public RxEventBus provideLocalEventBus() {
    return new RxEventBus();
  }


  /* Implicit Functionality */


}
