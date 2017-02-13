package com.estsoft.muvigram.ui.selector.videoselector;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.estsoft.muvigram.R;
import com.estsoft.muvigram.model.EditorVideo;
import com.estsoft.muvigram.model.SelectorVideoData;
import com.estsoft.muvigram.ui.common.BackToHomeDialogFragment;
import com.estsoft.muvigram.ui.selector.SelectorActivity;
import com.estsoft.muvigram.ui.selector.videoselector.injection.VideoSelectorComponent;
import com.estsoft.muvigram.ui.selector.videoselector.injection.VideoSelectorModule;

import java.util.ArrayList;

import javax.inject.Inject;


public class VideoSelectorFragment extends Fragment implements VideoSelectorMvpView {
    private String TAG = "VideoSelectorFragment";
    private String TAG_Lib = "Lib:";
//    private com.estsoft.muvicam.ui.selector.videoselector.legacy.LVideoSelectorAdapter videoSelectorAdapter;
    private TextView nextButton, homeButton;
    private RecyclerView videoPickerRecyclerView;
//    BasePresenter presenter;

    public interface DataPassListener {
        void passData(ArrayList<EditorVideo> data);
    }

//    private ThumbnailUtil.VideoMetaDataListener videoMetaDataListener = new ThumbnailUtil.VideoMetaDataListener() {
//
//        @Override
//        public void onProgress(final ThumbnailUtil.VideoMetaData data) {
////            ((VideoSelectorPresenter) presenter).progress(data);
//        }
//
//        @Override
//        public void onComplete() {
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    LVideoSelectorAdapter tempA = videoSelectorAdapter;
//                    videoSelectorAdapter = new LVideoSelectorAdapter(getActivity());
//                    ((VideoSelectorPresenter) presenter).setPickerAdapterModel(videoSelectorAdapter);
//                    ((VideoSelectorPresenter) presenter).setPickerAdapterView(videoSelectorAdapter);
//                    ((VideoSelectorPresenter) presenter).addItems(tempA.getItems());
//                    videoPickerRecyclerView.setAdapter(videoSelectorAdapter);
//                    // ?    videoSelectorAdapter.notifyDataSetChanged();
//                }
//            });
//
//        }
//
//        @Override
//        public void onError(Exception e) {
//            Log.d(TAG_Lib, "VMDListener");
//        }
//
//    };
    Thread getThumbnailObjectThread = new Thread(new Runnable() {
        @Override
        public void run() {
            mPresenter.loadVideos( );
//            ((VideoSelectorPresenter) presenter).loadVideos(getActivity());
        }
    });

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        presenter = ((SelectorActivity) getActivity()).getPresenter();
//        Log.d(TAG, "onAttach: isnull?" + (presenter == null));
//        ((VideoSelectorPresenter) presenter).setSelectorVideoData(new SelectorVideoData());
//        presenter.attachView(this);

//        ((VideoSelectorPresenter) presenter).setmCallBack(((DataPassListener) getContext()));

    }

    public VideoSelectorFragment() {
    }

    public static VideoSelectorFragment newInstance() {
        VideoSelectorFragment fragment = new VideoSelectorFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Inject VideoSelectorAdapter mAdapter;
    @Inject VideoSelectorPresenter mPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_selector_video, container, false);

        homeButton = (TextView) v.findViewById(R.id.selector_home_button);
        nextButton = (TextView) v.findViewById(R.id.selector_next_button);
        videoPickerRecyclerView = (RecyclerView) v.findViewById(R.id.selector_recycler_view_videos);
        videoPickerRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

//        videoSelectorAdapter = new LVideoSelectorAdapter(getActivity());
//        ((VideoSelectorPresenter) presenter).setPickerAdapterModel(videoSelectorAdapter);
//        ((VideoSelectorPresenter) presenter).setPickerAdapterView(videoSelectorAdapter);
//        ((VideoSelectorPresenter) presenter).addItems(new ArrayList<EditorVideo>());
//        videoPickerRecyclerView.setAdapter(videoSelectorAdapter);

//        getThumbnailObjectThread.setPriority(Thread.MAX_PRIORITY);
//        getThumbnailObjectThread.start();


        return v;
    }


    VideoSelectorComponent mVideoSelectorComponent;
    //
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mVideoSelectorComponent = SelectorActivity.get(this)
                .getComponent().plus( new VideoSelectorModule() );
        mVideoSelectorComponent.inject(this);
        mPresenter.attachView( this );

        mPresenter.setSelectorVideoData(new SelectorVideoData());
        mPresenter.setmCallBack(((DataPassListener) getContext()));

        videoPickerRecyclerView.setAdapter( mAdapter );
        mPresenter.setAdapter( mAdapter );
//        mPresenter.setPickerAdapterModel( mAdapter );
//        mPresenter.setPickerAdapterView( mAdapter );

        getThumbnailObjectThread.setPriority(Thread.MAX_PRIORITY);
        getThumbnailObjectThread.start();

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.nextButtonClick( view );
//                ((VideoSelectorPresenter) presenter).nextButtonClick(view);
            }
        });

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                BackToHomeDialogFragment fragment = BackToHomeDialogFragment.newInstance(
                        view.getResources().getString(R.string.dialog_back_to_home));
                fragment.show(getFragmentManager(), BackToHomeDialogFragment.TAG);
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
//        presenter.detachView();
    }

//    @Override
//    public void setPresent(BasePresenter basePresenter) {
//        this.presenter = basePresenter;
//    }

//    @Override
//    public void setupVideoSelectorAdapter() {
//    }

}
