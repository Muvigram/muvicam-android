package com.estsoft.muvicam.model;

import android.content.Context;
import android.os.Environment;
import android.util.Pair;


import com.estsoft.muvicam.transcoder.utils.ThumbnailUtil;
import com.estsoft.muvicam.ui.selector.videoselector.VideoSelectorFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017-01-05.
 */

public class SelectorVideoData {
    private static SelectorVideoData selectorVideoData;
    private List<EditorVideo> selectedVideos = new ArrayList<>();
    private ArrayList<EditorVideo> allVideos = new ArrayList<>();

    private VideoSelectorFragment.DataPassListener mCallBack;

    private SelectorVideoData() {
    }

    public static SelectorVideoData getInstance() {
        if (selectorVideoData == null) selectorVideoData = new SelectorVideoData();
        return selectorVideoData;
    }

    public Pair<ArrayList<EditorVideo>, ArrayList<String>> getVideos(Context context) {
        ArrayList<String> videoPaths = new ArrayList<>();
        String audioDirectoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/sample_song_15s.mp3";
        File audioTest = new File(audioDirectoryPath);
        String externalStorageDirectoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Camera";
        File cameraDir = new File(externalStorageDirectoryPath);
        if (cameraDir.listFiles() != null) {
            for (File f : cameraDir.listFiles()) {
                if (f.getName().endsWith(".mp4")) {
                    EditorVideo editorVideo = new EditorVideo();
                    editorVideo.setVideoPath(f.getAbsolutePath());
                    if (audioTest != null)
                        editorVideo.setAudioPath(audioTest.getAbsolutePath());

                    allVideos.add(editorVideo);
                    videoPaths.add(editorVideo.getVideoPath());

                }
            }
        }
        Pair<ArrayList<EditorVideo>, ArrayList<String>> pair = new Pair<>(allVideos,videoPaths);
        return pair;
    }

    public void progressGetThumbnail(ThumbnailUtil.VideoMetaData data) {
        allVideos.get(data.position).setDurationMiliSec(data.durationMs);
        allVideos.get(data.position).setResolutionacceptable(isSixTVFour(data.width, data.height));
        if (data.durationMs > 180000) {
            allVideos.get(data.position).setResolutionacceptable(false);
        }
        allVideos.get(data.position).setThumbnailBitmap(data.thumbnailBitmap);
    }


    private boolean isSixTVFour(int width, int height) {
        if (width < height) {
            int temp = width;
            width = height;
            height = temp;
        }


        if (width * 9 == height * 16) {
            return true;
        } else {
            return false;
        }

    }

    public List<EditorVideo> getSelectedVideos() {
        return selectedVideos;
    }

    public void setSelectedVideos(List<EditorVideo> selectedVideos) {
        this.selectedVideos = selectedVideos;
    }

    public void removeSelectedVideo(EditorVideo selectedVideo) {
        selectedVideos.remove(selectedVideo);
    }

    public void addSelectedVideo(EditorVideo selectedVideo) {
        selectedVideos.add(selectedVideo);
    }

    public void setmCallBack(VideoSelectorFragment.DataPassListener mCallBack) {
        try {
            this.mCallBack = mCallBack;
        } catch (ClassCastException e) {
            throw new ClassCastException(mCallBack.toString()
                    + " must implement DataPassListener");
        }
    }

    public VideoSelectorFragment.DataPassListener getmCallBack() {
        return mCallBack;
    }
}
