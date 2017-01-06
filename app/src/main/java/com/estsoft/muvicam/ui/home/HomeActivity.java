package com.estsoft.muvicam.ui.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Camera;
import android.net.Uri;
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
    setDecorView();


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

  /* Restore state from restart activity. */
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
  protected void onStop() {
    stopBackgroundThread();
    super.onStop();
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

  View mDecorView;
  private final static int DEFAULT_UI_SETTING = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
          | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
          | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
          | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
          | View.SYSTEM_UI_FLAG_FULLSCREEN      // hide status bar
          | View.SYSTEM_UI_FLAG_IMMERSIVE;

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

  public void setDecorView() {
    mDecorView = getWindow().getDecorView();

    mDecorView.setOnSystemUiVisibilityChangeListener(visibility -> {
      Timber.e("onSystemUiVisibilityChange");
      int xor = DEFAULT_UI_SETTING ^ visibility;
      if (xor != 0) {
        mBackgroundHandler.postDelayed(this::hideDecorView, 3000);
      }
    });
  }


  public void hideDecorView() {
    new Handler(getMainLooper()).post(() -> mDecorView.setSystemUiVisibility(DEFAULT_UI_SETTING));
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

  private boolean isCuttingVideo;

  public void setCuttingVideo(boolean cuttingVideo) {
    isCuttingVideo = cuttingVideo;
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
      backToCamera(null);
    }
  }

  public boolean isScrollable() {
    return mViewPager.getCurrentItem() != PAGE_CAMERA || mViewPager.isScrollable();
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


