package com.estsoft.muvicam.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.estsoft.muvicam.injection.component.DaggerHomeComponent;
import com.estsoft.muvicam.injection.component.HomeComponent;
import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.Music;
import com.estsoft.muvicam.ui.base.BaseActivity;
import com.estsoft.muvicam.ui.home.camera.CameraFragment;
import com.estsoft.muvicam.ui.home.camera.ControllableViewPager;
import com.estsoft.muvicam.ui.home.camera.MusicCutFragment;
import com.estsoft.muvicam.ui.home.music.MusicFragment;

import java.util.Arrays;
import java.util.Collections;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 *
 *
 * Created by jaylim on 12/12/2016.
 */
public class HomeActivity extends BaseActivity {

  private final static int PAGE_MUSIC = 0;
  private final static int PAGE_CAMERA = 1;

  private HomeComponent mHomeComponent;

  public HomeComponent getComponent() {
    return mHomeComponent;
  }

  public static Intent newIntent(Context packageContext) {
    Intent intent = new Intent(packageContext, HomeActivity.class);
    return intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
  }

  public static HomeActivity get(Fragment fragment) {
    return (HomeActivity) fragment.getActivity();
  }

  public static HomeActivity get(View view) {
    return (HomeActivity) view.getContext();
  }

  @Inject HomePagerAdapter mPagerAdapter;

  @BindView(R.id.home_view_pager) ControllableViewPager mViewPager;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

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

  /* Restore state, when restart activity. */
  @Override
  protected void onStart() {
    super.onStart();
    if (isScrollable()) {
      enableScroll();
    } else {
      disableScroll();
    }
  }

  @Override
  protected void onDestroy() {
    if (mHomeComponent != null) {
      mHomeComponent = null;
    }
    super.onDestroy();
  }

  // VIEW PAGER //////////////////////////////////////////////////////////////

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

  // TRANSITIONS ////////////////////////////////////////////////////////////////////////

  private boolean isCuttingVideo;

  public void setCuttingVideo(boolean cuttingVideo) {
    isCuttingVideo = cuttingVideo;
  }

  public void selectMusic(Music music) {
    if (music != null) {
      ((CameraFragment) mPagerAdapter.getItem(PAGE_CAMERA)).changeMusic(music);
    }
    mViewPager.setCurrentItem(PAGE_CAMERA);
  }

  @Override
  public void onBackPressed() {
    if (mViewPager.getCurrentItem() == PAGE_CAMERA) {
      if (isCuttingVideo) {
        CameraFragment childFragment = (CameraFragment) mPagerAdapter.getItem(PAGE_CAMERA);
        FragmentManager cfm = childFragment.getChildFragmentManager();
        MusicCutFragment fragment = (MusicCutFragment) cfm.findFragmentById(R.id.camera_container_music_cut);
        fragment._cancelMusicCut();
      } else {
        super.onBackPressed();
      }
    } else {
      mViewPager.setCurrentItem(PAGE_CAMERA);
    }
  }

  // SCROLLABLE ////////////////////////////////////////////////////////////////////////

  public boolean isScrollable() {
    return !(mViewPager.getCurrentItem() == PAGE_CAMERA) || mViewPager.isScrollable();
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


