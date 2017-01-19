package com.estsoft.muvicam.ui.share;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import com.estsoft.muvicam.injection.qualifier.ActivityContext;
import com.estsoft.muvicam.transcoder.noneencode.MediaConcater;
import com.estsoft.muvicam.transcoder.utils.TranscodeUtils;
import com.estsoft.muvicam.transcoder.wrappers.MediaEditorNew;
import com.estsoft.muvicam.transcoder.wrappers.MediaTranscoder;
import com.estsoft.muvicam.transcoder.wrappers.ProgressListener;
import com.estsoft.muvicam.ui.base.BasePresenter;
import com.estsoft.muvicam.ui.share.injection.ShareScope;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import rx.Subscription;

/**
 * Created by estsoft on 2017-01-19.
 */

@ShareScope
public class SharePresenter extends BasePresenter<ShareMvpView>{
    private static final String TAG = "SharePresenter";
    private final int MILLI_TO_MICRO = 1000;
    private final int MODE_TRANSCODE = -2016;
    private final int MODE_CONCATENATION = -2017;

    Context mContext;
    Activity mActivity;
    Subscription mSubscription;
    private ShareMvpView mView;

    /* for concatenation */
    private String[] mVideoPaths;
    private int[] mVideoOffsets;
    private String mMusicPath;
    private int mMusicOffset;
    private int mMusicLength;

    /* for transcode */
    private int[] mVideoStarts;
    private int[] mVideoEnds;

    /* temporary OutputPath app cashing dir */
    private String mTmpStoredPath;

    private int mTranscodeMode;
    private MediaTranscoder mEditor;


    private boolean localCopied;
    private String galleryFileName;

    @Inject
    public SharePresenter(@ActivityContext Context context, Activity activity ) {
        mContext = context;
        mActivity = activity;
        mTmpStoredPath = TranscodeUtils.getAppCashingFile( mContext );
    }

    @Override
    public void attachView(ShareMvpView mvpView) {
        super.attachView(mvpView);
        this.mView = mvpView;
    }

    @Override
    public void detachView() {
        super.detachView();
    }

    public void setVideoParams( String[] videoPaths, int[] videoOffsets, int[] videoStarts, int[] videoEnds,
                                String musicPath, int musicOffset, int musicLength, boolean fromEditor ) {
        mVideoPaths = videoPaths;
        mMusicPath = musicPath;
        mMusicOffset = musicOffset;
        mMusicLength = musicLength;
        mTranscodeMode = fromEditor ? MODE_TRANSCODE : MODE_CONCATENATION;
        if ( fromEditor ) {
            mVideoStarts = videoStarts;
            mVideoEnds = videoEnds;
        } else {
            mVideoOffsets = videoOffsets;
        }
    }


    /* Business logic here */

    /* video control */
    public void doTranscode() {

        if ( mTranscodeMode == MODE_TRANSCODE ) mView.showToast( "Transcoding" );
        else mView.showToast( "Concating" );

        mView.holdFirstThumbnail( getFirstThumbnail(mVideoPaths[0]) );

        if (mEditor != null) mEditor = null;
        new Thread(() ->  {

            if ( mTranscodeMode == MODE_TRANSCODE ) {
                mEditor = transcodeTranslate();
            } else {
                mEditor = concatTranslate();
            }
            mEditor.startWork();
            mEditor = null;

        }).start();

    }

    private void setProgressInUiThread( int progress, boolean isFinished ) {
        mActivity.runOnUiThread(() -> {
            final float percentage = progress > 90 ? 99.9f : progress;
            mView.updateProgress( percentage, isFinished );
        });
    }

    private void videoSetAndStart() {
        mView.videoSetAndStart(mTmpStoredPath);
    }

