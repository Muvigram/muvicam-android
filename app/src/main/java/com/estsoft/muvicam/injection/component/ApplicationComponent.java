package com.estsoft.muvicam.injection.component;

import javax.inject.Singleton;

import dagger.Component;

import com.estsoft.muvicam.injection.module.ActivityModule;
import com.estsoft.muvicam.injection.module.ApplicationModule;

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

