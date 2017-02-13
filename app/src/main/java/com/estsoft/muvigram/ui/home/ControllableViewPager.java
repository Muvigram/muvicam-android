package com.estsoft.muvigram.ui.home;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by jaylim on 12/24/2016.
 */

public class ControllableViewPager extends ViewPager {
  public ControllableViewPager(Context context) {
    this(context, null);
  }

  public ControllableViewPager(Context context, AttributeSet attrs) {
    super(context, attrs);
    scrollable = true;
  }

  private boolean scrollable;

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
    return scrollable && super.onInterceptTouchEvent(ev);
  }

  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    return scrollable && super.onTouchEvent(ev);
  }

  public void enableScroll() {
    scrollable = true;
  }

  public void disableScroll() {
    scrollable = false;
  }

  public boolean isScrollable() {
    return scrollable;
  }
}
