package com.estsoft.muvigram.ui.share;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import com.estsoft.muvigram.R;
import com.estsoft.muvigram.injection.qualifier.ActivityContext;
import com.estsoft.muvigram.transcoder.noneencode.MediaConcater;
import com.estsoft.muvigram.transcoder.utils.TranscodeUtils;
import com.estsoft.muvigram.transcoder.wrappers.MediaEditor;
import com.estsoft.muvigram.transcoder.wrappers.MediaTranscoder;
import com.estsoft.muvigram.transcoder.wrappers.ProgressListener;
import com.estsoft.muvigram.ui.base.BasePresenter;
import com.estsoft.muvigram.ui.share.injection.ShareMediaScope;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import rx.Subscription;
import timber.log.Timber;

/**
 * Created by estsoft on 2017-01-19.
 */

@ShareMediaScope
public class SharePresenter extends BasePresenter<ShareMvpView>{
    private final String EXPORT_VIDEO_TYPE = "video/*";
    private final int REAR_ROTATION = 90;
    private final int FRONT_ROTATION = 270;
    private final int MILLI_TO_MICRO = 1000;
    private final int MODE_TRANSCODE = -2016;
    private final int MODE_CONCATENATION = -2017;

    private boolean mVideoFlipping = false;
    private int mVideoRotation = 0;

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
    private AssetFileDescriptor mLogoVideoFile;

    private int mTranscodeMode;
    private MediaTranscoder mEditor;

    private boolean localCopied;
    private String galleryFileName;

    @Inject
    public SharePresenter(@ActivityContext Context context, Activity activity ) {
        mContext = context;
        mActivity = activity;
        mTmpStoredPath = TranscodeUtils.getAppCashingFile( mContext );
        mLogoVideoFile = mContext.getResources().openRawResourceFd(R.raw.logo_sound_0d_1s);
        Timber.v("SharePresenter: %s", mTmpStoredPath );
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
        Timber.d("setVideoParams: %s", musicPath);
        mMusicOffset = musicOffset;
        mMusicLength = musicLength;
        mTranscodeMode = fromEditor ? MODE_TRANSCODE : MODE_CONCATENATION;

        if ( !fromEditor ) {
            mVideoOffsets = videoOffsets;
            translate();
        } else {
            mVideoStarts = videoStarts;
            mVideoEnds = videoEnds;
        }

        setupVideoRotationAndFlipping( mVideoPaths[0] );

    }

    /* Business logic here */

    /* video control */
    public void doTranscode() {
        mView.holdFirstThumbnail( getFirstThumbnail(mVideoPaths[0]) );

        if (mEditor != null) mEditor = null;
        new Thread(() ->  {

            mEditor = getTranscoder();
            mEditor.startWork();
            mEditor = null;

        }).start();

    }

    private void setProgressInUiThread( int progress, boolean isFinished ) {
        mActivity.runOnUiThread(() -> {
            final float percentage = progress > 95 ? 100f : progress;
            mView.updateProgress( percentage, isFinished );
        });
    }

    private void videoSetAndStart() {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource( mTmpStoredPath );
        int duration = Integer.parseInt(retriever.extractMetadata( MediaMetadataRetriever.METADATA_KEY_DURATION ));
        Timber.d("videoSetAndStart: %d", duration);
        retriever.setDataSource(mLogoVideoFile.getFileDescriptor(), mLogoVideoFile.getStartOffset(), mLogoVideoFile.getLength());
        int logoDuration = Integer.parseInt(
                        retriever.extractMetadata(  MediaMetadataRetriever.METADATA_KEY_DURATION ));

//        mView.videoSetAndStart( mTmpStoredPath, duration );
        mView.videoSetAndStart( mTmpStoredPath, duration - logoDuration );
    }

    /* click control */
    public void facebookConnect() {
        String pakageName = mContext.getResources().getString(R.string.facebook_package);
        Intent intent = getIntentTo( pakageName );
        intent = intent == null ? getMarketIntent(pakageName) : intent;
        mContext.startActivity( intent );
    }
    public void twitterConnect() {
        String pakageName = mContext.getResources().getString(R.string.twitter_package);
        Intent intent = getIntentTo( pakageName );
        intent = intent == null ? getMarketIntent( pakageName ) : intent;
        mContext.startActivity( intent );
    }
    public void instagramConnect() {
        String pakageName = mContext.getResources().getString(R.string.instagram_package);
        Intent intent = getIntentTo( pakageName );
        intent = intent == null ? getMarketIntent( pakageName ) : intent;
        mContext.startActivity( intent );
    }
    public void storeToGallery() {
        storingToGallery();
    }

