package com.estsoft.muvicam.injection.module;

import android.app.Application;
import android.content.Context;

import dagger.Module;
import dagger.Provides;
import com.estsoft.muvicam.data.local.MusicService;
import com.estsoft.muvicam.injection.qualifier.ApplicationContext;
import com.estsoft.muvicam.util.RxEventBus;

import javax.inject.Singleton;

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
  @Provides
  @Singleton
  @ApplicationContext
  public RxEventBus provideGlobalEventBus() {
    return new RxEventBus();
  }

  /* Implicit Functionality */
  @Provides
  @Singleton
  public MusicService provideMusicService() {
    return new MusicService(mApplication);
  }
}
