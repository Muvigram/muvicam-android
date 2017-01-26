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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.transcoder.utils.ThumbnailUtil;
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
    FrameLayout resultSpaceLinearLayout;
    RecyclerView videoEdit;
    ImageView seekBarLeft, seekBarRight;
    VideoEditorEditAdapter videoEditAdapter;
    boolean isSeekBarChanged = false;
    ImageView cancelButton, insertButton;
    int videoThumbnailendTime;
    MuvicamMediaPlayer videoPlayer;
    MediaPlayer musicPlayer;
    VideoPlayerTextureView videoPlayerTextureView;
    LinearLayout editSeekBarSpace;
    boolean flag = true;
    FrameLayout editorThumbnailFrameLayout;
    TrimmerBackGroundView trimmerBackground;
    FrameLayout videoTextureLayout;
    float progressInit;
    boolean wasPlaying, isMaxLength;
    ArrayList<VideoEditorEditSeekBar> editSeekBars = new ArrayList<>();
    ThumbnailUtil.UserBitmapListener thumbnailUtilListener = new ThumbnailUtil.UserBitmapListener() {
        @Override
        public void onBitmapNext(final Bitmap bitmap, final long presentationTimeUs, final boolean isLast) {
//            Log.d(TAG, "onBitmapNext: " + presentationTimeUs);
//            Log.d(TAG, "onBitmapNextSize: " + resultMyVideos.size());
//            Log.d(TAG, "onBitmapNextCount: " + videoEditAdapter.getItemCount());
  //          Log.d(TAG, "onBitmapNext: p" + presentationTimeUs);

            if (!isLast) {
                EditorVideo editMyVideo = new EditorVideo(bitmap, (int) presentationTimeUs / 1000, isLast, selectedNum);
                videoThumbnails.add(editMyVideo);
                videoEditAdapter.notifyDataSetChanged();
            }
    //        Log.d(TAG, "onBitmapNext123: " + presentationTimeUs + " /. " + isLast);
        }

        @Override
        public void onError(Exception e) {

        }

        @Override
        public void onComplete(long endTimeUs) {
            videoThumbnailendTime = (int) endTimeUs / 1000;
     //       Log.d(TAG, "onComplete: " + videoThumbnailendTime);
            DisplayMetrics outMetrics = new DisplayMetrics();
            ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
            float disPlayWidth = outMetrics.widthPixels;
            float dpi = outMetrics.densityDpi / 160;

            int remainTotalTime = 15000 - resultVideosTotalTime;

            if (videoThumbnailendTime < 15000) {
                if (remainTotalTime < videoThumbnailendTime) {
                    float length = 5 * dpi + getThumbnailSizePSec() * (remainTotalTime) / 1000;
       //             Log.d(TAG, "onComplete: <15<thum " + length);
                    seekBarRight.setTranslationX(length);
                    nowVideo.setEnd(remainTotalTime);
                } else {
                    float length = 5 * dpi + getThumbnailSizePSec() * 3 * (videoThumbnails.size() - 1) + getThumbnailSizePSec() * (videoThumbnailendTime - videoThumbnails.get(videoThumbnails.size() - 1).getPresentationTimeUs()) / 1000;
          //          Log.d(TAG, "onComplete: <15>thum " + length);

                    seekBarRight.setTranslationX(length);
                    nowVideo.setEnd(videoThumbnailendTime);
                }

            } else {
                if (remainTotalTime < 15000) {
                    float length = 5 * dpi + getThumbnailSizePSec() * (remainTotalTime) / 1000;
         //           Log.d(TAG, "onComplete: >=15<thum " + length);

                    seekBarRight.setTranslationX(length);
                    nowVideo.setEnd(remainTotalTime);
                } else {
                    seekBarRight.setTranslationX(disPlayWidth - 5 * dpi);
           //         Log.d(TAG, "onComplete: >=15>thum " + (disPlayWidth - 5 * dpi));

                    nowVideo.setEnd(15000);
          //          Log.d(TAG, "onClick: endtime" + nowVideo.getEnd());
                }

            }

            seekBarLeft.setTranslationX(-seekBarLeft.getWidth() + 5 * dpi);
       //     Log.d(TAG, "onComplete: seekbarL X :" + seekBarLeft.getX());
 // maybe use this?           (Math.round(videoEdit.computeHorizontalScrollOffset() * 15000 / disPlayWidth))
            nowVideo.setStart(0);
       //     Log.d(TAG, "onClick: starttime" + nowVideo.getStart());
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    seekBarLeft.setVisibility(View.VISIBLE);
                    seekBarRight.setVisibility(View.VISIBLE);

                }
            });

            trimmerBackground = new TrimmerBackGroundView(getContext(), seekBarLeft.getX() + seekBarLeft.getWidth(), seekBarRight.getX());


            videoPlayer.seekTo(nowVideo.getStart());
            musicPlayer.seekTo(musicOffset + resultVideosTotalTime);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    VideoEditorEditSeekBar editSeekBar = new VideoEditorEditSeekBar(getContext(), (int)Math.floor(seekBarLeft.getX()+seekBarLeft.getWidth()));
            //        Log.d(TAG, "run: progressStart" + nowVideo.getStart());
                    editSeekBarSpace.addView(editSeekBar);
                    editSeekBars.add(editSeekBar);
                    editSeekBarSpace.invalidate();
                }
            });
            musicPlayer.start();
            videoPlayer.start();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    videoPlayerTextureView.bringToFront();
                    editorThumbnailFrameLayout.addView(trimmerBackground);
                    editorThumbnailFrameLayout.invalidate();
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
        //    Log.d(TAG, "onCreate: edit rvt" + resultVideosTotalTime);
            musicPath = args.getString(VideoEditorResultFragment.EXTRA_MUSIC_PATH);
            musicOffset = args.getInt(VideoEditorResultFragment.EXTRA_MUSIC_OFFSET, 0);
            musicLength = args.getInt(VideoEditorResultFragment.EXTRA_MUSIC_LENGTH, 0);
            nowVideo = new EditorVideo();
            nowVideo.setVideoPath(selectedVideos.get(selectedNum - 1).getVideoPath());
            nowVideo.setDurationMiliSec(selectedVideos.get(selectedNum - 1).getDurationMiliSec());
            nowVideo.setNumSelected(selectedNum);

        }

        wasPlaying = false;
        isMaxLength = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_video_editor_edit, container, false);
        resultSpaceLinearLayout = (FrameLayout) v.findViewById(R.id.editor_edit_result_space_linear);
   //     Log.d(TAG, "onCreateView: edit rvt" + resultVideosTotalTime);
        ResultBarView resultBarView;
        int resultTime = 0;
        for (int i = 0; i < resultVideos.size(); i++) {

            int nowVideoTime = resultVideos.get(i).getEnd() - resultVideos.get(i).getStart();
            int remainTime = 15000 - resultVideosTotalTime;
            if (i == resultVideos.size() - 1 && remainTime < 1000) {
                resultBarView = new ResultBarView(getContext(), resultTime, 15000 - resultTime, false);
            } else {
                resultBarView = new ResultBarView(getContext(), resultTime, nowVideoTime, false);
            }
            resultTime += nowVideoTime;
            resultSpaceLinearLayout.addView(resultBarView);
        }
        editorThumbnailFrameLayout = (FrameLayout) v.findViewById(R.id.editor_edit_thumbnail_background);
        videoEdit = (RecyclerView) v.findViewById(R.id.editor_edit_recycler_thumbnails);
        LinearLayoutManager linearLayoutManagerE = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        videoEdit.setLayoutManager(linearLayoutManagerE);

        videoEditAdapter = new VideoEditorEditAdapter(getActivity(), selectedNum, nowVideo, videoThumbnails);
        videoEdit.setAdapter(videoEditAdapter);

        seekBarLeft = (ImageView) v.findViewById(R.id.editor_seekbar_left);
        seekBarRight = (ImageView) v.findViewById(R.id.editor_seekbar_right);


        insertButton = (ImageView) v.findViewById(R.id.editor_edit_insert);
        cancelButton = (ImageView) v.findViewById(R.id.editor_edit_cancel);
        videoTextureLayout = (FrameLayout) v.findViewById(R.id.editor_edit_frame_layout);

        editSeekBarSpace = (LinearLayout) v.findViewById(R.id.editor_edit_progress_space);
        thumbnailUtil = new ThumbnailUtil(thumbnailUtilListener, getActivity(), true);
     //   Log.d(TAG, "onCreateView: " + videoEdit.getWidth());
        // 3/15 -> 1/5
        musicPlayer = new MediaPlayer();
        videoPlayer = new MuvicamMediaPlayer();
        try {
            videoPlayer.setVolume(0, 0);
      //      Log.d(TAG, "onCreateView: video " + selectedVideos.get(selectedNum - 1).getVideoPath());
            FileInputStream fisMusic = new FileInputStream(musicPath);
            FileDescriptor fdMusic = fisMusic.getFD();

            videoPlayer.setDataSource(selectedVideos.get(selectedNum - 1).getVideoPath());
            musicPlayer.setDataSource(fdMusic);
            musicPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
         //           Log.d(TAG, "onCompletion: now1");
                    videoPlayer.seekTo(nowVideo.getStart());
                    musicPlayer.seekTo(musicOffset + resultVideosTotalTime);
                    musicPlayer.start();

                }
            });
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
        videoPlayerTextureView = new VideoPlayerTextureView(getActivity(), videoPlayer, nowVideo, width, height, rotation);

        videoTextureLayout.addView(videoPlayerTextureView);
        return v;
    }

    float lX1 = 0, lX2 = 0, rX1 = 0, rX2 = 0;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        videoTextureLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (videoPlayer.isPlaying()) {
                    videoPlayer.pause();
                    musicPlayer.pause();
                } else {
                    videoPlayer.seekTo(nowVideo.getStart());
                    musicPlayer.seekTo(musicOffset + resultVideosTotalTime);

                    videoPlayer.start();
                    musicPlayer.start();
                }
            }
        });
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
                                                       if (musicPlayer.isPlaying()) {
                                                           musicPlayer.pause();
                                                           videoPlayer.pause();
                                                           wasPlaying = true;
                                                       }
                                                       delta = X - view.getTranslationX();
                                                       lX1 = motionEvent.getX();
                                        //               Log.d(TAG, "seekBarLeft: action_down" + delta);

                                                       break;
                                                   case MotionEvent.ACTION_MOVE:
                                                       position = X - delta;
                                                       lX2 = motionEvent.getX();
                                                       if (lX1 < lX2 && distance <= getThumbnailSizePSec()) {
                                            //               Log.d(TAG, "seekBarLeft: distance less than mindistance");
                                                           position = seekBarRight.getX() - getThumbnailSizePSec() - view.getWidth();
                                           //                Log.d(TAG, "seekBarLeft: distance less than mindistance: " + position);
                                                       }
                                                       if (lX1 > lX2 && position <= -view.getWidth() + 5 * dpi) {
                                                           position = -view.getWidth() + 5 * dpi;
                                         //                  Log.d(TAG, "seekBarLeft: distance less than 0: " + position);
                                                       }

                                                       //minwidth
                                                       view.setTranslationX(position);
                                           //            Log.d(TAG, "seekBarLeft: " + (X - delta));
                                          //             Log.d(TAG, "seekBarGetXLeft: " + view.getX());
                                           //            Log.d(TAG, "seekBarLeftWidth: " + view.getWidth());
                                                       nowVideo.setStart((int) Math.ceil((position - 5 * dpi + view.getWidth()) * 1000 / getThumbnailSizePSec()));

                                                       if (lX1 > lX2 && distance >= getThumbnailSizePSec() * remainTime / 1000) {
                                                           float rightPosition = view.getX() + view.getWidth() + getThumbnailSizePSec() * remainTime / 1000;
                                                           if (rightPosition > 5 * dpi + getThumbnailSizePSec() * 3 * (videoThumbnails.size() - 1) + getThumbnailSizePSec() * (videoThumbnailendTime - videoThumbnails.get(videoThumbnails.size() - 1).getPresentationTimeUs()) / 1000) {
                                                               rightPosition = 5 * dpi + getThumbnailSizePSec() * 3 * (videoThumbnails.size() - 1) + getThumbnailSizePSec() * (videoThumbnailendTime - videoThumbnails.get(videoThumbnails.size() - 1).getPresentationTimeUs()) / 1000;
                                                           }
                                                           seekBarRight.setTranslationX(rightPosition);
                                                           nowVideo.setEnd((int) Math.floor((rightPosition - 5 * dpi) * 1000 / getThumbnailSizePSec()));
                                                       }

                                                       trimmerBackground.setStartX(position + view.getWidth());
                                                       trimmerBackground.setEndX(seekBarRight.getX());
                                                       trimmerBackground.invalidate();
                                                       videoEditAdapter.notifyDataSetChanged();

                                                       break;

                                                   case MotionEvent.ACTION_UP:

                                         //              Log.d(TAG, "seekBarLeft: distance" + distance);
                                                       if (lX1 < lX2 && distance <= getThumbnailSizePSec()) {
                                         //                  Log.d(TAG, "seekBarLeft: distance less than mindistance");
                                                           position = seekBarRight.getX() - getThumbnailSizePSec() - view.getWidth();
                                          //                 Log.d(TAG, "seekBarLeft: distance less than mindistance: " + position);
                                                       }


                                                       if (lX1 > lX2 && position <= -view.getWidth() + 5 * dpi) {
                                                           position = -view.getWidth() + 5 * dpi;
                                                           Log.d(TAG, "seekBarLeft: distance less than 0: " + position);
                                                       }

                                                       view.setTranslationX(position);
                                                       //editProgress.setX(leftPosition);
                                                       nowVideo.setStart((int) Math.ceil((position - 5 * dpi + view.getWidth()) * 1000 / getThumbnailSizePSec()));
                                           //            Log.d(TAG, "seekBarLeft: starttime: " + nowVideo.getStart());
                                                       if (lX1 > lX2 && distance >= getThumbnailSizePSec() * remainTime / 1000) {
                                                           float rightPosition = view.getX() + view.getWidth() + getThumbnailSizePSec() * remainTime / 1000;
                                                           if (rightPosition > 5 * dpi + getThumbnailSizePSec() * 3 * (videoThumbnails.size() - 1) + getThumbnailSizePSec() * (videoThumbnailendTime - videoThumbnails.get(videoThumbnails.size() - 1).getPresentationTimeUs()) / 1000) {
                                                               rightPosition = 5 * dpi + getThumbnailSizePSec() * 3 * (videoThumbnails.size() - 1) + getThumbnailSizePSec() * (videoThumbnailendTime - videoThumbnails.get(videoThumbnails.size() - 1).getPresentationTimeUs()) / 1000;
                                                           }
                                                           seekBarRight.setTranslationX(rightPosition);
                                                           nowVideo.setEnd((int) Math.floor((rightPosition - 5 * dpi) * 1000 / getThumbnailSizePSec()));

                                                       }
                                                       videoEditAdapter.notifyDataSetChanged();
                                                       isSeekBarChanged = true;
                                           //            Log.d(TAG, "onTouch: start " + nowVideo.getStart());
                                                       trimmerBackground.setStartX(position + view.getWidth());
                                                       trimmerBackground.setEndX(seekBarRight.getX());
                                                       trimmerBackground.invalidate();

                                                       if (wasPlaying) {
                                                           videoPlayer.seekTo(nowVideo.getStart());
                                                           musicPlayer.seekTo(musicOffset + resultVideosTotalTime);
                                                           musicPlayer.start();
                                                           wasPlaying = false;
                                                           for (VideoEditorEditSeekBar progress : editSeekBars) {
                                                               editSeekBarSpace.removeView(progress);
                                                           }
                                                           editSeekBars.clear();
                                                           VideoEditorEditSeekBar editSeekBar = new VideoEditorEditSeekBar(getContext(), (int)Math.floor(seekBarLeft.getX()+seekBarLeft.getWidth()));
                                                           editSeekBars.add(editSeekBar);
                                                           editSeekBarSpace.addView(editSeekBar);
                                                           editSeekBarSpace.invalidate();


                                                       }
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
                                                        if (musicPlayer.isPlaying()) {
                                                            musicPlayer.pause();
                                                            videoPlayer.pause();
                                                            wasPlaying = true;
                                                        }
                                                        rX1 = motionEvent.getX();
                                                        delta = X - view.getTranslationX();
                                                //        Log.d(TAG, "seekBarRight: action_down" + delta);
                                                        break;
                                                    case MotionEvent.ACTION_MOVE:
                                                        rX2 = motionEvent.getX();
                                                        position = X - delta;
