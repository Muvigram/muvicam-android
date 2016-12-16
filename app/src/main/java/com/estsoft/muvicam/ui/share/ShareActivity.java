package com.estsoft.muvicam.ui.share;

import android.content.Context;
import android.content.Intent;

import com.estsoft.muvicam.model.Music;

import java.io.File;
import java.util.Arrays;
import java.util.Stack;

/**
 * Created by jaylim on 12/15/2016.
 */

public class ShareActivity {

  private final static String EXTRA_VIDEO_FILES = "ShareActivity.videoFiles";
  private final static String EXTRA_MUSIC_FILE = "ShareActivity.musicFiles";
  private final static String EXTRA_MUSIC_OFFSET = "ShareActivity.musicOffset";
  private final static String EXTRA_MUSIC_LENGTH = "ShareActivity.musicLength";

  public static Intent newIntent(Context packageContext, Stack<File> files) {
    return newIntent(packageContext, files, null);
  }

  public static Intent newIntent(Context packageContext, File[] files) {
    return newIntent(packageContext, files, null);
  }

  public static Intent newIntent(Context packageContext, Stack<File> files, Music music) {
    File[] fileArray = new File[files.size()];
    for (int i = 0; i < fileArray.length; i++) {
      fileArray[fileArray.length - 1 - i] = files.pop();
    }
    return newIntent(packageContext, fileArray, music);
  }

  public static Intent newIntent(Context packageContext, File[] files, Music music) {
    Intent intent = new Intent(packageContext, ShareActivity.class);
    intent.putExtra(EXTRA_VIDEO_FILES, files);
    if (music != null) {
      intent.putExtra(EXTRA_MUSIC_FILE, EXTRA_MUSIC_FILE);
    }
    return null;
  }
}
