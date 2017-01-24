package com.estsoft.muvicam.ui.share;


import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.injection.component.ActivityComponent;
import com.estsoft.muvicam.ui.share.injection.DaggerShareComponent;
import com.estsoft.muvicam.ui.share.injection.ShareComponent;
import com.estsoft.muvicam.ui.common.BackToHomeDialogFragment;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ShareFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ShareFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShareFragment extends Fragment implements ShareMvpView {
    private static final String TAG = "ShareFragment";
    private final static String EXTRA_VIDEO_PATHS = "ShareFragment.videoPaths";
    private final static String EXTRA_VIDEO_OFFSETS = "ShareFragment.videoOffsets";
    private final static String EXTRA_MUSIC_PATH = "ShareFragment.musicPath";
    private final static String EXTRA_MUSIC_OFFSET = "ShareFragment.musicOffset";
    private final static String EXTRA_MUSIC_LENGTH = "ShareFragment.musicLength";
    private final static String EXTRA_FROM_CAMERA = "ShareFragment.fromCamera";
    private final static String EXTRA_VIDEO_STARTS = "ShareFragment.videoStarts";
    private final static String EXTRA_VIDEO_ENDS = "ShareFragment.videoEnds";


    ShareComponent mShareComponent;
    @Inject SharePresenter mPresenter;

    @BindView(R.id.share_result_video) ShareVideoView mVideoView;
    @BindView(R.id.share_camera_home) ImageView mHome;
    @BindView(R.id.share_save) ImageView mSave;
    @BindView(R.id.share_sns_facebook) ImageView mFacebook;
    @BindView(R.id.share_sns_twitter) ImageView mTwitter;
    @BindView(R.id.share_sns_instagram) ImageView mInstagram;
    @BindView(R.id.share_sns_local_store) ImageView mLocalStore;
    @BindView(R.id.share_custom_progress) CircularProgressBar mProgressbar;
    @BindView(R.id.share_progress_container) FrameLayout mProgressContainer;
    @BindView(R.id.share_thumbnail_holder) ImageView mThumbnailHolder;

    @OnClick(R.id.share_sns_facebook) public void onFacebookClicked() { mPresenter.facebookConnect(); }
    @OnClick(R.id.share_sns_twitter) public void onTwitterClicked() { mPresenter.twitterConnect(); }
    @OnClick(R.id.share_sns_instagram) public void onInstagramClicked() { mPresenter.instagramConnect(); }
    @OnClick(R.id.share_sns_local_store) public void onLocalstoreClicked() { mPresenter.storeToGallery(); }
    @OnClick(R.id.share_save) public void onSaveClicked() { mPresenter.storeToGallery(); }
    @OnClick(R.id.share_camera_home) public void OnHomeClicked() {
        BackToHomeDialogFragment fragment = BackToHomeDialogFragment.newInstance(
                getResources().getString(R.string.dialog_discard_video));
        fragment.show(ShareActivity.get(this).getSupportFragmentManager(), BackToHomeDialogFragment.TAG);
    }

    Unbinder mUnbinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate( R.layout.fragment_share, container, false );
        mUnbinder = ButterKnife.bind(this, view);
        mProgressContainer.setOnTouchListener(((containerView, event) -> true));
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ActivityComponent ac = ShareActivity.get( this ).getComponent();
        mShareComponent = DaggerShareComponent.builder()
                .activityComponent( ac ).build();
        mShareComponent.inject( this );
        mPresenter.attachView( this );
        if (getArguments() != null ) {
            mPresenter.setVideoParams(
                    (String[])getArguments().getSerializable( EXTRA_VIDEO_PATHS ),
                    (int[])getArguments().getSerializable( EXTRA_VIDEO_OFFSETS ),
                    (int[])getArguments().getSerializable( EXTRA_VIDEO_STARTS ),
                    (int[])getArguments().getSerializable( EXTRA_VIDEO_ENDS ),
                    (String)getArguments().getSerializable( EXTRA_MUSIC_PATH ),
                    (int)getArguments().getSerializable( EXTRA_MUSIC_OFFSET ),
                    (int)getArguments().getSerializable( EXTRA_MUSIC_LENGTH ),
                    (boolean)getArguments().getSerializable( EXTRA_FROM_CAMERA ));
        }
        mPresenter.doTranscode();

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        mVideoView.start();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mVideoView.pause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        mVideoView.stopPlayback();
        mUnbinder.unbind();
        if (mUnbinder != null) {
            mUnbinder = null;
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mPresenter.detachView();
        if (mPresenter != null) {
            mPresenter = null;
        }
        if (mShareComponent != null) {
            mShareComponent = null;
        }
        super.onDestroy();
    }

    @Override
    public void showToast( String msg ) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT ).show();
    }

    @Override
    public void holdFirstThumbnail(Bitmap bitmap) {
        mThumbnailHolder.setImageBitmap( bitmap );
    }
    @Override
    public void updateProgress( float progress, boolean isFinished ) {
        if (!isFinished ) {
            if (mProgressContainer.getVisibility() != View.VISIBLE ) mProgressContainer.setVisibility( View.VISIBLE );
            if ( progress == 0 ) mProgressbar.setProgress( 0 );
            mProgressbar.setProgressWithAnimation( progress );
        } else {
            mProgressbar.setProgressWithAnimation( 100 );
            mProgressContainer.setVisibility( View.GONE );
        }
    }

    /**
     * view settings and view binding
     */

    @Override
    public void videoSetAndStart( String videoPath, int durationMs ) {
        mVideoView.setOnCompletionListener( mediaPlayer  ->    mVideoView.start()   );
        mVideoView.setOnPreparedListener( mediaPlayer ->  {

//            mVideoView.start();
            mThumbnailHolder.setVisibility(View.GONE);

        });
        mVideoView.setupReplay( 0, durationMs , null );
        mVideoView.setVideoPath( videoPath );
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    private OnFragmentInteractionListener mListener;
    public interface OnFragmentInteractionListener {
//         TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    public ShareFragment() {
        // Required empty public constructor
    }

    public static ShareFragment get(Fragment fragment) {
        return (ShareFragment) fragment.getParentFragment();
    }

    public static ShareFragment newInstance(String[] videoPaths, int[] videoOffsets, int[] videoStarts, int[] videoEnds,
                                            String musicPath, int musicOffset, int musicLength, boolean fromEditor) {
        ShareFragment fragment = new ShareFragment();
        Bundle args = new Bundle();
        args.putSerializable( EXTRA_VIDEO_PATHS, videoPaths );
        args.putSerializable( EXTRA_VIDEO_OFFSETS, videoOffsets );
        args.putSerializable( EXTRA_VIDEO_STARTS, videoStarts );
        args.putSerializable( EXTRA_VIDEO_ENDS, videoEnds );
        args.putSerializable( EXTRA_MUSIC_PATH, musicPath );
        args.putSerializable( EXTRA_MUSIC_OFFSET, musicOffset );
        args.putSerializable( EXTRA_MUSIC_LENGTH, musicLength );
        args.putSerializable( EXTRA_FROM_CAMERA, fromEditor);
        fragment.setArguments(args);
        return fragment;
    }
}
