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

/**
 * Created by Administrator on 2017-01-10.
 */

public class ResultBarView extends View {
    private Paint paint;
    // ms
    private int nowVideoTime;
    private int totalTime;
    String TAG = "ResultBarView";
    private boolean isProgressBar;

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
        Log.d(TAG, "onCreate: rbv rvt" + totalTime);

        paint = new Paint();
        this.nowVideoTime = nowVideoTime;
        this.totalTime = totalTime;
        this.isProgressBar = isProgressBar;
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
        DisplayMetrics outMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        int widthPSec = outMetrics.widthPixels / 15;
        int dpi = outMetrics.densityDpi / 160;
        Log.d("onDraw", "onDraw: " + ((float) totalTime) / 1000);
        Log.d("onDraw", "onDraw: editorvideoWidth" + widthPSec);
        Log.d("onDraw", "onDraw: dpi" + dpi);

        canvas.drawRect((float) Math.round(((float) totalTime) / 1000 * widthPSec), 0, (float) Math.round(((float) totalTime + nowVideoTime) / 1000 * widthPSec), Math.round(20 * dpi), paint);
        if (!isProgressBar) {
            paint.setColor(ContextCompat.getColor(getContext(), R.color.selectorVideoSelected));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dpi);
            canvas.drawRect((float) Math.round(((float) totalTime) / 1000 * widthPSec), 0, (float) Math.round(((float) totalTime + nowVideoTime) / 1000 * widthPSec), Math.round(20 * dpi), paint);
        }
    }
}
