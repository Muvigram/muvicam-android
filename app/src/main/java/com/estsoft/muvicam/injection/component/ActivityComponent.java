package com.estsoft.muvicam.injection.component;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.v4.app.FragmentManager;

import dagger.Subcomponent;
import com.estsoft.muvicam.data.DataManager;
import com.estsoft.muvicam.injection.module.ActivityModule;
import com.estsoft.muvicam.injection.qualifier.ActivityContext;
import com.estsoft.muvicam.injection.qualifier.ApplicationContext;
import com.estsoft.muvicam.injection.scope.ActivityScope;
import com.estsoft.muvicam.util.RxEventBus;

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
