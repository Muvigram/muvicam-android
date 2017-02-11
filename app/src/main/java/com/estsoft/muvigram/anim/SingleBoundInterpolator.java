package com.estsoft.muvigram.anim;

/**
 * Created by jaylim on 12/16/2016.
 */

public class SingleBoundInterpolator implements android.view.animation.Interpolator {
  private double mAmplitude = 1.0;

  public SingleBoundInterpolator(double amplitude) {
    mAmplitude = amplitude;
  }

  public float getInterpolation(float time) {
    return (float) ( (-1.0) * mAmplitude * time * (time - 1.0) + 1.0);
  }
}
