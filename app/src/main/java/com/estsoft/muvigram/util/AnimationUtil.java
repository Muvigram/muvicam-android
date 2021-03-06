package com.estsoft.muvigram.util;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.estsoft.muvigram.R;

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
    return AnimationUtils.loadAnimation(context, R.anim.clicking_105);
  }

  public static Animation getClickingAnimation(Context context, Animation.AnimationListener listener) {
    Animation animation = AnimationUtils.loadAnimation(context, R.anim.clicking_105);
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
