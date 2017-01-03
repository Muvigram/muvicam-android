package com.estsoft.muvicam.ui.home.camera;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.estsoft.muvicam.R;

import java.util.Stack;

import timber.log.Timber;

/**
 * {@link StackBar} is a horizontal bar that visualizes the progress using real-time and
 * responsive animation. A total amount of time, which will represent a length of the horizontal
 * bar, can be defined by setting attribute {@link R.styleable#}
 * <p>
 * Created by jaylim on 27/12/2016.
 */
public class StackBar extends View {

  /**
   * Default maximum time which is measured by unit second (1s). When you consume all of your
   * given time, the stack bar will be fully (or completely) filled with bars of different sizes.
   */
  private static final int DEFAULT_MAXIMUM_TIME = 15000;

  /**
   * Default minimum time which is measured by unit second (1s). It will be marked on the bar
   * so that it indicates the minimum time threshold.
   */
  private static final int DEFAULT_MINIMUM_TIME = 5000;

  /**
   * The default background color.
   */
  private static final int DEFAULT_BACKGROUND_COLOR = 0x7fd7dadb;

  /**
   * The default color of bars.
   */
  private static final int DEFAULT_STACK_COLOR = 0xffffffff;

  /**
   * The default color of divider, which is a kind of vertical line that separate two different
   * sub bars positioning between them.
   */
  private static final int DEFAULT_DIVIDER_COLOR = 0xffe8474e;

  private int mMaximumTime;
  private int mMinimumTime;

  private int mBackgroundColor;
  private int mStackColor;
  private int mDividerColor;

  public StackBar(Context context) {
    this(context, null);
  }

  public StackBar(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public StackBar(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    final TypedArray attrArray = getContext().obtainStyledAttributes(
        attrs, R.styleable.StackBar, defStyleAttr, 0);

    try {
      mMaximumTime = attrArray.getInt(R.styleable.StackBar_maximumTime, DEFAULT_MAXIMUM_TIME);
      mMinimumTime = attrArray.getInt(R.styleable.StackBar_minimumTime, DEFAULT_MINIMUM_TIME);

      mBackgroundColor = attrArray.getColor(R.styleable.StackBar_backgroundColor, DEFAULT_BACKGROUND_COLOR);
      mStackColor = attrArray.getColor(R.styleable.StackBar_stackColor, DEFAULT_STACK_COLOR);
      mDividerColor = attrArray.getColor(R.styleable.StackBar_dividerColor, DEFAULT_DIVIDER_COLOR);
    } finally {
      attrArray.recycle();
    }
    init();
  }

  private Paint mThresholdPaint;
  private Paint mBackgroundPaint;
  private Paint mStackPaint;
  private Paint mDividerPaint;
  private RectF mBackgroundRectF = new RectF(0.0f, 0.0f, 0.0f, 0.0f);
  private RectF mStackRectF = new RectF(0.0f, 0.0f, 0.0f, 0.0f);

  private void init() {
    mThresholdPaint = new Paint();
    mThresholdPaint.setColor(mDividerColor);
    mThresholdPaint.setStrokeWidth(5.0f);

    mBackgroundPaint = new Paint();
    mBackgroundPaint.setColor(mBackgroundColor);

    mStackPaint = new Paint();
    mStackPaint.setColor(mStackColor);

    mDividerPaint = new Paint();
    mDividerPaint.setColor(mDividerColor);
    mDividerPaint.setStrokeWidth(5.0f);

    mOffsetStack = new Stack<>();
  }

  private int mWidth;
  private int mHeight;

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    mWidth = MeasureSpec.getSize(widthMeasureSpec);
    mHeight = MeasureSpec.getSize(heightMeasureSpec);
  }

  private Stack<Integer> mOffsetStack;

  private int mOffset;

  public void updateStackBar(int millisec) {
    mOffset = millisec;
    invalidate();
  }

  public void recordOffset() {
    mOffsetStack.push(mOffset);
  }

  public int deleteRecentRecord() {
    if (!mOffsetStack.isEmpty()) {
      mOffsetStack.pop();
    }
    mOffset = mOffsetStack.isEmpty() ? 0 : mOffsetStack.peek();
    invalidate();
    return mOffset;
  }

  @Override
  protected void onDraw(Canvas canvas) {

    mBackgroundRectF.set(0.0f, 0.0f, mWidth, mHeight);

    float minThreshold = (float) mWidth * mMinimumTime / mMaximumTime;
    float curOffset = (float) mWidth * mOffset / mMaximumTime;

    mStackRectF.set(0.0f, 0.0f, curOffset, mHeight);

    Timber.i("draw background : { %f, %f, %d, %d}", 0.0f, 0.0f, mWidth, mHeight);
    canvas.drawRect(mBackgroundRectF, mBackgroundPaint);
    canvas.drawLine(minThreshold, 0.0f, minThreshold, mHeight, mThresholdPaint);

    Timber.i("draw stack : { %f, %f, %f, %d}", 0.0f, 0.0f, curOffset, mHeight);
    canvas.drawRect(mStackRectF, mStackPaint);

    Timber.i("draw divider : {%f, %f, %f}", 0.0f, (float) mHeight, curOffset);
    canvas.drawLine(curOffset, 0.0f, curOffset, mHeight, mDividerPaint);

    for (int i = 0; i < mOffsetStack.size(); i++) {
      Integer splitLine = mOffsetStack.get(i);
      float splitX = (float) mWidth * splitLine / mMaximumTime;
      canvas.drawLine(splitX, 0.0f, splitX, mHeight, mDividerPaint);
    }
  }

  public int getPosition() {
    return mWidth * mOffset / mMaximumTime;
  }

  public int getBarWidth() {
    return mWidth;
  }



}
