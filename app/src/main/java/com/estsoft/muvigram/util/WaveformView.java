package com.estsoft.muvigram.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.estsoft.muvigram.R;

import java.io.File;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by jaylim on 12/21/2016.
 */

public class WaveformView extends View {

  private final static int TIME_LENGTH = 15;

  // Sample rate
  private SoundFile mSoundFile;

  private float mOffset;
  private int mSampleRate;
  private int mSamplesPerFrame;

  private int mFrameOffset;
  private int mFrameLength;
  private int mFrameCur;

  private float mScaledInterval;

  private int mMaxGain;
  private int mMinGain;
  private int mAvgGain;

  private Paint mPlayheadPaint;
  private Paint mBeforeHeadPaint;
  private Paint mAfterHeadPaint;

  private boolean mMusicUpdated = false;

  public WaveformView(Context context) {
    this(context, null);
  }

  public WaveformView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public WaveformView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs, defStyleAttr);
  }

  private void init(AttributeSet attrs, int defStyleAttr) {

    mPlayheadPaint = new Paint();
    mPlayheadPaint.setAntiAlias(false);
    mPlayheadPaint.setStyle(Paint.Style.STROKE);
    mPlayheadPaint.setStrokeWidth(2.0f);
    mPlayheadPaint.setColor(getResources().getColor(R.color.red_A700));

    mBeforeHeadPaint = new Paint();
    mBeforeHeadPaint.setAntiAlias(false);
    mBeforeHeadPaint.setStyle(Paint.Style.STROKE);
    mBeforeHeadPaint.setStrokeWidth(2.0f);
    mBeforeHeadPaint.setColor(getResources().getColor(R.color.red_A700));

    mAfterHeadPaint = new Paint();
    mAfterHeadPaint.setAntiAlias(false);
    mAfterHeadPaint.setStyle(Paint.Style.STROKE);
    mAfterHeadPaint.setStrokeWidth(2.0f);
    mAfterHeadPaint.setColor(getResources().getColor(R.color.backgroundLightGrey));

  }

  // fix offset
  public float fixOffset() {
    invalidate();
    Timber.v("[fixOffset] frameOffset : %d", mFrameOffset);
    return getOffset(mFrameOffset, mSampleRate, mSamplesPerFrame);
  }

  // move offset
  public void moveOffset(float displacement) {
    int frameOffset = getFrameNumber(displacement, mScaledInterval, mFrameOffset);
    Timber.v("[moveOffset] displacement : %10.4f, frameOffset : %d", displacement, frameOffset);
    if (frameOffset < 0 || frameOffset + mFrameLength >= mSoundFile.getTotalFrameNum()) {
      return;
    }
    mFrameOffset = frameOffset;
    mFrameCur = mFrameOffset;
    invalidate();
  }

  public boolean hasSoundFile() {
    return mSoundFile != null;
  }

  private Subscription mSubscription;

  public void unsubscribe() {
    RxUtil.unsubscribe(mSubscription);
  }

  public void setSoundFile(Uri uri, float offset) {
    mMusicUpdated = true;
    File file = new File(uri.toString());
    mOffset = offset;

    RxUtil.unsubscribe(mSubscription);
    mSubscription = MP3File.create(file)
        .subscribeOn(Schedulers.computation())
        .map(MP3File::new) // TODO - Caching
        .map(mp3 -> (SoundFile) mp3)
        .filter(sound -> {
          sound.moderateGains();
          return true;
        })
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe( // TODO - parse only a 15-seconds-piece.
            sound -> {
              mSampleRate = sound.getSampleRate();
              mSamplesPerFrame = sound.getSamplesPerFrame();
              mSoundFile = sound;

              mFrameOffset = getFrameNumber(mOffset, mSampleRate, mSamplesPerFrame);
              mFrameLength = getFrameNumber(TIME_LENGTH, mSampleRate, mSamplesPerFrame);
              mFrameCur = mFrameOffset;

              mMaxGain = mSoundFile.getMaxGain();
              mMinGain = mSoundFile.getMinGain();
              mAvgGain = (mMaxGain + mMinGain) / 2;
              invalidate();
            },
            Throwable::printStackTrace,
            this::doOnPrepared
        );
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (mSoundFile == null) {
      return;
    }

    int w = getMeasuredWidth();
    int h = getMeasuredHeight();

    if (mMusicUpdated) {
      mScaledInterval = getScaledInterval(mFrameLength, getMeasuredWidth());
      Timber.v("mScaledInterval %f [%d, %d]", mScaledInterval, mFrameLength, getMeasuredWidth());
//      float strokeWidth = getScaledStrokeWidth(mScaledInterval);
//      mAfterHeadPaint.setStrokeWidth(strokeWidth);
//      mBeforeHeadPaint.setStrokeWidth(strokeWidth);
      mMusicUpdated = false;
    }

    float sh;
    float sx;

    for (int i = mFrameOffset; i < mFrameCur; i++) {
      sh = getScaledHeight(i, mSoundFile.getGlobalGains(), mMaxGain, mMinGain, h);
      sx = getScaledXPosition(i, mFrameOffset, mFrameLength, w);
      canvas.drawLine(sx, h/2.0f - sh, sx, h/2.0f + sh, mBeforeHeadPaint);
    }

//    sh = getScaledHeight(mFrameCur, mSoundFile.getGlobalGains(), mMaxGain, mAvgGain, h);
//    sx = getScaledXPosition(mFrameCur, mFrameOffset, mFrameLength, w);
//    canvas.drawLine(sx, 0f, sx, sh * 2.0f, mPlayheadPaint);

    for (int i = mFrameCur /*+ 1*/; i <= mFrameOffset + mFrameLength; i++) {
      sh = getScaledHeight(i, mSoundFile.getGlobalGains(), mMaxGain, mMinGain, h);
      sx = getScaledXPosition(i, mFrameOffset, mFrameLength, w);
      canvas.drawLine(sx, h/2.0f - sh, sx, h/2.0f + sh, mAfterHeadPaint);

    }
  }

  // On Prepared Listener
  private OnPreparedListener mOnPreparedListener;

  public interface OnPreparedListener {
    void onPrepared();
  }

  public void setOnPreparedListener(OnPreparedListener onPreparedListener) {
    mOnPreparedListener = onPreparedListener;
  }

  private boolean isOnPrepared = false;

  public boolean isOnPrepared() {
    return isOnPrepared;
  }

  public void doOnPrepared() {
    mOnPreparedListener.onPrepared();
    isOnPrepared = true;
  }

  // Waveform Listener
  private WaveformListener mWaveformListener;

  public interface WaveformListener {
    void waveformTouchStart(float x);

    void waveformTouchMove(float x);

    void waveformTouchEnd();
  }

  public void setWaveformListener(WaveformListener waveformListener) {
    mWaveformListener = waveformListener;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (mWaveformListener == null) return false;

    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        mWaveformListener.waveformTouchStart(event.getX());
        break;
      case MotionEvent.ACTION_MOVE:
        mWaveformListener.waveformTouchMove(event.getX());
        break;
      case MotionEvent.ACTION_UP:
        mWaveformListener.waveformTouchEnd();
        break;
    }
    return true;
  }

  public void updateUi(float sec) {
    mFrameCur = getFrameNumber(sec, mSampleRate, mSamplesPerFrame);
    if (mFrameCur == mFrameOffset + mFrameLength - 1) mFrameCur++;
    invalidate();
  }

  public boolean isValidRunningAt(float sec) {
    int curFrame = getFrameNumber(sec, mSampleRate, mSamplesPerFrame) + 1;
    Timber.v("offset : %d, cur : %d, end : %d\n", mFrameOffset, curFrame, mFrameOffset + mFrameLength);
    return (curFrame >= (mFrameOffset) && curFrame <= (mFrameOffset + mFrameLength));
  }

  public static float getOffset(int frameOffset, int sampleRate, int samplesPerFrame) {
    return (float) frameOffset * samplesPerFrame / sampleRate;
  }

  public static int getFrameNumber(float dip, float scaledInterval, int mFrameOffset) {
    int frame = (int) (dip / scaledInterval); // rounding down (1.9 frame -> 1 frame)
    return mFrameOffset + frame;
  }

  public static int getFrameNumber(float seconds, int sampleRate, int samplesPerFrame) {
    return (int) (1.0 * seconds * sampleRate / samplesPerFrame); // rounding up (1.9 frame -> 2 frame)
  }

  private static float getScaledHeight(int i, int[] frameGain, int maxGain, int minGain, int viewHeight) {
    return (frameGain[i] - minGain) * (viewHeight / 2.0f) / (maxGain - minGain);
  }

  private static float getScaledInterval(int frameLength, int viewWidth) {
    return viewWidth / frameLength;
  }

  private static float getScaledXPosition(int i, int frameOffset, int frameLength, int viewWidth) {
    return (i - frameOffset - 0.5f) * viewWidth / frameLength;
  }

  private static float getScaledStrokeWidth(float scaledInterval) {
    return scaledInterval * 2 / 3;
  }

}
