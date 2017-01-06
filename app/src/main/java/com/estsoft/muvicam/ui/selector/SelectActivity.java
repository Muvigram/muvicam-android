package com.estsoft.muvicam.ui.selector;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;


import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.EditorVideo;
import com.estsoft.muvicam.ui.base.BaseActivity;
import com.estsoft.muvicam.ui.base.BasePresenter;
import com.estsoft.muvicam.ui.editor.edit.VideoEditorEditFragment;
import com.estsoft.muvicam.ui.selector.picker.EditorPickerPresenter;
import com.estsoft.muvicam.ui.selector.picker.VideoEditorPickerFragment;
import com.estsoft.muvicam.ui.editor.result.VideoEditorResultFragment;

import java.util.ArrayList;

// VideoEditorFragment.DataPassListener
public class SelectActivity extends BaseActivity implements VideoEditorPickerFragment. DataPassListener,VideoEditorResultFragment.DataPassListener{
    Fragment fragment;
    private BasePresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_NoTitleBar_Fullscreen);
        setContentView(R.layout.activity_editor);

        presenter = new EditorPickerPresenter();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            fragment = new VideoEditorPickerFragment();
            Bundle bundle = new Bundle();
            // here put passible things
            fragment.setArguments(bundle);
            fragmentManager.beginTransaction().add(R.id.fragment_container, fragment).commit();
        }
    }

    // 0 : result fragment
    // 1 : edit fragment #selectedNum
    // or we can use another passData

    @Override
    public void passData(ArrayList<EditorVideo> data, ArrayList<EditorVideo> resultData) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (fragment != null) {
            //remove previous fragments to reduce memory :  picker Fragment or other edit Fragments
            fragmentManager.beginTransaction().remove(fragment).commit();
        }
        fragment = new VideoEditorResultFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(VideoEditorResultFragment.DATA_RECEIVE, data);
        args.putParcelableArrayList(VideoEditorResultFragment.RESULT_VIDEO_RECEIVE, resultData);
        fragment.setArguments(args);
        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }

    @Override
    public void passDataFToF(ArrayList<EditorVideo> data, int selectedNum, ArrayList<EditorVideo> resultEditorVideos) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if(fragment != null){
            //remove previous fragments to reduce memory :  picker Fragment or other edit Fragments
            fragmentManager.beginTransaction().remove(fragment).commit();
        }
        fragment = new VideoEditorEditFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(VideoEditorEditFragment.DATA_RECEIVE, data);
        args.putInt(VideoEditorEditFragment.SELECTED_NUM_RECEIVE,selectedNum);
        args.putParcelableArrayList(VideoEditorEditFragment.RESULT_VIDEO_RECEIVE, resultEditorVideos);
        fragment .setArguments(args);
        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public BasePresenter getPresenter() {
        return presenter;
    }

}
