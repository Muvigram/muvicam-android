package com.estsoft.muvigram.ui.library.videolibrary;

import android.content.ContentUris;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.estsoft.muvigram.R;
import com.estsoft.muvigram.model.Video;
import com.estsoft.muvigram.ui.library.videolibrary.injection.VideoLibraryScope;
import com.estsoft.muvigram.util.DialogFactory;
import com.estsoft.muvigram.util.ThumbnailImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static android.R.attr.thumbnail;

@VideoLibraryScope
public class VideoLibraryAdapter extends RecyclerView.Adapter<VideoLibraryAdapter.VideoViewHolder> {

  private List<Video> mVideos;

  private VideoLibraryFragment mFragment;

  @Inject
  public VideoLibraryAdapter() {
    mVideos = new ArrayList<>();
  }

  public void register(VideoLibraryFragment fragment) {
    mFragment = fragment;
  }

  public void deregister() {
    mFragment = null;
  }

  public void setVideos(List<Video> videos) {
    this.mVideos = videos;
  }

  public void clearVideos() {
    int size = this.mVideos.size();
    this.mVideos.clear();
    notifyItemRangeRemoved(0, size);
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

      Observable.just(video.id())
          .subscribeOn(Schedulers.io())
          .observeOn(Schedulers.io())
          .map((id) -> getThumbnail(id))
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(
              bitmap -> mVideoThumbnail.setImageBitmap(bitmap),
              Throwable::printStackTrace
          );
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
        mLayoutMain.setOnClickListener(v -> {
          if (video.isSelected()) onItemReleased(position);
          else onItemSelected(position);
        });
      } else { // unsupported aspect ratio (NOT 16:9)
        Timber.v("Unsupported video [%d:%d], %s", video.width(), video.height(), video.uri());
        mLayoutUnsupported.setVisibility(View.VISIBLE);
        mLayoutMain.setOnClickListener(v -> {
          DialogFactory.createSimpleOkErrorDialog(mFragment.getActivity(),
              R.string.library_video_title_text, R.string.library_video_item_unsupported)
              .show();
        });
      }
    }

    public VideoViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  private Bitmap getThumbnail(long id) {
    return MediaStore.Video.Thumbnails.getThumbnail(
        mFragment.getContext().getContentResolver(),
        id, MediaStore.Video.Thumbnails.MINI_KIND, null);
  }

//  TODO - More reasonable criteria is required.
//  private static boolean isFlexibleSupportedRatio(int w, int h) {
//    return Math.abs(w * 9 - h * 16) <= 16 || Math.abs(w * 16 - h * 9) <= 16;
//  }

  private static boolean isSupportedRatio(int w, int h) {
    return w * 9 == h * 16 || w * 16 == h * 9;
  }

  private static String getDurationFormat(long totalDurationMillisec) {
    long sec = totalDurationMillisec / 1000;
    return String.format(Locale.US, "%02d:%02d", sec / 60, sec % 60);
  }

  /* Update specific views */
  private void updateView() {
    //noinspection Convert2streamapi
    for (int pos : mSelectedPosition) {
      notifyItemChanged(pos);
    }
  }

  private void updateView(int removed) {
    //noinspection Convert2streamapi
    for (int pos : mSelectedPosition) {
      notifyItemChanged(pos);
    }
    notifyItemChanged(removed);
  }

  private void onItemSelected(int pos) {
    // push item
    pushPos(pos);

    // update view
    updateView();
  }

  private void onItemReleased(int pos) {
    // remove item
    removePos(pos);

    // update view
    updateView(pos);
  }

  public List<Video> getVideos() {
    List<Video> videos = new ArrayList<>();
    for (int i = 0; i < mSelectedPosition.size(); i++) {
      videos.add(mVideos.get(mSelectedPosition.get(i)));
    }
    return videos;
  }

  //

  private static final int MAX_SELECTION = 5;
  private List<Integer> mSelectedPosition = new ArrayList<>();

  private void pushPos(Integer pos) {
    if (isFull()) {
      Timber.d("Selected video array is full.");
      DialogFactory.createSimpleOkErrorDialog(mFragment.getActivity(),
          R.string.library_video_title_text, R.string.library_video_selection_is_full)
          .show();
      return;
    }

    int idx = mSelectedPosition.indexOf(pos);
    if (idx != -1) {
      Timber.w("m/pushPos Already matching item exists in the list.");
      return;
    }

    mVideos.get(pos).selected();
    mVideos.get(pos).setSelectionOrder(mSelectedPosition.size());

    mSelectedPosition.add(pos);
  }

  private void removePos(Integer pos) {
    if (isEmpty()) {
      Timber.d("Selected video array is empty.");
      return;
    }

    int idx = mSelectedPosition.indexOf(pos);
    if (idx == -1) {
      Timber.w("m/removePos There is no matching item in the list.");
      return;
    }

    mVideos.get(pos).released();
    for (int i = idx + 1; i < mSelectedPosition.size(); i++) {
      mVideos.get(mSelectedPosition.get(i)).decreaseSelectionOrder();
    }

    mSelectedPosition.remove(idx);
  }

  private boolean isEmpty() {
    return mSelectedPosition.size() == 0;
  }

  private boolean isFull() {
    return mSelectedPosition.size() == MAX_SELECTION;
  }
}
