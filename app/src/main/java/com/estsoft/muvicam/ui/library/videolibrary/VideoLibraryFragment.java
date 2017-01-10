package com.estsoft.muvicam.ui.library.videolibrary;

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
import com.estsoft.muvicam.ui.library.LibraryActivity;

import java.util.ArrayList;
import java.util.List;


public class VideoLibraryFragment extends Fragment implements VideoLibraryView {
    private String TAG = "VideoLibraryFragment";
    private String TAG_Lib = "Lib:";
    private VideoSelectorAdapter videoSelectorAdapter;
    private TextView nextButton;
    private RecyclerView videoPickerRecyclerView;
    BasePresenter presenter;

    public interface DataPassListener {
        void passData(List<EditorVideo> videos);
    }

    private ThumbnailUtil.VideoMetaDataListener videoMetaDataListener = new ThumbnailUtil.VideoMetaDataListener() {

        @Override
        public void onProgress(final ThumbnailUtil.VideoMetaData data) {
            ((VideoLibraryPresenter) presenter).progress(data);
        }

        @Override
        public void onComplete() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    VideoSelectorAdapter tempA = videoSelectorAdapter;
                    videoSelectorAdapter = new VideoSelectorAdapter(getActivity());
                    ((VideoLibraryPresenter) presenter).setPickerAdapterModel(videoSelectorAdapter);
                    ((VideoLibraryPresenter) presenter).setPickerAdapterView(videoSelectorAdapter);
                    ((VideoLibraryPresenter) presenter).addItems(tempA.getItems());
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
            ((VideoLibraryPresenter) presenter).loadVideos(getActivity(), videoMetaDataListener);
        }
    });

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        presenter = ((LibraryActivity) getActivity()).getPresenter();
        ((VideoLibraryPresenter) presenter).setSelectorVideoData(SelectorVideoData.getInstance());
        presenter.attachView(this);

        ((VideoLibraryPresenter) presenter).setmCallBack(((DataPassListener) context));
    }

    public VideoLibraryFragment() {
    }

    public static VideoLibraryFragment newInstance() {
        VideoLibraryFragment fragment = new VideoLibraryFragment();
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
        View v = inflater.inflate(R.layout.fragment_video_editor_picker, container, false);
        videoPickerRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view_video_picker);
        nextButton = (TextView) v.findViewById(R.id.next_button);
        videoPickerRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        videoSelectorAdapter = new VideoSelectorAdapter(getActivity());
        ((VideoLibraryPresenter) presenter).setPickerAdapterModel(videoSelectorAdapter);
        ((VideoLibraryPresenter) presenter).setPickerAdapterView(videoSelectorAdapter);
        ((VideoLibraryPresenter) presenter).addItems(new ArrayList<EditorVideo>());

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
                ((VideoLibraryPresenter) presenter).nextButtonClick(view);

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