//                                                        if (rX1 < rX2) {
//                                                            Log.d(TAG, "onTouch: right");
//                                                        } else {
//                                                            Log.d(TAG, "onTouch: left");
//                                                        }
//                                                        Log.d(TAG, "onTouch: distance" + distance);
//                                                        Log.d(TAG, "onTouch: distance" + getThumbnailSizePSec());

                                                        //left position
                                                        if (rX1 > rX2 && distance <= getThumbnailSizePSec()) {
//                                                            Log.d(TAG, "seekBarRight: distance less than mindistance");
                                                            position = (int) Math.floor(seekBarLeft.getX() + seekBarLeft.getWidth() + getThumbnailSizePSec());
                                                        }
                                                        //disPlayWidth - 5 * dpi
                                                        if (rX1 < rX2 && position >= 5 * dpi + getThumbnailSizePSec() * 3 * (videoThumbnails.size() - 1) + getThumbnailSizePSec() * (videoThumbnailendTime - videoThumbnails.get(videoThumbnails.size() - 1).getPresentationTimeUs()) / 1000) {
                                                            if (videoThumbnailendTime < 15000) {
                                                                position = 5 * dpi + getThumbnailSizePSec() * 3 * (videoThumbnails.size() - 1) + getThumbnailSizePSec() * (videoThumbnailendTime - videoThumbnails.get(videoThumbnails.size() - 1).getPresentationTimeUs()) / 1000;
                                                            } else {
                                                                position = disPlayWidth - 5 * dpi;
                                                            }

                                                        }

                                                        //minwidth
                                                        view.setTranslationX(position);
