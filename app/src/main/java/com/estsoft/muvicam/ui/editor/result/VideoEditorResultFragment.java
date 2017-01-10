package com.estsoft.muvicam.ui.editor.result;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
    public final static String EXTRA_FRAGMENT_NUM = "VideoEditorResultFragment.fragmentNum";
    public final static String EXTRA_VIDEOS = "VideoEditorResultFragment.videoList";
    public final static String EXTRA_RESULT_VIDEOS = "VideoEditorResultFragment.resultVideoList";
    public final static String EXTRA_RESULT_VIDEO_TOTAL_TIME = "VideoEditorResultFragment.resultVideoTotalTime";
    public final static String EXTRA_MUSIC_PATH = "VideoEditorResultFragment.musicPath";
    public final static String EXTRA_MUSIC_OFFSET = "VideoEditorResultFragment.musicOffset";
    public final static String EXTRA_MUSIC_LENGTH = "VideoEditorResultFragment.musicLength";
    RecyclerView selectedVideoButtons;
    ImageView deleteButton;
    List<EditorVideo> resultVideos, selectedVideos;
    String musicPath;
    int musicOffset, musicLength;
    float resultVideosTotalTime;
    DataPassListener mCallBack;
    VideoEditSelectedNumberAdapter.OnItemClickListener itemClickListener = new VideoEditSelectedNumberAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {

            mCallBack.passDataFToF(position+1, (ArrayList<EditorVideo>) selectedVideos, (ArrayList<EditorVideo>) resultVideos, resultVideosTotalTime, musicPath, musicOffset, musicLength);
        }
    };


    public interface DataPassListener {
        void passDataFToF(int selectedNum, ArrayList<EditorVideo> selectedVideos, ArrayList<EditorVideo> resultEditorVideos,float resultTotalTime, String musicPath, int musicOffset, int musicLength);
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
            resultVideosTotalTime = args.getFloat(VideoEditorResultFragment.EXTRA_RESULT_VIDEO_TOTAL_TIME);
            musicPath = args.getString(VideoEditorResultFragment.EXTRA_MUSIC_PATH);
            musicOffset = args.getInt(VideoEditorResultFragment.EXTRA_MUSIC_OFFSET);
            musicLength = args.getInt(VideoEditorResultFragment.EXTRA_MUSIC_LENGTH);
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
        LinearLayout linearResultSpace = (LinearLayout) v.findViewById(R.id.editor_result_space_linear);
        ResultBarView resultBarView = new ResultBarView(getContext(),resultVideosTotalTime);
        linearResultSpace.addView(resultBarView);
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
                //    int num = videoResult.getLastVideo().getNumSelected();
                //      will be changed to num
                mCallBack.passDataFToF(0, (ArrayList<EditorVideo>) selectedVideos, (ArrayList<EditorVideo>) resultVideos, resultVideosTotalTime, musicPath, musicOffset, musicLength);
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
}
