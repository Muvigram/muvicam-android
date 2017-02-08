package com.estsoft.muvicam.ui.library.videolibrary;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.Video;
import com.estsoft.muvicam.ui.library.videolibrary.injection.VideoLibraryScope;
import com.estsoft.muvicam.util.ThumbnailImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

@VideoLibraryScope
public class VideoSelectorAdapter extends RecyclerView.Adapter<VideoSelectorAdapter.VideoViewHolder> {

  private List<Video> mVideos;

  private VideoLibraryFragment mFragment;

  @Inject
  public VideoSelectorAdapter() {
    mVideos = new ArrayList<>();
  }

  public void register(VideoLibraryFragment fragment) {
    mFragment = fragment;
  }

  public void setVideos(List<Video> videos) {
    this.mVideos = videos;
  }

  @Override
  public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
    View view = mInflater.inflate(R.layout.item_library_video, parent, false);
    return new VideoViewHolder(view);
  }

  @Override
  public long getItemId(int position) {
    return super.getItemId(position);
  }

  @Override
  public int getItemCount() {
    return mVideos.size();
  }

  @Override
  public void onBindViewHolder(VideoViewHolder holder, int position) {
    holder.bindVideo(mVideos.get(position));
  }

  //
  public class VideoViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.library_video_item_layout_main)        RelativeLayout mLayoutMain;
    @BindView(R.id.library_video_item_layout_selected)    RelativeLayout mLayoutSelected;
    @BindView(R.id.library_video_item_layout_unsupported) RelativeLayout mLayoutUnsupported;

    @BindView(R.id.library_video_item_main_thumbnail)
    ThumbnailImageView mVideoThumbnail;
    @BindView(R.id.library_video_item_main_duration)  TextView mVideoDuration;
    @BindView(R.id.library_video_item_selected_order) TextView mSelectedOrder;

    public void bindVideo(Video video) {
      // Main frame
      mLayoutMain.setVisibility(View.VISIBLE);
      if (video.thumbnail() != null) {
        mVideoThumbnail.setImageBitmap(video.thumbnail());
      }
      mVideoDuration.setText(getDurationFormat(video.duration()));

      // Selected frame
      if (video.isSelected()) {
        mLayoutSelected.setVisibility(View.VISIBLE);
        mSelectedOrder.setText(String.valueOf(video.getSelectionOrder() + 1));
      } else {
        mLayoutSelected.setVisibility(View.INVISIBLE);
      }

      // Supported aspect ratio (16:9)
      if (isSupportedRatio(video.width(), video.height())) {
        mLayoutUnsupported.setVisibility(View.INVISIBLE);
      } else { // unsupported aspect ratio (NOT 16:9)
        mLayoutUnsupported.setVisibility(View.VISIBLE);
      }

      mLayoutMain.setOnClickListener(v -> {
        if (video.isSelected()) {
          mFragment.getPresenter().onItemReleased(video);
        } else /* if not selected */ {
          mFragment.getPresenter().onItemSelected(video);
        }

      });
    }

    public VideoViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  public void updateView(List<Video> videos) {
    int pos;
    for (int i = 0; i < videos.size(); i++) {
      Video video = videos.get(i);
      pos = mVideos.indexOf(video);
      notifyItemChanged(pos);
    }
  }

  private static boolean isSupportedRatio(int w, int h) {
    return w * 9 == h * 16 || w * 16 == h * 9;
  }

  private static String getDurationFormat(long totalDurationMillisec) {
    long sec = totalDurationMillisec / 1000;
    return String.format(Locale.US, "%02d:%02d", sec / 60, sec % 60);
  }

}
