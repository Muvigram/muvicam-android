package com.estsoft.muvicam.util;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.estsoft.muvicam.R;

/**
 * Created by jaylim on 12/19/2016.
 */

public class AnimationUtil {
  public static abstract class AnimationEndListener implements Animation.AnimationListener {
    @Override public void onAnimationStart(Animation animation) {}
    @Override public void onAnimationRepeat(Animation animation) {}
  }

  public static Animation getAnimation(Context context, int id) {
    return AnimationUtils.loadAnimation(context, id);
  }

  public static Animation getAnimation(Context context, int id, Animation.AnimationListener listener) {
    Animation animation = AnimationUtils.loadAnimation(context, id);
    animation.setAnimationListener(listener);
    return animation;
  }

  public static Animation getClickingAnimation(Context context) {
    return AnimationUtils.loadAnimation(context, R.anim.clicking);
  }

  public static Animation getClickingAnimation(Context context, Animation.AnimationListener listener) {
    Animation animation = AnimationUtils.loadAnimation(context, R.anim.clicking);
    animation.setAnimationListener(listener);
    return animation;
  }

  public static Animation getRotatingAnimation(Context context) {
    return AnimationUtils.loadAnimation(context, R.anim.rotating);
  }

  public static Animation getRotatingAnimation(Context context, Animation.AnimationListener listener) {
    Animation animation = AnimationUtils.loadAnimation(context, R.anim.rotating);
    animation.setAnimationListener(listener);
    return animation;
  }

}
