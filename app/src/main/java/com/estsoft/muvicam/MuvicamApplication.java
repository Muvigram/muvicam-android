package com.estsoft.muvicam;

import android.app.Application;
import android.content.Context;

import com.estsoft.muvicam.injection.component.ApplicationComponent;
import com.estsoft.muvicam.injection.component.DaggerApplicationComponent;
import com.estsoft.muvicam.injection.module.ApplicationModule;
import timber.log.Timber;

/**
 * The custom application class to scope it with dagger.
 * <p>
 * Created by jaylim on 12/12/2016.
 */

public class MuvicamApplication extends Application {

  ApplicationComponent mApplicationComponent;

  public static MuvicamApplication get(Context context) {
    return (MuvicamApplication) context.getApplicationContext();
  }

  @Override
  public void onCreate() {
    super.onCreate();
    Timber.plant(new Timber.DebugTree());
  }

  public ApplicationComponent getApplicationComponent() {
    if (mApplicationComponent == null) {
      mApplicationComponent = DaggerApplicationComponent.builder()
          .applicationModule(new ApplicationModule(this))
          .build();
    }
    return mApplicationComponent;
  }
}
