package com.estsoft.muvicam.ui.home.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageButton;

import com.estsoft.muvicam.R;

import timber.log.Timber;

/**
 * Created by jaylim on 12/16/2016.
 */

public class AlbumArtButton extends ImageButton {

  public AlbumArtButton(Context context) {
    this(context, null);
  }

  public AlbumArtButton(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public AlbumArtButton(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs);
  }

  private Paint mBackgroundPaint;
  private Paint mInnerCirclePaint;

  private void init(AttributeSet attrs) {
    // If any, get attributeSets
    mBackgroundPaint = new Paint();
    mBackgroundPaint.setColor(getResources().getColor(R.color.grey_black_1000));
    mBackgroundPaint.setStyle(Paint.Style.FILL);
    mBackgroundPaint.setAntiAlias(true);
    mInnerCirclePaint = new Paint();
    mInnerCirclePaint.setColor(getResources().getColor(R.color.yellow_700));
    mInnerCirclePaint.setStrokeWidth(1.0f);

  }

  private boolean isAlbumArt = false;
  private Bitmap mThumbnail;
  private Rect mSrcRect;
  private Rect mDestRect;

  public void setAlbumArt(@Nullable Bitmap thumbnail) {
    if (thumbnail == null) {
      return;
    }
    isAlbumArt = true;
    mThumbnail = AlbumArtButton.getCroppedBitmap(thumbnail);
    mSrcRect = new Rect(0, 0, mThumbnail.getWidth(), mThumbnail.getHeight());
    mDestRect = new Rect(0, 0, getMeasuredWidth(), getMeasuredHeight());

    invalidate();
  }

  private static Bitmap getCroppedBitmap(Bitmap thumbnail) {
    Bitmap output = Bitmap.createBitmap(thumbnail.getWidth(),
        thumbnail.getHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(output);

    final int color = 0xff424242;
    final Paint paint = new Paint();
    final Rect rect = new Rect(0, 0, thumbnail.getWidth(), thumbnail.getHeight());

    paint.setAntiAlias(true);
    canvas.drawARGB(0, 0, 0, 0);
    paint.setColor(color);

    canvas.drawCircle(thumbnail.getWidth() / 2, thumbnail.getHeight() / 2,
        thumbnail.getWidth() / 2, paint);
    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    canvas.drawBitmap(thumbnail, rect, rect, paint);

    return output;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (isAlbumArt) {
      canvas.drawBitmap(mThumbnail, mSrcRect, mDestRect, mBackgroundPaint);
    } else {
      canvas.drawOval(0.0f, 0.0f, getMeasuredWidth(), getMeasuredHeight(), mBackgroundPaint);
    }
    canvas.drawCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2,
        getMeasuredWidth() * 0.1f, mInnerCirclePaint);
  }
}
