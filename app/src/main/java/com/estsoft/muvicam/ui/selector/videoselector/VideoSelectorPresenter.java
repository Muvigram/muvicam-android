package com.estsoft.muvicam.ui.selector.videoselector;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.estsoft.muvicam.ui.common.BackToHomeDialogFragment;
import com.estsoft.muvicam.ui.selector.SelectorActivity;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2017-01-05.
 */

public class VideoSelectorPresenter extends BasePresenter<VideoSelectorView> implements VideoSelectorAdapter.OnItemClickListener {
    private SelectorVideoData selectorVideoData;
    private VideoSelectorAdapterContract.Model adapterModel;
    private VideoSelectorAdapterContract.View adapterView;
    private int countSelected = 0;
    private String TAG = "VideoSelectorPresenter";

    @Override
    public boolean isViewAttached() {
        return super.isViewAttached();
    }

    @Override
    public VideoSelectorView getMvpView() {
        return super.getMvpView();
    }

    @Override
    public void checkViewAttached() {
        super.checkViewAttached();
    }

    @Override
    public void attachView(VideoSelectorView mvpView) {
        super.attachView(mvpView);
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
        Log.d(TAG, "onItemClick: " + countSelected + " / " + position);
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
                    Toast.makeText(view.getContext(), view.getResources().getString(R.string.selector_next_more_than_six_warning), Toast.LENGTH_SHORT).show();
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

    @Override
    public void detachView() {
        super.detachView();
        adapterModel.clearItem();
        selectorVideoData.removeAllVideos();
        countSelected = 0;
        adapterView.notifyAdapter();
    }

    // position : list of position
    public void progress(ThumbnailUtil.VideoMetaData data) {
        selectorVideoData.progressGetThumbnail(data);
    }


    public void addItems(ArrayList<EditorVideo> videos) {
        adapterModel.addItems(videos);
    }

    public void setmCallBack(VideoSelectorFragment.DataPassListener context) {
        selectorVideoData.setmCallBack(context);
    }


    public void nextButtonClick(View view, Activity activity) {
        Log.d(TAG, "nextButtonClick: " + selectorVideoData.getSelectedVideos().size());
        if (selectorVideoData.getSelectedVideos().size() == 0) {
            Toast.makeText(view.getContext(), view.getResources().getString(R.string.selector_next_less_than_one_warning), Toast.LENGTH_SHORT).show();
        } else {

            ArrayList<EditorVideo> results = new ArrayList<>();
            for (EditorVideo e : selectorVideoData.getSelectedVideos()) {
                Log.d(TAG, "nextButtonClick: " + e.toString());
                EditorVideo temp = new EditorVideo();
                temp.setStart(e.getStart());
                temp.setEnd(e.getEnd());
                temp.setVideoPath(e.getVideoPath());
                temp.setDurationMiliSec(e.getDurationMiliSec());
                results.add(temp);
            }
            selectorVideoData.getmCallBack().passData(results);
            activity.finish();
        }
    }
}
