package com.estsoft.muvicam.injection.component;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Component;
import com.estsoft.muvicam.injection.module.ActivityModule;
import com.estsoft.muvicam.injection.module.ApplicationModule;
import com.estsoft.muvicam.injection.qualifier.ApplicationContext;

/**
 * Created by jaylim on 12/12/2016.
 */

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

  /* Subcomponent */
  ActivityComponent plus(ActivityModule activityModule);

  /* Dependency objects extended by constructor injections */
  // TODO - DataManager

  /* Dependency objects provided from modules and dependencies */
  @ApplicationContext Context context();
  Application application();

  /* Field injection */
}

