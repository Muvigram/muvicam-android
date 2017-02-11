package com.estsoft.muvigram.ui.share;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.estsoft.muvigram.R;
import com.estsoft.muvigram.injection.component.DaggerShareComponent;
import com.estsoft.muvigram.injection.component.ShareComponent;
import com.estsoft.muvigram.ui.base.BaseSingleFragmentActivity;
import com.estsoft.muvigram.ui.common.BackToHomeDialogFragment;

/**
 * Created by jaylim on 12/15/2016.
 */

public class ShareActivity extends BaseSingleFragmentActivity {
  private final static String EXTRA_VIDEO_PATHS = "ShareActivity.videoPaths";
  private final static String EXTRA_VIDEO_OFFSETS = "ShareActivity.videoOffsets";
  private final static String EXTRA_MUSIC_PATH = "ShareActivity.musicPath";
  private final static String EXTRA_MUSIC_OFFSET = "ShareActivity.musicOffset";
  private final static String EXTRA_MUSIC_LENGTH = "ShareActivity.musicLength";
  private final static String EXTRA_FROM_CAMERA = "ShareActivity.fromCamera";


  private final static String EXTRA_VIDEO_START_TIMES = "ShareActivity.videoStartTimes";
  private final static String EXTRA_VIDEO_END_TIMES = "ShareActivity.videoEndTimes";

  public static Intent newIntent( Context packageContext, String[] videoPaths, int[] videoStartTimes, int[] videoEndTimes,
                           String musicPath, int musicOffset, int musicLength, boolean fromEditor) {
    Intent intent = new Intent(packageContext, ShareActivity.class);
    intent.putExtra(EXTRA_VIDEO_PATHS, videoPaths);
    intent.putExtra(EXTRA_VIDEO_START_TIMES, videoStartTimes);
    intent.putExtra(EXTRA_VIDEO_END_TIMES, videoEndTimes);
    intent.putExtra(EXTRA_MUSIC_PATH, musicPath);
    intent.putExtra(EXTRA_MUSIC_OFFSET, musicOffset);
    intent.putExtra(EXTRA_MUSIC_LENGTH, musicLength);
    intent.putExtra(EXTRA_FROM_CAMERA, fromEditor);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    return intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

  }

  public static Intent newIntent(Context packageContext, String[] videoPaths, int[] videoOffsets,
                                 String musicPath, int musicOffset, int musicLength) {
    Intent intent = new Intent(packageContext, ShareActivity.class);
    intent.putExtra(EXTRA_VIDEO_PATHS, videoPaths);
    intent.putExtra(EXTRA_VIDEO_OFFSETS, videoOffsets);
    intent.putExtra(EXTRA_MUSIC_PATH, musicPath);
    intent.putExtra(EXTRA_MUSIC_OFFSET, musicOffset);
    intent.putExtra(EXTRA_MUSIC_LENGTH, musicLength);
    intent.putExtra(EXTRA_FROM_CAMERA, false);

    return intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
  }

  private String[] mVideoPaths;
  private int[] mVideoOffsets;
  private String mMusicPath;
  private int mMusicOffset;
  private int mMusicLength;
  private boolean mFromEditor;

  private int[] mVideoStarts;
  private int[] mVideoEnds;

  private ShareComponent mShareComponent;

  public static ShareActivity get(Fragment fragment) {
    return (ShareActivity)fragment.getActivity();
  }

  public ShareComponent getComponent() { return mShareComponent; }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mShareComponent = DaggerShareComponent.builder()
            .activityComponent( getActivityComponent() ).build();
    mShareComponent.inject( this );

  }

  @Override
  protected Fragment createDefaultFragment() {
    mVideoPaths = getIntent().getStringArrayExtra(EXTRA_VIDEO_PATHS);
    mVideoOffsets = getIntent().getIntArrayExtra(EXTRA_VIDEO_OFFSETS);
    mVideoStarts = getIntent().getIntArrayExtra(EXTRA_VIDEO_START_TIMES);
    mVideoEnds = getIntent().getIntArrayExtra(EXTRA_VIDEO_END_TIMES);
    mMusicPath = getIntent().getStringExtra(EXTRA_MUSIC_PATH);
    mMusicOffset = getIntent().getIntExtra(EXTRA_MUSIC_OFFSET, 0);
    mMusicLength = getIntent().getIntExtra(EXTRA_MUSIC_LENGTH, 0);
    mFromEditor = getIntent().getBooleanExtra(EXTRA_FROM_CAMERA, false);

    return ShareFragment.newInstance( mVideoPaths, mVideoOffsets, mVideoStarts, mVideoEnds,
            mMusicPath, mMusicOffset, mMusicLength, mFromEditor );
  }

  @Override
  public void onBackPressed() {

    BackToHomeDialogFragment fragment = BackToHomeDialogFragment.newInstance(
            getResources().getString(R.string.dialog_discard_video));
    fragment.show( getSupportFragmentManager(), BackToHomeDialogFragment.TAG);


  }
}
