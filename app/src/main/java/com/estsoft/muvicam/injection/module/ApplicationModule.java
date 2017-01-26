package com.estsoft.muvicam.injection.module;

import android.app.Application;
import android.content.Context;

import dagger.Module;
import dagger.Provides;
import com.estsoft.muvicam.data.local.MusicService;
import com.estsoft.muvicam.injection.qualifier.ApplicationContext;

/**
 * Created by jaylim on 12/12/2016.
 */

@Module
public class ApplicationModule {
  private final Application mApplication;

  public ApplicationModule(Application application) {
    mApplication = application;
  }

  /* Android System */
  @Provides
  public Application provideApplication() {
    return mApplication;
  }

  @Provides
  @ApplicationContext
  public Context provideContext() {
    return mApplication;
  }

  /* Explicit Functionality */


  /* Implicit Functionality */
  @Provides
  public MusicService provideMusicService() {
    return new MusicService(mApplication);
  }
}
