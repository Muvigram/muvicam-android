package com.estsoft.muvigram.transcoder.wrappers;

/**
 * Created by estsoft on 2017-01-05.
 */

public interface ProgressListener {
    int PROGRESS_INTERVAL = 10;
    int START = -123;
    int PROGRESS = -124;
    int COMPLETE = -125;
    int ERROR = -126;
    void onStart(long estimatedDurationUs);
    void onProgress(long currentDurationUs, int percentage);
    void onComplete(long totalDuration);
    void onError(Exception exception);
}
