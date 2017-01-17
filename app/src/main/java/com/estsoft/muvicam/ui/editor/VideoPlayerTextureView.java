package com.estsoft.muvicam.ui.editor;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import java.io.FileNotFoundException;
import java.io.IOException;

public class VideoPlayerTextureView extends TextureView implements TextureView.SurfaceTextureListener {
    String TAG = "VideoTextureView";
    MediaPlayer videoPlayer, musicPlayer;
    Activity activity;
    int rotation, editorvideoWidth, editorVideoHeight;

    public VideoPlayerTextureView(Activity activity, MediaPlayer videoPlayer, int editorvideoWidth, int editorVideoHeight, int rotation) {
        super(activity);
        Log.d(TAG, "VideoPlayerTextureView:w " + editorvideoWidth);
        Log.d(TAG, "VideoPlayerTextureView:h " + editorVideoHeight);
        Log.d(TAG, "VideoPlayerTextureView:r " + rotation);
        this.activity = activity;
        this.videoPlayer = videoPlayer;
        this.setSurfaceTextureListener(this);
        this.editorVideoHeight = editorVideoHeight;
        this.editorvideoWidth = editorvideoWidth;
        this.rotation = rotation;
        if (rotation != 90) {
            this.setRotation(90);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        Surface s = new Surface(surfaceTexture);
        try {

            videoPlayer.setSurface(s);
            videoPlayer.setScreenOnWhilePlaying(true);

            videoPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    Log.d(TAG, "onCompletion: ");
                }
            });

            videoPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    Log.d(TAG, "onPrepared: ");
//                    ((Videoeditor) context).findViewById(R.id.video_start_button).setVisibility(VISIBLE);
                }
            });

            videoPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mediaPlayer) {
                    //       fragment.setVideoEditStartTime(mediaPlayer.getCurrentPosition());
                    Log.d(TAG, "onSeekComplete: start" + mediaPlayer.getCurrentPosition());
           //         videoPlayer.start();
                }
            });
            videoPlayer.prepare();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        Log.d(TAG, "onSurfaceTextureDestroyed: ");

        if (videoPlayer != null) videoPlayer.release();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG, "onMeasure: " + widthMeasureSpec + " / " + heightMeasureSpec);
        Log.d(TAG, "onMeasure: " + editorvideoWidth + " / " + editorVideoHeight);

        if (rotation != 90) {
            DisplayMetrics outMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
            setMeasuredDimension(outMetrics.heightPixels, outMetrics.widthPixels);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

    }
}
