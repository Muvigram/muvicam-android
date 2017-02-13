package com.estsoft.muvigram.util;

import android.support.annotation.Nullable;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import timber.log.Timber;

/**
 * In release mode, {@linkplain CrashlythicsTree this class} help {@link Timber} to report logs
 * at error and warning level via {@link Crashlytics}.
 * In debug mode, however, {@link Timber} use only it's own {@linkplain Timber.DebugTree debug tree}.
 * </p>
 * Created by jaylim on 10/02/2017.
 */
public class CrashlythicsTree extends Timber.DebugTree {

  private static final String CRASHLYTICS_KEY_PRIORITY = "priority";
  private static final String CRASHLYTICS_KEY_TAG      = "tag";
  private static final String CRASHLYTICS_KEY_MESSAGE  = "message";

  @Override
  protected void log(int priority, @Nullable String tag, @Nullable String message, @Nullable Throwable t) {
    if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
      return;
    }

    Crashlytics.setInt(CRASHLYTICS_KEY_PRIORITY, priority);
    Crashlytics.setString(CRASHLYTICS_KEY_TAG, tag);
    Crashlytics.setString(CRASHLYTICS_KEY_MESSAGE, message);

    if (t == null) {
      Crashlytics.logException(new Exception(message));
    } else {
      Crashlytics.logException(t);
    }
  }
}
