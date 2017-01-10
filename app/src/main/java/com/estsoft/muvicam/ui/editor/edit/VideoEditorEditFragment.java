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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.EditorVideo;
import com.estsoft.muvicam.transcoder.utils.ThumbnailUtil;
import com.estsoft.muvicam.ui.editor.result.VideoEditorResultFragment;
import com.estsoft.muvicam.ui.selector.videoselector.ThumbnailImageView;

import java.util.ArrayList;
import java.util.List;

public class VideoEditorEditFragment extends Fragment {
    String TAG = "VideoEditorEditFragment";
    List<EditorVideo> resultVideos, selectedVideos;
    List<EditorVideo> videoThumbnails = new ArrayList<>();
    EditorVideo nowVideo;
    String musicPath;
    int selectedNum, musicOffset, musicLength;
    float resultVideosTotalTime;
    VideoEditorResultFragment.DataPassListener mCallBack;
    RecyclerView videoEdit;
    ImageView seekBarLeft, seekBarRight;
    VideoEditorEditAdapter videoEditAdapter;

    ThumbnailUtil.UserBitmapListener thumbnailUtilListener = new ThumbnailUtil.UserBitmapListener() {
        @Override
        public void onBitmapNext(final Bitmap bitmap, final long presentationTimeUs, final boolean isLast) {
//            Log.d(TAG, "onBitmapNext: " + presentationTimeUs);
//            Log.d(TAG, "onBitmapNextSize: " + resultMyVideos.size());
//            Log.d(TAG, "onBitmapNextCount: " + videoEditAdapter.getItemCount());
            Log.d(TAG, "onBitmapNext: "+presentationTimeUs);
            if (!isLast) {
            EditorVideo editMyVideo = new EditorVideo(bitmap, presentationTimeUs, isLast, selectedNum);
            editMyVideo.setVideoPath(selectedVideos.get(selectedNum - 1).getVideoPath());
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
        float maxX = mdispSize.x;
        if (videoThumbnails.size() < 6) {
            float myLength = getActivity().findViewById(R.id.video_edit_thumbnail).getWidth() * videoThumbnails.size();
            seekBarRight.setTranslationX(myLength);
            nowVideo.setEnd(Math.round(15000 * myLength / maxX));
            Log.d(TAG, "onClick: endtime" + nowVideo.getEnd());
        } else {
            seekBarRight.setTranslationX(maxX - 18);
            nowVideo.setEnd(15000 + videoEdit.computeHorizontalScrollOffset());
            Log.d(TAG, "onClick: endtime" + nowVideo.getEnd());
        }
        seekBarLeft.setTranslationX(18 - maxX);
        nowVideo.setStart((int) (Math.round(videoEdit.computeHorizontalScrollOffset()) * 15000 / maxX));
        Log.d(TAG, "onClick: starttime" + nowVideo.getStart());
        Log.d(TAG, "onClick: starttimeView" + Math.round((((float) videoEdit.getWidth()) / 40 + videoEdit.computeHorizontalScrollOffset()) * 1000 / (maxX / 15)));
        }
    };
    ThumbnailUtil thumbnailUtil = new ThumbnailUtil(thumbnailUtilListener, true);

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
            resultVideosTotalTime = args.getFloat(VideoEditorResultFragment.EXTRA_RESULT_VIDEO_TOTAL_TIME);
            musicPath = args.getString(VideoEditorResultFragment.EXTRA_MUSIC_PATH);
            musicOffset = args.getInt(VideoEditorResultFragment.EXTRA_MUSIC_OFFSET);
            musicLength = args.getInt(VideoEditorResultFragment.EXTRA_MUSIC_LENGTH);
            nowVideo = selectedVideos.get(selectedNum - 1);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_video_editor_edit, container, false);
        videoEdit = (RecyclerView) v.findViewById(R.id.editor_edit_recycler_thumbnails);
        LinearLayoutManager linearLayoutManagerE = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        videoEdit.setLayoutManager(linearLayoutManagerE);

        seekBarLeft = (ImageView) v.findViewById(R.id.editor_seekbar_left);
        seekBarRight = (ImageView) v.findViewById(R.id.editor_seekbar_right);
        videoEditAdapter = new VideoEditorEditAdapter(getActivity(), selectedNum, selectedVideos.get(selectedNum - 1), videoThumbnails);
        videoEdit.setAdapter(videoEditAdapter);

        thumbnailUtil.extractFromNewThread(selectedVideos.get(selectedNum - 1).getVideoPath(), 3.0, 300, 300);
        return v;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }


}
