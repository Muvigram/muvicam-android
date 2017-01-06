package com.estsoft.muvicam.ui.selector.picker;

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
import com.estsoft.muvicam.model.EditorVideoData;
import com.estsoft.muvicam.transcoder.utils.ThumbnailUtil;
import com.estsoft.muvicam.ui.base.BasePresenter;
import com.estsoft.muvicam.ui.selector.SelectActivity;

import java.util.ArrayList;


public class VideoEditorPickerFragment extends Fragment implements PickerView {
    private String TAG = "VideoEditorPickerFragment";
    private String TAG_Lib = "Lib:";
    private VideoEditorPickerAdapter videoEditorPickerAdapter;
    private TextView nextButton;
    private RecyclerView videoPickerRecyclerView;
    BasePresenter presenter;

    public interface DataPassListener {
        void passData(ArrayList<EditorVideo> data, ArrayList<EditorVideo> resultData);
    }

    private ThumbnailUtil.VideoMetaDataListener videoMetaDataListener = new ThumbnailUtil.VideoMetaDataListener() {

        @Override
        public void onProgress(final ThumbnailUtil.VideoMetaData data) {
            ((EditorPickerPresenter) presenter).progress(data);
        }

        @Override
        public void onComplete() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    VideoEditorPickerAdapter tempA = videoEditorPickerAdapter;
                    videoEditorPickerAdapter = new VideoEditorPickerAdapter(getActivity());
                    ((EditorPickerPresenter) presenter).setPickerAdapterModel(videoEditorPickerAdapter);
                    ((EditorPickerPresenter) presenter).setPickerAdapterView(videoEditorPickerAdapter);
                    ((EditorPickerPresenter) presenter).addItems(tempA.getItems());
                    videoPickerRecyclerView.setAdapter(videoEditorPickerAdapter);
                    // ?    videoEditorPickerAdapter.notifyDataSetChanged();
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
            ((EditorPickerPresenter) presenter).loadVideos(getActivity(), videoMetaDataListener);
        }
    });

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        presenter = ((SelectActivity) getActivity()).getPresenter();
        ((EditorPickerPresenter) presenter).setEditorVideoData(EditorVideoData.getInstance());
        presenter.attachView(this);

        ((EditorPickerPresenter) presenter).setmCallBack(((DataPassListener) context));
    }

    public VideoEditorPickerFragment() {
    }

    public static VideoEditorPickerFragment newInstance() {
        VideoEditorPickerFragment fragment = new VideoEditorPickerFragment();
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
        videoEditorPickerAdapter = new VideoEditorPickerAdapter(getActivity());
        ((EditorPickerPresenter) presenter).setPickerAdapterModel(videoEditorPickerAdapter);
        ((EditorPickerPresenter) presenter).setPickerAdapterView(videoEditorPickerAdapter);
        ((EditorPickerPresenter) presenter).addItems(new ArrayList<EditorVideo>());

        videoPickerRecyclerView.setAdapter(videoEditorPickerAdapter);
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
                ((EditorPickerPresenter) presenter).nextButtonClick(view);

            }
        });


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.detachView();;
    }

    @Override
    public void setPresent(BasePresenter basePresenter) {
        this.presenter = basePresenter;
    }
}
