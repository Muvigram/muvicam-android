package com.estsoft.muvicam.ui.home.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.Music;
import com.estsoft.muvicam.util.MP3File;
import com.estsoft.muvicam.util.SoundFile;

import java.io.File;

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

  private WaveformListener mListener;

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
    mPlayheadPaint.setColor(getResources().getColor(R.color.red_A700));
    mBeforeHeadPaint = new Paint();
    mBeforeHeadPaint.setAntiAlias(false);
    mBeforeHeadPaint.setColor(getResources().getColor(R.color.backgroundWhite));
    mAfterHeadPaint = new Paint();
    mAfterHeadPaint.setAntiAlias(false);
    mAfterHeadPaint.setColor(getResources().getColor(R.color.backgroundLightGrey));

  }

  public void setListener(WaveformListener listener) {
    mListener = listener;
  }

  public float fixOffset() {
    invalidate();
    Timber.e("[fixOffset] frameOffset : %d", mFrameOffset);
    return getOffset(mFrameOffset, mSampleRate, mSamplesPerFrame);
  }

  public void moveOffset(float displacement) {
    int frameOffset = getFrameNumber(displacement, mScaledInterval, mFrameOffset);
    Timber.e("[moveOffset] displacement : %10.4f, frameOffset : %d", displacement, frameOffset);
    if (frameOffset < 0 || frameOffset + mFrameLength > mSoundFile.getTotalFrameNum()) {
      return;
    }
    mFrameOffset = frameOffset;
    mFrameCur = mFrameOffset;
    invalidate();
  }

  public boolean hasSoundFile() {
    return mSoundFile != null;
  }

  public void setSoundFile(Uri uri, float offset) {
    mMusicUpdated = true;
    File file = new File(uri.toString());
    mOffset = offset;

    mSoundFile = new MP3File(file);
    mSampleRate = mSoundFile.getSampleRate();
    mSamplesPerFrame = mSoundFile.getSamplesPerFrame();

    mFrameOffset = getFrameNumber(mOffset, mSampleRate, mSamplesPerFrame);
    mFrameLength = getFrameNumber(TIME_LENGTH, mSampleRate, mSamplesPerFrame);
    mFrameCur = mFrameOffset;

    mMaxGain = mSoundFile.getMaxGain();
    mMinGain = mSoundFile.getMinGain();
    mAvgGain = (mMaxGain + mMinGain) / 2;
    invalidate();
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
      Timber.e("mScaledInterval %f [%d, %d]", mScaledInterval, mFrameLength, getMeasuredWidth());
      float strokeWidth = getScaledStrokeWidth(mScaledInterval);
      mAfterHeadPaint.setStrokeWidth(strokeWidth);
      mBeforeHeadPaint.setStrokeWidth(strokeWidth);
      mMusicUpdated = false;
    }

    float sh;
    float sx;

    for (int i = mFrameOffset; i < mFrameCur; i++) {
      sh = getScaledHeight(i, mSoundFile.getGlobalGains(), mMaxGain, mAvgGain, h);
      sx = getScaledInterval(i, mFrameOffset, mFrameLength, w);
      canvas.drawLine(sx, h/2.0f - sh, sx, h/2.0f + sh, mBeforeHeadPaint);
    }

    sh = getScaledHeight(mFrameCur, mSoundFile.getGlobalGains(), mMaxGain, mAvgGain, h);
    sx = getScaledInterval(mFrameCur, mFrameOffset, mFrameLength, w);
    canvas.drawLine(sx, 0f, sx, sh * 2f, mPlayheadPaint);

    for (int i = mFrameCur + 1; i < mFrameOffset + mFrameLength; i++) {
      sh = getScaledHeight(i, mSoundFile.getGlobalGains(), mMaxGain, mAvgGain, h);
      sx = getScaledInterval(i, mFrameOffset, mFrameLength, w);
      canvas.drawLine(sx, h/2.0f - sh, sx, h/2.0f + sh, mBeforeHeadPaint);

    }
  }

  public interface WaveformListener {
    public void waveformTouchStart(float x);

    public void waveformTouchMove(float x);

    public void waveformTouchEnd();
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        mListener.waveformTouchStart(event.getX());
        break;
      case MotionEvent.ACTION_MOVE:
        mListener.waveformTouchMove(event.getX());
        break;
      case MotionEvent.ACTION_UP:
        mListener.waveformTouchEnd();
        break;
    }
    return true;
  }

  public static float getOffset(int frameOffset, int sampleRate, int samplesPerFrame) {
    return (frameOffset - 1.0f) / sampleRate * samplesPerFrame;
  }

  public static int getFrameNumber(float dp, float scaledInterval, int mFrameOffset) {
    int displacement = (int) (dp / scaledInterval);
    return mFrameOffset + displacement;
  }

  public static int getFrameNumber(float seconds, int sampleRate, int samplesPerFrame) {
    return (int) (1.0 * seconds * sampleRate / samplesPerFrame + 1);
  }

  private static float getScaledHeight(int i, int[] frameGain, int maxGain, int avgGain, int viewHeight) {
    return (frameGain[i] - avgGain) * (viewHeight / 2.0f) / (maxGain - avgGain);
  }

  private static float getScaledInterval(int frameLength, int viewWidth) {
    return viewWidth / frameLength;
  }

  private static float getScaledInterval(int i, int frameOffset, int frameLength, int viewWidth) {
    return (i - frameOffset - 0.5f) * viewWidth / frameLength;
  }

  private static float getScaledStrokeWidth(float scaledInterval) {
    return scaledInterval * 2 / 3;
  }

}
