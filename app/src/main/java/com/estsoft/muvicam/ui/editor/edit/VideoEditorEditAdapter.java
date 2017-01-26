package com.estsoft.muvicam.ui.editor.edit;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.EditorVideo;
import com.estsoft.muvicam.ui.library.videolibrary.ThumbnailImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017-01-10.
 */

public class VideoEditorEditAdapter extends RecyclerView.Adapter<VideoEditorEditAdapter.ViewHolder> {
    String TAG = "";
    private final FragmentActivity mActivity;
    OnItemClickListener itemClickListener;
    List<EditorVideo> videoThumbnails = new ArrayList<>();
    private EditorVideo nowVideo;

    int selectedFragmentNum;

    public VideoEditorEditAdapter(FragmentActivity fragmentActivity, int selectedFragmentNum, EditorVideo nowVideo, List<EditorVideo> videoThumbnails) {
        mActivity = fragmentActivity;
        this.nowVideo = nowVideo;
        this.videoThumbnails = videoThumbnails;
        this.selectedFragmentNum = selectedFragmentNum;
    }

    @Override
    public VideoEditorEditAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
        View view = mInflater.inflate(R.layout.layout_recyclerview_editor_video, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return videoThumbnails.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.thumbnailImageView.setImageBitmap(videoThumbnails.get(position).getThumbnailBitmap());

    }

    public void addVideo(EditorVideo myVideoThumbnail) {
        videoThumbnails.add(myVideoThumbnail);
        notifyDataSetChanged();
    }

    //
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ThumbnailImageView thumbnailImageView;

        public ViewHolder(View view) {
            super(view);
            thumbnailImageView = (ThumbnailImageView) view.findViewById(R.id.video_edit_thumbnail);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //        videoEditorFragment.setResultClicked(false);
            //       videoEditorFragment.setEditClicked(true);
            //       mActivity.findViewById(R.id.video_result).setBackgroundColor(ContextCompat.getColor(mActivity, R.color.colorEditBackground));
            if (itemClickListener != null) {
                itemClickListener.onItemClick(view, getAdapterPosition());
            }
            //           if (!videoEditorFragment.videoRunningThread.isAlive() && !videoEditorFragment.isThreadRun()) {
            //               videoEditorFragment.setThreadRun(true);
            //               videoEditorFragment.videoRunningThread.start();
            //           }

        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {

        this.itemClickListener = itemClickListener;
    }

    public EditorVideo getNowVideo() {
        return nowVideo;
    }

    public int getSelectedFragmentNum() {
        return selectedFragmentNum;
    }
}
