package com.estsoft.muvicam.ui.library.videolibrary;

import android.app.Activity;
import android.content.Context;
import android.util.Pair;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.EditorVideo;
import com.estsoft.muvicam.model.SelectorVideoData;
import com.estsoft.muvicam.transcoder.utils.ThumbnailUtil;
import com.estsoft.muvicam.ui.base.BasePresenter;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2017-01-05.
 */

public class VideoLibraryPresenter extends BasePresenter<VideoLibraryView> implements VideoSelectorAdapter.OnItemClickListener {
    private SelectorVideoData selectorVideoData;
    private VideoSelectorAdapterContract.Model adapterModel;
    private VideoSelectorAdapterContract.View adapterView;
    private int countSelected = 0;
    private String TAG = "VideoLibraryPresenter";

    @Override
    public boolean isViewAttached() {
        return super.isViewAttached();
    }

    @Override
    public VideoLibraryView getMvpView() {
        return super.getMvpView();
    }

    @Override
    public void checkViewAttached() {
        super.checkViewAttached();
    }

    @Override
    public void attachView(VideoLibraryView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
    }

    public void setSelectorVideoData(SelectorVideoData selectorVideoData) {
        this.selectorVideoData = selectorVideoData;
    }

    public void loadVideos(final Context context, final ThumbnailUtil.VideoMetaDataListener listener) {
        Pair<ArrayList<EditorVideo>, ArrayList<String>> pair = selectorVideoData.getVideos(context);
        final ArrayList videos = pair.first;
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapterModel.addItems(videos);
            }
        });
        ThumbnailUtil.getThumbnails(pair.second, context, listener);
    }

    public void setPickerAdapterModel(VideoSelectorAdapterContract.Model adapterModel) {
        this.adapterModel = adapterModel;

    }

    public void setPickerAdapterView(VideoSelectorAdapterContract.View adapterView) {
        this.adapterView = adapterView;
        this.adapterView.setOnClickListener(this);
    }

    //position : touched position
    @Override
    public void onItemClick(View view, int position) {
        RelativeLayout layoutSelected = (RelativeLayout) view.findViewById(R.id.layout_selected);
        FrameLayout hide = (FrameLayout) view.findViewById(R.id.video_hided);
        TextView selectedNum = (TextView) view.findViewById(R.id.video_num);
        if (position > 2 && hide.getVisibility() == View.GONE) {
            if (adapterModel.getItem(position - 3).isSelected()) {
                selectorVideoData.removeSelectedVideo(adapterModel.getItem(position - 3));
                layoutSelected.setVisibility(View.GONE);
                for (EditorVideo mvt : adapterModel.getItems()) {
                    if (mvt.getNumSelected() > adapterModel.getItem(position - 3).getNumSelected()) {
                        mvt.setNumSelected((mvt.getNumSelected() - 1));
                        adapterView.notifyAdapter();
                    }
                }
                adapterModel.getItem(position - 3).setNumSelected(-1);
                adapterModel.getItem(position - 3).setSelected(false);
                --countSelected;
            } else {
                if (countSelected > 4) {
                    //more than 5 things
                    Toast.makeText(view.getContext(), "select less than 6 ", Toast.LENGTH_SHORT).show();
                } else {
//                    Log.d(TAG, "audio" + adapterModel.getItem(position - 3).getAudioPath());
                    adapterModel.getItem(position - 3).setSelected(true);
                    adapterModel.getItem(position - 3).setNumSelected(countSelected + 1);
                    String selectedNumS = "" + adapterModel.getItem(position - 3).getNumSelected();
                    selectedNum.setText(selectedNumS);
                    layoutSelected.setVisibility(View.VISIBLE);
                    selectorVideoData.addSelectedVideo(adapterModel.getItem(position - 3));
                    ++countSelected;
                }


            }
        }
    }

    // position : list of position
    public void progress(ThumbnailUtil.VideoMetaData data) {
        selectorVideoData.progressGetThumbnail(data);
    }


    public void addItems(ArrayList<EditorVideo> videos) {
        adapterModel.addItems(videos);
    }

    public void setmCallBack(VideoLibraryFragment.DataPassListener context) {
        selectorVideoData.setmCallBack(context);
    }

    public void nextButtonClick(View view) {
        if (selectorVideoData.getSelectedVideos().size() == 0) {
            Toast.makeText(view.getContext(), "Select at least 1 video", Toast.LENGTH_SHORT).show();
        } else {
            selectorVideoData.getmCallBack().passData((List<EditorVideo>) selectorVideoData.getSelectedVideos());
        }
    }
}
