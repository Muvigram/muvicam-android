package com.estsoft.muvigram.ui.editor.result;

import android.app.Activity;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.estsoft.muvigram.R;
import com.estsoft.muvigram.model.EditorVideo;
import com.estsoft.muvigram.ui.common.BackToHomeDialogFragment;
import com.estsoft.muvigram.ui.editor.ResultBarView;
import com.estsoft.muvigram.ui.editor.VideoPlayerTextureView;
import com.estsoft.muvigram.ui.share.ShareActivity;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import timber.log.Timber;

/*
 * will show edit view
  * */
public class VideoEditorResultFragment extends Fragment {
    public final static String EXTRA_FRAGMENT_NUM = "VideoEditorResultFragment.fragmentNum";
    public final static String EXTRA_VIDEOS = "VideoEditorResultFragment.videoList";
    public final static String EXTRA_RESULT_VIDEOS = "VideoEditorResultFragment.resultVideoList";
    public final static String EXTRA_RESULT_VIDEO_TOTAL_TIME = "VideoEditorResultFragment.resultVideoTotalTime";
    public final static String EXTRA_MUSIC_PATH = "VideoEditorResultFragment.musicPath";
    public final static String EXTRA_MUSIC_OFFSET = "VideoEditorResultFragment.musicOffset";
    public final static String EXTRA_MUSIC_LENGTH = "VideoEditorResultFragment.musicLength";
    RecyclerView selectedVideoButtons;
    ImageView deleteButton, buttonsGone;
    LinearLayout editorResultBlackScreen;
    FrameLayout linearResultSpace;
    ArrayList<ResultBarView> resultBarViews = new ArrayList<>();
    EditorResultMediaPlayer videoResultPlayer, videoResultPlayer2;
    MediaPlayer musicResultPlayer;
    VideoPlayerTextureView videoResultTextureView, videoResultTextureView2;
    FrameLayout videoSpaceFrameLayout;
    boolean flag = true;
    boolean isFirstNumChanged = false, isSecondNumChanged = false, isMusicComplition = false;
    private ArrayList<EditorVideo> resultVideos = new ArrayList<>(), selectedVideos = new ArrayList<>();
    String musicPath;
    int musicOffset, musicLength;
    int resultVideosTotalTime;
    int nowVideoNum;
    DataPassListener mCallBack;
    ImageView doneButton, homeButton;
    ResultBarView resultProgressBar;


    VideoEditSelectedNumberAdapter.OnItemClickListener itemClickListener = new VideoEditSelectedNumberAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            selectedVideoButtons.setClickable(false);
            Timber.d("onCreateView: resultVideosTotalTime6 %d", resultVideosTotalTime);