    public void deleteCashFile() {
        File file = new File( mTmpStoredPath );
        if (file.exists()) file.delete();
    }

    private void setupVideoRotationAndFlipping ( String videoSamplePath ) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource( videoSamplePath );
        mVideoRotation = Integer.parseInt(retriever.extractMetadata( MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION ));
        if (mVideoRotation == FRONT_ROTATION) mVideoFlipping = true;
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
        intent.putExtra( Intent.EXTRA_TEXT, mContext.getResources().getString(R.string.export_hashtag) );
        intent.setType( EXPORT_VIDEO_TYPE );
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

    private void translate() {
        mVideoStarts = new int[ mVideoOffsets.length ];
        mVideoEnds = new int[ mVideoOffsets.length ];
        for ( int i = 0; i < mVideoOffsets.length; i ++ ) {
            int startTimeMs = 0;
            int endTimeMs = i == mVideoOffsets.length - 1 ? mMusicLength - mVideoOffsets[i] : mVideoOffsets[i + 1] - mVideoOffsets[i];
            Timber.i("translate: [%d] ... %d/%d", i, startTimeMs, endTimeMs);
            mVideoStarts[i] = startTimeMs;
            mVideoEnds[i] = endTimeMs;
        }
    }

    private MediaTranscoder getTranscoder() {
        int transcodeMode = mMusicPath.equals("") ? MediaEditor.NORMAL : MediaEditor.MUTE_AND_ADD_MUSIC;
        MediaTranscoder editor = new MediaEditor(mTmpStoredPath, transcodeMode, mTranscodeProgressListener);
        editor.initVideoTarget(1, 30, 5000000, 90, 1280, 720 );
        editor.initAudioTarget(44100, 2, 128 * 1000);
        for (int i = 0; i < mVideoPaths.length; i++) {
            editor.addSegment(mVideoPaths[i], mVideoStarts[i] * MILLI_TO_MICRO, mVideoEnds[i] * MILLI_TO_MICRO, 100);
            Timber.d("getTranscoder: [%d] ... %d/%d", i, mVideoStarts[i] * MILLI_TO_MICRO, mVideoEnds[i] * MILLI_TO_MICRO);
        }

        editor.addLogoSegment(mLogoVideoFile, -1, -1, 100);

        if (transcodeMode == MediaEditor.MUTE_AND_ADD_MUSIC) {
            editor.addMusicSegment(mMusicPath, (long) (mMusicOffset * MILLI_TO_MICRO), 100);
        }
        return editor;
    }

    private MediaTranscoder concatTranslate() {
        int concatMode = mMusicPath.equals("") ? MediaConcater.NORMAL : MediaConcater.MUTE_AND_ADD_MUSIC;
        MediaConcater concater = new MediaConcater(mTmpStoredPath, concatMode, mTranscodeProgressListener);
        for ( int i = 0; i < mVideoPaths.length; i ++ ) {
            long startTimeUs = 0;
            long endTimeUs = i == mVideoOffsets.length - 1 ? mMusicLength - mVideoOffsets[i] : mVideoOffsets[i + 1] - mVideoOffsets[i];
            endTimeUs *= MILLI_TO_MICRO;
            Timber.i("translate: [%d] ... %d/%d", i, startTimeUs, endTimeUs);
            concater.addSegment( mVideoPaths[i], startTimeUs, endTimeUs, 100);
        }
        if (concatMode == MediaConcater.MUTE_AND_ADD_MUSIC) {
            Timber.i("concatTranslater: [music] ... %d/%d/%d", mMusicOffset, mMusicLength, (mMusicLength - mMusicOffset) );
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
        if ( mVideoFlipping ) {
            Matrix flipMatrix = new Matrix();
            flipMatrix.preScale(-1, 1);
            bitmap = Bitmap.createBitmap( bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), flipMatrix, false);
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
