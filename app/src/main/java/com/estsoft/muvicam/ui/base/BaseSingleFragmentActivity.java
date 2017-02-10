package com.estsoft.muvicam.ui.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.estsoft.muvicam.R;

/**
 * Created by jaylim on 12/12/2016.
 */

public abstract class BaseSingleFragmentActivity extends BaseActivity {

  protected abstract Fragment createDefaultFragment();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Inflate activity fragment layout
    setContentView(R.layout.activity_fragment_container);

    // Get fragment manager and find fragment view object
    FragmentManager fragmentManager = getSupportFragmentManager();
    Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);

    if (fragment == null) {
      fragment = createDefaultFragment();
      fragmentManager.beginTransaction()
          .add(R.id.fragment_container, fragment)
          .commit();
    }
  }

  }
