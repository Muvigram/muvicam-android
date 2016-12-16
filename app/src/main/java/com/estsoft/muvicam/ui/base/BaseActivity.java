package com.estsoft.muvicam.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.estsoft.muvicam.MuvicamApplication;
import com.estsoft.muvicam.injection.component.ActivityComponent;
import com.estsoft.muvicam.injection.module.ActivityModule;

/**
 * Created by jaylim on 12/12/2016.
 */

public abstract class BaseActivity extends AppCompatActivity {

  ActivityComponent mActivityComponent;

  protected ActivityComponent getActivityComponent() {
    return mActivityComponent;
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mActivityComponent = MuvicamApplication.get(this)
        .getApplicationComponent()
        .plus(new ActivityModule(this));
  }
}
