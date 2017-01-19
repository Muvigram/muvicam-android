package com.estsoft.muvicam.ui.share;


import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.injection.component.ActivityComponent;
import com.estsoft.muvicam.transcoder.noneencode.MediaConcater;
import com.estsoft.muvicam.transcoder.utils.TranscodeUtils;
import com.estsoft.muvicam.transcoder.wrappers.MediaEditorNew;
import com.estsoft.muvicam.transcoder.wrappers.ProgressListener;
import com.estsoft.muvicam.ui.share.injection.DaggerShareComponent;
import com.estsoft.muvicam.ui.share.injection.ShareComponent;

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

    ShareComponent mShareComponent;

    private final static String EXTRA_VIDEO_PATHS = "ShareFragment.videoPaths";
    private final static String EXTRA_VIDEO_OFFSETS = "ShareFragment.videoOffsets";
    private final static String EXTRA_MUSIC_PATH = "ShareFragment.musicPath";
    private final static String EXTRA_MUSIC_OFFSET = "ShareFragment.musicOffset";
    private final static String EXTRA_MUSIC_LENGTH = "ShareFragment.musicLength";
    private final static String EXTRA_FROM_CAMERA = "ShareFragment.fromCamera";
    private final static String EXTRA_VIDEO_STARTS = "ShareFragment.videoStarts";
    private final static String EXTRA_VIDEO_ENDS = "ShareFragment.videoEnds";

