package com.estsoft.muvicam.ui.editor.edit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
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
import com.estsoft.muvicam.ui.selector.videoselector.ThumbnailImageView;

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


    ThumbnailUtil.UserBitmapListener thumbnailUtilListener = new ThumbnailUtil.UserBitmapListener() {
        @Override
        public void onBitmapNext(final Bitmap bitmap, final long presentationTimeUs, final boolean isLast) {
//            Log.d(TAG, "onBitmapNext: " + presentationTimeUs);
//            Log.d(TAG, "onBitmapNextSize: " + resultMyVideos.size());
//            Log.d(TAG, "onBitmapNextCount: " + videoEditAdapter.getItemCount());
            Log.d(TAG, "onBitmapNext: " + presentationTimeUs);
            if (!isLast) {
                EditorVideo editMyVideo = new EditorVideo(bitmap, presentationTimeUs, isLast, selectedNum);
                videoThumbnails.add(editMyVideo);
                videoEditAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onError(Exception e) {

        }

        @Override
        public void onComplete() {
            Log.d(TAG, "onComplete: ");
            Display mdisp = getActivity().getWindowManager().getDefaultDisplay();
            Point mdispSize = new Point();
            mdisp.getSize(mdispSize);
            int maxWidth = mdispSize.x;
            if (videoThumbnails.size() < 5) {
                float myLength = getActivity().findViewById(R.id.video_edit_thumbnail).getWidth() * videoThumbnails.size();
                seekBarRight.setTranslationX(myLength);
                nowVideo.setEnd(Math.round(15000 * myLength / maxWidth));
                Log.d(TAG, "onClick: endtime" + nowVideo.getEnd());
            } else {
                seekBarRight.setTranslationX(maxWidth - 18);
                nowVideo.setEnd(15000 + videoEdit.computeHorizontalScrollOffset());
                Log.d(TAG, "onClick: endtime" + nowVideo.getEnd());
            }
            seekBarLeft.setTranslationX(18 - maxWidth);
            nowVideo.setStart((int) (Math.round(videoEdit.computeHorizontalScrollOffset()) * 15000 / maxWidth));
            Log.d(TAG, "onClick: starttime" + nowVideo.getStart());
            Log.d(TAG, "onClick: starttimeView" + Math.round((((float) videoEdit.getWidth()) / 40 + videoEdit.computeHorizontalScrollOffset()) * 1000 / (maxWidth / 15)));
        }
    };
    //    ThumbnailUtil thumbnailUtil = new ThumbnailUtil(thumbnailUtilListener, getActivity(), true);
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
            resultVideosTotalTime = args.getInt(VideoEditorResultFragment.EXTRA_RESULT_VIDEO_TOTAL_TIME);
            musicPath = args.getString(VideoEditorResultFragment.EXTRA_MUSIC_PATH);
            musicOffset = args.getInt(VideoEditorResultFragment.EXTRA_MUSIC_OFFSET);
            musicLength = args.getInt(VideoEditorResultFragment.EXTRA_MUSIC_LENGTH);
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
        thumbnailUtil.extractFromNewThread(nowVideo.getVideoPath(), 3.0, 300, 300);
        return v;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        seekBarLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
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
                        if (distance < getSeekBarsMinDistance()) {
                            Log.d(TAG, "seekBarLeft: distance less than mindistance");
                            position = seekBarRight.getX() - getSeekBarsMinDistance() - view.getWidth();
                            Log.d(TAG, "seekBarLeft: distance less than mindistance: " + position);
                        }
                        if (position < 18 - 15 * getSeekBarsMinDistance()) {
                            position = 18 - 15 * getSeekBarsMinDistance();
                            Log.d(TAG, "seekBarLeft: distance less than 0: " + position);
                        }
                        view.setTranslationX(position);
                        float leftPosition = position + (float) view.getWidth();
                        //editProgress.setX(leftPosition);
                        nowVideo.setStart((int) Math.floor((position + ((float) view.getWidth() * 39) / 40) * 1000 / getSeekBarsMinDistance()));
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
                        if (distance < getSeekBarsMinDistance()) {
                            Log.d(TAG, "seekBarRight: distance less than mindistance");
                            position = (int) Math.floor(seekBarLeft.getX() + seekBarLeft.getWidth() + getSeekBarsMinDistance());
                        }
                        if (videoThumbnails.size() < 6 && position > getActivity().findViewById(R.id.video_edit_thumbnail).getWidth() * videoThumbnails.size()) {
                            position = (int) Math.floor(getActivity().findViewById(R.id.video_edit_thumbnail).getWidth()) * videoThumbnails.size();

                        } else if (videoThumbnails.size() >= 6 && position > (15 * getSeekBarsMinDistance() - 18)) {
                            position = 15 * getSeekBarsMinDistance() - 18;
                        }

                        view.setTranslationX(position);
                        nowVideo.setEnd((int) Math.floor((position) * 1000 / getSeekBarsMinDistance()));
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
                nowVideo.setStart(nowVideo.getStart() + (int) Math.floor(videoEdit.computeHorizontalScrollOffset() * 1000 / getSeekBarsMinDistance()));
                nowVideo.setEnd(nowVideo.getEnd() + (int) Math.floor(videoEdit.computeHorizontalScrollOffset() * 1000 / getSeekBarsMinDistance()));
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
                    videoEditAdapter.notifyDataSetChanged();
                    Log.d(TAG, "onClick: getresultendTime"+getResultEndTime(resultVideos) + "sec");
                    int remain =15 -getResultEndTime(resultVideos) / 1000;
                    Log.d(TAG, "onClick: getremainedTime"+remain + "sec");

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

    public float getSeekBarsMinDistance() {
        Display mdisp = getActivity().getWindowManager().getDefaultDisplay();
        Point mdispSize = new Point();
        mdisp.getSize(mdispSize);
        float maxX = mdispSize.x;
        Log.d(TAG, "getSeekBarsMinDistance: " + maxX / 15);
        return maxX / 15;
    }

    public int getResultEndTime(List<EditorVideo> editedVideos) {
        int editedVideosEnd = 0;
        for (EditorVideo editedVideo : editedVideos) {
            editedVideosEnd += editedVideo.getEnd() - editedVideo.getStart();
        }
        Log.d(TAG, "getResultEndTime" + editedVideosEnd);
        return editedVideosEnd;
    }
}
