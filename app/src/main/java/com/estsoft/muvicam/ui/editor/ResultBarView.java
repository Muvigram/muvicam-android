package com.estsoft.muvicam.ui.editor;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.estsoft.muvicam.R;

/**
 * Created by Administrator on 2017-01-10.
 */

public class ResultBarView extends View {
    private Paint paint;
    private float totalTime;

    public ResultBarView(Context context) {
        super(context);
    }

    public ResultBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ResultBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ResultBarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ResultBarView(Context context, float totalTime) {
        super(context);
        paint = new Paint();
        this.totalTime = totalTime;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.resultSpace));
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        DisplayMetrics outMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        float width = ((float) outMetrics.widthPixels) / 15;
        float dpi = (float) outMetrics.densityDpi;

        canvas.drawRect(0, 0, totalTime * width, 20 / (160 / dpi), paint);
    }

}