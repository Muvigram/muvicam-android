package com.estsoft.muvicam.ui.editor;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.estsoft.muvicam.R;

import timber.log.Timber;

public class ResultBarView extends View {
    private Paint paint;
    // ms
    private int nowVideoTime;
    private int totalTime;
    private boolean isProgressBar;
    private int width;

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

    public ResultBarView(Context context, int totalTime, int nowVideoTime, boolean isProgressBar) {
        super(context);
        Timber.d("onCreate: rbv rvt%d", totalTime);
        paint = new Paint();
        this.nowVideoTime = nowVideoTime;
        this.totalTime = totalTime;
        this.isProgressBar = isProgressBar;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        Timber.d("onMeasure: %d", width);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isProgressBar) {
            paint.setColor(ContextCompat.getColor(getContext(), R.color.selectorVideoSelected));
        } else {
            paint.setColor(ContextCompat.getColor(getContext(), R.color.resultSpace));
        }
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect((float) Math.ceil(( totalTime / 15000f) * width), 0, (float) Math.round((( totalTime + nowVideoTime) / 15000f )* width), getResources().getDimension(R.dimen.resultbar_height), paint);

        if (!isProgressBar) {
            paint.setColor(ContextCompat.getColor(getContext(), R.color.selectorVideoSelected));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(getResources().getDimension(R.dimen.resultbar_line));
            canvas.drawRect((float) Math.ceil((totalTime / 15000f) * width), 0, (float) Math.round((( totalTime + nowVideoTime) / 15000f) * width), getResources().getDimension(R.dimen.resultbar_height), paint);
        }
    }


    public int getNowVideoTime() {
        return nowVideoTime;
    }

    public void setNowVideoTime(int nowVideoTime) {
        this.nowVideoTime = nowVideoTime;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }
}
