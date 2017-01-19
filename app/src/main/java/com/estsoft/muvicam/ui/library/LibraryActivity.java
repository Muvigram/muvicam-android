package com.estsoft.muvicam.ui.library;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


import com.estsoft.muvicam.R;
import com.estsoft.muvicam.injection.component.DaggerLibraryComponent;
import com.estsoft.muvicam.injection.component.LibraryComponent;
import com.estsoft.muvicam.model.EditorVideo;
import com.estsoft.muvicam.ui.base.BaseSingleFragmentActivity;
import com.estsoft.muvicam.ui.editor.EditorActivity;
import com.estsoft.muvicam.ui.library.musiclibrary.MusicLibraryFragment;

import java.util.ArrayList;
import java.util.List;


public class LibraryActivity extends BaseSingleFragmentActivity {


  public static Intent getIntent(Context packageContext) {
    return new Intent(packageContext, LibraryActivity.class);
  }

  private LibraryComponent mLibraryComponent;

  public LibraryComponent getComponent() {
    return mLibraryComponent;
  }

  public static LibraryActivity get(Fragment fragment) {
    return (LibraryActivity) fragment.getActivity();
  }

  public static LibraryActivity get(View view) {
    return (LibraryActivity) view.getContext();
  }


  @Override
  protected Fragment createDefaultFragment() {
    return new MusicLibraryFragment();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // set fullscreen
    setFullscreen();

    // inflate single fragment activity
    super.onCreate(savedInstanceState);

    // create component and inject dependencies
    mLibraryComponent = DaggerLibraryComponent.builder()
        .activityComponent(getActivityComponent()).build();
    mLibraryComponent.inject(this);
  }

  public void goToNext(@NonNull List<EditorVideo> videos) {
    FragmentManager fragmentManager = getSupportFragmentManager();
    Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
    if (fragment != null) {
      fragmentManager.beginTransaction()
          .remove(fragment)
          .commit();
    }
    setVideos(videos);
    fragment = MusicLibraryFragment.newInstance();
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container, fragment)
        .commit();
  }


  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  public void setFullscreen() {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(
        WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN
    );
  }

  private List<EditorVideo> mVideos = new ArrayList<>();

  /**
   * Set videos selected from library.
   * @param videos A list of videos selected from local library.
   */
  public void setVideos(List<EditorVideo> videos) {
    mVideos = videos;
  }

  /**
   * This method would be called to complete the selection of videos and a music.
   * Metadata of a music and videos selected from local library are transferred
   * to {@link EditorActivity}.
   *
   * @param musicPath   A path to music selected from local library.
   * @param musicOffset An offset where a music file would be started.
   * @param musicLength A length of music file.
   */
  public void completeSelection(@Nullable String musicPath, int musicOffset, int musicLength) {
    EditorActivity.newIntent(this,
        (ArrayList<EditorVideo>) mVideos, musicPath, musicOffset, musicLength);
  }
}
