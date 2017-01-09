package com.estsoft.muvicam.ui.selector.musicselector;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.EditorVideo;
import com.estsoft.muvicam.ui.editor.EditorActivity;

import java.util.ArrayList;

public class MusicSelectorFragment extends Fragment {
    public static final String DATA_RECEIVE = "SELECTED_VIDEOS";
    public static final String AUDIO_RECEIVE = "AUDIO_RECEIVE";
    public static final String AUDIO_OFFSET_RECEIVE = "AUDIO_OFFSET_RECEIVE";
    Button button;

    //선택된 video list (수정필요!)
    private static ArrayList<EditorVideo> selectedVideos = new ArrayList<>();

    public MusicSelectorFragment() {
        // Required empty public constructor
    }


    public static MusicSelectorFragment newInstance() {
        MusicSelectorFragment fragment = new MusicSelectorFragment();
        Bundle args = new Bundle();


        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        //선택된 video list 받는 곳 (수정필요!)
        if (args != null) {
            selectedVideos = args.getParcelableArrayList(DATA_RECEIVE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View v = inflater.inflate(R.layout.fragment_music_selector, container, false);

        button = (Button) v.findViewById(R.id.music_to_result);

        return v;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }



    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String audioPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/sample_song_15s.mp3";

                getActivity().startActivity(EditorActivity.newIntent(getContext(), selectedVideos, audioPath, 0, 15));
            }
        });
    }
}
