package com.estsoft.muvicam.ui.selector;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;


import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.EditorVideo;
import com.estsoft.muvicam.ui.base.BaseActivity;
import com.estsoft.muvicam.ui.base.BasePresenter;
import com.estsoft.muvicam.ui.library.LibraryActivity;
import com.estsoft.muvicam.ui.selector.videoselector.VideoSelectorPresenter;
import com.estsoft.muvicam.ui.selector.videoselector.VideoSelectorFragment;
import com.estsoft.muvicam.ui.common.BackToHomeDialogFragment;

import java.util.ArrayList;

// VideoEditorFragment.DataPassListener
public class SelectorActivity extends BaseActivity implements VideoSelectorFragment. DataPassListener{
    Fragment fragment;
    private BasePresenter presenter;

    public static Intent newIntent(Context packageContext) {
        return new Intent(packageContext, SelectorActivity.class);
    }

    @Override
    public void onBackPressed() {
        BackToHomeDialogFragment fragment = BackToHomeDialogFragment.newInstance(
                getResources().getString(R.string.dialog_back_to_home));
        fragment.show(getSupportFragmentManager(), BackToHomeDialogFragment.TAG);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_NoTitleBar_Fullscreen);
        setContentView(R.layout.activity_selector);
        presenter = new VideoSelectorPresenter();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragment = fragmentManager.findFragmentById(R.id.selector_fragment_container);
        if (fragment == null) {
            fragment = new VideoSelectorFragment();
            Bundle bundle = new Bundle();
            // here put passible things
            fragment.setArguments(bundle);

        }

        fragmentManager.beginTransaction().replace(R.id.selector_fragment_container, fragment).commit();

    }

    // 0 : result fragment
    // 1 : edit fragment #selectedNum
    // or we can use another passData

    @Override
    public void passData(ArrayList<EditorVideo> data) {
        startActivity(LibraryActivity.newIntent(this, data));
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public BasePresenter getPresenter() {
        return presenter;
    }

}
