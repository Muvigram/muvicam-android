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
import timber.log.Timber;

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
    holder.bindVideo(mVideos.get(position), position);
  }

  //
  public class VideoViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.library_video_item_layout_main)        RelativeLayout mLayoutMain;
    @BindView(R.id.library_video_item_layout_selected)    RelativeLayout mLayoutSelected;
    @BindView(R.id.library_video_item_layout_unsupported) RelativeLayout mLayoutUnsupported;

    @BindView(R.id.library_video_item_main_thumbnail) ThumbnailImageView mVideoThumbnail;
    @BindView(R.id.library_video_item_main_duration)  TextView mVideoDuration;
    @BindView(R.id.library_video_item_selected_order) TextView mSelectedOrder;

    public void bindVideo(Video video, int position) {
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
          onItemReleased(position);
        } else /* if not selected */ {
          onItemSelected(position);
        }

      });
    }

    public VideoViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  private static boolean isSupportedRatio(int w, int h) {
    return w * 9 == h * 16 || w * 16 == h * 9;
  }

  private static String getDurationFormat(long totalDurationMillisec) {
    long sec = totalDurationMillisec / 1000;
    return String.format(Locale.US, "%02d:%02d", sec / 60, sec % 60);
  }

  /* Update specific views */
  public void updateView() {
    for (int pos : mSelected) {
      notifyItemChanged(pos);
    }
  }

  public void updateView(int removed) {
    for (int pos : mSelected) {
      notifyItemChanged(pos);
    }
    notifyItemChanged(removed);
  }

  public void onItemSelected(int pos) {
    // push items
    pushPos(pos);

    // update view
    updateView();
  }

  public void onItemReleased(int pos) {
    // pop items
    removePos(pos);

    // update view
    updateView(pos);
  }

  public List<Video> getVideos() {
    return null;
  }

  private static final int MAX_SELECTION = 5;
  private List<Integer> mSelected = new ArrayList<>();

  private void pushPos(Integer pos) {
    if (isFull()) {
      Timber.e("Selected video array is full.");
      return;
    }

    int idx = mSelected.indexOf(pos);
    if (idx != -1) {
      Timber.e("Already matching item exists in the list.");
      return;
    }

    mVideos.get(pos).selected();
    mVideos.get(pos).setSelectionOrder(mSelected.size());

    mSelected.add(pos);
  }

  private void removePos(Integer pos) {
    if (isEmpty()) {
      Timber.e("Selected video array is empty.");
      return;
    }

    int idx = mSelected.indexOf(pos);
    if (idx == -1) {
      Timber.e("There is no matching item in the list.");
      return;
    }

    mVideos.get(pos).released();
    for (int i = idx + 1; i < mSelected.size(); i++) {
      mVideos.get(mSelected.get(i)).decreaseSelectionOrder();
    }

    mSelected.remove(idx);
  }

  private boolean isEmpty() {
    return mSelected.size() == 0;
  }

  private boolean isFull() {
    return mSelected.size() == MAX_SELECTION;
  }
}