//                                                        Log.d(TAG, "seekBarRight: " + (X - delta));
//                                                        Log.d(TAG, "seekBarGetXRight: " + view.getX());
//                                                        Log.d(TAG, "seekBarRightWidth: " + view.getWidth());

                                                        if (rX1 < rX2 && distance >= getThumbnailSizePSec() * ((float) remainTime / 1000)) {
                                                            float leftPosition = view.getX() - seekBarLeft.getWidth() - getThumbnailSizePSec() * ((float) remainTime / 1000);
                                                            if (leftPosition < -seekBarLeft.getWidth() + 5 * dpi) {
                                                                leftPosition = -seekBarLeft.getWidth() + 5 * dpi;
//                                                                Log.d(TAG, "seekBarLeft: distance less than 0: " + position);
                                                                position = leftPosition + seekBarLeft.getWidth();
                                                            }
                                                            seekBarLeft.setTranslationX(leftPosition);
                                                            view.setTranslationX(position);
                                                            nowVideo.setStart((int) Math.ceil((leftPosition + seekBarLeft.getWidth() - 5 * dpi)) * 1000 / getThumbnailSizePSec());

                                                        }

                                                        trimmerBackground.setStartX(seekBarLeft.getX() + seekBarLeft.getWidth());
                                                        trimmerBackground.setEndX(position);
                                                        trimmerBackground.invalidate();
                                                        break;

                                                    case MotionEvent.ACTION_UP:

