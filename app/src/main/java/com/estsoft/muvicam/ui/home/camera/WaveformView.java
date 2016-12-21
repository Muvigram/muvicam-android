package com.estsoft.muvicam.ui.home.camera;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.Music;
import com.estsoft.muvicam.util.CheapMP3;
import com.estsoft.muvicam.util.CheapSoundFile;

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
  protected CheapSoundFile mSoundFile;

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

    mSoundFile = new CheapMP3(file);

    mSampleRate = mSoundFile.getSampleRate();
    mSamplesPerFrame = mSoundFile.getSamplesPerFrame();
    // TODO - computeDoublesForAllZoomLevels();
  }


}
