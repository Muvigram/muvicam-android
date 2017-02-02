package com.estsoft.muvicam.ui.selector.videoselector.legacy;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.EditorVideo;
import com.estsoft.muvicam.ui.selector.videoselector.ThumbnailImageView;
import com.estsoft.muvicam.ui.selector.videoselector.VideoSelectorAdapterContract;
import com.estsoft.muvicam.ui.selector.videoselector.VideoSelectorAdaptor;

import java.util.ArrayList;
import java.util.List;


public class VideoSelectorAdapter extends RecyclerView.Adapter<VideoSelectorAdapter.ViewHolder> implements VideoSelectorAdapterContract.View, VideoSelectorAdapterContract.Model {
    String TAG = "VideoSelectorAdapter";
    private final FragmentActivity mActivity;
    OnItemClickListener itemClickListener;
    List<EditorVideo> thumbnailImageViews;

    public VideoSelectorAdapter(FragmentActivity fragmentActivity) {
        mActivity = fragmentActivity;
    }

    @Override
    public VideoSelectorAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
        View view = mInflater.inflate(R.layout.layout_recyclerview_selector_video, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        return thumbnailImageViews.size() + 3;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.space.setImageResource(R.drawable.editor_picker_white);
        holder.toShowTime.setImageResource(R.drawable.editor_picker_toshowtime);
        if (position < 3) {
            holder.space.setVisibility(View.VISIBLE);
            holder.layoutThumbnail.setVisibility(View.GONE);
            holder.layoutSelected.setVisibility(View.GONE);
            holder.hide.setVisibility(View.GONE);
        } else {
            holder.videoThumbnail.setImageBitmap(thumbnailImageViews.get(position - 3).getThumbnailBitmap());
            holder.videoRunningTime.setText(getDurationFormat(thumbnailImageViews.get(position - 3).getDurationMiliSec()));
            holder.layoutThumbnail.setVisibility(View.VISIBLE);
            holder.space.setVisibility(View.GONE);

            if (thumbnailImageViews.get(position - 3).isSelected()) {
                holder.videoNum.setText("" + thumbnailImageViews.get(position - 3).getNumSelected());
                holder.layoutSelected.setVisibility(View.VISIBLE);
            } else {
                holder.layoutSelected.setVisibility(View.GONE);

            }
            if (!thumbnailImageViews.get(position - 3).isResolutionAcceptable()) {
                holder.hide.setVisibility(View.VISIBLE);
            } else {
                holder.hide.setVisibility(View.GONE);
            }

        }
    }

    //
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ThumbnailImageView videoThumbnail, toShowTime;
        ImageView space;
        View selected;
        FrameLayout hide;
        TextView videoNum, videoRunningTime;
        RelativeLayout layoutSelected, layoutThumbnail;

        public ViewHolder(View view) {
            super(view);
            layoutThumbnail = (RelativeLayout) view.findViewById(R.id.layout_thumbnail);
            videoThumbnail = (ThumbnailImageView) view.findViewById(R.id.video_thumbnail);
            toShowTime = (ThumbnailImageView) view.findViewById(R.id.to_show_time);
            videoRunningTime = (TextView) view.findViewById(R.id.video_running_time);

            layoutSelected = (RelativeLayout) view.findViewById(R.id.layout_selected);
            videoNum = (TextView) view.findViewById(R.id.video_num);
            space = (ImageView) view.findViewById(R.id.video_thumbnail_space);
            selected = view.findViewById(R.id.video_selected);
            hide = (FrameLayout) view.findViewById(R.id.video_hided);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(view, getAdapterPosition());
            }

        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }


    private String getDurationFormat(long durationTotalMSecond) {
        long durationTSec = durationTotalMSecond/1000;
        long durationMin = durationTSec / 60;
        long durationSec = durationTSec % 60;
        String durationMinString = "";
        String durationSecString = "";

        if (durationMin > 0 && durationMin < 10) {
            durationMinString += "0" + durationMin;
        } else if (durationMin == 0) {
            durationMinString += "00";
        } else {
            durationMinString += durationMin;
        }

        if (durationSec > 0 && durationSec < 10) {
            durationSecString += ":0" + durationSec;
        } else if (durationSec == 0) {
            durationSecString += ":00";
        } else {
            durationSecString += ":" + durationSec;
        }


        return durationMinString + durationSecString;
    }

    @Override
    public void clearItem() {
        if (thumbnailImageViews != null) {
            thumbnailImageViews.clear();
            notifyAdapter();
        }
    }

    @Override
    public EditorVideo getItem(int position) {
        return thumbnailImageViews.get(position);
    }

    @Override
    public void notifyAdapter() {
        notifyDataSetChanged();
    }


    @Override
    public void setOnClickListener(VideoSelectorAdaptor.OnItemClickListener clickListener) {
//        this.itemClickListener = clickListener;
    }

    @Override
    public ArrayList<EditorVideo> getItems() {
        return (ArrayList<EditorVideo>) thumbnailImageViews;
    }

    @Override
    public void addItems(List<EditorVideo> items) {
        this.thumbnailImageViews = items;
        notifyAdapter();
    }

    @Override
    public void notifyDataListChanged() {
        notifyAdapter();
    }
}
