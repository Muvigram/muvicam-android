package com.estsoft.muvicam.ui.selector;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;


import com.estsoft.muvicam.R;
import com.estsoft.muvicam.injection.component.DaggerSelectorComponent;
import com.estsoft.muvicam.injection.component.SelectorComponent;
import com.estsoft.muvicam.model.EditorVideo;
import com.estsoft.muvicam.ui.base.BaseActivity;
import com.estsoft.muvicam.ui.base.BasePresenter;
import com.estsoft.muvicam.ui.base.BaseSingleFragmentActivity;
import com.estsoft.muvicam.ui.library.LibraryActivity;
import com.estsoft.muvicam.ui.selector.videoselector.VideoSelectorPresenter;
import com.estsoft.muvicam.ui.selector.videoselector.VideoSelectorFragment;
import com.estsoft.muvicam.ui.common.BackToHomeDialogFragment;
import com.estsoft.muvicam.util.DialogFactory;

import java.util.ArrayList;

// VideoEditorFragment.DataPassListener
public class SelectorActivity extends BaseSingleFragmentActivity implements VideoSelectorFragment.DataPassListener {

    public static Intent newIntent(Context packageContext) {
        Intent intent = new Intent(packageContext, SelectorActivity.class);
        return intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }

    private SelectorComponent mSelectorComponent;

    public SelectorComponent getComponent() { return mSelectorComponent; }

    public static SelectorActivity get(Fragment fragment) {
        return (SelectorActivity) fragment.getActivity();
    }

    @Override
    protected Fragment createDefaultFragment() {
        return new VideoSelectorFragment();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSelectorComponent = DaggerSelectorComponent.builder()
                .activityComponent( getActivityComponent() ).build();
        mSelectorComponent.inject( this );

//        setTheme(android.R.style.Theme_NoTitleBar_Fullscreen);
//        setContentView(R.layout.activity_selector);
//        presenter = new VideoSelectorPresenter();
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        fragment = fragmentManager.findFragmentById(R.id.selector_fragment_container);
//        if (fragment == null) {
//            fragment = new VideoSelectorFragment();
//            Bundle bundle = new Bundle();
//             here put passible things
//            fragment.setArguments(bundle);
//        }
//        fragmentManager.beginTransaction().replace(R.id.selector_fragment_container, fragment).commit();
    }

//    Fragment fragment;
//    private BasePresenter presenter;


    @Override
    public void onBackPressed() {
        BackToHomeDialogFragment fragment = BackToHomeDialogFragment.newInstance(
                getResources().getString(R.string.dialog_back_to_home));
        fragment.show(getSupportFragmentManager(), BackToHomeDialogFragment.TAG);
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

//    public BasePresenter getPresenter() {
//        return presenter;
//    }

}
