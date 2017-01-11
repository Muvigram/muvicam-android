package com.estsoft.muvicam.ui.editor.result;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.EditorVideo;
import com.estsoft.muvicam.ui.editor.ResultBarView;

import java.util.ArrayList;
import java.util.List;

/*
 * will show edit view
  * */
public class VideoEditorResultFragment extends Fragment {
    String TAG = "VideoEditorResultF";
    public final static String EXTRA_FRAGMENT_NUM = "VideoEditorResultFragment.fragmentNum";
    public final static String EXTRA_VIDEOS = "VideoEditorResultFragment.videoList";
    public final static String EXTRA_RESULT_VIDEOS = "VideoEditorResultFragment.resultVideoList";
    public final static String EXTRA_RESULT_VIDEO_TOTAL_TIME = "VideoEditorResultFragment.resultVideoTotalTime";
    public final static String EXTRA_MUSIC_PATH = "VideoEditorResultFragment.musicPath";
    public final static String EXTRA_MUSIC_OFFSET = "VideoEditorResultFragment.musicOffset";
    public final static String EXTRA_MUSIC_LENGTH = "VideoEditorResultFragment.musicLength";
    RecyclerView selectedVideoButtons;
    ImageView deleteButton;
    LinearLayout linearResultSpace;
    ResultBarView resultBarView;

    List<EditorVideo> resultVideos, selectedVideos;
    String musicPath;
    int musicOffset, musicLength;
    int resultVideosTotalTime;
    DataPassListener mCallBack;
    VideoEditSelectedNumberAdapter.OnItemClickListener itemClickListener = new VideoEditSelectedNumberAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            mCallBack.passDataFToF(position + 1, (ArrayList<EditorVideo>) selectedVideos, (ArrayList<EditorVideo>) resultVideos, resultVideosTotalTime, musicPath, musicOffset, musicLength);
        }
    };


    public interface DataPassListener {
        void passDataFToF(int selectedNum, ArrayList<EditorVideo> selectedVideos, ArrayList<EditorVideo> resultEditorVideos, int resultTotalTime, String musicPath, int musicOffset, int musicLength);
    }

    public VideoEditorResultFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static VideoEditorResultFragment newInstance(String param1, String param2) {
        VideoEditorResultFragment fragment = new VideoEditorResultFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            selectedVideos = args.getParcelableArrayList(VideoEditorResultFragment.EXTRA_VIDEOS);
            resultVideos = args.getParcelableArrayList(VideoEditorResultFragment.EXTRA_RESULT_VIDEOS);
            resultVideosTotalTime = args.getInt(VideoEditorResultFragment.EXTRA_RESULT_VIDEO_TOTAL_TIME, 0);
            Log.d(TAG, "onCreate: r rvt"+resultVideosTotalTime);
            for(EditorVideo re:resultVideos){
                Log.d(TAG, "onCreate: r rvt get" +re.getStart());
            }
            musicPath = args.getString(VideoEditorResultFragment.EXTRA_MUSIC_PATH);
            musicOffset = args.getInt(VideoEditorResultFragment.EXTRA_MUSIC_OFFSET, 0);
            musicLength = args.getInt(VideoEditorResultFragment.EXTRA_MUSIC_LENGTH, 0);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_video_editor_result, container, false);
        selectedVideoButtons = (RecyclerView) v.findViewById(R.id.editor_result_buttons);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        selectedVideoButtons.setLayoutManager(linearLayoutManager);
        deleteButton = (ImageView) v.findViewById(R.id.editor_result_delete);
        linearResultSpace = (LinearLayout) v.findViewById(R.id.editor_result_space_linear);

        resultBarView = new ResultBarView(getContext(), resultVideosTotalTime);
        linearResultSpace.addView(resultBarView);

        if (resultVideosTotalTime > 0) {
            deleteButton.setTranslationX(deleteButtonLocation(resultVideosTotalTime));
            deleteButton.setVisibility(View.VISIBLE);
        }

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        VideoEditSelectedNumberAdapter videoEditSelectedNumberAdapter = new VideoEditSelectedNumberAdapter(getActivity(), selectedVideos, itemClickListener);

        selectedVideoButtons.setAdapter(videoEditSelectedNumberAdapter);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditorVideo removedVideo = resultVideos.get(resultVideos.size() - 1);
                resultVideosTotalTime -= (removedVideo.getEnd() - removedVideo.getStart());
                resultVideos.remove(resultVideos.get(resultVideos.size() - 1));
                linearResultSpace.removeView(resultBarView);
                resultBarView = new ResultBarView(getContext(), resultVideosTotalTime);
                linearResultSpace.addView(resultBarView);
                if (resultVideos.size() > 0) {
                    deleteButton.setTranslationX(deleteButtonLocation(resultVideosTotalTime));
                } else {
                    deleteButton.setVisibility(View.GONE);
                }
//                mCallBack.passDataFToF(removedVideo.getNumSelected(), (ArrayList<EditorVideo>) selectedVideos, (ArrayList<EditorVideo>) resultVideos, resultVideosTotalTime, musicPath, musicOffset, musicLength);
            }
        });
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallBack = (DataPassListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement DataPassListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private float deleteButtonLocation(float resultVideosTotalTime) {
        DisplayMetrics outMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        float widthPSec = (float)outMetrics.widthPixels/15;
        int dpi = outMetrics.densityDpi / 160;
        return (resultVideosTotalTime/1000) * widthPSec - 20*dpi;
    }
}
