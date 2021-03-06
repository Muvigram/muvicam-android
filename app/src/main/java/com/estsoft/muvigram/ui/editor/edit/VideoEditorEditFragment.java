package com.estsoft.muvigram.ui.editor.edit;

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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.estsoft.muvigram.R;
import com.estsoft.muvigram.model.EditorVideo;
import com.estsoft.muvigram.transcoder.utils.ThumbnailUtil;
import com.estsoft.muvigram.ui.editor.ResultBarView;
import com.estsoft.muvigram.ui.editor.VideoPlayerTextureView;
import com.estsoft.muvigram.ui.editor.result.VideoEditorResultFragment;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class VideoEditorEditFragment extends Fragment {
    private ArrayList<EditorVideo> resultVideos;
    private ArrayList<EditorVideo> selectedVideos = new ArrayList<>();
    private ArrayList<EditorVideo> videoThumbnails = new ArrayList<>();
    EditorVideo nowVideo;
    String musicPath;
    int selectedNum, musicOffset, musicLength;
    int resultVideosTotalTime;
    VideoEditorResultFragment.DataPassListener mCallBack;
    FrameLayout resultSpaceLinearLayout;
    RecyclerView videoEditThumbnailRecyclerView;
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
    VideoEditorEditSeekBar editSeekBar;
    ThumbnailUtil.UserBitmapListener thumbnailUtilListener = new ThumbnailUtil.UserBitmapListener() {
        @Override
        public void onBitmapNext(final Bitmap bitmap, final long presentationTimeUs, final boolean isLast) {
            if (!isLast) {
                EditorVideo editMyVideo = new EditorVideo(bitmap, (int) presentationTimeUs / 1000, isLast, selectedNum);
                videoThumbnails.add(editMyVideo);
                videoEditAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onError(Exception e) {

        }

        @Override
        public void onComplete(long endTimeUs) {
            videoEditThumbnailRecyclerView.setClickable(true);
            videoThumbnailendTime = (int) endTimeUs / 1000;
            DisplayMetrics outMetrics = new DisplayMetrics();
            ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
            float disPlayWidth = outMetrics.widthPixels;
            float dpi = getResources().getDimension(R.dimen.resultbar_line);
            Timber.d("onComplete: %d", videoThumbnailendTime);
            int remainTotalTime = 15000 - resultVideosTotalTime;

            if (videoThumbnailendTime < 15000) {
                if (remainTotalTime < videoThumbnailendTime) {
                    float length = 10 * dpi + getThumbnailSizePSec() * (remainTotalTime) / 1000;
                    seekBarRight.setTranslationX(length);
                    nowVideo.setEnd(remainTotalTime);
                } else {
                    float length = 9 * dpi + getThumbnailSizePSec() * 3 * (videoThumbnails.size() - 1) + getThumbnailSizePSec() * (videoThumbnailendTime - videoThumbnails.get(videoThumbnails.size() - 1).getPresentationTimeUs()) / 1000;
                    seekBarRight.setTranslationX(length);
                    nowVideo.setEnd(videoThumbnailendTime);
                }

            } else {
                if (remainTotalTime < 15000) {
                    float length = getThumbnailSizePSec() * (remainTotalTime) / 1000;
                    seekBarRight.setTranslationX(length);
                    nowVideo.setEnd(remainTotalTime);
                } else {
                    seekBarRight.setTranslationX(disPlayWidth - 10 * dpi);
                    nowVideo.setEnd(15000);
                }

            }

            seekBarLeft.setTranslationX(-seekBarLeft.getWidth() + 10 * dpi);

            // maybe use this?
            nowVideo.setStart(0);

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
                    editSeekBar = new VideoEditorEditSeekBar(getContext(), (int) Math.floor(seekBarLeft.getX() + seekBarLeft.getWidth()));
                    editSeekBarSpace.addView(editSeekBar);
                    //        Log.d(TAG, "run: progressStart" + nowVideo.getStartX());

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        if (args != null) {
            selectedNum = args.getInt(VideoEditorResultFragment.EXTRA_FRAGMENT_NUM);
            selectedVideos = args.getParcelableArrayList(VideoEditorResultFragment.EXTRA_VIDEOS);
            resultVideos = args.getParcelableArrayList(VideoEditorResultFragment.EXTRA_RESULT_VIDEOS);
            resultVideosTotalTime = args.getInt(VideoEditorResultFragment.EXTRA_RESULT_VIDEO_TOTAL_TIME, 0);
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
        View v = inflater.inflate(R.layout.fragment_editor_edit, container, false);
        resultSpaceLinearLayout = (FrameLayout) v.findViewById(R.id.editor_edit_result_space_linear);
        ResultBarView resultBarView;
        int resultTime = 0;
        for (int i = 0; i < resultVideos.size(); i++) {

            int nowVideoTime = resultVideos.get(i).getEnd() - resultVideos.get(i).getStart();
            Timber.d("onCreateView: nowV%d", nowVideoTime);
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
        videoEditThumbnailRecyclerView = (RecyclerView) v.findViewById(R.id.editor_edit_recycler_thumbnails);
        LinearLayoutManager linearLayoutManagerE = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        videoEditThumbnailRecyclerView.setLayoutManager(linearLayoutManagerE);
        videoEditThumbnailRecyclerView.setClickable(false);
        videoEditAdapter = new VideoEditorEditAdapter(getActivity(), selectedNum, nowVideo, videoThumbnails);
        videoEditThumbnailRecyclerView.setAdapter(videoEditAdapter);

        seekBarLeft = (ImageView) v.findViewById(R.id.editor_seekbar_left);
        seekBarRight = (ImageView) v.findViewById(R.id.editor_seekbar_right);


        insertButton = (ImageView) v.findViewById(R.id.editor_edit_insert);
        cancelButton = (ImageView) v.findViewById(R.id.editor_edit_cancel);
        videoTextureLayout = (FrameLayout) v.findViewById(R.id.editor_edit_frame_layout);

        editSeekBarSpace = (LinearLayout) v.findViewById(R.id.editor_edit_progress_space);
        thumbnailUtil = new ThumbnailUtil(thumbnailUtilListener, getActivity(), true);
        //   Log.d(TAG, "onCreateView: " + videoEditThumbnailRecyclerView.getWidth());
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
                                               float dpi = getResources().getDimension(R.dimen.resultbar_line);
                                               float remainTime = 15000 - resultVideosTotalTime;
                                               float X = motionEvent.getRawX() - view.getWidth() + 10 * dpi;
                                               float delta = 0;
                                               float position = X - delta;

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
                                                       if (lX1 < lX2 && getDistanceSeekBars() <= getThumbnailSizePSec()) {
                                                           position = seekBarRight.getX() - getThumbnailSizePSec() - view.getWidth();
                                                       }
                                                       if (lX1 > lX2 && position <= -view.getWidth() + 10 * dpi) {
                                                           position = -view.getWidth() + 10 * dpi;
                                                       }

                                                       //minwidth
                                                       view.setTranslationX(position);
                                                       nowVideo.setStart((int) Math.ceil((position - 10 * dpi + view.getWidth()) * 1000 / getThumbnailSizePSec()));

                                                       if (getDistanceSeekBars() >= getThumbnailSizePSec() * (remainTime / 1000)) {
                                                           float rightPosition = seekBarRight.getX();
                                                           if (remainTime < nowVideo.getDurationMiliSec()) {
                                                               rightPosition = view.getX() + view.getWidth() + getThumbnailSizePSec() * (remainTime / 1000);
                                                           }
                                                           seekBarRight.setTranslationX(rightPosition);
                                                           nowVideo.setEnd((int) Math.floor((rightPosition - 10 * dpi) * 1000 / getThumbnailSizePSec()));
                                                       }

                                                       trimmerBackground.setStartX(position + view.getWidth());
                                                       trimmerBackground.setEndX(seekBarRight.getX());
                                                       trimmerBackground.invalidate();
                                                       videoEditAdapter.notifyDataSetChanged();

                                                       break;

                                                   case MotionEvent.ACTION_UP:

                                                       if (lX1 < lX2 && getDistanceSeekBars() <= getThumbnailSizePSec()) {
                                                           position = seekBarRight.getX() - getThumbnailSizePSec() - view.getWidth();
                                                       }


                                                       if (lX1 > lX2 && position <= -view.getWidth() + 10 * dpi) {
                                                           position = -view.getWidth() + 10 * dpi;
                                                           Timber.d("seekBarLeft: distance less than 0: %d", position);
                                                       }

                                                       view.setTranslationX(position);
                                                       //editProgress.setX(leftPosition);
                                                       nowVideo.setStart((int) Math.ceil((position - 10 * dpi + view.getWidth()) * 1000 / getThumbnailSizePSec()));
                                                       if (getDistanceSeekBars() >= getThumbnailSizePSec() * (remainTime / 1000)) {
                                                           float rightPosition = seekBarRight.getX();
                                                           if (remainTime < nowVideo.getDurationMiliSec()) {
                                                               rightPosition = view.getX() + view.getWidth() + getThumbnailSizePSec() * (remainTime / 1000);
                                                           }
                                                           seekBarRight.setTranslationX(rightPosition);
                                                           nowVideo.setEnd((int) Math.floor((rightPosition - 10 * dpi) * 1000 / getThumbnailSizePSec()));
                                                       }
                                                       videoEditAdapter.notifyDataSetChanged();
                                                       isSeekBarChanged = true;
                                                       //            Log.d(TAG, "onTouch: start " + nowVideo.getStartX());
                                                       trimmerBackground.setStartX(position + view.getWidth());
                                                       trimmerBackground.setEndX(seekBarRight.getX());
                                                       trimmerBackground.invalidate();

                                                       if (wasPlaying) {
                                                           videoPlayer.seekTo(nowVideo.getStart());
                                                           musicPlayer.seekTo(musicOffset + resultVideosTotalTime);
                                                           musicPlayer.start();
                                                           wasPlaying = false;
                                                           editSeekBar.setStartX((int) Math.floor(seekBarLeft.getX() + seekBarLeft.getWidth()));
                                                           editSeekBar.invalidate();

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
                                                float dpi = getResources().getDimension(R.dimen.resultbar_line);
                                                float X = (int) motionEvent.getRawX() - 10 * dpi;
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


                                                        break;
                                                    case MotionEvent.ACTION_MOVE:

                                                        rX2 = motionEvent.getX();
                                                        position = X - delta;
                                                        //left position
                                                        if (rX1 > rX2 && getDistanceSeekBars() <= getThumbnailSizePSec()) {
                                                           position = (int) Math.floor(seekBarLeft.getX() + seekBarLeft.getWidth() + getThumbnailSizePSec());
                                                        }
                                                        //disPlayWidth - 5 * dpi
                                                        if (rX1 < rX2 && position >= 10 * dpi + getThumbnailSizePSec() * 3 * (videoThumbnails.size() - 1) + getThumbnailSizePSec() * (videoThumbnailendTime - videoThumbnails.get(videoThumbnails.size() - 1).getPresentationTimeUs()) / 1000) {
                                                            if (videoThumbnailendTime < 15000) {
                                                                position = 10 * dpi + getThumbnailSizePSec() * 3 * (videoThumbnails.size() - 1) + getThumbnailSizePSec() * (videoThumbnailendTime - videoThumbnails.get(videoThumbnails.size() - 1).getPresentationTimeUs()) / 1000;
                                                            } else {
                                                                position = disPlayWidth - 10 * dpi;
                                                            }

                                                        }

                                                        //minwidth
                                                        view.setTranslationX(position);
                                                        //distance time > remaintime
                                                        if (getDistanceSeekBars() >= getThumbnailSizePSec() * (remainTime / 1000)) {
                                                            float leftPosition = seekBarLeft.getX();
                                                            if (remainTime < nowVideo.getDurationMiliSec()) {
                                                                leftPosition = view.getX() - getThumbnailSizePSec() * (remainTime / 1000) - seekBarLeft.getWidth();
                                                            }

                                                            seekBarLeft.setTranslationX(leftPosition);
                                                            nowVideo.setStart((int) Math.floor((leftPosition + seekBarLeft.getWidth() - 10 * dpi) * 1000 / getThumbnailSizePSec()));
                                                        }

                                                        trimmerBackground.setStartX(seekBarLeft.getX() + seekBarLeft.getWidth());
                                                        trimmerBackground.setEndX(position);
                                                        trimmerBackground.invalidate();
                                                        break;

                                                    case MotionEvent.ACTION_UP:
                                                        if (rX1 > rX2 && getDistanceSeekBars() <= getThumbnailSizePSec()) {
                                                            position = (int) Math.floor(seekBarLeft.getX() + seekBarLeft.getWidth() + getThumbnailSizePSec());
                                                        }

                                                        //disPlayWidth - 5 * dpi
                                                        if (position >= 10 * dpi + getThumbnailSizePSec() * 3 * (videoThumbnails.size() - 1) + getThumbnailSizePSec() * (videoThumbnailendTime - videoThumbnails.get(videoThumbnails.size() - 1).getPresentationTimeUs()) / 1000) {
                                                            if (videoThumbnailendTime < 15000) {
                                                                position = 10 * dpi + getThumbnailSizePSec() * 3 * (videoThumbnails.size() - 1) + getThumbnailSizePSec() * (videoThumbnailendTime - videoThumbnails.get(videoThumbnails.size() - 1).getPresentationTimeUs()) / 1000;
                                                            } else {
                                                                position = disPlayWidth - 10 * dpi;
                                                            }
                                                            if (position - (seekBarLeft.getX() + seekBarLeft.getWidth()) <= getThumbnailSizePSec()) {
                                                                seekBarLeft.setTranslationX(position - getThumbnailSizePSec() - seekBarLeft.getWidth());
                                                                nowVideo.setStart((int) Math.floor((seekBarLeft.getX() + seekBarLeft.getWidth() - 10 * dpi) * 1000 / getThumbnailSizePSec()));
                                                            }
                                                        }

                                                        view.setTranslationX(position);


                                                        nowVideo.setEnd((int) Math.floor((position - 10 * dpi) * 1000 / getThumbnailSizePSec()));

//distance time > remaintime
                                                        if (getDistanceSeekBars() >= getThumbnailSizePSec() * (remainTime / 1000f)) {
                                                            float leftPosition = seekBarLeft.getX();
                                                            if (remainTime < nowVideo.getDurationMiliSec()) {
                                                                leftPosition = view.getX() - getThumbnailSizePSec() * (remainTime / 1000f) - seekBarLeft.getWidth();
                                                            }

                                                            seekBarLeft.setTranslationX(leftPosition);
                                                            nowVideo.setStart((int) Math.floor((leftPosition + seekBarLeft.getWidth() - 10 * dpi) * 1000f / getThumbnailSizePSec()));
                                                        }
                                                        videoEditAdapter.notifyDataSetChanged();
                                                        isSeekBarChanged = true;
                                                        trimmerBackground.setStartX(seekBarLeft.getX() + seekBarLeft.getWidth());
                                                        trimmerBackground.setEndX(position);
                                                        trimmerBackground.invalidate();
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

        videoEditThumbnailRecyclerView.setOnFlingListener(new RecyclerView.OnFlingListener()

                                                          {
                                                              @Override
                                                              public boolean onFling(int velocityX, int velocityY) {
                                                                  float dpi = getResources().getDimension(R.dimen.resultbar_line);


                                                                  int length = nowVideo.getEnd() - nowVideo.getStart();
                                                                  int start, end;
                                                                  if (velocityX > 0) {
                                                                      start = nowVideo.getStart() + Math.round((float) videoEditThumbnailRecyclerView.computeHorizontalScrollOffset() / getThumbnailSizePSec()) * 1000;
                                                                      end = nowVideo.getEnd() + Math.round((float) videoEditThumbnailRecyclerView.computeHorizontalScrollOffset() / getThumbnailSizePSec()) * 1000;

                                                                  } else {
                                                                      start = nowVideo.getStart() - Math.round(videoEditThumbnailRecyclerView.computeHorizontalScrollOffset() / getThumbnailSizePSec()) * 1000;
                                                                      end = nowVideo.getEnd() - Math.round(videoEditThumbnailRecyclerView.computeHorizontalScrollOffset() / getThumbnailSizePSec()) * 1000;

                                                                  }
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

                                                if (resultTime <= 15000) {

                                                    resultVideos.add(nowVideo);
                                                    resultVideosTotalTime = getResultEndTime(resultVideos);
                                                    flag = false;
                                                    thumbnailUtil.release();
                                                    videoEditAdapter.recycleThumbnails();
                                                    mCallBack.passDataFToF(0, selectedVideos, resultVideos, resultVideosTotalTime, musicPath, musicOffset, musicLength);
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
                                                thumbnailUtil.release();
                                                videoEditAdapter.recycleThumbnails();
                                                mCallBack.passDataFToF(0, selectedVideos, resultVideos, resultVideosTotalTime, musicPath, musicOffset, musicLength);
                                            }
                                        }

        );

    }


    @Override
    public void onPause() {
        flag = false;
        thumbnailUtil.release();
        videoEditAdapter.recycleThumbnails();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!flag) {
            mCallBack.passDataFToF(selectedNum, selectedVideos, resultVideos, resultVideosTotalTime, musicPath, musicOffset, musicLength);
        }
    }

    public int getResultEndTime(List<EditorVideo> editedVideos) {
        int editedVideosEnd = 0;
        for (EditorVideo editedVideo : editedVideos) {
            editedVideosEnd += (editedVideo.getEnd() - editedVideo.getStart());
        }
        return editedVideosEnd;
    }

    public int getThumbnailSizePSec() {
        DisplayMetrics outMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        int widthPSec = outMetrics.widthPixels;
        float dpi = getResources().getDimension(R.dimen.resultbar_line);
        float size = (widthPSec - 10 * dpi) / 15;
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
                                        editSeekBar.setStartX((int) Math.floor(seekBarLeft.getX() + seekBarLeft.getWidth() + ((float) videoPlayer.getCurrentPosition() - nowVideo.getStart()) / (nowVideo.getEnd() - nowVideo.getStart()) * (seekBarRight.getX() - (seekBarLeft.getX() + seekBarLeft.getWidth()))));
                                        editSeekBar.invalidate();
                                    }
                                }
                            });

                            if (nowVideo.getEnd() - nowVideo.getStart() + 1000 >= musicLength - resultVideosTotalTime) {
                                isMaxLength = true;
                            } else {
                                isMaxLength = false;
                            }

                            if (flag && videoPlayer.getCurrentPosition() >= nowVideo.getEnd() - 100) {

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        editSeekBar.setStartX((int) Math.floor(seekBarLeft.getX() + seekBarLeft.getWidth()));
                                        editSeekBar.invalidate();
                                    }
                                });
                                if (!isMaxLength || (isMaxLength && musicPlayer.getCurrentPosition() < musicPlayer.getDuration())) {
                                    videoPlayer.seekTo(nowVideo.getStart());
                                    musicPlayer.seekTo(musicOffset + resultVideosTotalTime);
                                }
                                continue;
                            }
                        }


                    }
                } catch (InterruptedException e) {
                    Timber.e(e, "m/getResultVideo");
                }
            }
            if (musicPlayer != null) {
                if (flag && musicPlayer.isPlaying()) {
                    musicPlayer.pause();
                    musicPlayer.stop();
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

    private float getDistanceSeekBars() {
        return seekBarRight.getX() - (seekBarLeft.getX() + seekBarLeft.getWidth());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (EditorVideo e : videoThumbnails){
            Timber.d("onDestroy: %s", e.getThumbnailBitmap().toString());
            if(e.getThumbnailBitmap() != null) e.getThumbnailBitmap().recycle();
        }
    }
}
