package com.estsoft.muvicam.ui.editor.result;

import android.app.Activity;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.ProgressBar;

import com.estsoft.muvicam.R;

public class EditorResultMediaPlayer extends MediaPlayer {
    private boolean isFirst;
    private boolean isMusicPlayer;
    private int offset;
    private Activity activity;
    private MediaPlayer.OnPreparedListener preparedListener = new OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {

            mediaPlayer.seekTo(offset);

        }
    };
    private MediaPlayer.OnSeekCompleteListener seekCompleteListener = new OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mediaPlayer) {
            if (isFirst) {
                mediaPlayer.start();
            }

            if (isMusicPlayer) {
                activity.findViewById(R.id.editor_result_progress).bringToFront();
                activity.findViewById(R.id.editor_result_delete).setVisibility(View.VISIBLE);
                activity.findViewById(R.id.editor_result_progress).setVisibility(View.VISIBLE);
            }
        }
    };
    private MediaPlayer.OnCompletionListener completionListener = new OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            if (isMusicPlayer) {
                activity.findViewById(R.id.editor_result_black_screen).bringToFront();
                ((ProgressBar) activity.findViewById(R.id.editor_result_progress)).setProgress(0);

                activity.findViewById(R.id.editor_result_progress).setVisibility(View.GONE);
                activity.findViewById(R.id.editor_result_space_linear).bringToFront();
                activity.findViewById(R.id.editor_result_delete).bringToFront();
                activity.findViewById(R.id.editor_result_progress).setVisibility(View.GONE);

            }
        }
    };

    public EditorResultMediaPlayer(Activity activity, int offset, boolean isFirst, boolean isMusicPlayer) {
        super();
        this.isFirst = isFirst;
        this.setOnSeekCompleteListener(seekCompleteListener);
        this.setOnPreparedListener(preparedListener);
        this.setOnCompletionListener(completionListener);
        this.offset = offset;
        this.activity = activity;
        this.isMusicPlayer = isMusicPlayer;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setFirst(boolean first) {
        isFirst = first;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
