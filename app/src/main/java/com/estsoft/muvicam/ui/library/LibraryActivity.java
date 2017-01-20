package com.estsoft.muvicam.ui.library;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
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
import com.estsoft.muvicam.ui.base.BaseSingleFragmentActivity;
import com.estsoft.muvicam.ui.editor.EditorActivity;
import com.estsoft.muvicam.ui.library.musiclibrary.MusicLibraryFragment;

import java.util.ArrayList;


public class LibraryActivity extends BaseSingleFragmentActivity {

  private static final String EXTRA_VIDEOS = "library.LibraryActivity.editorVideos";

  public static Intent newIntent(Context packageContext) {
    return new Intent(packageContext, LibraryActivity.class);
  }

  public static Intent newIntent(Context packageContext, ArrayList<EditorVideo> editorVideos) {
    Intent intent = new Intent(packageContext, LibraryActivity.class);
    intent.putExtra(EXTRA_VIDEOS, editorVideos);
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
    ArrayList<EditorVideo> editorVideos = getIntent().getParcelableArrayListExtra(EXTRA_VIDEOS);
    Intent intent = EditorActivity.newIntent(this, editorVideos, musicPath, musicOffset, musicLength);
    startActivity(intent);
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
    return super.dispatchTouchEvent( event );
  }
}
