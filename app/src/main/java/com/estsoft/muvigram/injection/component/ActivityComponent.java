package com.estsoft.muvigram.injection.component;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.v4.app.FragmentManager;

import dagger.Subcomponent;
import com.estsoft.muvigram.data.DataManager;
import com.estsoft.muvigram.injection.module.ActivityModule;
import com.estsoft.muvigram.injection.qualifier.ActivityContext;
import com.estsoft.muvigram.injection.qualifier.ApplicationContext;
import com.estsoft.muvigram.injection.scope.ActivityScope;
import com.estsoft.muvigram.util.RxEventBus;

/**
 * Created by jaylim on 12/12/2016.
 */

@ActivityScope
@Subcomponent(modules = ActivityModule.class)
public interface ActivityComponent {

  /* Subcomponent */

  /* Dependency objects extended by constructor injections */
  DataManager dataManager();

  /* Dependency objects provided from modules and dependencies */

  // Event bus
  @ApplicationContext RxEventBus globalBus();
  @ActivityContext RxEventBus localBus();

  // Context
  @ApplicationContext Context applicationContext();
  @ActivityContext Context activityContext();

  Application application();
  Activity activity();


  FragmentManager fragmentManager();

  /* Field injection */

}
