package com.estsoft.muvigram.ui.editor.edit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.estsoft.muvigram.R;


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

    public VideoEditorEditSeekBar(Context context, int startX) {
        super(context);
        paint = new Paint();
        this.startX = startX;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.resultSpace));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(startX, 0, startX + getResources().getDimension(R.dimen.editbar_seek_width), getResources().getDimension(R.dimen.editbar_seek_height), paint);
    }

    public int getStartX() {
        return startX;
    }

    public void setStartX(int startX) {
        this.startX = startX;
    }
}

