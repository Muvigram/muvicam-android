package com.estsoft.muvicam.ui.share;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.estsoft.muvicam.R;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by estsoft on 2017-02-08.
 */

public class ShareBottomSheetDialogFragment extends BottomSheetDialogFragment {
    private static final String TAG = "ShareBottomSheetDialogF";

    public static final String SELECTION_CODE = "ShareBottomSheetDialogFragment.selection";
    public static final int SELECTION_REQUEST = 1;
    public static final int SELECTION_RESULT = 2;
    public static final int SELECTION_FACEBOOK = 3;
    public static final int SELECTION_INSTAGRAM = 4;
    public static final int SELECTION_TWITTER = 5;
    public static final int SELECTION_GALLERY = 6;

    public static ShareBottomSheetDialogFragment newInstance() {
        ShareBottomSheetDialogFragment fragment = new ShareBottomSheetDialogFragment();
        Bundle args = new Bundle();
        fragment.setArguments( args );
        return fragment;
    }


    private BottomSheetBehavior.BottomSheetCallback mBottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            Log.d(TAG, "onStateChanged: " + newState );
            switch ( newState ) {
                case BottomSheetBehavior.STATE_HIDDEN : dismiss(); break;
                case BottomSheetBehavior.STATE_COLLAPSED : dismiss(); break;
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {

        }
    };

    Unbinder mUnbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate( R.layout.fragment_share_bottom_sheet, container, false);
        // butterKnife doesn't work
        mUnbinder = ButterKnife.bind( view );

        view.findViewById(R.id.share_bottom_facebook).setOnClickListener(view1 -> {
            onFacebookClicked();
        });
        view.findViewById(R.id.share_bottom_instagram).setOnClickListener(view1 -> {
            onInstagramClicked();
        });
        view.findViewById(R.id.share_bottom_twitter).setOnClickListener(view1 -> {
            onTwitterClicked();
        });
        view.findViewById(R.id.share_bottom_gallery).setOnClickListener(view1 -> {
            onGalleryClicked();
        });

        return view;
//        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {

        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.fragment_share_bottom_sheet, null);
        dialog.setContentView(contentView);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetCallback);
        }
    }

    @Override
    public void onDestroyView() {
        mUnbinder.unbind();
        super.onDestroyView();
    }
    private void onFacebookClicked() {
        getTargetFragmentIntent( SELECTION_FACEBOOK );
    }
    private void onInstagramClicked() {
        getTargetFragmentIntent( SELECTION_INSTAGRAM );
    }
    private void onTwitterClicked() {
        getTargetFragmentIntent( SELECTION_TWITTER );
    }
    private void onGalleryClicked() {
        getTargetFragmentIntent( SELECTION_GALLERY );
    }

    private void getTargetFragmentIntent( int tag ) {
        Intent intent = new Intent();
        intent.putExtra(SELECTION_CODE, tag);
        getTargetFragment().onActivityResult(SELECTION_REQUEST, SELECTION_RESULT, intent);
        dismiss();
    }
}
