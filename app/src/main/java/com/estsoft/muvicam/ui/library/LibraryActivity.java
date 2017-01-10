package com.estsoft.muvicam.ui.library;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Window;
import android.view.WindowManager;


import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.EditorVideo;
import com.estsoft.muvicam.ui.base.BasePresenter;
import com.estsoft.muvicam.ui.base.BaseSingleFragmentActivity;
import com.estsoft.muvicam.ui.library.musiclibrary.MusicLibraryFragment;
import com.estsoft.muvicam.ui.library.videolibrary.VideoLibraryFragment;

import java.util.List;


public class LibraryActivity extends BaseSingleFragmentActivity implements VideoLibraryFragment.DataPassListener {
  Fragment fragment;
  private BasePresenter presenter;

  public static Intent getIntent(Context packageContext) {
    return new Intent(packageContext, LibraryActivity.class);
  }

  @Override
  protected Fragment createDefaultFragment() {
    return new VideoLibraryFragment();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setFullscreen();
  }

  @Override
  public void passData(List<EditorVideo> videos) {
    FragmentManager fragmentManager = getSupportFragmentManager();
    fragment = fragmentManager.findFragmentById(R.id.fragment_container);
    if (fragment != null) {
      fragmentManager.beginTransaction()
          .remove(fragment)
          .commit();
    }
    fragment = MusicLibraryFragment.newInstance(videos);
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container, fragment)
        .commit();
  }


  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  public BasePresenter getPresenter() {
    return presenter;
  }

  public void setFullscreen() {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(
        WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN
    );
  }
}
