package com.estsoft.muvicam.ui.editor;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.estsoft.muvicam.model.EditorVideo;
import com.estsoft.muvicam.ui.editor.edit.MuvicamMediaPlayer;
import com.estsoft.muvicam.ui.editor.result.EditorResultMediaPlayer;

import java.io.FileNotFoundException;
import java.io.IOException;

public class VideoPlayerTextureView extends TextureView implements TextureView.SurfaceTextureListener {
    String TAG = "VideoTextureView";
    MuvicamMediaPlayer muvicamMediaPlayer;
    EditorResultMediaPlayer editorResultMediaPlayer;
    Activity activity;
    int rotation, editorvideoWidth, editorVideoHeight;
    EditorVideo nowVideo;

    public VideoPlayerTextureView(Activity activity, MuvicamMediaPlayer muvicamMediaPlayer, EditorVideo editVideo, int editorVideoWidth, int editorVideoHeight, int rotation) {
        super(activity);
        this.nowVideo = editVideo;
        this.editorvideoWidth = editorVideoWidth;
        this.rotation = rotation;
        this.editorVideoHeight = editorVideoHeight;
        this.muvicamMediaPlayer = muvicamMediaPlayer;
        this.setSurfaceTextureListener(this);
        this.muvicamMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Log.d(TAG, "onCompletion: ");
                mediaPlayer.seekTo(nowVideo.getStart());
            }
        });

        this.muvicamMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                Log.d(TAG, "onPrepared: ");
                if (nowVideo != null) mediaPlayer.seekTo(nowVideo.getStart());
        }
        });

        this.muvicamMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mediaPlayer) {
                Log.d(TAG, "seekTest1 c: " + mediaPlayer.getCurrentPosition());
                nowVideo.setStart(mediaPlayer.getCurrentPosition());
                mediaPlayer.start();
            }
        });
    }

    public VideoPlayerTextureView(Activity activity, EditorResultMediaPlayer editorResultMediaPlayer, EditorVideo resultVideo, int editorVideoWidth, int editorVideoHeight, int rotation) {
        super(activity);
        this.nowVideo = resultVideo;
        this.editorvideoWidth = editorVideoWidth;
        this.rotation = rotation;
        this.editorVideoHeight = editorVideoHeight;
       this.editorResultMediaPlayer = editorResultMediaPlayer;
        this.setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        Surface s = new Surface(surfaceTexture);
        try {
            if (editorResultMediaPlayer != null) {
                editorResultMediaPlayer.setSurface(s);
                editorResultMediaPlayer.prepare();
            } else {
                muvicamMediaPlayer.setSurface(s);
                muvicamMediaPlayer.prepare();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        if (muvicamMediaPlayer != null) muvicamMediaPlayer.release();
        if (editorResultMediaPlayer != null) editorResultMediaPlayer.release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG, "onMeasure: w " + editorvideoWidth + "/ h " + editorVideoHeight + "r " + rotation);

        if ((editorvideoWidth > editorVideoHeight && rotation == 0)||(editorvideoWidth < editorVideoHeight && rotation != 0)) {
            this.setRotation(90);
        }
        if ((editorvideoWidth > editorVideoHeight && rotation == 0)||(editorvideoWidth < editorVideoHeight && rotation != 0)) {
            setMeasuredDimension(heightMeasureSpec, widthMeasureSpec);
        } else {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        FrameLayout.LayoutParams layoutParam = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        layoutParam.gravity = Gravity.CENTER;
        this.setLayoutParams(layoutParam);
    }

    public EditorVideo getResultVideo() {
        return nowVideo;
    }

    public void setResultVideo(EditorVideo nowVideo) {
        this.nowVideo = nowVideo;
    }
}
