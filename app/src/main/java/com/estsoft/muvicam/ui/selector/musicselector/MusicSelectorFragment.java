package com.estsoft.muvicam.ui.selector.musicselector;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.EditorVideo;

import java.util.ArrayList;

public class MusicSelectorFragment extends Fragment {
    public static final String DATA_RECEIVE = "SELECTED_VIDEOS";

    //선택된 video list (수정필요!)
    private static ArrayList<EditorVideo> list = new ArrayList<>();

    public MusicSelectorFragment() {
        // Required empty public constructor
    }


    public static MusicSelectorFragment newInstance() {
        MusicSelectorFragment fragment = new MusicSelectorFragment();
        Bundle args = new Bundle();

        //선택된 video list 받는 곳 (수정필요!)
        if (args != null) {
            list = (ArrayList<EditorVideo>) args.get(DATA_RECEIVE);
        }
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
        return inflater.inflate(R.layout.fragment_music_selector, container, false);
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
    }
}
