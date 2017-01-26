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


public class VideoEditorEditSeekBar extends View {
  private Paint paint;
    // ms
    private int startX;
    String TAG = "VideoEditorEditSeekBar";

    public VideoEditorEditSeekBar(Context context) {
        super(context);
    }

    public VideoEditorEditSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoEditorEditSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public VideoEditorEditSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public VideoEditorEditSeekBar(Context context,int startX) {
        super(context);
//        Log.d(TAG, "onCreate: rbv rvt" + totalTime);

        paint = new Paint();
        this.startX = startX;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(ContextCompat.getColor(getContext(), R.color.resultSpace));
        paint.setStyle(Paint.Style.FILL);
        DisplayMetrics outMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        int widthPSec = outMetrics.widthPixels / 15;
        int dpi = outMetrics.densityDpi / 160;
        Log.d("onDraw", "onDraw: editorvideoWidth" + widthPSec);
        Log.d("onDraw", "onDraw: dpi" + dpi);

        canvas.drawRect(startX, 0, startX +3*dpi, Math.round(77 * dpi), paint);
    }

    public int getStartX() {
        return startX;
    }

    public void setStartX(int startX) {
        this.startX = startX;
    }
}