            flag = false;
            mCallBack.passDataFToF(position + 1, selectedVideos, resultVideos, resultVideosTotalTime, musicPath, musicOffset, musicLength);
        }
    };


    public interface DataPassListener {
        void passDataFToF(int selectedNum, ArrayList<EditorVideo> selectedVideos, ArrayList<EditorVideo> resultEditorVideos, int resultTotalTime, String musicPath, int musicOffset, int musicLength);
    }

    public VideoEditorResultFragment() {
        // Required empty public constructor
    }

    public static VideoEditorResultFragment newInstance() {
        VideoEditorResultFragment fragment = new VideoEditorResultFragment();
        Bundle args = new Bundle();
//        args.putParcelableArrayList(VideoEditorResultFragment.EXTRA_VIDEOS, selectedVideos);
//        args.putParcelableArrayList(VideoEditorResultFragment.EXTRA_RESULT_VIDEOS, resultEditorVideos);
//        args.putInt(VideoEditorResultFragment.EXTRA_RESULT_VIDEO_TOTAL_TIME,resultVideosTotalTime);
//        args.putString(VideoEditorResultFragment.EXTRA_MUSIC_PATH, musicPath);
//        args.putInt(VideoEditorResultFragment.EXTRA_MUSIC_OFFSET, musicOffset);
//        args.putInt(VideoEditorResultFragment.EXTRA_MUSIC_LENGTH, musicLength);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            selectedVideos = args.getParcelableArrayList(VideoEditorResultFragment.EXTRA_VIDEOS);
            resultVideos = args.getParcelableArrayList(VideoEditorResultFragment.EXTRA_RESULT_VIDEOS);
            resultVideosTotalTime = args.getInt(VideoEditorResultFragment.EXTRA_RESULT_VIDEO_TOTAL_TIME, 0);
            musicPath = args.getString(VideoEditorResultFragment.EXTRA_MUSIC_PATH);
            musicOffset = args.getInt(VideoEditorResultFragment.EXTRA_MUSIC_OFFSET, 0);
            musicLength = args.getInt(VideoEditorResultFragment.EXTRA_MUSIC_LENGTH, 0);

        }
        nowVideoNum = 0;
        videoResultPlayer = new EditorResultMediaPlayer(getActivity(), 0, true, false);
        videoResultPlayer2 = new EditorResultMediaPlayer(getActivity(), 0, false, false);
        videoResultPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Timber.d("onCompletion1: nowVideoNum %d", nowVideoNum);
                if (!isFirstNumChanged) {
                    nowVideoNum = nowVideoNum + 1;
                    isFirstNumChanged = true;
                } else {
                    isFirstNumChanged = false;
                }
                Timber.d("run: nowVideoNum1 %d/%d", nowVideoNum, resultVideos.size());

                if (nowVideoNum < resultVideos.size()) {
                    //     Log.d(TAG, "run sec: " + nowVideoNum + " / " + resultVideos.size());
                    videoResultPlayer2.start();
                    videoResultTextureView2.bringToFront();
                    //   Log.d(TAG, "run third: " + nowVideoNum + " / " + resultVideos.size());
                    if (nowVideoNum + 1 < resultVideos.size()) {
                        videoResultPlayer.setFirst(false);
                        prepareVideoPlayer(videoResultPlayer, nowVideoNum + 1, true);
                    }
                } else {

                    musicResultPlayer.pause();
                    nowVideoNum = 0;

                    if (flag && resultVideos.size() > nowVideoNum && !musicResultPlayer.isPlaying()) {
                        if (resultVideos.size() > nowVideoNum + 1) {
                            videoResultPlayer2.setFirst(false);
                            prepareVideoPlayer(videoResultPlayer2, nowVideoNum + 1, false);
                        }
                        videoResultPlayer.setFirst(false);
                        prepareVideoPlayer(videoResultPlayer, nowVideoNum, true);

                    }
                }

            }
        });
        videoResultPlayer2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Timber.d("onCompletion2: nowVideoNum %d", nowVideoNum);
                if (!isSecondNumChanged) {
                    nowVideoNum = nowVideoNum + 1;
                    isSecondNumChanged = true;
                } else {
                    isSecondNumChanged = false;
                }
                Timber.d("run: nowVideoNum2 %d/%d", nowVideoNum, resultVideos.size());

                if (nowVideoNum < resultVideos.size()) {
                    videoResultPlayer.start();
                    videoResultTextureView.bringToFront();
                    if (nowVideoNum + 1 < resultVideos.size()) {
                        videoResultPlayer2.setFirst(false);
                        prepareVideoPlayer(videoResultPlayer2, nowVideoNum + 1, false);
                    }
                    //else videoplayer1 to start
                } else {
                    musicResultPlayer.pause();
                    nowVideoNum = 0;

                    if (flag && resultVideos.size() > nowVideoNum && !musicResultPlayer.isPlaying()) {
                        if (resultVideos.size() > nowVideoNum + 1) {
                            videoResultPlayer2.setFirst(false);
                            prepareVideoPlayer(videoResultPlayer2, nowVideoNum + 1, false);
                        }
                        videoResultPlayer.setFirst(false);
                        prepareVideoPlayer(videoResultPlayer, nowVideoNum, true);
                    }
                }
                videoResultTextureView.bringToFront();
            }
        });
        musicResultPlayer = new MediaPlayer();
        if (resultVideos.size() > 0) {
            try {
                FileInputStream fisMusic = new FileInputStream(musicPath);
                FileDescriptor fdMusic = fisMusic.getFD();
                musicResultPlayer.setDataSource(fdMusic);
                musicResultPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                    @Override
                    public void onSeekComplete(MediaPlayer mediaPlayer) {
                        deleteButton.setVisibility(View.VISIBLE);
                        mediaPlayer.start();
                    }
                });
                musicResultPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                                              @Override
                                                              public void onCompletion(MediaPlayer mediaPlayer) {
                                                                  isMusicComplition = true;
                                                                  videoResultTextureView.bringToFront();
                                                                  nowVideoNum = 0;
                                                                  isMusicComplition = false;
                                                              }
                                                          }
                );
                musicResultPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener()

                                                        {
                                                            @Override
                                                            public void onPrepared(MediaPlayer mediaPlayer) {
                                                                mediaPlayer.seekTo(musicOffset);
                                                            }
                                                        }

                );
                videoResultPlayer.setVolume(0, 0);
                videoResultPlayer2.setVolume(0, 0);
            } catch (FileNotFoundException e) {
                Timber.e(e, "m/onCraete e/FileNotFoundException");
            } catch (IOException e) {
                Timber.e(e, "m/onCraete e/IOException");
            }

            resultThread.start();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (!flag) {
            mCallBack.passDataFToF(0, selectedVideos, resultVideos, resultVideosTotalTime, musicPath, musicOffset, musicLength);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_editor_result, container, false);
        videoSpaceFrameLayout = (FrameLayout) v.findViewById(R.id.editor_result_frame_layout);
        selectedVideoButtons = (RecyclerView) v.findViewById(R.id.editor_result_buttons);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        selectedVideoButtons.setLayoutManager(linearLayoutManager);
        deleteButton = (ImageView) v.findViewById(R.id.editor_result_delete);
        linearResultSpace = (FrameLayout) v.findViewById(R.id.editor_result_space_linear);

        buttonsGone = (ImageView) v.findViewById(R.id.editor_result_buttons_gone);
        ResultBarView resultBarView;
        Timber.d("onCreateView: resultVideosTotalTime1 %d", resultVideosTotalTime);
        int resultTime = 0;
        for (int i = 0; i < resultVideos.size(); i++) {

            int nowVideoTime = resultVideos.get(i).getEnd() - resultVideos.get(i).getStart();
            int remainTime = 15000 - resultVideosTotalTime;
            Timber.d("onCreateView: resultTime %d", resultTime);
            if (i == resultVideos.size() - 1 && remainTime < 1000) {
                resultBarView = new ResultBarView(getContext(), resultTime, 15000 - resultTime, false);
            } else {
                resultBarView = new ResultBarView(getContext(), resultTime, nowVideoTime, false);
            }
            resultTime += nowVideoTime;
            linearResultSpace.addView(resultBarView);
            resultBarViews.add(resultBarView);
        }

        editorResultBlackScreen = (LinearLayout) v.findViewById(R.id.editor_result_black_screen);
        doneButton = (ImageView) v.findViewById(R.id.editor_done);
        homeButton = (ImageView) v.findViewById(R.id.editor_home);

        if (resultVideosTotalTime > 0) {
            Timber.d("onCreateView: resultVideosTotalTime2 %d", resultVideosTotalTime);
            deleteButton.setTranslationX(deleteButtonLocation(resultVideosTotalTime));
        }
        int remainTime = 15000 - resultVideosTotalTime;
        if (remainTime < 1000) {
            selectedVideoButtons.setVisibility(View.GONE);
            buttonsGone.setVisibility(View.VISIBLE);
        }
        resultProgressBar = new ResultBarView(getContext(), 0, 0, true);
        linearResultSpace.addView(resultProgressBar);
        linearResultSpace.invalidate();
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        VideoEditSelectedNumberAdapter videoEditSelectedNumberAdapter = new VideoEditSelectedNumberAdapter(getActivity(), selectedVideos, itemClickListener);

        selectedVideoButtons.setAdapter(videoEditSelectedNumberAdapter);
        if (resultVideos.size() > 0) {
            try {
                if (resultVideos.size() > 1) {
                    MediaMetadataRetriever retriever2 = new MediaMetadataRetriever();
                    FileInputStream fis2 = new FileInputStream(resultVideos.get(1).getVideoPath());
                    FileDescriptor fd2 = fis2.getFD();
                    videoResultPlayer2.setDataSource(fd2);
                    videoResultPlayer2.setOffset(resultVideos.get(1).getStart());
                    retriever2.setDataSource(fd2);
                    int width2 = Integer.valueOf(retriever2.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                    int height2 = Integer.valueOf(retriever2.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                    int rotation2 = Integer.valueOf(retriever2.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
                    videoResultTextureView2 = new VideoPlayerTextureView(getActivity(), videoResultPlayer2, resultVideos.get(1), width2, height2, rotation2);
                    videoSpaceFrameLayout.addView(videoResultTextureView2);
                    fis2.close();

                }
                MediaMetadataRetriever retriever1 = new MediaMetadataRetriever();
                FileInputStream fis = new FileInputStream(resultVideos.get(0).getVideoPath());
                FileDescriptor fd = fis.getFD();
                videoResultPlayer.setDataSource(fd);
                videoResultPlayer.setOffset(resultVideos.get(0).getStart());
                retriever1.setDataSource(fd);
                int width1 = Integer.valueOf(retriever1.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                int height1 = Integer.valueOf(retriever1.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                int rotation1 = Integer.valueOf(retriever1.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
                videoResultTextureView = new VideoPlayerTextureView(getActivity(), videoResultPlayer, resultVideos.get(0), width1, height1, rotation1);
                musicResultPlayer.prepare();
                videoSpaceFrameLayout.addView(videoResultTextureView);
                fis.close();
            } catch (IOException ioe) {
                Timber.e(ioe, "m/onViewCreated exception");
            }

        }

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditorVideo removedVideo = resultVideos.get(resultVideos.size() - 1);
                resultVideosTotalTime = resultVideosTotalTime - (removedVideo.getEnd() - removedVideo.getStart());
                Timber.d("onCreateView: resultVideosTotalTime3 %d", resultVideosTotalTime);
                if (videoResultPlayer.isPlaying()) {
                    videoResultPlayer.pause();
                }
                if (videoResultPlayer2.isPlaying()) {
                    videoResultPlayer2.pause();
                }
                if (musicResultPlayer.isPlaying()) {
                    musicResultPlayer.pause();
                }
                videoResultPlayer.stop();
                videoResultPlayer2.stop();


                //at least one video should removed.
                resultVideos.remove(resultVideos.get(resultVideos.size() - 1));
                linearResultSpace.removeView(resultBarViews.get(resultBarViews.size() - 1));
                resultBarViews.remove(resultBarViews.get(resultBarViews.size() - 1));
                deleteButton.setTranslationX(deleteButtonLocation(resultVideosTotalTime));
                Timber.d("onCreateView: resultVideosTotalTime5 %d", resultVideosTotalTime);
                nowVideoNum = 0;

                if (selectedVideoButtons.getVisibility() == View.GONE) {
                    selectedVideoButtons.setVisibility(View.VISIBLE);
                    buttonsGone.setVisibility(View.GONE);
                }
                //after removed last video

                if (resultVideos.size() > 0) {
                    if (resultVideos.size() > 1)
                        prepareVideoPlayer(videoResultPlayer2, nowVideoNum + 1, false);
                    prepareVideoPlayer(videoResultPlayer, nowVideoNum, true);
                    videoResultTextureView.bringToFront();
                    musicResultPlayer.seekTo(musicOffset);
                    videoResultPlayer.start();
                } else {
                    musicResultPlayer.stop();
                    deleteButton.setVisibility(View.GONE);
                    editorResultBlackScreen.bringToFront();
                }

                linearResultSpace.invalidate();
            }
        });
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (resultVideos.size() > 0) {
                    flag = false;
                    String[] videoPaths = new String[resultVideos.size()];
                    int[] videoStartTimes = new int[resultVideos.size()];
                    int[] videoEndTimes = new int[resultVideos.size()];
                    for (EditorVideo resultVideo : resultVideos) {
                        videoPaths[resultVideos.indexOf(resultVideo)] = resultVideo.getVideoPath();
                        videoStartTimes[resultVideos.indexOf(resultVideo)] = resultVideo.getStart();
                        videoEndTimes[resultVideos.indexOf(resultVideo)] = resultVideo.getEnd();
                    }
                    getActivity().startActivity(ShareActivity.getIntent(getContext(), videoPaths, videoStartTimes, videoEndTimes, musicPath, musicOffset, resultVideosTotalTime, true));
                    getActivity().finish();
                } else {
                    Toast.makeText(getContext(), getResources().getString(R.string.editor_result_done_warning), Toast.LENGTH_SHORT).show();
                }
            }
        });
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BackToHomeDialogFragment fragment = BackToHomeDialogFragment.newInstance(
                        getResources().getString(R.string.dialog_back_to_home));
                fragment.show(getFragmentManager(), BackToHomeDialogFragment.TAG);
            }
        });

        videoSpaceFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag && !isMusicComplition && resultVideos.size() > nowVideoNum && !musicResultPlayer.isPlaying()) {
                    musicResultPlayer.seekTo(musicOffset);
                    videoResultPlayer.start();
                    videoResultTextureView.bringToFront();
                }
            }
        });
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallBack = (DataPassListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement DataPassListener");
        }
    }

    @Override
    public void onPause() {
        super.onPause();


        nowVideoNum = 0;
        flag = false;
    }

    private float deleteButtonLocation(float resultVideosTotalTime) {
        DisplayMetrics outMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        float widthPSec = (float) outMetrics.widthPixels / 15;
        float height = getResources().getDimension(R.dimen.resultbar_height);
        if (resultVideosTotalTime > 14000) {
            return (15) * widthPSec - height;

        } else {
            return (resultVideosTotalTime / 1000) * widthPSec - height;

        }
    }

    int progress = 0;

    Thread resultThread = new Thread(new Runnable() {
        @Override
        public void run() {

            while (flag) {
                try {

                    if (flag && musicResultPlayer.isPlaying()) {
                        resultThread.sleep(50);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (flag) {
                                    if (musicResultPlayer.getCurrentPosition() - musicOffset >= resultVideosTotalTime) {
                                        resultProgressBar.setTotalTime(0);
                                        resultProgressBar.setNowVideoTime(resultVideosTotalTime);
                                    } else {
                                        resultProgressBar.setTotalTime(0);
                                        resultProgressBar.setNowVideoTime(musicResultPlayer.getCurrentPosition() - musicOffset);
                                    }

                                    resultProgressBar.invalidate();
                                }
                            }
                        });
                        if (nowVideoNum % 2 == 0 && flag && videoResultPlayer.isPlaying()) {
                            Timber.v("run first: %d/%d", nowVideoNum, resultVideos.size());
                            if (videoResultPlayer.getCurrentPosition() >= resultVideos.get(nowVideoNum).getEnd()) {
                                videoResultPlayer.pause();
                                videoResultPlayer.stop();
                                Timber.v("run: isplaying %s", videoResultPlayer.isPlaying() ? "true" : "false");
                                nowVideoNum = nowVideoNum + 1;
                                isFirstNumChanged = true;
                                Timber.v("run: nowVideoNum %d", nowVideoNum);
                                if (nowVideoNum < resultVideos.size()) {
                                    //     Log.d(TAG, "run sec: " + nowVideoNum + " / " + resultVideos.size());
                                    videoResultPlayer2.start();
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            videoResultTextureView2.bringToFront();
                                        }
                                    });

                                    //   Log.d(TAG, "run third: " + nowVideoNum + " / " + resultVideos.size());
                                    if (nowVideoNum + 1 < resultVideos.size()) {
                                        videoResultPlayer.setFirst(false);
                                        prepareVideoPlayer(videoResultPlayer, nowVideoNum + 1, true);
                                    }
                                } else {
                                    isMusicComplition = true;
                                    musicResultPlayer.pause();
                                    Timber.v("run: isMusicResultPlayer playing? %s", musicResultPlayer.isPlaying() ? "true" : "false");
                                    nowVideoNum = 0;


                                    if (flag && resultVideos.size() > nowVideoNum && !musicResultPlayer.isPlaying()) {
                                        if (resultVideos.size() > nowVideoNum + 1) {
                                            videoResultPlayer2.setFirst(false);
                                            prepareVideoPlayer(videoResultPlayer2, nowVideoNum + 1, false);
                                        }
                                        if (videoResultPlayer.isPlaying()) {
                                            videoResultPlayer.pause();
                                            videoResultPlayer.stop();
                                        }
                                        videoResultPlayer.setFirst(false);
                                        prepareVideoPlayer(videoResultPlayer, nowVideoNum, true);

                                    }
                                    isMusicComplition = false;
                                }

                            }


                        } else if (nowVideoNum % 2 == 1 && flag && videoResultPlayer2.isPlaying()) {

                            if (videoResultPlayer2.getCurrentPosition() >= resultVideos.get(nowVideoNum).getEnd()) {
                                videoResultPlayer2.pause();
                                videoResultPlayer2.stop();
                                nowVideoNum = nowVideoNum + 1;
                                isSecondNumChanged = true;
                                Timber.d("run: nowVideoNum %d", nowVideoNum);
                                if (nowVideoNum < resultVideos.size()) {
                                    videoResultPlayer.start();
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            videoResultTextureView.bringToFront();
                                        }
                                    });
                                    if (nowVideoNum + 1 < resultVideos.size()) {
                                        videoResultPlayer2.setFirst(false);
                                        prepareVideoPlayer(videoResultPlayer2, nowVideoNum + 1, false);
                                    }
                                    //else videoplayer1 to start
                                } else {
                                    isMusicComplition = true;
                                    musicResultPlayer.pause();
                                    nowVideoNum = 0;

                                    if (flag && resultVideos.size() > nowVideoNum && !musicResultPlayer.isPlaying()) {
                                        if (resultVideos.size() > nowVideoNum + 1) {
                                            videoResultPlayer2.setFirst(false);
                                            prepareVideoPlayer(videoResultPlayer2, nowVideoNum + 1, false);
                                        }
                                        if (videoResultPlayer.isPlaying()) {
                                            videoResultPlayer.pause();
                                            videoResultPlayer.stop();
                                        }
                                        videoResultPlayer.setFirst(false);
                                        prepareVideoPlayer(videoResultPlayer, nowVideoNum, true);
                                    }
                                    isMusicComplition = false;
                                }
                            }

                        }
                    } else {

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (flag) {
                                    //remove progress bar
                                    resultProgressBar.setNowVideoTime(0);
                                    resultProgressBar.setTotalTime(0);
                                    resultProgressBar.invalidate();
                                }
                            }
                        });
                    }
                } catch (InterruptedException ie) {
                    Timber.w(ie, "thread got exception");
                }
            }

            if (videoResultPlayer != null) {
                if (flag && videoResultPlayer.isPlaying()) {
                    videoResultPlayer.pause();
                    videoResultPlayer.stop();
                }
                videoResultPlayer.release();
            }
            if (videoResultPlayer2 != null) {
                if (flag && videoResultPlayer2.isPlaying()) {
                    videoResultPlayer2.pause();
                    videoResultPlayer2.stop();
                }
                videoResultPlayer2.release();
            }
            if (musicResultPlayer != null) {
                if (flag && musicResultPlayer.isPlaying()) {
                    musicResultPlayer.pause();
                    musicResultPlayer.stop();

                }
                musicResultPlayer.release();
            }
        }
    });

    private void prepareVideoPlayer(EditorResultMediaPlayer mediaPlayer, int videoNum, boolean isPlayer1) {

        try {

            EditorVideo video = resultVideos.get(videoNum);
            FileInputStream fis = new FileInputStream(video.getVideoPath());
            FileDescriptor fd = fis.getFD();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(fd);
            mediaPlayer.setOffset(resultVideos.get(videoNum).getStart());
            mediaPlayer.prepare();
            if (isPlayer1) {
                videoResultTextureView.setResultVideo(video);
            } else {
                videoResultTextureView2.setResultVideo(video);
            }
            fis.close();

        } catch (FileNotFoundException e) {
            Timber.e(e, "m/prepareVideoPlayer e/FileNotFoundException");
        } catch (IOException e) {
            Timber.e(e, "m/prepareVideoPlayer e/IOException");
        }
    }

}
