package com.estsoft.muvigram.ui.library;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;


import com.estsoft.muvigram.R;
import com.estsoft.muvigram.injection.component.DaggerLibraryComponent;
import com.estsoft.muvigram.injection.component.LibraryComponent;
import com.estsoft.muvigram.model.EditorVideo;
import com.estsoft.muvigram.model.Video;
import com.estsoft.muvigram.ui.base.BaseMultiFragmentActivity;
import com.estsoft.muvigram.ui.common.BackToHomeDialogFragment;
import com.estsoft.muvigram.ui.editor.EditorActivity;
import com.estsoft.muvigram.ui.library.musiclibrary.MusicLibraryFragment;
import com.estsoft.muvigram.ui.library.videolibrary.VideoLibraryFragment;

import java.util.ArrayList;
import java.util.List;


public class LibraryActivity extends BaseMultiFragmentActivity {

  public static Intent getIntent(Context packageContext) {
    Intent intent = new Intent(packageContext, LibraryActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
    return intent;

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
    Intent intent = EditorActivity.getIntent(this, editorVideos, musicPath, musicOffset, musicLength);
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

  @Override
  public void onBackPressed() {
    BackToHomeDialogFragment fragment = BackToHomeDialogFragment.newInstance(
        getResources().getString(R.string.dialog_back_to_home));
    fragment.show(getSupportFragmentManager(), BackToHomeDialogFragment.TAG);
  }
}
