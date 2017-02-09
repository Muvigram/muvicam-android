package com.estsoft.muvicam.ui.editor;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.EditorVideo;
import com.estsoft.muvicam.ui.base.BaseActivity;
import com.estsoft.muvicam.ui.base.BasePresenter;
import com.estsoft.muvicam.ui.editor.edit.VideoEditorEditFragment;
import com.estsoft.muvicam.ui.editor.result.VideoEditorResultFragment;
import com.estsoft.muvicam.ui.common.BackToHomeDialogFragment;

import java.util.ArrayList;

public class EditorActivity extends BaseActivity implements VideoEditorResultFragment.DataPassListener {
    Fragment fragment;
    private BasePresenter presenter;
    String TAG = "EditorActicity";
    private final static String EXTRA_VIDEOS = "EditorActivity.videoList";
    private final static String EXTRA_MUSIC_PATH = "EditorActivity.musicPath";
    private final static String EXTRA_MUSIC_OFFSET = "EditorActivity.musicOffset";
    private final static String EXTRA_MUSIC_LENGTH = "EditorActivity.musicLength";

    //context, selected editorArray, music path, muscic offset, music length
    public static Intent newIntent(Context packageContext, ArrayList<EditorVideo> selectedVideos,
                                   String musicPath, int musicOffset, int musicLength) {
        Intent intent = new Intent(packageContext, EditorActivity.class);
        Bundle args = new Bundle();
        args.putParcelableArrayList(EXTRA_VIDEOS, selectedVideos);
        args.putString(EXTRA_MUSIC_PATH, musicPath);
        args.putInt(EXTRA_MUSIC_OFFSET, musicOffset);
        args.putInt(EXTRA_MUSIC_LENGTH, musicLength);
        intent.putExtras(args);
        return intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Bundle args = getIntent().getExtras();
        ArrayList<EditorVideo> selectedVideos = args.getParcelableArrayList(EXTRA_VIDEOS);
        String musicPath = args.getString(EXTRA_MUSIC_PATH);
        int musicOffset = args.getInt(EXTRA_MUSIC_OFFSET, 0);
        int musicLength = args.getInt(EXTRA_MUSIC_LENGTH, 0);
        passDataFToF(0, selectedVideos, new ArrayList<>(), 0, musicPath, musicOffset, musicLength);

    }

    @Override
    public void passDataFToF(int selectedNum, ArrayList<EditorVideo> selectedVideos, ArrayList<EditorVideo> resultEditorVideos, int resultVideosTotalTime, String musicPath, int musicOffset, int musicLength) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Bundle args = new Bundle();

        if (selectedNum == 0) {
            fragment = new VideoEditorResultFragment();
        } else {
            fragment = new VideoEditorEditFragment();
            args.putInt(VideoEditorResultFragment.EXTRA_FRAGMENT_NUM, selectedNum);
        }

        for (EditorVideo e : resultEditorVideos) {
            Log.d(TAG, "passDataFToF: EV" + e.toString());
        }
        Log.d(TAG, "passDataFToF: T" + resultVideosTotalTime);


        args.putParcelableArrayList(VideoEditorResultFragment.EXTRA_VIDEOS, selectedVideos);
        args.putParcelableArrayList(VideoEditorResultFragment.EXTRA_RESULT_VIDEOS, resultEditorVideos);
        args.putInt(VideoEditorResultFragment.EXTRA_RESULT_VIDEO_TOTAL_TIME, resultVideosTotalTime);
        args.putString(VideoEditorResultFragment.EXTRA_MUSIC_PATH, musicPath);
        args.putInt(VideoEditorResultFragment.EXTRA_MUSIC_OFFSET, musicOffset);
        args.putInt(VideoEditorResultFragment.EXTRA_MUSIC_LENGTH, musicLength);

        fragment.setArguments(args);
        fragmentManager.beginTransaction().replace(R.id.editor_fragment_container, fragment).disallowAddToBackStack().commit();
    }

    @Override
    public void onBackPressed() {
        BackToHomeDialogFragment fragment = BackToHomeDialogFragment.newInstance(
                getResources().getString(R.string.dialog_back_to_home));
        fragment.show(getSupportFragmentManager(), BackToHomeDialogFragment.TAG);
    }
}
