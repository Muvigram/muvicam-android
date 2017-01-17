package com.estsoft.muvicam.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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

    // set fullscreen mode
    setFullscreen();
    setUpDecorView();

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
  protected void onResume() {
    super.onResume();
    startBackgroundThread();
    hideDecorView();
  }

  @Override
  protected void onPause() {
    mBackgroundHandler.removeCallbacksAndMessages(null);
    stopBackgroundThread();
    super.onPause();
  }

  @Override
  protected void onStop() {
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    if (mHomeComponent != null) {
      mHomeComponent = null;
    }
    super.onDestroy();
  }

  // DISPLAY SETTING //////////////////////////////////////////////////////////////


  public void setFullscreen() {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(
        WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN
    );
  }

  View mDecorView;
  private final static int DEFAULT_UI_SETTING = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
          | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
          | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
          | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
          | View.SYSTEM_UI_FLAG_FULLSCREEN      // hide status bar
          | View.SYSTEM_UI_FLAG_IMMERSIVE;

  Runnable hideDecorView;

  public void setUpDecorView() {
    mDecorView = getWindow().getDecorView();
    hideDecorView = this::hideDecorView;

    mDecorView.setOnSystemUiVisibilityChangeListener(visibility -> {
      Timber.e("onSystemUiVisibilityChange");
      int xor = DEFAULT_UI_SETTING ^ visibility;
      if (xor != 0) {
        mBackgroundHandler.post(hideDecorView);
      }
    });
  }

  public void hideDecorView() {
    new Handler(getMainLooper()).postDelayed(
        () -> mDecorView.setSystemUiVisibility(DEFAULT_UI_SETTING),
        1500
    );
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
      ((CameraFragment) mPagerAdapter.getItem(PAGE_CAMERA)).updateMusic(music);
      // TODO - might be deleted before deployment
      Toast.makeText(this, "Music selected : " + music.uri().toString(), Toast.LENGTH_SHORT).show();
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

  // HANDLER ////////////////////////////////////////////////////////////////////////

  private final static String BACKGROUND_HANDLER_THREAD = "BACKGROUND_HANDLER";

  HandlerThread mBackgroundThread;
  Handler mBackgroundHandler;

  private void startBackgroundThread() {
    mBackgroundThread = new HandlerThread(BACKGROUND_HANDLER_THREAD);
    mBackgroundThread.start();
    mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
  }

  private void stopBackgroundThread() {
    mBackgroundThread.quitSafely();
    try {
      // Waits forever for this thread to die.
      mBackgroundThread.join();
      mBackgroundThread = null;
      mBackgroundHandler = null;
    } catch (InterruptedException e) {
      e.printStackTrace();
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


