package com.estsoft.muvicam.ui.home;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.estsoft.muvicam.injection.component.DaggerHomeComponent;
import com.estsoft.muvicam.injection.component.HomeComponent;
import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.Music;
import com.estsoft.muvicam.ui.base.BaseActivity;
import com.estsoft.muvicam.ui.home.camera.CameraFragment;
import com.estsoft.muvicam.ui.home.camera.ControllableViewPager;
import com.estsoft.muvicam.ui.home.music.MusicFragment;

import java.util.Arrays;
import java.util.Collections;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by jaylim on 12/12/2016.
 */

public class HomeActivity extends BaseActivity {

  private final static String TAG = HomeActivity.class.getSimpleName();

  private final static String EXTRA_MUSIC_URI = "HomeActivity.musicUri";

  private final static int PAGE_MUSIC = 0;
  private final static int PAGE_CAMERA = 1;

  private HomeComponent mHomeComponent;

  public static Intent newIntent(Context packageContext) {
    return new Intent(packageContext, HomeActivity.class);
  }

  public static Intent newIntent(Context packageContext, Uri musicUri) {
    Intent intent = new Intent(packageContext, HomeActivity.class);
    intent.putExtra(EXTRA_MUSIC_URI, musicUri);
    return intent;
  }

  public static HomeActivity get(Fragment fragment) {
    return (HomeActivity) fragment.getActivity();
  }


  @Inject
  HomePagerAdapter mPagerAdapter;

  @BindView(R.id.home_view_pager)
  ControllableViewPager mViewPager;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // set fullscreen mode
    setFullscreen();

    // bind view
    setContentView(R.layout.activity_home);
    ButterKnife.bind(this);

    // create component and inject dependencies
    mHomeComponent = DaggerHomeComponent.builder()
        .activityComponent(getActivityComponent()).build();
    mHomeComponent.inject(this);

    // set pager adapter
    setUpViewPager();
  }

  @Override
  protected void onDestroy() {
    if (mHomeComponent != null) {
      mHomeComponent = null;
    }
    super.onDestroy();
  }


  public void setFullscreen() {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(
        WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN
    );
  }

  public void setUpViewPager() {
    mPagerAdapter.setFragmentList(
        Collections.unmodifiableList(Arrays.asList(
            MusicFragment.newInstance(),
            CameraFragment.newInstance()
        ))
    );
    mViewPager.setAdapter(mPagerAdapter);
    mViewPager.setCurrentItem(PAGE_CAMERA);
  }

  public void backToCamera(Music music) {
    if (music != null) {
      ((CameraFragment) mPagerAdapter.getItem(1)).updateMusic(music);
      Toast.makeText(this, "Music selected : " + music.uri().toString(), Toast.LENGTH_SHORT).show();
    }
    mViewPager.setCurrentItem(PAGE_CAMERA);
  }

  public HomeComponent getComponent() {
    return mHomeComponent;
  }

  @Override
  public void onBackPressed() {
    if (mViewPager.getCurrentItem() == PAGE_CAMERA) {
      super.onBackPressed();
    } else {
      backToCamera(null);
    }
  }

  public void enableScroll() {
    if(mViewPager.getCurrentItem() == PAGE_CAMERA) {
      mViewPager.enableScroll();
    }
  }

  public void disableScroll() {
    if(mViewPager.getCurrentItem() == PAGE_CAMERA) {
      mViewPager.disableScroll();
    }
  }
}
