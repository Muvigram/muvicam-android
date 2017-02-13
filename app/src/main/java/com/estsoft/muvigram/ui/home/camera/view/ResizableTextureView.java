package com.estsoft.muvigram.ui.home.camera.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.View;

import timber.log.Timber;

/**
 * A resizable {@link TextureView} adapting to aspect ration.
 *
 * Created by jaylim on Nov. 23, 2016.
 */

public class ResizableTextureView extends TextureView {

    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    public ResizableTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public ResizableTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ResizableTextureView(Context context) {
        this(context, null);
    }

    public void setAspectRatio(int width, int height) {

        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        /* MeasureSpec.AT_MOST
         *   typically means the layout_width or layout_height value was set to match_parent
         *   or wrap_content where a maximum size is needed (this is layout dependent in the
         *   framework), and the size of the parent dimension is the value. You should not be
         *   any larger than this size.
         */
        int defaultWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                ((View) getParent()).getMeasuredWidth(), MeasureSpec.AT_MOST);
        int defaultHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                ((View) getParent()).getMeasuredHeight(), MeasureSpec.AT_MOST);

        // Re-initialize the MeasureSpec
        if (widthMeasureSpec != defaultWidthMeasureSpec ||
                heightMeasureSpec != defaultHeightMeasureSpec) {

            widthMeasureSpec = defaultWidthMeasureSpec;
            heightMeasureSpec = defaultHeightMeasureSpec;

            Timber.i("onMeasure(int, int) : size is re-initialized as a fullscreen. width %s:%s, height %s:%s",
                MeasureSpec.toString(widthMeasureSpec), MeasureSpec.toString(defaultWidthMeasureSpec),
                MeasureSpec.toString(heightMeasureSpec), MeasureSpec.toString(defaultHeightMeasureSpec));
        }

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }
    }
}
