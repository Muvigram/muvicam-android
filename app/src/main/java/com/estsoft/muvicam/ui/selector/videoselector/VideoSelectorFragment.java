package com.estsoft.muvicam.ui.selector.videoselector;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.EditorVideo;
import com.estsoft.muvicam.model.SelectorVideoData;
import com.estsoft.muvicam.transcoder.utils.ThumbnailUtil;
import com.estsoft.muvicam.ui.base.BasePresenter;
import com.estsoft.muvicam.ui.selector.SelectorActivity;

import java.util.ArrayList;


public class VideoSelectorFragment extends Fragment implements VideoSelectorView {
    private String TAG = "VideoSelectorFragment";
    private String TAG_Lib = "Lib:";
    private VideoSelectorAdapter videoSelectorAdapter;
    private TextView nextButton;
    private RecyclerView videoPickerRecyclerView;
    BasePresenter presenter;

    public interface DataPassListener {
        void passData(ArrayList<EditorVideo> data);
    }

    private ThumbnailUtil.VideoMetaDataListener videoMetaDataListener = new ThumbnailUtil.VideoMetaDataListener() {

        @Override
        public void onProgress(final ThumbnailUtil.VideoMetaData data) {
            ((VideoSelectorPresenter) presenter).progress(data);
        }

        @Override
        public void onComplete() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    VideoSelectorAdapter tempA = videoSelectorAdapter;
                    videoSelectorAdapter = new VideoSelectorAdapter(getActivity());
                    ((VideoSelectorPresenter) presenter).setPickerAdapterModel(videoSelectorAdapter);
                    ((VideoSelectorPresenter) presenter).setPickerAdapterView(videoSelectorAdapter);
                    ((VideoSelectorPresenter) presenter).addItems(tempA.getItems());
                    videoPickerRecyclerView.setAdapter(videoSelectorAdapter);
                    // ?    videoSelectorAdapter.notifyDataSetChanged();
                }
            });

        }

        @Override
        public void onError(Exception e) {
            Log.d(TAG_Lib, "VMDListener");
        }
    };
    Thread getThumbnailObjectThread = new Thread(new Runnable() {
        @Override
        public void run() {
            ((VideoSelectorPresenter) presenter).loadVideos(getActivity(), videoMetaDataListener);
        }
    });

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        presenter = ((SelectorActivity) getActivity()).getPresenter();
        Log.d(TAG, "onAttach: isnull?"+(presenter==null));
        ((VideoSelectorPresenter) presenter).setSelectorVideoData(SelectorVideoData.getInstance());
        presenter.attachView(this);

        ((VideoSelectorPresenter) presenter).setmCallBack(((DataPassListener) getContext()));

    }

    public VideoSelectorFragment() {
    }

    public static VideoSelectorFragment newInstance() {
        VideoSelectorFragment fragment = new VideoSelectorFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_selector_video, container, false);
        videoPickerRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view_video_picker);
        nextButton = (TextView) v.findViewById(R.id.next_button);
        videoPickerRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        videoSelectorAdapter = new VideoSelectorAdapter(getActivity());
        ((VideoSelectorPresenter) presenter).setPickerAdapterModel(videoSelectorAdapter);
        ((VideoSelectorPresenter) presenter).setPickerAdapterView(videoSelectorAdapter);
        ((VideoSelectorPresenter) presenter).addItems(new ArrayList<EditorVideo>());

        videoPickerRecyclerView.setAdapter(videoSelectorAdapter);
        getThumbnailObjectThread.setPriority(Thread.MAX_PRIORITY);
        getThumbnailObjectThread.start();


        return v;
    }

    //
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        getThumbnailsThread.start();

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Intent intent = new Intent();
                ((VideoSelectorPresenter) presenter).nextButtonClick(view);

            }
        });


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }

    @Override
    public void setPresent(BasePresenter basePresenter) {
        this.presenter = basePresenter;
    }
}
