package com.estsoft.muvicam.ui.home.camera;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.Music;
import com.estsoft.muvicam.util.MP3File;
import com.estsoft.muvicam.util.SoundFile;

import java.io.File;

/**
 *
 * Created by jaylim on 12/21/2016.
 */

public class WaveformView extends View {

  public interface WaveformListener {
    public void waveformTouchStart(float x);
    public void waveformTouchMove(float x);
    public void waveformTouchEnd();
    public void waveformFling(float x);
    public void waveformDraw();
  }

  protected Paint mBackgroundPaint;
  protected Paint mPlayheadPaint;
  protected Paint mBeforeHeadPaint;
  protected Paint mAfterHeadPaint;

  // Sample rate
  protected SoundFile mSoundFile;

  protected int mOffset;
  // Sample rate = number of samples / second
  protected int mSampleRate;
  // Frame Size = Sample Rate * number of channels
  protected int mSamplesPerFrame;

  protected WaveformListener mListener;

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

    mBackgroundPaint = new Paint();
    mBackgroundPaint.setAntiAlias(false);
    mBackgroundPaint.setColor(getResources().getColor(R.color.backgroundOpaqueGrey));
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

  public boolean hasSoundFile() {
    return mSoundFile != null;
  }

  public void setSoundFile(Music music) {
    File file = new File(music.uri().toString());

    mSoundFile = new MP3File(file);

    mSampleRate = mSoundFile.getSampleRate();
    mSamplesPerFrame = mSoundFile.getSamplesPerFrame();
    // TODO - computeDoublesForAllZoomLevels();
  }

  protected float adjustGains(int i, int numFrames, int[] frameGains) {
    int x = Math.min(i, numFrames);
    if (numFrames < 2) {
      return frameGains[x];
    } else {
      // TODO - !
      if (x == 0) {
        return (frameGains[0] / 2.0f) + (frameGains[1] / 2.0f);
      } else if (x == numFrames - 1) {
        return (frameGains[numFrames - 2] / 2.0f) + (frameGains[numFrames - 1] / 2.0f);
      } else {
        return (frameGains[x - 1] / 3.0f) + (frameGains[x] / 3.0f) + (frameGains[x + 1] / 3.0f);
      }
    }
  }

  float scaleFactor = 1.0f;

  protected void computeDoubles() {
    final int frameNum = mSoundFile.getCurFrameNum();

    float maxGain = 1.0f;
    for (int i = 0; i < frameNum; i++) {
      float gain = adjustGains(i, frameNum, mSoundFile.getGlobalGains());
      if (gain > maxGain) {
        maxGain = gain;
      }
    } // find maxGain
    scaleFactor = 1.0f;
    if (maxGain > 255.0) {
      scaleFactor = 255 / maxGain;
    } // calculate scale factor to normalize


    //TODO - !

  }


}