//    private static final String TEST_PATH_ORIGIN_CAMERA_01 = "/storage/emulated/0/test_video/20161212_112049.mp4";
//    private static final String TEST_PATH_ORIGIN_CAMERA_02 = "/storage/emulated/0/test_video/20161130_131929.mp4";
//    private static final String TEST_PATH_ORIGIN_CAMERA_03 = "/storage/emulated/0/test_video/20161208_144759.mp4";
//
//    private static final String TEST_PATH_MUVIGRAM_CAMERA_01 = "/storage/emulated/0/test_video/1483402446855_0.mp4";
//    private static final String TEST_PATH_MUVIGRAM_CAMERA_02 = "/storage/emulated/0/test_video/1483402446906_1.mp4";
//    private static final String TEST_PATH_MUVIGRAM_CAMERA_03 = "/storage/emulated/0/test_video/1483402484394_0.mp4";
//    private static final String TEST_PATH_MUVIGRAM_CAMERA_04 = "/storage/emulated/0/test_video/1483402593109_0.mp4";
//    private static final String TEST_PATH_MUVIGRAM_CAMERA_05 = "/storage/emulated/0/test_video/1483402633321_2.mp4";
//
//    private static final String TEST_PATH_MUVIGRAM_CAMERA_5000K = "/storage/emulated/0/test_video/camera_sample_5000k.mp4";
//    private static final String TEST_PATH_OUTSIDE_GIRLSDAY = "/storage/emulated/0/test_video/girlsday_sample.mp4";
//
//    private static final String TEST_PATH_NEXUS_CAMERA_01 = "/storage/emulated/0/test_video/nexus_sample01.mp4";
//    private static final String TEST_PATH_NEXUS_CAMERA_02 = "/storage/emulated/0/test_video/nexus_sample02.mp4";
//
//    private static final String TEST_PATH_MP3_63S = "/storage/emulated/0/test_video/sample_sound_63s.mp3";
//    private static final String TEST_PATH_MP3_221S = "/storage/emulated/0/test_video/sample_song_221s.mp3";
//    private static final String TEST_PATH_MP3_253S = "/storage/emulated/0/test_video/sample_song_253s.mp3";

    private static final int MICRO_WEIGHT = 1000000;
    private static final int MILLI_TO_MICRO = 1000;

    private final int LOCAL_STORE = -55;
    private final int TRANSCODE = -56;
    private int currentWork = LOCAL_STORE;

    private String[] mVideoPaths;
    private int[] mVideoOffsets;
    private int[] mVideoStarts;
    private int[] mVideoEnds;
    private String mMusicPath;
    private int mMusicOffset;
    private int mMusicLength;
    private String mOutputPath;
    private boolean mFromEditor;

    private boolean localCopied;

    @Inject SharePresenter mPresenter;

    @BindView(R.id.result_video) VideoView mVideoView;
    @BindView(R.id.sns_facebook) ImageView mFacebook;
    @BindView(R.id.sns_twitter) ImageView mTwitter;
    @BindView(R.id.sns_instagram) ImageView mInstagram;
    @BindView(R.id.sns_local_store) ImageView mLocalStore;
    @BindView(R.id.custom_progress) CircularProgressBar mProgressbar;
    @BindView(R.id.progress_container) FrameLayout mProgressContainer;
    @BindView(R.id.thumbnail_holder) ImageView mThumbnailHolder;

    @OnClick(R.id.sns_facebook) public void onFacebookClicked() { mPresenter.facebookConnect(); }
    @OnClick(R.id.sns_twitter) public void onTwitterClicked() { mPresenter.twitterConnect(); }
    @OnClick(R.id.sns_instagram) public void onInstagramClicked() { mPresenter.instagramConnect(); }
    @OnClick(R.id.sns_local_store) public void onLocalstoreClicked() { mPresenter.storeToGallery(); }

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
//        viewBind( view );
//        viewListenerSetting();
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
//        workTranscode();

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroyView() {
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


//    private void onFacebookClicked() {
//        Toast.makeText(getContext(), "onFacebookClicked", Toast.LENGTH_SHORT).show();
//    }
//    private void onInstagramClicked() {
//        Toast.makeText(getContext(), "onInstagramClicked", Toast.LENGTH_SHORT).show();
//    }

    private void onYoutubeClicked() {
        Toast.makeText(getContext(), "onYoutubeClicked", Toast.LENGTH_SHORT).show();
    }
    private void onNaverLineClicked() {
        Toast.makeText(getContext(), "onNaverLineClicked", Toast.LENGTH_SHORT).show();
    }
    private void onLocalStoreClicked() {
        if (localCopied) return;
        localCopied = true;
        currentWork = LOCAL_STORE;
        new Thread(() -> {
                final String fileName = TranscodeUtils.distinctCodeByCurrentTime("muvigram", ".mp4");
                TranscodeUtils.storeInGallery(TranscodeUtils.getAppCashingFile(getContext()),
                        fileName, getContext(), mProgressListener);
                getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), fileName, Toast.LENGTH_LONG).show();
                });
        }).start();
    }

    private void workTranscode() {
        currentWork = TRANSCODE;

        if (mFromEditor) Toast.makeText( getContext(), " Transcoding ", Toast.LENGTH_LONG ).show();
        else Toast.makeText( getContext(), " Concating ", Toast.LENGTH_LONG ).show();

        new Thread(() -> {

            if (mFromEditor) {
                transcodeTranslator();
            }
            else{
                concatTranslator();
            }

        }).start();
    }

    @Override
    public void showToast( String msg ) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT ).show();
    }

    @Override
    public void holdFirstThumbnail(Bitmap bitmap) {
        mThumbnailHolder.setImageBitmap( bitmap );
    }

    private void transcodeTranslator() {
        int transcodeMode = mMusicPath.equals("") ? MediaEditorNew.NORMAL : MediaEditorNew.MUTE_AND_ADD_MUSIC;
        MediaEditorNew editor = new MediaEditorNew( mOutputPath, transcodeMode, mProgressListener );
        editor.initVideoTarget( 1, 30, 5000000, 90, 1280, 720 );
        editor.initAudioTarget( 44100, 2, 128 * 1000 );
        for ( int i = 0; i < mVideoPaths.length; i ++ ) {
            long startTimeUs = 0;
            long endTimeUs = i == mVideoOffsets.length - 1 ? mMusicLength - mVideoOffsets[i] : mVideoOffsets[i + 1] - mVideoOffsets[i];
            endTimeUs *= MILLI_TO_MICRO;
            Log.e(TAG, "transcodeTranslator: [" + i + "] ... "  + startTimeUs + " / " + endTimeUs );
            editor.addSegment( mVideoPaths[i], startTimeUs, endTimeUs, 100);
        }
        if (transcodeMode == MediaEditorNew.MUTE_AND_ADD_MUSIC) {
            Log.e(TAG, "transcodeTranslator: [music] ... "  + mMusicOffset + " / " + mMusicLength + " / " + (mMusicLength - mMusicOffset) );
            editor.addMusicSegment( mMusicPath, (long)(mMusicOffset * MILLI_TO_MICRO), 100 );
        }
        editor.startWork();
    }

    private void concatTranslator() {
        int concatMode = mMusicPath.equals("") ? MediaConcater.NORMAL : MediaConcater.MUTE_AND_ADD_MUSIC;
        MediaConcater concater = new MediaConcater( mOutputPath, concatMode, mProgressListener );
        for ( int i = 0; i < mVideoPaths.length; i ++ ) {
            long startTImeUs = 0;
            long endTimeUs = i == mVideoOffsets.length - 1 ? mMusicLength - mVideoOffsets[i] : mVideoOffsets[i + 1] - mVideoOffsets[i];
            endTimeUs *= MILLI_TO_MICRO;
            Log.e(TAG, "concatTranslater: [" + i + "] ... "  + startTImeUs + " / " + endTimeUs );
            concater.addSegment( mVideoPaths[i], startTImeUs, endTimeUs, 100);
        }
        if (concatMode == MediaConcater.MUTE_AND_ADD_MUSIC) {
            Log.e(TAG, "concatTranslater: [music] ... "  + mMusicOffset + " / " + mMusicLength + " / " + (mMusicLength - mMusicOffset) );
            concater.addMusicSegment( mMusicPath, (long)(mMusicOffset * MILLI_TO_MICRO), 100 );
        }
        concater.startWork();
    }

    @Override
    public void updateProgress( float progress, boolean isFinished ) {
        if (!isFinished ) {
            if (mProgressContainer.getVisibility() != View.VISIBLE ) mProgressContainer.setVisibility( View.VISIBLE );
            if ( progress == 0 ) mProgressbar.setProgress( 0 );
            mProgressbar.setProgressWithAnimation( progress + 10 );
        } else {
            mProgressbar.setProgressWithAnimation( 100 );
            mProgressContainer.setVisibility( View.GONE );
        }
    }

    private ProgressListener mProgressListener = new ProgressListener() {
        @Override
        public void onStart(long estimatedDurationUs) {
            getActivity().runOnUiThread(() -> {
                    mProgressbar.setProgress(0);
                    mProgressContainer.setVisibility(View.VISIBLE);
                });
        }

        @Override
        public void onProgress(long currentDurationUs, final int percentage) {
            getActivity().runOnUiThread( () -> {
                mProgressbar.setProgressWithAnimation(percentage + 10);
            });
        }

        @Override
        public void onComplete(long totalDuration) {
            getActivity().runOnUiThread(() -> {
                    mProgressbar.setProgressWithAnimation( 100 );
                    mProgressContainer.setVisibility(View.GONE);
                    if (currentWork == TRANSCODE) videoViewSetting();
            });
        }
        @Override
        public void onError(Exception exception) {

        }
    };

    /**
     * view settings and view binding
     */
    private void viewBind( View view ) {
        mFacebook.setVisibility(View.INVISIBLE);
        mInstagram.setVisibility(View.INVISIBLE);
        mTwitter.setVisibility(View.INVISIBLE);
        mProgressContainer.setOnTouchListener(disableTouch);
        mProgressContainer.setVisibility(View.GONE);
    }

    @Override
    public void videoSetAndStart( String videoPath ) {
        mVideoView.setOnCompletionListener( mediaPlayer  ->    mVideoView.start()   );
        mVideoView.setOnPreparedListener( mediaPlayer ->  {
            mVideoView.start();
//                disappearThumbnail( 200 );
            mThumbnailHolder.setVisibility(View.GONE);
        });
        mVideoView.setVideoPath( videoPath );

    }

    private void videoViewSetting() {
        mVideoView.setOnCompletionListener( mediaPlayer  ->    mVideoView.start()   );
        mVideoView.setOnPreparedListener( mediaPlayer ->  {
            mVideoView.start();
//                disappearThumbnail( 200 );
            mThumbnailHolder.setVisibility(View.GONE);
        });
        mVideoView.setVideoPath( mOutputPath );
    }

    private void disappearThumbnail( final long timeUs ) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(timeUs);
                } catch ( InterruptedException e ) {
                    e.printStackTrace();
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mThumbnailHolder.setVisibility(View.GONE);
                    }
                });
            }
        }).start();
    }

    private View.OnTouchListener disableTouch = ((view, event) -> true);


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