    /* click control */
    public void facebookConnect() {
        Intent intent = getIntentTo( "com.facebook.katana" );
        intent = intent == null ? getMarketIntent( "com.facebook.katana" ) : intent;
        mContext.startActivity( intent );
    }
    public void twitterConnect() {
        Intent intent = getIntentTo( "com.twitter.android" );
        intent = intent == null ? getMarketIntent( "com.twitter.android" ) : intent;
        mContext.startActivity( intent );
    }
    public void instagramConnect() {
        Intent intent = getIntentTo( "com.instagram.android" );
        intent = intent == null ? getMarketIntent( "com.twitter.android" ) : intent;
        mContext.startActivity( intent );
    }
    public void storeToGallery() {
        storingToGallery();
    }

    /* for model? */
    private Intent getMarketIntent( String packageName ) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + packageName));
        return intent;
    }
    private Intent getIntentTo( String packageName ) {
        File videoFile = new File( mTmpStoredPath );
        Intent intent = new Intent( Intent.ACTION_SEND );
        intent.putExtra( Intent.EXTRA_STREAM, Uri.fromFile( videoFile ) );
        intent.putExtra( Intent.EXTRA_TEXT, "#FromMuvicam" );
        intent.setType( "video/*" );
        PackageManager packManager = mContext.getPackageManager();
        List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(intent,  PackageManager.MATCH_DEFAULT_ONLY);
        for(ResolveInfo resolveInfo: resolvedInfoList){
            if(resolveInfo.activityInfo.packageName.startsWith(packageName)){
                intent.setClassName(
                        resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name );
                return intent;
            }
        }
        return null;
    }

    private void storingToGallery() {
        if ( localCopied ) {
            mView.showToast( galleryFileName );
            return;
        }
        localCopied = true;
        galleryFileName = TranscodeUtils.distinctCodeByCurrentTime("muvigram", ".mp4");
        new Thread( () -> {
            TranscodeUtils.storeInGallery( mTmpStoredPath, galleryFileName, mContext, localStoreProgressListener);
            mActivity.runOnUiThread( () -> {
                mView.showToast( galleryFileName );
            });
        } ).start();
    }

    private MediaTranscoder transcodeTranslate() {
        int transcodeMode = mMusicPath.equals("") ? MediaEditorNew.NORMAL : MediaEditorNew.MUTE_AND_ADD_MUSIC;
        MediaEditorNew editor = new MediaEditorNew(mTmpStoredPath, transcodeMode, mTranscodeProgressListener);
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
        return editor;
    }
    private MediaTranscoder concatTranslate() {
        int concatMode = mMusicPath.equals("") ? MediaConcater.NORMAL : MediaConcater.MUTE_AND_ADD_MUSIC;
        MediaConcater concater = new MediaConcater(mTmpStoredPath, concatMode, mTranscodeProgressListener);
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

        return concater;
    }
    private Bitmap getFirstThumbnail(String path ) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource( path );
        Bitmap bitmap = retriever.getFrameAtTime(0);
        if (bitmap.getWidth() > bitmap.getHeight()) {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            bitmap = Bitmap.createBitmap( bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        return bitmap;
    }


    /* listeners */
    private ProgressListener mTranscodeProgressListener = new ProgressListener() {
        @Override
        public void onStart(long estimatedDurationUs) {
            setProgressInUiThread( 0, false );
        }

        @Override
        public void onProgress(long currentDurationUs, int percentage) {
            setProgressInUiThread( percentage, false );
        }

        @Override
        public void onComplete(long totalDuration) {
            setProgressInUiThread( 100, true );
            mActivity.runOnUiThread(() -> {
                videoSetAndStart();
            });
        }

        @Override
        public void onError(Exception exception) {
        }

    };

    private ProgressListener localStoreProgressListener = new ProgressListener() {
        @Override
        public void onStart(long estimatedDurationUs) {
            setProgressInUiThread( 0, false );
        }

        @Override
        public void onProgress(long currentDurationUs, int percentage) {
            setProgressInUiThread( percentage, false );
        }

        @Override
        public void onComplete(long totalDuration) {
            setProgressInUiThread( 100, true );
        }

        @Override
        public void onError(Exception exception) {
        }
    };

}
