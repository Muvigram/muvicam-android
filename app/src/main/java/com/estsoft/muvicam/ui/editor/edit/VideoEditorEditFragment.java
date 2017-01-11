package com.estsoft.muvicam.ui.editor.edit;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.EditorVideo;
import com.estsoft.muvicam.transcoder.utils.ThumbnailUtil;
import com.estsoft.muvicam.ui.editor.ResultBarView;
import com.estsoft.muvicam.ui.editor.result.VideoEditorResultFragment;

import java.util.ArrayList;
import java.util.List;

public class VideoEditorEditFragment extends Fragment {
    String TAG = "VideoEditorEditFragment";
    ArrayList<EditorVideo> resultVideos = new ArrayList<>(), selectedVideos = new ArrayList<>();
    ArrayList<EditorVideo> videoThumbnails = new ArrayList<>();
    EditorVideo nowVideo;
    String musicPath;
    int selectedNum, musicOffset, musicLength;
    int resultVideosTotalTime;
    VideoEditorResultFragment.DataPassListener mCallBack;
    LinearLayout resultSpaceLinearLayout;
    ResultBarView resultBarView;
    RecyclerView videoEdit;
    ImageView seekBarLeft, seekBarRight;
    VideoEditorEditAdapter videoEditAdapter;
    boolean isSeekBarChanged = false;
    ImageView cancelButton, insertButton;
    EditorVideo videoThumbnailLastVideo;
    ThumbnailUtil.UserBitmapListener thumbnailUtilListener = new ThumbnailUtil.UserBitmapListener() {
        @Override
        public void onBitmapNext(final Bitmap bitmap, final long presentationTimeUs, final boolean isLast) {
//            Log.d(TAG, "onBitmapNext: " + presentationTimeUs);
//            Log.d(TAG, "onBitmapNextSize: " + resultMyVideos.size());
//            Log.d(TAG, "onBitmapNextCount: " + videoEditAdapter.getItemCount());
            Log.d(TAG, "onBitmapNext: " + presentationTimeUs);
            if (!isLast) {
                EditorVideo editMyVideo = new EditorVideo(bitmap, (int) presentationTimeUs / 1000, isLast, selectedNum);
                videoThumbnails.add(editMyVideo);
                videoEditAdapter.notifyDataSetChanged();
            } else {
                videoThumbnailLastVideo = new EditorVideo(bitmap, (int) presentationTimeUs / 1000, isLast, selectedNum);
            }

            Log.d(TAG, "onBitmapNext123: " + presentationTimeUs + " /. " + isLast);
        }

        @Override
        public void onError(Exception e) {

        }

        @Override
        public void onComplete( long endTimeUs ) {
            Log.d(TAG, "onComplete: ");
            DisplayMetrics outMetrics = new DisplayMetrics();
            ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
            float disPlayWidth = outMetrics.widthPixels;
            float dpi = outMetrics.densityDpi / 160;

            if (videoThumbnails.size() < Math.floor(disPlayWidth / (getThumbnailSizePSec() * 3))) {
                float myLength = 5 * dpi + getThumbnailSizePSec() * 3 * videoThumbnails.size();
                seekBarRight.setTranslationX(myLength);
                nowVideo.setEnd(videoThumbnailLastVideo.getPresentationTimeUs());
                Log.d(TAG, "onClick: endtime" + nowVideo.getEnd());
            } else {
                seekBarRight.setTranslationX(disPlayWidth - 5 * dpi);
                // + Math.round(videoEdit.computeHorizontalScrollOffset() * 15000 / disPlayWidth)
                nowVideo.setEnd(15000);
                Log.d(TAG, "onClick: endtime" + nowVideo.getEnd());
            }
            seekBarLeft.setTranslationX(-seekBarLeft.getWidth() + 5 * dpi);
            Log.d(TAG, "onComplete: seekbarL X :" + seekBarLeft.getX());
            //(Math.round(videoEdit.computeHorizontalScrollOffset() * 15000 / disPlayWidth))
            nowVideo.setStart(0);
            Log.d(TAG, "onClick: starttime" + nowVideo.getStart());

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
            musicPath = args.getString(VideoEditorResultFragment.EXTRA_MUSIC_PATH);
            musicOffset = args.getInt(VideoEditorResultFragment.EXTRA_MUSIC_OFFSET, 0);
            musicLength = args.getInt(VideoEditorResultFragment.EXTRA_MUSIC_LENGTH, 0);
            nowVideo = selectedVideos.get(selectedNum - 1);
            nowVideo.setNumSelected(selectedNum);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_video_editor_edit, container, false);

        resultSpaceLinearLayout = (LinearLayout) v.findViewById(R.id.editor_edit_result_space_linear);
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


        thumbnailUtil = new ThumbnailUtil(thumbnailUtilListener, getActivity(), true);
        Log.d(TAG, "onCreateView: " + videoEdit.getWidth());
        // 3/15 -> 1/5
        thumbnailUtil.extractFromNewThread(nowVideo.getVideoPath(), 3.0, getThumbnailSizePSec() * 3, getThumbnailSizePSec() * 3);
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

                float X = motionEvent.getRawX() - ((float) view.getWidth() * 39) / 40;
                float delta = 0;
                float position = X - delta;
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

                        break;

                    case MotionEvent.ACTION_UP:
                        float distance = seekBarRight.getX() - (view.getX() + view.getWidth());
                        Log.d(TAG, "seekBarLeft: distance" + distance);
                        if (distance < getThumbnailSizePSec()) {
                            Log.d(TAG, "seekBarLeft: distance less than mindistance");
                            position = seekBarRight.getX() - getThumbnailSizePSec() - view.getWidth();
                            Log.d(TAG, "seekBarLeft: distance less than mindistance: " + position);
                        }
                        if (position < -seekBarLeft.getWidth() + 5 * dpi) {
                            position = -seekBarLeft.getWidth() + 5 * dpi;
                            Log.d(TAG, "seekBarLeft: distance less than 0: " + position);
                        }
                        view.setTranslationX(position);
                        float leftPosition = position + (float) view.getWidth();
                        //editProgress.setX(leftPosition);
                        nowVideo.setStart((int) Math.floor((position - 5 * dpi + view.getWidth()) * 1000 / getThumbnailSizePSec()));
                        Log.d(TAG, "seekBarLeft: starttime: " + nowVideo.getStart());
                        videoEditAdapter.notifyDataSetChanged();
                        isSeekBarChanged = true;
                        break;
                }
                return true;
            }
        });

        seekBarRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                DisplayMetrics outMetrics = new DisplayMetrics();
                ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
                float disPlayWidth = outMetrics.widthPixels;
                float dpi = outMetrics.densityDpi / 160;

                float X = (int) motionEvent.getRawX() - view.getWidth() / 40;
                float delta = 0;
                float position = X - delta;
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
                        break;

                    case MotionEvent.ACTION_UP:
                        float distance = view.getX() - (seekBarLeft.getX() + seekBarLeft.getWidth());
                        Log.d(TAG, "seekBarRight: distance" + distance);
                        if (distance < getThumbnailSizePSec()) {
                            Log.d(TAG, "seekBarRight: distance less than mindistance");
                            position = (int) Math.floor(seekBarLeft.getX() + seekBarLeft.getWidth() + getThumbnailSizePSec());
                        }
                        if (videoThumbnails.size() < Math.floor(disPlayWidth / (getThumbnailSizePSec() * 3)) && position > 5 * dpi + getThumbnailSizePSec() * 3 * videoThumbnails.size()) {
                            position = 5 * dpi + getThumbnailSizePSec() * 3 * videoThumbnails.size();

                        } else if (videoThumbnails.size() >= 6 && position > disPlayWidth - 5 * dpi) {
                            position = disPlayWidth - 5 * dpi;
                        }
                        view.setTranslationX(position);

                        nowVideo.setEnd((int) Math.floor((position - 5 * dpi) * 1000 / getThumbnailSizePSec()));
                        videoEditAdapter.notifyDataSetChanged();
                        Log.d(TAG, "seekBarRight: videoEditEndTime " + nowVideo.getEnd());
                        isSeekBarChanged = true;
                        break;
                }
                return true;
            }
        });

        videoEdit.setOnFlingListener(new RecyclerView.OnFlingListener() {
            @Override
            public boolean onFling(int velocityX, int velocityY) {
                DisplayMetrics outMetrics = new DisplayMetrics();
                ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
                float disPlayWidth = outMetrics.widthPixels;
                int length = nowVideo.getEnd() - nowVideo.getStart();
                int start = nowVideo.getStart() + Math.round(videoEdit.computeHorizontalScrollOffset() * 15000 / disPlayWidth);
                int end = nowVideo.getEnd() + Math.round(videoEdit.computeHorizontalScrollOffset() * 15000 / disPlayWidth);
                if (start < 0) {
                    nowVideo.setStart(0);
                    nowVideo.setEnd(length);
                } else if (end > videoThumbnailLastVideo.getPresentationTimeUs()) {
                    nowVideo.setEnd(videoThumbnailLastVideo.getPresentationTimeUs());
                    nowVideo.setStart(nowVideo.getEnd() - length);
                } else {
                    nowVideo.setStart(start);
                    nowVideo.setEnd(end);
                }
                videoEditAdapter.notifyDataSetChanged();
                isSeekBarChanged = true;
                Log.d(TAG, "onFling: offset" + videoEdit.computeHorizontalScrollOffset());
                Log.d(TAG, "onFling:starttime " + nowVideo.getStart());
                Log.d(TAG, "onFling:endtime " + nowVideo.getEnd());
                return false;
            }
        });


        insertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int resultTime = getResultEndTime(resultVideos) + nowVideo.getEnd() - nowVideo.getStart();
                Log.d(TAG, "onClick: starttime" + nowVideo.getStart());
                Log.d(TAG, "onClick: videoEditEndTime" + nowVideo.getEnd());
                Log.d(TAG, "onClick: resultTime" + resultTime);
                if (resultTime <= 15000) {
                    resultVideos.add(nowVideo);
                    Log.d(TAG, "onClick: getresultendTime" + getResultEndTime(resultVideos) + "sec");
                    int remain = 15 - Math.round((float) getResultEndTime(resultVideos) / 1000);
                    Log.d(TAG, "onClick: getremainedTime" + remain + "sec");
                    resultSpaceLinearLayout.removeView(resultBarView);
                    mCallBack.passDataFToF(0, selectedVideos, resultVideos, resultTime, musicPath, musicOffset, musicLength);
                } else {
                    Toast.makeText(getActivity(), "the edited video time is shorter than " + (15000 - (getResultEndTime(resultVideos))) / 1000 + "sec", Toast.LENGTH_SHORT).show();
                }

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallBack.passDataFToF(0, selectedVideos, resultVideos, resultVideosTotalTime, musicPath, musicOffset, musicLength);
            }
        });

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
        float size = (widthPSec - 10 / dpi) / 15;
        Log.d(TAG, "getThumbnailSizePSec: " + size);
        return (int) Math.floor(size);
    }

}
