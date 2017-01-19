package com.estsoft.muvicam.ui.editor.result;

import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.EditorVideo;

import java.util.List;

public class VideoEditSelectedNumberAdapter extends RecyclerView.Adapter<VideoEditSelectedNumberAdapter.ViewHolder> {
    String TAG = "EditSelectedNumAdapter";
    private final FragmentActivity mActivity;
    OnItemClickListener itemClickListener;
    List<EditorVideo>selectedVideos, resultVideos;

    public VideoEditSelectedNumberAdapter(FragmentActivity fragmentActivity, List<EditorVideo>selectedVideos, OnItemClickListener itemClickListener) {
        mActivity = fragmentActivity;
        this.selectedVideos = selectedVideos;
    this.itemClickListener = itemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
        View view = mInflater.inflate(R.layout.layout_recyclerview_video_numbers, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {

        return selectedVideos.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int fragmentNumber = position + 1;
        Log.d(TAG, "onBindViewHolder: "+fragmentNumber);
        holder.videoNumber.setText("" + fragmentNumber);
        holder.videoNumber.setTextColor(ContextCompat.getColor(mActivity, R.color.textWhite));
        // TODO - caution when merging.
      }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        //   ImageView thumbnailImageView;
        TextView videoNumber;

        public ViewHolder(View view) {
            super(view);
            videoNumber = (TextView) view.findViewById(R.id.editor_result_video_num);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //go to selected fragment

            if (itemClickListener != null) {
                itemClickListener.onItemClick(view, getAdapterPosition());

            }

        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {

        this.itemClickListener = itemClickListener;
    }

}
