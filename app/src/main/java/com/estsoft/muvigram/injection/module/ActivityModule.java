package com.estsoft.muvigram.injection.module;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentManager;

import dagger.Module;
import dagger.Provides;
import com.estsoft.muvigram.injection.qualifier.ActivityContext;
import com.estsoft.muvigram.injection.scope.ActivityScope;
import com.estsoft.muvigram.ui.base.BaseActivity;
import com.estsoft.muvigram.util.rx.RxEventBus;

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
