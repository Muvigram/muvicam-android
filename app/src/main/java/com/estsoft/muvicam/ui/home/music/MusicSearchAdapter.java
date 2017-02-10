package com.estsoft.muvicam.ui.home.music;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.Music;
import com.estsoft.muvicam.ui.home.HomeActivity;
import com.estsoft.muvicam.ui.home.music.injection.MusicScope;

/**
 * Music search adapter
 * Created by jaylim on 12/12/2016.
 */

@MusicScope
public class MusicSearchAdapter extends RecyclerView.Adapter<MusicSearchAdapter.MusicViewHolder> {

  private List<Music> mMusics;

  @Inject
  public MusicSearchAdapter() {
    mMusics = new ArrayList<>();
  }

  public void setMusics(List<Music> musics) {
    mMusics = musics;
  }

  @Override
  public MusicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_library_music, parent, false);
    return new MusicViewHolder(itemView);

  }

  @Override
  public void onBindViewHolder(MusicViewHolder holder, int position) {
    Music music = mMusics.get(position);
    holder.bindMusic(music);
  }

  @Override
  public int getItemCount() {
    return mMusics.size();
  }

  class MusicViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.music_item)
    RelativeLayout mMusicButton;
    @BindView(R.id.music_item_thumbnail)
    ImageView mThumbnail;
    @BindView(R.id.music_item_title)
    TextView mTitle;
    @BindView(R.id.music_item_artist)
    TextView mArtist;

    public void bindMusic(Music music) {
      if (music.thumbnail() != null) {
        mThumbnail.setImageBitmap(music.thumbnail());
      } else {
        mThumbnail.setImageResource(R.drawable.music_item_no_album_art);
      }
      mTitle.setText(music.title());
      mArtist.setText(music.artist());
      mMusicButton.setOnClickListener(v ->
          HomeActivity.get(mMusicButton).selectMusic(music));
    }

    public MusicViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
