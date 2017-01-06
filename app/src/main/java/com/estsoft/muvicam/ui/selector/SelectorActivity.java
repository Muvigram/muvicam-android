package com.estsoft.muvicam.ui.selector;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;


import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.EditorVideo;
import com.estsoft.muvicam.ui.base.BaseActivity;
import com.estsoft.muvicam.ui.base.BasePresenter;
import com.estsoft.muvicam.ui.selector.musicselector.MusicSelectorFragment;
import com.estsoft.muvicam.ui.selector.videoselector.VideoSelectorPresenter;
import com.estsoft.muvicam.ui.selector.videoselector.VideoSelectorFragment;
import com.estsoft.muvicam.ui.editor.result.VideoEditorResultFragment;

import java.util.ArrayList;

// VideoEditorFragment.DataPassListener
public class SelectorActivity extends BaseActivity implements VideoSelectorFragment. DataPassListener{
    Fragment fragment;
    private BasePresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_NoTitleBar_Fullscreen);
        setContentView(R.layout.activity_selector);

        presenter = new VideoSelectorPresenter();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            fragment = new VideoSelectorFragment();
            Bundle bundle = new Bundle();
            // here put passible things
            fragment.setArguments(bundle);
            fragmentManager.beginTransaction().add(R.id.selector_fragment_container, fragment).commit();
        }
    }

    // 0 : result fragment
    // 1 : edit fragment #selectedNum
    // or we can use another passData

    @Override
    public void passData(EditorVideo[] data) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (fragment != null) {
            //remove previous fragments to reduce memory :  picker Fragment or other edit Fragments
            fragmentManager.beginTransaction().remove(fragment).commit();
        }
        fragment = new MusicSelectorFragment();
        Bundle args = new Bundle();
        args.putParcelableArray(MusicSelectorFragment.DATA_RECEIVE, data);
        fragment.setArguments(args);
        fragmentManager.beginTransaction().replace(R.id.selector_fragment_container, fragment).commit();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public BasePresenter getPresenter() {
        return presenter;
    }

}
