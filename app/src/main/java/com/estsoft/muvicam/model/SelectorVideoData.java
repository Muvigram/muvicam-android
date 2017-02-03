package com.estsoft.muvicam.model;


import com.estsoft.muvicam.data.local.VideoService;
import com.estsoft.muvicam.ui.selector.videoselector.VideoSelectorFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017-01-05.
 */

public class SelectorVideoData {
    private List<EditorVideo> selectedVideos = new ArrayList<>();
    private ArrayList<EditorVideo> allVideos = new ArrayList<>();

    private VideoSelectorFragment.DataPassListener mCallBack;

    public void progressGetThumbnail(VideoService.VideoMetaData data) {

        EditorVideo video = new EditorVideo();
        video.setVideoPath(data.videoPath);
        video.setDurationMiliSec((int)(data.durationMs));
        video.setResolutionAcceptable(isSixTVFour(data.width, data.height));
        if (data.durationMs > 180000 || data.durationMs < 3000) {
            video.setResolutionAcceptable(false);
        }
        video.setThumbnailBitmap(data.thumbnailBitmap);

        allVideos.add( video );

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

    public List<EditorVideo> getAllVideos() {
        return allVideos;
    }

    public void removeSelectedVideo(EditorVideo selectedVideo) {
        selectedVideos.remove(selectedVideo);
    }
    public void removeAllVideos(){
        selectedVideos.clear();
        selectedVideos = null;
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
