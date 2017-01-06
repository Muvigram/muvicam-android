package com.estsoft.muvicam.ui.editor;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.EditorVideo;
import com.estsoft.muvicam.ui.base.BasePresenter;
import com.estsoft.muvicam.ui.editor.edit.VideoEditorEditFragment;
import com.estsoft.muvicam.ui.editor.result.VideoEditorResultFragment;
import com.estsoft.muvicam.ui.share.ShareActivity;

import java.util.ArrayList;
import java.util.List;

public class EditorActivity extends AppCompatActivity implements VideoEditorResultFragment.DataPassListener {
    Fragment fragment;
    private BasePresenter presenter;

    private final static String EXTRA_VIDEOS = "EditorActivity.videoList";
    private final static String EXTRA_MUSIC_PATH = "EditorActivity.musicPath";
    private final static String EXTRA_MUSIC_OFFSET = "EditorActivity.musicOffset";
    private final static String EXTRA_MUSIC_LENGTH = "EditorActivity.musicLength";

    //context, 선택된 editorArray, 음악 path, 음악 offset, 음악 길이
    public static Intent newIntent(Context packageContext, EditorVideo[] editorArray,
                                   String musicPath, int musicOffset, int musicLength) {
        Intent intent = new Intent(packageContext, ShareActivity.class);
        intent.putExtra(EXTRA_VIDEOS, editorArray);
        intent.putExtra(EXTRA_MUSIC_PATH, musicPath);
        intent.putExtra(EXTRA_MUSIC_OFFSET, musicOffset);
        intent.putExtra(EXTRA_MUSIC_LENGTH, musicLength);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
    }


    @Override
    public void passDataFToF(ArrayList<EditorVideo> data, int selectedNum, ArrayList<EditorVideo> resultEditorVideos) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (fragment != null) {
            //remove previous fragments to reduce memory :  picker Fragment or other edit Fragments
            fragmentManager.beginTransaction().remove(fragment).commit();
        }
        if (selectedNum == 0) {
            fragment = new VideoEditorResultFragment();

        } else {
            fragment = new VideoEditorEditFragment();

        }
        Bundle args = new Bundle();
        args.putParcelableArrayList(VideoEditorEditFragment.DATA_RECEIVE, data);
        args.putInt(VideoEditorEditFragment.SELECTED_NUM_RECEIVE, selectedNum);
        args.putParcelableArrayList(VideoEditorEditFragment.RESULT_VIDEO_RECEIVE, resultEditorVideos);
        fragment.setArguments(args);
        fragmentManager.beginTransaction().replace(R.id.editor_fragment_container, fragment).commit();
    }

}
