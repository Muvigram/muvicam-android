package com.estsoft.muvicam.ui.home;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.estsoft.muvicam.injection.scope.HomeScope;
import com.estsoft.muvicam.ui.home.camera.CameraFragment;

/**
 * Created by jaylim on 12/12/2016.
 */

@HomeScope
public class HomePagerAdapter extends FragmentStatePagerAdapter {

  private List<Fragment> mFragmentList;

  @Inject
  public HomePagerAdapter(FragmentManager fm) {
    super(fm);
    mFragmentList = new ArrayList<>();
  }

  public void setFragmentList(List<Fragment> fragmentList) {
    mFragmentList = fragmentList;
  }

  @Override
  public Fragment getItem(int position) {
    return mFragmentList.get(position);
  }

  @Override
  public int getCount() {
    return mFragmentList.size();
  }
}
