package com.estsoft.muvicam.ui.selector.videoselector;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.EditorVideo;
import com.estsoft.muvicam.model.SelectorVideoData;
import com.estsoft.muvicam.ui.base.BasePresenter;
import com.estsoft.muvicam.ui.selector.videoselector.injection.VideoSelectorScope;
import com.estsoft.muvicam.util.RxUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * Created by Administrator on 2017-01-05.
 */
@VideoSelectorScope
public class VideoSelectorPresenter extends BasePresenter<VideoSelectorMvpView> implements VideoSelectorAdapter.OnItemClickListener {
//    private VideoSelectorView mView;
    private SelectorVideoData selectorVideoData;
    private VideoSelectorAdapter mAdapter;
//    private VideoSelectorAdapterContract.Model adapterModel;
//    private VideoSelectorAdapterContract.View adapterView;
    private int countSelected = 0;
    private String TAG = "VideoSelectorPresenter";

    Subscription subscription;

    @Inject
    public VideoSelectorPresenter() {
    }

    @Override
    public boolean isViewAttached() {
        return super.isViewAttached();
    }

//    @Override
//    public VideoSelectorView getMvpView() {
//        return super.getMvpView();
//    }

    @Override
    public void checkViewAttached() {
        super.checkViewAttached();
    }

//    @Override
//    public void attachView(VideoSelectorView mvpView) {
//        super.attachView(mvpView);
//        mView = mvpView;
//    }


    public void setSelectorVideoData(SelectorVideoData selectorVideoData) {
        this.selectorVideoData = selectorVideoData;
    }

    public void loadVideos(final Context context) {

        VideoMetaDataScanner scanner = new VideoMetaDataScanner();
        subscription = scanner.getVideoMetaData(context)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<VideoMetaDataScanner.VideoMetaData>() {
                               @Override
                               public void onCompleted() {

                               }

                               @Override
                               public void onError(Throwable e) {

                               }

                               @Override
                               public void onNext(VideoMetaDataScanner.VideoMetaData data) {
                                   selectorVideoData.progressGetThumbnail(data);
                                   mAdapter.notifyDataListChanged();
//                                   adapterModel.notifyDataListChanged();
                               }
                           }
                );

    }

//    public void setPickerAdapterModel(VideoSelectorAdapterContract.Model adapterModel) {
//        this.adapterModel = adapterModel;
//        adapterModel.addItems( selectorVideoData.getAllVideos() );
//    }
//
//    public void setPickerAdapterView(VideoSelectorAdapterContract.View adapterView) {
//        this.adapterView = adapterView;
//        this.adapterView.setOnClickListener(this);
//    }

    public void setAdapter( VideoSelectorAdapter adapter ) {
        mAdapter = adapter;
        mAdapter.setOnClickListener( this );
        mAdapter.addItems( selectorVideoData.getAllVideos() );
    }

    //position : touched position
    @Override
    public void onItemClick(View view, int position) {
        Log.d(TAG, "onItemClick: " + countSelected + " / " + position);
        RelativeLayout layoutSelected = (RelativeLayout) view.findViewById(R.id.layout_selected);
        FrameLayout hide = (FrameLayout) view.findViewById(R.id.video_hided);
        TextView selectedNum = (TextView) view.findViewById(R.id.video_num);
        if (position > 2 && hide.getVisibility() == View.GONE) {
            if (mAdapter.getItem(position - 3).isSelected()) {
//            if (adapterModel.getItem(position - 3).isSelected()) {
                selectorVideoData.removeSelectedVideo( mAdapter.getItem( position - 3));
//                selectorVideoData.removeSelectedVideo(adapterModel.getItem(position - 3));
                layoutSelected.setVisibility(View.GONE);
                for (EditorVideo mvt : mAdapter.getItems()) {
//                for (EditorVideo mvt : adapterModel.getItems()) {
                    if (mvt.getNumSelected() > mAdapter.getItem(position - 3).getNumSelected()) {
                        mvt.setNumSelected((mvt.getNumSelected() - 1));
                        mAdapter.notifyAdapter();
//                        adapterView.notifyAdapter();
                    }
                }
                mAdapter.getItem(position - 3).setNumSelected( -1 );
//                adapterModel.getItem(position - 3).setNumSelected(-1);
                mAdapter.getItem(position - 3).setSelected( false );
//                adapterModel.getItem(position - 3).setSelected(false);
                --countSelected;
            } else {
                if (countSelected > 4) {
                    //more than 5 things
                    Toast.makeText(view.getContext(), view.getResources().getString(R.string.selector_next_more_than_six_warning), Toast.LENGTH_SHORT).show();
                } else {
//                    Log.d(TAG, "audio" + adapterModel.getItem(position - 3).getAudioPath());
                    mAdapter.getItem( position - 3).setSelected(true);
//                    adapterModel.getItem(position - 3).setSelected(true);
                    mAdapter.getItem(position - 3).setNumSelected( countSelected + 1);
//                    adapterModel.getItem(position - 3).setNumSelected(countSelected + 1);
                    String selectedNumS = "" + mAdapter.getItem(position - 3).getNumSelected();
//                    String selectedNumS = "" + adapterModel.getItem(position - 3).getNumSelected();
                    selectedNum.setText(selectedNumS);
                    layoutSelected.setVisibility(View.VISIBLE);
                    selectorVideoData.addSelectedVideo(mAdapter.getItem(position - 3));
//                    selectorVideoData.addSelectedVideo(adapterModel.getItem(position - 3));
                    ++countSelected;
                }


            }
        }
    }

    @Override
    public void detachView() {
        super.detachView();
        RxUtil.unsubscribe(subscription);
        mAdapter.clearItem();
//        adapterModel.clearItem();
        selectorVideoData.removeAllVideos();
        countSelected = 0;
        mAdapter.notifyAdapter();
//        adapterView.notifyAdapter();
    }

    // position : list of position
    @Deprecated
    public void progress(VideoMetaDataScanner.VideoMetaData data) {
        selectorVideoData.progressGetThumbnail(data);
    }

    public void addItems(List<EditorVideo> videos) {
        mAdapter.addItems(videos);
//        adapterModel.addItems(videos);
    }

    public void setmCallBack(VideoSelectorFragment.DataPassListener context) {
        selectorVideoData.setmCallBack(context);
    }


    public void nextButtonClick(View view) {
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
        }
    }
}
