package com.estsoft.muvicam.ui.editor.result;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.EditorVideo;

import java.util.ArrayList;

/*
 * will show edit view
  * */
public class VideoEditorResultFragment extends Fragment {
    public static final String DATA_RECEIVE = "Selected_Videos";
    public static final String RESULT_VIDEO_RECEIVE = "Result_Videos";
    public interface DataPassListener {
        void passDataFToF(ArrayList<EditorVideo> data, int selectedNum, ArrayList<EditorVideo> resultEditorVideos);
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video_editor_result, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
