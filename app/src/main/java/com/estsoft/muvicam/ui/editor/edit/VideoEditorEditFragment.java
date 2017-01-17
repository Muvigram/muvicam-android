package com.estsoft.muvicam.ui.editor.edit;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.EditorVideo;
import com.estsoft.muvicam.model.ParcelableVideos;
import com.estsoft.muvicam.transcoder.utils.ThumbnailUtil;
import com.estsoft.muvicam.ui.editor.MuvicamMediaPlayer;
import com.estsoft.muvicam.ui.editor.ResultBarView;
import com.estsoft.muvicam.ui.editor.VideoPlayerTextureView;
import com.estsoft.muvicam.ui.editor.result.VideoEditorResultFragment;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VideoEditorEditFragment extends Fragment {
    String TAG = "VideoEditorEditFragment";
    private ArrayList<EditorVideo> resultVideos;
    private ArrayList<EditorVideo> selectedVideos = new ArrayList<>();
    private ArrayList<EditorVideo> videoThumbnails = new ArrayList<>();
    EditorVideo nowVideo;
    String musicPath;
    int selectedNum, musicOffset, musicLength;
    int resultVideosTotalTime;
    VideoEditorResultFragment.DataPassListener mCallBack;
    LinearLayout resultSpaceLinearLayout, blackScreen;
    ResultBarView resultBarView;
    RecyclerView videoEdit;
    ImageView seekBarLeft, seekBarRight;
    VideoEditorEditAdapter videoEditAdapter;
    boolean isSeekBarChanged = false;
    ImageView cancelButton, insertButton;
    int videoThumbnailendTime;
    MuvicamMediaPlayer videoPlayer;
    MediaPlayer musicPlayer;
    VideoPlayerTextureView videoPlayerTextureView;
    View editProgressBar;
    boolean flag = true;
    ThumbnailUtil.UserBitmapListener thumbnailUtilListener = new ThumbnailUtil.UserBitmapListener() {
        @Override
        public void onBitmapNext(final Bitmap bitmap, final long presentationTimeUs, final boolean isLast) {
//            Log.d(TAG, "onBitmapNext: " + presentationTimeUs);
//            Log.d(TAG, "onBitmapNextSize: " + resultMyVideos.size());
//            Log.d(TAG, "onBitmapNextCount: " + videoEditAdapter.getItemCount());
            Log.d(TAG, "onBitmapNext: p" + presentationTimeUs);

            if (!isLast) {
                EditorVideo editMyVideo = new EditorVideo(bitmap, (int) presentationTimeUs / 1000, isLast, selectedNum);
                videoThumbnails.add(editMyVideo);
                videoEditAdapter.notifyDataSetChanged();
            }
            Log.d(TAG, "onBitmapNext123: " + presentationTimeUs + " /. " + isLast);
        }

        @Override
        public void onError(Exception e) {

        }

        @Override
        public void onComplete(long endTimeUs) {
            videoThumbnailendTime = (int) endTimeUs / 1000;
            Log.d(TAG, "onComplete: " + videoThumbnailendTime);
            DisplayMetrics outMetrics = new DisplayMetrics();
            ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
            float disPlayWidth = outMetrics.widthPixels;
            float dpi = outMetrics.densityDpi / 160;

            int remainTotalTime = 15000 - resultVideosTotalTime;

            if (videoThumbnailendTime < 15000) {
                if (remainTotalTime < videoThumbnailendTime) {
                    float length = 5 * dpi + getThumbnailSizePSec() * (remainTotalTime) / 1000;
                    Log.d(TAG, "onComplete: <15<thum " + length);
                    seekBarRight.setTranslationX(length);
                    nowVideo.setEnd(remainTotalTime);
                } else {
                    float length = 5 * dpi + getThumbnailSizePSec() * 3 * (videoThumbnails.size() - 1) + getThumbnailSizePSec() * (videoThumbnailendTime - videoThumbnails.get(videoThumbnails.size() - 1).getPresentationTimeUs()) / 1000;
                    Log.d(TAG, "onComplete: <15>thum " + length);

                    seekBarRight.setTranslationX(length);
                    nowVideo.setEnd(videoThumbnailendTime);
                }

            } else {
                if (remainTotalTime < 15000) {
                    float length = 5 * dpi + getThumbnailSizePSec() * (remainTotalTime) / 1000;
                    Log.d(TAG, "onComplete: >=15<thum " + length);

                    seekBarRight.setTranslationX(length);
                    nowVideo.setEnd(remainTotalTime);
                } else {
                    seekBarRight.setTranslationX(disPlayWidth - 5 * dpi);
                    Log.d(TAG, "onComplete: >=15>thum " + (disPlayWidth - 5 * dpi));

                    nowVideo.setEnd(15000);
                    Log.d(TAG, "onClick: endtime" + nowVideo.getEnd());
                }

            }

            seekBarLeft.setTranslationX(-seekBarLeft.getWidth() + 5 * dpi);
            Log.d(TAG, "onComplete: seekbarL X :" + seekBarLeft.getX());
            //(Math.round(videoEdit.computeHorizontalScrollOffset() * 15000 / disPlayWidth))
            nowVideo.setStart(0);
            Log.d(TAG, "onClick: starttime" + nowVideo.getStart());
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    seekBarLeft.setVisibility(View.VISIBLE);
                    seekBarRight.setVisibility(View.VISIBLE);

                }
            });

            videoPlayer.seekTo(nowVideo.getStart());
            musicPlayer.seekTo(musicOffset + resultVideosTotalTime);
            editProgressBar.setX(seekBarLeft.getX() + seekBarLeft.getWidth());
            musicPlayer.start();
            videoPlayer.start();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    videoPlayerTextureView.bringToFront();
                }
            });
            editThread.start();
        }
    };
    ThumbnailUtil thumbnailUtil;

    public VideoEditorEditFragment() {
    }

    public static VideoEditorEditFragment newInstance() {
        VideoEditorEditFragment fragment = new VideoEditorEditFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        if (args != null) {
            selectedNum = args.getInt(VideoEditorResultFragment.EXTRA_FRAGMENT_NUM);
            selectedVideos = args.getParcelableArrayList(VideoEditorResultFragment.EXTRA_VIDEOS);
            resultVideos = args.getParcelableArrayList(VideoEditorResultFragment.EXTRA_RESULT_VIDEOS);
            resultVideosTotalTime = args.getInt(VideoEditorResultFragment.EXTRA_RESULT_VIDEO_TOTAL_TIME, 0);
            Log.d(TAG, "onCreate: edit rvt" + resultVideosTotalTime);
            musicPath = args.getString(VideoEditorResultFragment.EXTRA_MUSIC_PATH);
            musicOffset = args.getInt(VideoEditorResultFragment.EXTRA_MUSIC_OFFSET, 0);
            musicLength = args.getInt(VideoEditorResultFragment.EXTRA_MUSIC_LENGTH, 0);
            nowVideo = new EditorVideo();
            nowVideo.setVideoPath(selectedVideos.get(selectedNum - 1).getVideoPath());
            nowVideo.setNumSelected(selectedNum);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_video_editor_edit, container, false);
        resultSpaceLinearLayout = (LinearLayout) v.findViewById(R.id.editor_edit_result_space_linear);
        blackScreen = (LinearLayout) v.findViewById(R.id.editor_edit_black_screen);
        Log.d(TAG, "onCreateView: edit rvt" + resultVideosTotalTime);
        resultBarView = new ResultBarView(getContext(), resultVideosTotalTime);
        resultSpaceLinearLayout.addView(resultBarView);
        videoEdit = (RecyclerView) v.findViewById(R.id.editor_edit_recycler_thumbnails);
        LinearLayoutManager linearLayoutManagerE = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        videoEdit.setLayoutManager(linearLayoutManagerE);

        seekBarLeft = (ImageView) v.findViewById(R.id.editor_seekbar_left);
        seekBarRight = (ImageView) v.findViewById(R.id.editor_seekbar_right);
        videoEditAdapter = new VideoEditorEditAdapter(getActivity(), selectedNum, nowVideo, videoThumbnails);
        videoEdit.setAdapter(videoEditAdapter);

        insertButton = (ImageView) v.findViewById(R.id.editor_edit_insert);
        cancelButton = (ImageView) v.findViewById(R.id.editor_edit_cancel);
        FrameLayout videoTextureLayout = (FrameLayout) v.findViewById(R.id.editor_edit_frame_layout);

        editProgressBar = v.findViewById(R.id.editor_edit_progress);
        thumbnailUtil = new ThumbnailUtil(thumbnailUtilListener, getActivity(), true);
        Log.d(TAG, "onCreateView: " + videoEdit.getWidth());
        // 3/15 -> 1/5
        musicPlayer = new MediaPlayer();
        videoPlayer = new MuvicamMediaPlayer();
        try {
            videoPlayer.setVolume(0, 0);
            Log.d(TAG, "onCreateView: video " + selectedVideos.get(selectedNum - 1).getVideoPath());
            FileInputStream fisMusic = new FileInputStream(musicPath);
            FileDescriptor fdMusic = fisMusic.getFD();

            videoPlayer.setDataSource(selectedVideos.get(selectedNum - 1).getVideoPath());
            musicPlayer.setDataSource(fdMusic);
            musicPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    Log.d(TAG, "onCompletion: now1");
                    if (videoPlayer.isPlaying()) videoPlayer.pause();
                    mediaPlayer.seekTo(musicOffset + resultVideosTotalTime);
                    videoPlayer.seekTo(nowVideo.getStart());

                    editProgressBar.setX(seekBarLeft.getX() + seekBarLeft.getWidth());
                    videoPlayer.start();
                    mediaPlayer.start();
                    videoPlayerTextureView.bringToFront();
                }
            });
            editProgressBar.bringToFront();
            musicPlayer.prepare();
            fisMusic.close();
        } catch (IOException io) {
            io.getStackTrace();
        }
        thumbnailUtil.extractFromNewThread(nowVideo.getVideoPath(), 3.0, getThumbnailSizePSec() * 3, getThumbnailSizePSec() * 3);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(selectedVideos.get(selectedNum - 1).getVideoPath());
        int width = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        int height = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        int rotation = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
        videoPlayerTextureView = new VideoPlayerTextureView(getActivity(), videoPlayer, width, height, rotation);
        videoTextureLayout.addView(videoPlayerTextureView);
        return v;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        seekBarLeft.setOnTouchListener(new View.OnTouchListener() {
                                           @Override
                                           public boolean onTouch(View view, MotionEvent motionEvent) {
                                               DisplayMetrics outMetrics = new DisplayMetrics();
                                               ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
                                               float dpi = outMetrics.densityDpi / 160;
                                               float remainTime = 15000 - resultVideosTotalTime;
                                               float X = motionEvent.getRawX() - ((float) view.getWidth() * 39) / 40;
                                               float delta = 0;
                                               float position = X - delta;
                                               float distance = seekBarRight.getX() - (view.getX() + view.getWidth());
                                               switch (motionEvent.getAction()) {
                                                   case MotionEvent.ACTION_DOWN:
                                                       delta = X - view.getTranslationX();
                                                       Log.d(TAG, "seekBarLeft: action_down" + delta);

                                                       break;
                                                   case MotionEvent.ACTION_MOVE:
                                                       position = X - delta;
                                                       //minwidth
                                                       view.setTranslationX(position);
                                                       Log.d(TAG, "seekBarLeft: " + (X - delta));
                                                       Log.d(TAG, "seekBarGetXLeft: " + view.getX());
                                                       Log.d(TAG, "seekBarLeftWidth: " + view.getWidth());
                                                       nowVideo.setStart((int) Math.floor((position - 5 * dpi + view.getWidth()) * 1000 / getThumbnailSizePSec()));

                                                       if (distance > getThumbnailSizePSec() * remainTime / 1000) {
                                                           float rightPosition = view.getX() + view.getWidth() + getThumbnailSizePSec() * remainTime / 1000;
                                                           if (rightPosition > 5 * dpi + getThumbnailSizePSec() * 3 * (videoThumbnails.size() - 1) + getThumbnailSizePSec() * (videoThumbnailendTime - videoThumbnails.get(videoThumbnails.size() - 1).getPresentationTimeUs()) / 1000) {
                                                               rightPosition = 5 * dpi + getThumbnailSizePSec() * 3 * (videoThumbnails.size() - 1) + getThumbnailSizePSec() * (videoThumbnailendTime - videoThumbnails.get(videoThumbnails.size() - 1).getPresentationTimeUs()) / 1000;
                                                           }
                                                           seekBarRight.setTranslationX(rightPosition);
                                                           nowVideo.setEnd((int) Math.floor((rightPosition - 5 * dpi) * 1000 / getThumbnailSizePSec()));
                                                       }
                                                       videoEditAdapter.notifyDataSetChanged();
                                                       videoPlayer.seekTo(nowVideo.getStart());
                                                       videoPlayer.pause();
                                                       musicPlayer.pause();
                                                       editProgressBar.setX(seekBarLeft.getX() + seekBarLeft.getWidth());

                                                       break;

                                                   case MotionEvent.ACTION_UP:

                                                       Log.d(TAG, "seekBarLeft: distance" + distance);
                                                       if (distance < getThumbnailSizePSec()) {
                                                           Log.d(TAG, "seekBarLeft: distance less than mindistance");
                                                           position = seekBarRight.getX() - getThumbnailSizePSec() - view.getWidth();
                                                           Log.d(TAG, "seekBarLeft: distance less than mindistance: " + position);
                                                       }


                                                       if (position < -view.getWidth() + 5 * dpi) {
                                                           position = -view.getWidth() + 5 * dpi;
                                                           Log.d(TAG, "seekBarLeft: distance less than 0: " + position);
                                                       }

                                                       view.setTranslationX(position);
                                                       //editProgress.setX(leftPosition);
                                                       nowVideo.setStart((int) Math.floor((position - 5 * dpi + view.getWidth()) * 1000 / getThumbnailSizePSec()));
                                                       Log.d(TAG, "seekBarLeft: starttime: " + nowVideo.getStart());
                                                       if (distance > getThumbnailSizePSec() * remainTime / 1000) {
                                                           float rightPosition = view.getX() + view.getWidth() + getThumbnailSizePSec() * remainTime / 1000;
                                                           if (rightPosition > 5 * dpi + getThumbnailSizePSec() * 3 * (videoThumbnails.size() - 1) + getThumbnailSizePSec() * (videoThumbnailendTime - videoThumbnails.get(videoThumbnails.size() - 1).getPresentationTimeUs()) / 1000) {
                                                               rightPosition = 5 * dpi + getThumbnailSizePSec() * 3 * (videoThumbnails.size() - 1) + getThumbnailSizePSec() * (videoThumbnailendTime - videoThumbnails.get(videoThumbnails.size() - 1).getPresentationTimeUs()) / 1000;
                                                           }
                                                           seekBarRight.setTranslationX(rightPosition);
                                                           nowVideo.setEnd((int) Math.floor((rightPosition - 5 * dpi) * 1000 / getThumbnailSizePSec()));

                                                       }
                                                       videoEditAdapter.notifyDataSetChanged();
                                                       isSeekBarChanged = true;
                                                       Log.d(TAG, "onTouch: start " + nowVideo.getStart());
                                                       videoPlayer.seekTo(nowVideo.getStart());
                                                       musicPlayer.seekTo(musicOffset + resultVideosTotalTime);
                                                       editProgressBar.setX(seekBarLeft.getX() + seekBarLeft.getWidth());


                                                       musicPlayer.start();
                                                       videoPlayer.start();
                                                       videoPlayerTextureView.bringToFront();
                                                       break;
                                               }
                                               return true;
                                           }
                                       }

        );

        seekBarRight.setOnTouchListener(new View.OnTouchListener()

                                        {
                                            @Override
                                            public boolean onTouch(View view, MotionEvent motionEvent) {
                                                DisplayMetrics outMetrics = new DisplayMetrics();
                                                ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
                                                float disPlayWidth = outMetrics.widthPixels;
                                                float dpi = outMetrics.densityDpi / 160;
                                                float distance = view.getX() - (seekBarLeft.getX() + seekBarLeft.getWidth());
                                                float X = (int) motionEvent.getRawX() - view.getWidth() / 40;
                                                float delta = 0;
                                                float position = X - delta;
                                                float remainTime = 15000 - resultVideosTotalTime;
                                                switch (motionEvent.getAction()) {
                                                    case MotionEvent.ACTION_DOWN:
                                                        delta = X - view.getTranslationX();
                                                        Log.d(TAG, "seekBarRight: action_down" + delta);
                                                        break;
                                                    case MotionEvent.ACTION_MOVE:
                                                        position = X - delta;
                                                        //minwidth
                                                        view.setTranslationX(position);
                                                        Log.d(TAG, "seekBarRight: " + (X - delta));
                                                        Log.d(TAG, "seekBarGetXRight: " + view.getX());
                                                        Log.d(TAG, "seekBarRightWidth: " + view.getWidth());

                                                        if (distance > getThumbnailSizePSec() * remainTime / 1000) {
                                                            float leftPosition = view.getX() - seekBarLeft.getWidth() - getThumbnailSizePSec() * remainTime / 1000;
                                                            if (leftPosition < -seekBarLeft.getWidth() + 5 * dpi) {
                                                                leftPosition = -seekBarLeft.getWidth() + 5 * dpi;
                                                                Log.d(TAG, "seekBarLeft: distance less than 0: " + position);
                                                            }
                                                            seekBarLeft.setTranslationX(leftPosition);
                                                            nowVideo.setStart((int) Math.floor((leftPosition + seekBarLeft.getWidth() - 5 * dpi)) * 1000 / getThumbnailSizePSec());
                                                            editProgressBar.setX(seekBarLeft.getX() + seekBarLeft.getWidth());

                                                        }
                                                        videoPlayer.pause();
                                                        musicPlayer.pause();

                                                        break;

                                                    case MotionEvent.ACTION_UP:

                                                        Log.d(TAG, "seekBarRight: distance" + distance);
                                                        if (distance < getThumbnailSizePSec()) {
                                                            Log.d(TAG, "seekBarRight: distance less than mindistance");
                                                            position = (int) Math.floor(seekBarLeft.getX() + seekBarLeft.getWidth() + getThumbnailSizePSec());
                                                        }

//disPlayWidth - 5 * dpi
                                                        if (position > 5 * dpi + getThumbnailSizePSec() * 3 * (videoThumbnails.size() - 1) + getThumbnailSizePSec() * (videoThumbnailendTime - videoThumbnails.get(videoThumbnails.size() - 1).getPresentationTimeUs()) / 1000) {
                                                            if (videoThumbnailendTime < 15000) {
                                                                position = 5 * dpi + getThumbnailSizePSec() * 3 * (videoThumbnails.size() - 1) + getThumbnailSizePSec() * (videoThumbnailendTime - videoThumbnails.get(videoThumbnails.size() - 1).getPresentationTimeUs()) / 1000;

                                                            } else {
                                                                position = disPlayWidth - 5 * dpi;
                                                            }

                                                        }

                                                        view.setTranslationX(position);
                                                        nowVideo.setEnd((int) Math.floor((position - 5 * dpi) * 1000 / getThumbnailSizePSec()));

                                                        if (distance > getThumbnailSizePSec() * remainTime / 1000) {
                                                            float leftPosition = view.getX() - seekBarLeft.getWidth() - getThumbnailSizePSec() * remainTime / 1000;
                                                            if (leftPosition < -seekBarLeft.getWidth() + 5 * dpi) {
                                                                leftPosition = -seekBarLeft.getWidth() + 5 * dpi;
                                                                Log.d(TAG, "seekBarLeft: distance less than 0: " + position);
                                                            }
                                                            seekBarLeft.setTranslationX(leftPosition);
                                                            nowVideo.setStart((int) Math.floor((leftPosition + seekBarLeft.getWidth() - 5 * dpi)) * 1000 / getThumbnailSizePSec());
                                                        }

                                                        videoEditAdapter.notifyDataSetChanged();
                                                        Log.d(TAG, "seekBarRight: videoEditEndTime " + nowVideo.getEnd());
                                                        isSeekBarChanged = true;
                                                        videoPlayer.seekTo(nowVideo.getStart());
                                                        musicPlayer.seekTo(musicOffset + resultVideosTotalTime);

                                                        videoPlayer.start();
                                                        musicPlayer.start();
                                                        videoPlayerTextureView.bringToFront();
                                                        break;
                                                }
                                                return true;
                                            }
                                        }

        );
        videoEdit.setOnFlingListener(new RecyclerView.OnFlingListener()

                                     {
                                         @Override
                                         public boolean onFling(int velocityX, int velocityY) {
                                             DisplayMetrics outMetrics = new DisplayMetrics();
                                             ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
                                             float disPlayWidth = outMetrics.widthPixels;
                                             float dpi = outMetrics.densityDpi / 160;

                                             int length = nowVideo.getEnd() - nowVideo.getStart();
                                             //       int start = nowVideo.getStart() + Math.round(velocityX * 15000 / disPlayWidth);
                                             //     int end = nowVideo.getEnd() + Math.round(velocityX * 15000 / disPlayWidth);
                                             int start, end;
                                             if (velocityX > 0) {
                                                 start = nowVideo.getStart() + Math.round((float) videoEdit.computeHorizontalScrollOffset() / getThumbnailSizePSec()) * 1000;
                                                 end = nowVideo.getEnd() + Math.round((float) videoEdit.computeHorizontalScrollOffset() / getThumbnailSizePSec()) * 1000;

                                             } else {
                                                 start = nowVideo.getStart() - Math.round(videoEdit.computeHorizontalScrollOffset() / getThumbnailSizePSec()) * 1000;
                                                 end = nowVideo.getEnd() - Math.round(videoEdit.computeHorizontalScrollOffset() / getThumbnailSizePSec()) * 1000;

                                             }
                                             Log.d(TAG, "onFling:starttime B" + nowVideo.getStart());
                                             Log.d(TAG, "onFling:endtime B" + nowVideo.getEnd());

                                             if (start < 0) {
                                                 nowVideo.setEnd(nowVideo.getStart() + length);
                                             } else if (end > videoThumbnailendTime) {
                                                 nowVideo.setStart(nowVideo.getEnd() - length);
                                             } else {
                                                 nowVideo.setStart(start);
                                                 nowVideo.setEnd(end);
                                             }
                                             videoEditAdapter.notifyDataSetChanged();
                                             isSeekBarChanged = true;
                                             Log.d(TAG, "onFling: offset" + velocityX);
                                             Log.d(TAG, "onFling:starttime " + nowVideo.getStart());
                                             Log.d(TAG, "onFling:endtime " + nowVideo.getEnd());
                                             videoPlayer.seekTo(nowVideo.getStart());
                                             musicPlayer.seekTo(musicOffset + resultVideosTotalTime);

                                            videoPlayer.start();
                                             musicPlayer.start();
                                             videoPlayerTextureView.bringToFront();
                                             return false;
                                         }
                                     }

        );


        insertButton.setOnClickListener(new View.OnClickListener()

                                        {
                                            @Override
                                            public void onClick(View view) {

                                                int resultTime = getResultEndTime(getResultVideos()) + nowVideo.getEnd() - nowVideo.getStart();
                                                Log.d(TAG, "onClick:I resultTime: " + getResultEndTime(resultVideos));
                                                Log.d(TAG, "onClick:I startTime: " + nowVideo.getStart());
                                                Log.d(TAG, "onClick:I videoEditEndTime: " + nowVideo.getEnd());
                                                Log.d(TAG, "onClick:I resultTime: " + resultTime);
                                                Log.d(TAG, "onClick:I remainTime: " + (15000 - resultTime));
                                                if (resultTime <= 15000) {

                                                    resultVideos.add(nowVideo);

                                                    Log.d(TAG, "onClick: nowVideo" + nowVideo.toString());
                                                    Log.d(TAG, "onClick:I resultTimeA: " + getResultEndTime(resultVideos));
                                                    resultVideosTotalTime = getResultEndTime(resultVideos);

                                                    int remain = (int) Math.floor((15000 - (float) getResultEndTime(resultVideos)) / 1000);
                                                    flag = false;
                                                    mCallBack.passDataFToF(0, selectedVideos, resultVideos, resultVideosTotalTime, musicPath, musicOffset, musicLength);

                                                    for (EditorVideo e : resultVideos) {
                                                        Log.d(TAG, "editFR____: " + e.toString() + " / " + System.identityHashCode(e));
                                                    }
                                                } else {
                                                    Toast.makeText(getActivity(), "the edited video time is shorter than " + (15000 - (getResultEndTime(resultVideos))) / 1000 + "sec", Toast.LENGTH_SHORT).show();
                                                }

                                            }
                                        }

        );

        cancelButton.setOnClickListener(new View.OnClickListener()

                                        {
                                            @Override
                                            public void onClick(View view) {
                                                flag = false;
                                                Log.d(TAG, "onCreateView: resultVideosTotalTime6" + resultVideosTotalTime);
                                                mCallBack.passDataFToF(0, selectedVideos, resultVideos, resultVideosTotalTime, musicPath, musicOffset, musicLength);
                                            }
                                        }

        );

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallBack = (VideoEditorResultFragment.DataPassListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement DataPassListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (musicPlayer != null) musicPlayer.release();
        if (videoPlayer != null) videoPlayer.release();
        flag = false;
    }


    public int getResultEndTime(List<EditorVideo> editedVideos) {
        int editedVideosEnd = 0;
        for (EditorVideo editedVideo : editedVideos) {
            editedVideosEnd += (editedVideo.getEnd() - editedVideo.getStart());
        }
        Log.d(TAG, "getResultEndTime" + editedVideosEnd);
        return editedVideosEnd;
    }

    public int getThumbnailSizePSec() {
        DisplayMetrics outMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        int widthPSec = outMetrics.widthPixels;
        float dpi = outMetrics.densityDpi / 160;
        float size = (widthPSec - 10 * dpi) / 15;
        Log.d(TAG, "getThumbnailSizePSec: " + size);
        return (int) Math.floor(size);
    }

    public ArrayList<EditorVideo> getResultVideos() {
        return resultVideos;
    }

    Thread editThread = new Thread(new Runnable() {
        @Override
        public void run() {

            while (flag) {
                try {
                    if (videoPlayer.isPlaying()) {

                        if (videoPlayer.getCurrentPosition() >= nowVideo.getEnd()) {
                            Log.d(TAG, "run: paused");
                            videoPlayer.pause();
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    blackScreen.bringToFront();

                                }
                            });
                            editProgressBar.setX(seekBarLeft.getX() + seekBarLeft.getWidth());
                        }


                        // progress bar
                        float progressTime = ((float) videoPlayer.getCurrentPosition() - nowVideo.getStart()) / (nowVideo.getEnd() - nowVideo.getStart());
                        float progress = progressTime * (seekBarRight.getX() - (seekBarLeft.getX() + seekBarLeft.getWidth()));
                        editProgressBar.setX(seekBarLeft.getX()+seekBarLeft.getWidth()+progress);
                        Thread.sleep(50);
                        editProgressBar.setVisibility(View.VISIBLE);
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            blackScreen.bringToFront();
//                        }
//                    });


                    }else{

                        editProgressBar.setX(seekBarLeft.getX() + seekBarLeft.getWidth());
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });
}
