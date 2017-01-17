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
import android.widget.Toast;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.EditorVideo;
import com.estsoft.muvicam.model.ParcelableVideos;
import com.estsoft.muvicam.ui.editor.ResultBarView;
import com.estsoft.muvicam.ui.editor.edit.VideoEditorEditFragment;

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

    private  ArrayList<EditorVideo> resultVideos = new ArrayList<>(), selectedVideos = new ArrayList<>();
    String musicPath;
    int musicOffset, musicLength;
    int resultVideosTotalTime;
    DataPassListener mCallBack;
    VideoEditSelectedNumberAdapter.OnItemClickListener itemClickListener = new VideoEditSelectedNumberAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {

            Log.d(TAG, "onCreateView: resultVideosTotalTime6" + resultVideosTotalTime);
            int remainTime = 15000-resultVideosTotalTime;
            if(remainTime<1000){
                Toast.makeText(getContext(),"can not add video less than 1 second",Toast.LENGTH_SHORT).show();
            }else{
            mCallBack.passDataFToF(position + 1, selectedVideos, resultVideos, resultVideosTotalTime, musicPath, musicOffset, musicLength);
        }}
    };


    public interface DataPassListener {
        void passDataFToF(int selectedNum, ArrayList<EditorVideo> selectedVideos,ArrayList<EditorVideo>  resultEditorVideos, int resultTotalTime, String musicPath, int musicOffset, int musicLength);
    }

    public VideoEditorResultFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static VideoEditorResultFragment newInstance() {
        VideoEditorResultFragment fragment = new VideoEditorResultFragment();
        Bundle args = new Bundle();
//        args.putParcelableArrayList(VideoEditorResultFragment.EXTRA_VIDEOS, selectedVideos);
//        args.putParcelableArrayList(VideoEditorResultFragment.EXTRA_RESULT_VIDEOS, resultEditorVideos);
//        args.putInt(VideoEditorResultFragment.EXTRA_RESULT_VIDEO_TOTAL_TIME,resultVideosTotalTime);
//        args.putString(VideoEditorResultFragment.EXTRA_MUSIC_PATH, musicPath);
//        args.putInt(VideoEditorResultFragment.EXTRA_MUSIC_OFFSET, musicOffset);
//        args.putInt(VideoEditorResultFragment.EXTRA_MUSIC_LENGTH, musicLength);
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
            musicPath = args.getString(VideoEditorResultFragment.EXTRA_MUSIC_PATH);
            musicOffset = args.getInt(VideoEditorResultFragment.EXTRA_MUSIC_OFFSET, 0);
            musicLength = args.getInt(VideoEditorResultFragment.EXTRA_MUSIC_LENGTH, 0);


            for(EditorVideo e:resultVideos){
                Log.d(TAG, "onCreate: R editFR"+e.toString());
            }
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

        Log.d(TAG, "onCreateView: resultVideosTotalTime1" + resultVideosTotalTime);
        resultBarView = new ResultBarView(getContext(), resultVideosTotalTime);
        linearResultSpace.addView(resultBarView);

        if (resultVideosTotalTime > 0) {
            Log.d(TAG, "onCreateView: resultVideosTotalTime2" + resultVideosTotalTime);
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
                resultVideosTotalTime = resultVideosTotalTime - (removedVideo.getEnd() - removedVideo.getStart());
                Log.d(TAG, "onCreateView: resultVideosTotalTime3" + resultVideosTotalTime);
                resultVideos.remove(resultVideos.get(resultVideos.size() - 1));
                linearResultSpace.removeView(resultBarView);
                resultBarView = new ResultBarView(getContext(), resultVideosTotalTime);
                Log.d(TAG, "onCreateView: resultVideosTotalTime4" + resultVideosTotalTime);
                linearResultSpace.addView(resultBarView);
                if (resultVideos.size() > 0) {
                    deleteButton.setTranslationX(deleteButtonLocation(resultVideosTotalTime));
                    Log.d(TAG, "onCreateView: resultVideosTotalTime5" + resultVideosTotalTime);
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
        float widthPSec = (float) outMetrics.widthPixels / 15;
        int dpi = outMetrics.densityDpi / 160;
        return (resultVideosTotalTime / 1000) * widthPSec - 20 * dpi;
    }
}
