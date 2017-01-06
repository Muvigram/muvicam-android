package com.estsoft.muvicam.ui.editor.picker;

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
import com.estsoft.muvicam.model.EditorVideoData;
import com.estsoft.muvicam.transcoder.utils.ThumbnailUtil;
import com.estsoft.muvicam.ui.base.BasePresenter;
import com.estsoft.muvicam.ui.base.MvpView;

import java.util.ArrayList;


/**
 * Created by Administrator on 2017-01-05.
 */

public class EditorPickerPresenter extends BasePresenter<PickerView> implements VideoEditorPickerAdapter.OnItemClickListener {
    private EditorVideoData editorVideoData;
    private EditorPickerAdapterContract.Model adapterModel;
    private EditorPickerAdapterContract.View adapterView;
    private int countSelected = 0;
    private String TAG = "EditorPickerPresenter";

    @Override
    public boolean isViewAttached() {
        return super.isViewAttached();
    }

    @Override
    public PickerView getMvpView() {
        return super.getMvpView();
    }

    @Override
    public void checkViewAttached() {
        super.checkViewAttached();
    }

    @Override
    public void attachView(PickerView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
    }

    public void setEditorVideoData(EditorVideoData editorVideoData) {
        this.editorVideoData = editorVideoData;
    }

    public void loadVideos(final Context context, final ThumbnailUtil.VideoMetaDataListener listener) {
        Pair<ArrayList<EditorVideo>, ArrayList<String>> pair = editorVideoData.getVideos(context);
        final ArrayList videos = pair.first;
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapterModel.addItems(videos);
            }
        });
        ThumbnailUtil.getThumbnails(pair.second, context, listener);
    }

    public void setPickerAdapterModel(EditorPickerAdapterContract.Model adapterModel) {
        this.adapterModel = adapterModel;

    }

    public void setPickerAdapterView(EditorPickerAdapterContract.View adapterView) {
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
                editorVideoData.removeSelectedVideo(adapterModel.getItem(position - 3));
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
                    editorVideoData.addSelectedVideo(adapterModel.getItem(position - 3));
                    ++countSelected;
                }


            }
        }
    }

    // position : list of position
    public void progress(ThumbnailUtil.VideoMetaData data) {
        editorVideoData.progressGetThumbnail(data);
    }


    public void addItems(ArrayList<EditorVideo> videos) {
        adapterModel.addItems(videos);
    }

    public void setmCallBack(VideoEditorPickerFragment.DataPassListener context) {
        editorVideoData.setmCallBack(context);
    }

    public void nextButtonClick(View view) {
        if (editorVideoData.getSelectedVideos().size() == 0) {
            Toast.makeText(view.getContext(), "Select at least 1 video", Toast.LENGTH_SHORT).show();
        } else {
            editorVideoData.getmCallBack().passData((ArrayList<EditorVideo>) editorVideoData.getSelectedVideos(),new ArrayList<EditorVideo>());
        }
    }
}
