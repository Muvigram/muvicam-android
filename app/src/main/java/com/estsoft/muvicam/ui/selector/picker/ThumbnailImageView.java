package com.estsoft.muvicam.ui.selector.picker;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;


public class ThumbnailImageView extends ImageView {

   public ThumbnailImageView(final Context context){
       super(context);
   }

    public ThumbnailImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public ThumbnailImageView(final Context context, final AttributeSet attrs,
                              final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        setMeasuredDimension(measuredWidth,measuredWidth);
    }
}
