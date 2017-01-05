package com.estsoft.muvicam.ui.share;

import android.content.Context;
import android.content.Intent;

import com.estsoft.muvicam.model.Music;

import java.io.File;
import java.util.Stack;

/**
 * Created by jaylim on 12/15/2016.
 */

public class ShareActivity {

  private final static String EXTRA_VIDEO_PATHS = "ShareActivity.videoPaths";
  private final static String EXTRA_VIDEO_OFFSETS = "ShareActivity.videoOffsets";
  private final static String EXTRA_MUSIC_PATH = "ShareActivity.musicPath";
  private final static String EXTRA_MUSIC_OFFSET = "ShareActivity.musicOffset";
  private final static String EXTRA_MUSIC_LENGTH = "ShareActivity.musicLength";

  public static Intent newIntent(Context packageContext, String[] videoPaths, int[] videoOffsets,
                                 String musicPath, int musicOffset, int musicLength) {
    Intent intent = new Intent(packageContext, ShareActivity.class);
    intent.putExtra(EXTRA_VIDEO_PATHS, videoPaths);
    intent.putExtra(EXTRA_VIDEO_OFFSETS, videoOffsets);
    intent.putExtra(EXTRA_MUSIC_PATH, musicPath);
    intent.putExtra(EXTRA_MUSIC_OFFSET, musicOffset);
    intent.putExtra(EXTRA_MUSIC_LENGTH, musicLength);

    return intent;
  }


}