//                                                        Log.d(TAG, "seekBarRight: distance" + distance);
                                                        if (rX1 > rX2 && distance <= getThumbnailSizePSec()) {
//                                                            Log.d(TAG, "seekBarRight: distance less than mindistance");
                                                            position = (int) Math.floor(seekBarLeft.getX() + seekBarLeft.getWidth() + getThumbnailSizePSec());
                                                        }

                                                        //disPlayWidth - 5 * dpi
                                                        if (rX1 < rX2 && position >= 5 * dpi + getThumbnailSizePSec() * 3 * (videoThumbnails.size() - 1) + getThumbnailSizePSec() * (videoThumbnailendTime - videoThumbnails.get(videoThumbnails.size() - 1).getPresentationTimeUs()) / 1000) {
                                                            if (videoThumbnailendTime < 15000) {
                                                                position = 5 * dpi + getThumbnailSizePSec() * 3 * (videoThumbnails.size() - 1) + getThumbnailSizePSec() * (videoThumbnailendTime - videoThumbnails.get(videoThumbnails.size() - 1).getPresentationTimeUs()) / 1000;
                                                            } else {
                                                                position = disPlayWidth - 5 * dpi;
                                                            }

                                                        }

                                                        view.setTranslationX(position);
                                                        nowVideo.setEnd((int) Math.floor((position - 5 * dpi) * 1000 / getThumbnailSizePSec()));

                                                        if (rX1 < rX2 && distance >= getThumbnailSizePSec() * remainTime / 1000) {
                                                            float leftPosition = view.getX() - seekBarLeft.getWidth() - getThumbnailSizePSec() * remainTime / 1000;
                                                            if (leftPosition < -seekBarLeft.getWidth() + 5 * dpi) {
                                                                leftPosition = -seekBarLeft.getWidth() + 5 * dpi;
//                                                                Log.d(TAG, "seekBarLeft: distance less than 0: " + position);
                                                                position = leftPosition + seekBarLeft.getWidth();
                                                            }
                                                            seekBarLeft.setTranslationX(leftPosition);
                                                            view.setTranslationX(position);
                                                            nowVideo.setStart((int) Math.ceil((leftPosition + seekBarLeft.getWidth() - 5 * dpi)) * 1000 / getThumbnailSizePSec());
                                                        }

                                                        videoEditAdapter.notifyDataSetChanged();
