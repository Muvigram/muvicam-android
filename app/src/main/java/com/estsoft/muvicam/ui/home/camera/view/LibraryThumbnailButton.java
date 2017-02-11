package com.estsoft.muvicam.ui.home.camera.view;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.widget.ImageButton;

import com.estsoft.muvicam.R;

/**
 *
 * Created by jaylim on 02/01/2017.
 */

public class LibraryThumbnailButton extends ImageButton {

  public LibraryThumbnailButton(Context context) {
    this(context, null);
  }

  public LibraryThumbnailButton(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public LibraryThumbnailButton(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs);
  }


  private Paint mBackgroundPaint = null;
  private RectF mBackgroundRectF = null;
  private Rect mSrcRect = null;

  private void init (AttributeSet attrs) {
    // If any, get attribute sets
    mBackgroundPaint = new Paint();
    mBackgroundPaint.setColor(getResources().getColor(R.color.grey_black_1000));
    mBackgroundPaint.setStyle(Paint.Style.FILL);
    mBackgroundPaint.setAntiAlias(true);
  }

  private Bitmap mThumbnail;

  public void updateThumbnailButton() {

    final Cursor videoCursor = getContext().getContentResolver().query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        null, null, null, MediaStore.Video.Media.DATE_TAKEN + " ASC"
    );

    if (videoCursor == null) {
      return;
    }
    while (videoCursor.moveToNext()) {
      String path = videoCursor.getString(
          videoCursor.getColumnIndex(MediaStore.Video.Media.DATA)
      );
      mThumbnail = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MICRO_KIND);
      if (mThumbnail != null) {
        break;
      }
    }
    mSrcRect = new Rect(0, 0, mThumbnail.getWidth(), mThumbnail.getHeight());
    videoCursor.close();

    invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (mBackgroundRectF == null) {
      mBackgroundRectF = new RectF(0, 0, getMeasuredWidth(), getMeasuredHeight());
    }
    if (mThumbnail != null) {
      canvas.drawBitmap(mThumbnail, mSrcRect, mBackgroundRectF, mBackgroundPaint);
    } else {
      canvas.drawRect(mBackgroundRectF, mBackgroundPaint);
    }
  }
}

