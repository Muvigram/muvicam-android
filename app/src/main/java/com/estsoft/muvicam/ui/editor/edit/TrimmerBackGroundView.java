package com.estsoft.muvicam.ui.editor.edit;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.estsoft.muvicam.R;

public class TrimmerBackGroundView extends View {
    private Paint paint;
    // ms
    private float startX;
    private float endX;
    public TrimmerBackGroundView(Context context) {
        super(context);
    }

    public TrimmerBackGroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TrimmerBackGroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TrimmerBackGroundView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public TrimmerBackGroundView(Context context,float startX, float endX) {
        super(context);
        paint = new Paint();
        this.startX = startX;
        this.endX = endX;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.editorTrimmer));
        paint.setStyle(Paint.Style.FILL);
        DisplayMetrics outMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        int dpi = outMetrics.densityDpi / 160;
        Log.d("onDraw", "onDraw: " +startX + " / "+ endX);
        Log.d("onDraw", "onDraw: dpi" + dpi);
        paint.setStrokeWidth(dpi);
        canvas.drawRect(startX, 0,endX, Math.round(77* dpi), paint);
       }

    public float getStartX() {
        return startX;
    }

    public void setStartX(float startX) {
        this.startX = startX;
    }

    public float getEndX() {
        return endX;
    }

    public void setEndX(float endX) {
        this.endX = endX;
    }
}