//                                                        Log.d(TAG, "seekBarRight: videoEditEndTime " + nowVideo.getEnd());
                                                        isSeekBarChanged = true;
                                                        trimmerBackground.setStartX(seekBarLeft.getX() + seekBarLeft.getWidth());
                                                        trimmerBackground.setEndX(position);
                                                        trimmerBackground.invalidate();
//                                                        Log.d(TAG, "onTouch: seekTest1 r" + nowVideo.getStart());
                                                        if (wasPlaying) {
                                                            videoPlayer.seekTo(nowVideo.getStart());
                                                            musicPlayer.seekTo(musicOffset + resultVideosTotalTime);
                                                            musicPlayer.start();
                                                            wasPlaying = false;
                                                        }
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
//                                             Log.d(TAG, "onFling:starttime B" + nowVideo.getStart());
//                                             Log.d(TAG, "onFling:endtime B" + nowVideo.getEnd());

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
//                                             Log.d(TAG, "onFling: offset" + velocityX);
//                                             Log.d(TAG, "onFling:starttime " + nowVideo.getStart());
//                                             Log.d(TAG, "onFling:endtime " + nowVideo.getEnd());
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
//                                                Log.d(TAG, "onClick:I resultTime: " + getResultEndTime(resultVideos));
//                                                Log.d(TAG, "onClick:I startTime: " + nowVideo.getStart());
//                                                Log.d(TAG, "onClick:I videoEditEndTime: " + nowVideo.getEnd());
//                                                Log.d(TAG, "onClick:I resultTime: " + resultTime);
//                                                Log.d(TAG, "onClick:I remainTime: " + (15000 - resultTime));
                                                if (resultTime <= 15000) {

                                                    resultVideos.add(nowVideo);

//                                                    Log.d(TAG, "onClick: nowVideo" + nowVideo.toString());
//                                                    Log.d(TAG, "onClick:I resultTimeA: " + getResultEndTime(resultVideos));
                                                    resultVideosTotalTime = getResultEndTime(resultVideos);

                                                    int remain = (int) Math.floor((15000 - (float) getResultEndTime(resultVideos)) / 1000);
                                                    flag = false;
                                                    mCallBack.passDataFToF(0, selectedVideos, resultVideos, resultVideosTotalTime, musicPath, musicOffset, musicLength);

                                                    for (EditorVideo e : resultVideos) {
//                                                        Log.d(TAG, "editFR____: " + e.toString() + " / " + System.identityHashCode(e));
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
                           //                     Log.d(TAG, "onCreateView: resultVideosTotalTime6" + resultVideosTotalTime);
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

    }

    @Override
    public void onPause() {
        super.onPause();
        flag = false;
    }

    public int getResultEndTime(List<EditorVideo> editedVideos) {
        int editedVideosEnd = 0;
        for (EditorVideo editedVideo : editedVideos) {
            editedVideosEnd += (editedVideo.getEnd() - editedVideo.getStart());
        }
  //      Log.d(TAG, "getResultEndTime" + editedVideosEnd);
        return editedVideosEnd;
    }

    public int getThumbnailSizePSec() {
        DisplayMetrics outMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        int widthPSec = outMetrics.widthPixels;
        float dpi = outMetrics.densityDpi / 160;
        float size = (widthPSec - 10 * dpi) / 15;
  //      Log.d(TAG, "getThumbnailSizePSec: " + size);
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
                    if (flag && musicPlayer.isPlaying()) {
                        if (flag && videoPlayer.getCurrentPosition() < nowVideo.getEnd()) {

                            // progress bar
                            Thread.sleep(50);

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (flag) {
                                        for (VideoEditorEditSeekBar progress : editSeekBars) {
                                            editSeekBarSpace.removeView(progress);
                                        }
                                        editSeekBars.clear();
                                        VideoEditorEditSeekBar editSeekBar = new VideoEditorEditSeekBar(getContext(), (int)Math.floor(seekBarLeft.getX()+seekBarLeft.getWidth()+((float) videoPlayer.getCurrentPosition() - nowVideo.getStart()) / (nowVideo.getEnd() - nowVideo.getStart()) * (seekBarRight.getX() - (seekBarLeft.getX() + seekBarLeft.getWidth()))));
                                        editSeekBarSpace.addView(editSeekBar);
                                        editSeekBars.add(editSeekBar);

                                        editSeekBarSpace.invalidate();

                                    }
                                }
                            });


                            //                 Log.d(TAG, "run: length 1 / " + (nowVideo.getEnd() - nowVideo.getStart() + 1000));
//                        Log.d(TAG, "run: length 2 / " + (musicLength - resultVideosTotalTime));
                        if (nowVideo.getEnd() - nowVideo.getStart() + 1000 >= musicLength - resultVideosTotalTime) {
                            isMaxLength = true;
                        } else {
                            isMaxLength = false;
                        }

//                        Log.d(TAG, "run: isMaxLength: " + isMaxLength);
//                        Log.d(TAG, "run: t getC" + videoPlayer.getCurrentPosition());
//                        Log.d(TAG, "run: t nowE" + nowVideo.getEnd());
                        if (flag && videoPlayer.getCurrentPosition() >= nowVideo.getEnd() - 100) {

//                            Log.d(TAG, "run: paused");
//                            Log.d(TAG, "run: geCur" + videoPlayer.getCurrentPosition());
//                            Log.d(TAG, "run: geStart" + nowVideo.getStart());
//                            Log.d(TAG, "run: geEnd" + nowVideo.getEnd());

                          getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    for (VideoEditorEditSeekBar progress : editSeekBars) {
                                        editSeekBarSpace.removeView(progress);
                                    }
                                    editSeekBars.clear();

                                    VideoEditorEditSeekBar editSeekBar = new VideoEditorEditSeekBar(getContext(), (int)Math.floor(seekBarLeft.getX()+seekBarLeft.getWidth()));
                                    editSeekBarSpace.addView(editSeekBar);
                                    editSeekBars.add(editSeekBar);

                                    editSeekBarSpace.invalidate();


                                }
                            });
                            if (!isMaxLength || (isMaxLength && musicPlayer.getCurrentPosition() < musicPlayer.getDuration())) {
//                                Log.d(TAG, "run: " + " / nowvideo: " + (nowVideo.getEnd() - nowVideo.getStart()));
                                videoPlayer.seekTo(nowVideo.getStart());
                                musicPlayer.seekTo(musicOffset + resultVideosTotalTime);
                            }
                            continue;
                        }
                        }


                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (musicPlayer != null) {
                if (flag && musicPlayer.isPlaying()) {
                    musicPlayer.pause();
                    musicPlayer.stop();
                    ;
                }
                musicPlayer.release();
            }

            if (videoPlayer != null) {
                if (flag && videoPlayer.isPlaying()) {
                    videoPlayer.pause();
                    videoPlayer.stop();
                }
                videoPlayer.release();
            }
        }
    });
}
