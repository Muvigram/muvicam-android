package com.estsoft.muvicam.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.estsoft.muvicam.R;

/**
 * Created by jaylim on 08/02/2017.
 */

public abstract class BaseMultiFragmentActivity extends BaseActivity {

  private Fragment[] mFragments;

  protected abstract Fragment[] setFragments();

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // set fragments
    mFragments = setFragments();

    // Inflate activity fragment layout
    setContentView(R.layout.activity_fragment_container);

    // Initially, inflate 0th fragment layout.
    FragmentManager fragmentManager = getSupportFragmentManager();
    Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);

    if (fragment == null) {
      fragment = mFragments[0];
      fragmentManager.beginTransaction()
          .add(R.id.fragment_container, fragment)
          .commit();
    }
  }

  final public void navigate(Class<? extends Fragment> klass) {
    Fragment newFragment = findFragment(mFragments, klass);
    if (newFragment == null) {
      return;
    }

    FragmentManager fragmentManager = getSupportFragmentManager();
    transactFragment(fragmentManager, newFragment);
  }

  final public void navigate(int index) {
    if (index < 0 || index >= mFragments.length) {
      return;
    }
    Fragment newFragment = mFragments[index];

    FragmentManager fragmentManager = getSupportFragmentManager();
    transactFragment(fragmentManager, newFragment);
  }

  private static Fragment findFragment(Fragment[] fragments, Class<? extends Fragment> klass) {
    for (int i = 0; i < fragments.length; i++) {
      if (fragments[i].getClass().equals(klass)) {
        return fragments[i];
      }
    }
    return null;
  }

  private static void transactFragment(FragmentManager fm, Fragment newFragment) {

    Fragment fragment = fm.findFragmentById(R.id.fragment_container);
    if (fragment != null) {
      fm.beginTransaction()
          .remove(fragment)
          .commit();
    }

    fm.beginTransaction()
        .replace(R.id.fragment_container, newFragment)
        .commit();
  }

}
