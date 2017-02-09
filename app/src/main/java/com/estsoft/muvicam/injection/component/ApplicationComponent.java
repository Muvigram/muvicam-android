package com.estsoft.muvicam.injection.component;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Component;

import com.estsoft.muvicam.MuvicamApplication;
import com.estsoft.muvicam.injection.module.ActivityModule;
import com.estsoft.muvicam.injection.module.ApplicationModule;
import com.estsoft.muvicam.injection.qualifier.ApplicationContext;
import com.estsoft.muvicam.util.RxEventBus;

/**
 * Created by jaylim on 12/12/2016.
 */

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

  /* Subcomponent */
  ActivityComponent plus(ActivityModule activityModule);

  /* Dependency objects extended by constructor injections */

  /* Dependency objects provided from modules and dependencies */
  // @ApplicationContext RxEventBus globalBus();
  // @ApplicationContext Context context();
  // Application application();

  /* Field injection */
}

