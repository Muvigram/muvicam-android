package com.estsoft.muvicam.model;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Pair;


import com.estsoft.muvicam.transcoder.utils.ThumbnailUtil;
import com.estsoft.muvicam.ui.selector.videoselector.VideoSelectorFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rx.Subscription;

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

    private Subscription mSubscription;

    private int mPathColumn;

    public void initColumnIndex(Cursor cursor) {
        mPathColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
    }

    private boolean isValid(Cursor cursor) {
        return cursor.getString(mPathColumn).endsWith(".mp4");
    }

    public Uri getUri(Cursor cursor) {
        return Uri.parse(cursor.getString(mPathColumn));
    }

    // TODO - 본 directory가 아닐경우 탐색 불가, DB 사용해서 가져와야 함
    public Pair<ArrayList<EditorVideo>, ArrayList<String>> getVideos(Context context) {

        ArrayList<String> videoPaths = new ArrayList<>();

        Cursor videoCursor = context.getContentResolver().query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            null, null, null, null
        );
        initColumnIndex(videoCursor);

        if (videoCursor == null || !videoCursor.moveToFirst()) {
            return null;
        }

        for (; !videoCursor.isLast(); videoCursor.moveToNext()) {
            if (isValid(videoCursor)) {
                Uri uri = getUri(videoCursor);
                EditorVideo editorVideo = new EditorVideo();
                editorVideo.setVideoPath(uri.toString());
                allVideos.add(editorVideo);
                videoPaths.add(editorVideo.getVideoPath());
            }
        }

        Pair<ArrayList<EditorVideo>, ArrayList<String>> pair = new Pair<>(allVideos,videoPaths);
        return pair;
    }

    public void progressGetThumbnail(ThumbnailUtil.VideoMetaData data) {
        allVideos.get(data.position).setDurationMiliSec((int)(data.durationMs));
        allVideos.get(data.position).setResolutionAcceptable(isSixTVFour(data.width, data.height));
        if (data.durationMs > 180000) {
            allVideos.get(data.position).setResolutionAcceptable(false);
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
    public void removeAllVideos(){
        selectedVideos.clear();
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
