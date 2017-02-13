package com.estsoft.muvigram;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.estsoft.muvigram.injection.component.ApplicationComponent;
import com.estsoft.muvigram.injection.component.DaggerApplicationComponent;
import com.estsoft.muvigram.injection.module.ApplicationModule;
import com.estsoft.muvigram.util.CrashlythicsTree;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

/**
 * The custom application class to scope it with dagger.
 * <p>
 * Created by jaylim on 12/12/2016.
 */

public class MuvigramApplication extends Application {

  ApplicationComponent mApplicationComponent;

  public static MuvigramApplication get(Context context) {
    return (MuvigramApplication) context.getApplicationContext();
  }

  @Override
  public void onCreate() {
    super.onCreate();

    // Crashlytics
    CrashlyticsCore core = new CrashlyticsCore.Builder()
        .disabled(BuildConfig.DEBUG)
        .build();
    Fabric.with(this, new Crashlytics.Builder().core(core).build());

    // Timber
    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
    }
    Timber.plant(new CrashlythicsTree());

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
