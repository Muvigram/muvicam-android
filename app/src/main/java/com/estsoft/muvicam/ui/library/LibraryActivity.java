package com.estsoft.muvicam.ui.library;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;


import com.estsoft.muvicam.injection.component.DaggerLibraryComponent;
import com.estsoft.muvicam.injection.component.LibraryComponent;
import com.estsoft.muvicam.model.EditorVideo;
import com.estsoft.muvicam.model.Video;
import com.estsoft.muvicam.ui.base.BaseMultiFragmentActivity;
import com.estsoft.muvicam.ui.base.BaseSingleFragmentActivity;
import com.estsoft.muvicam.ui.editor.EditorActivity;
import com.estsoft.muvicam.ui.library.musiclibrary.MusicLibraryFragment;
import com.estsoft.muvicam.ui.library.videolibrary.VideoLibraryFragment;

import java.util.ArrayList;
import java.util.List;


public class LibraryActivity extends BaseMultiFragmentActivity {

  public static Intent newIntent(Context packageContext) {
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
  protected Fragment[] setFragments() {
    Fragment[] fragments = new Fragment[2];
    fragments[0] = VideoLibraryFragment.newInstance();
    fragments[1] = MusicLibraryFragment.newInstance();
    return fragments;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // inflate single fragment activity
    super.onCreate(savedInstanceState);

    // create component and inject dependencies
    mLibraryComponent = DaggerLibraryComponent.builder()
        .activityComponent(getActivityComponent()).build();
    mLibraryComponent.inject(this);
  }

  private List<Video> mVideos;

  public LibraryActivity completeVideoSelection(@NonNull List<Video> videos) {
    mVideos = videos;
    return this;
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
  public void completeMusicSelection(@Nullable String musicPath, int musicOffset, int musicLength) {
    ArrayList<EditorVideo> editorVideos = convertVideoObject(mVideos);
    Intent intent = EditorActivity.newIntent(this, editorVideos, musicPath, musicOffset, musicLength);
    startActivity(intent);
  }

  public static ArrayList<EditorVideo> convertVideoObject(@NonNull List<Video> videos) {
    ArrayList<EditorVideo> editorVideoList = new ArrayList<>();

    for (Video video : videos) {
      EditorVideo editorVideo = new EditorVideo();
      editorVideo.setDurationMiliSec(video.duration());
      editorVideo.setVideoPath(video.uri().toString());
      editorVideoList.add(editorVideo);
    }

    return editorVideoList;
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent event) {
    if (event.getAction() == MotionEvent.ACTION_DOWN) {
      View v = getCurrentFocus();
      if ( v instanceof EditText) {
        Rect outRect = new Rect();
        v.getGlobalVisibleRect(outRect);
        if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
          v.clearFocus();
          InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
      }
    }
    return super.dispatchTouchEvent(event);
  }
}
