package com.estsoft.muvicam.ui.editor;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.EditorVideo;
import com.estsoft.muvicam.ui.base.BasePresenter;
import com.estsoft.muvicam.ui.editor.edit.VideoEditorEditFragment;
import com.estsoft.muvicam.ui.editor.result.VideoEditorResultFragment;
import com.estsoft.muvicam.ui.home.HomeActivity;
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
    public static Intent newIntent(Context packageContext, ArrayList<EditorVideo> selectedVideos,
                                   String musicPath, int musicOffset, int musicLength) {
        Intent intent = new Intent(packageContext, EditorActivity.class);
        Bundle args = new Bundle();
        args.putParcelableArrayList(EXTRA_VIDEOS, selectedVideos);
        args.putString(EXTRA_MUSIC_PATH, musicPath);
        args.putInt(EXTRA_MUSIC_OFFSET, musicOffset);
        args.putInt(EXTRA_MUSIC_LENGTH, musicLength);
        intent.putExtras(args);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_NoTitleBar_Fullscreen);
        setContentView(R.layout.activity_editor);
     //   getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        Bundle args = getIntent().getExtras();
        ArrayList<EditorVideo> selectedVideos = args.getParcelableArrayList(EXTRA_VIDEOS);
        String musicPath = args.getString(EXTRA_MUSIC_PATH);
        int musicOffset = args.getInt(EXTRA_MUSIC_OFFSET, 0);
        int musicLength = args.getInt(EXTRA_MUSIC_LENGTH, 0);

        passDataFToF(0, selectedVideos, new ArrayList<>(),0, musicPath, musicOffset, musicLength);

    }


    @Override
    public void passDataFToF(int selectedNum, ArrayList<EditorVideo> selectedVideos, ArrayList<EditorVideo> resultEditorVideos,int resultVideosTotalTime, String musicPath, int musicOffset, int musicLength) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragment = fragmentManager.findFragmentById(R.id.editor_fragment_container);
        if (fragment != null) {
            //remove previous fragments to reduce memory :  picker Fragment or other edit Fragments
            fragmentManager.beginTransaction().remove(fragment).commit();
        }
        Bundle args = new Bundle();

        if (selectedNum == 0) {
            fragment = new VideoEditorResultFragment();
        } else {
            fragment = new VideoEditorEditFragment();
            args.putInt(VideoEditorResultFragment.EXTRA_FRAGMENT_NUM,selectedNum);
        }
        args.putParcelableArrayList(VideoEditorResultFragment.EXTRA_VIDEOS, selectedVideos);
        args.putParcelableArrayList(VideoEditorResultFragment.EXTRA_RESULT_VIDEOS, resultEditorVideos);
        args.putInt(VideoEditorResultFragment.EXTRA_RESULT_VIDEO_TOTAL_TIME,resultVideosTotalTime);
        args.putString(VideoEditorResultFragment.EXTRA_MUSIC_PATH, musicPath);
        args.putInt(VideoEditorResultFragment.EXTRA_MUSIC_OFFSET, musicOffset);
        args.putInt(VideoEditorResultFragment.EXTRA_MUSIC_LENGTH, musicLength);

        fragment.setArguments(args);
        fragmentManager.beginTransaction().add(R.id.editor_fragment_container, fragment).commit();
    }

}
