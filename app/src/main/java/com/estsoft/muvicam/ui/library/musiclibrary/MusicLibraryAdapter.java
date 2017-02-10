package com.estsoft.muvicam.ui.library.musiclibrary;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.Music;
import com.estsoft.muvicam.ui.library.musiclibrary.injection.MusicLibraryScope;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by jaylim on 10/01/2017.
 */

@MusicLibraryScope
public class MusicLibraryAdapter extends RecyclerView.Adapter<MusicLibraryAdapter.MusicLibraryViewHolder> {

  private List<Music> mMusics;
  private MusicLibraryFragment mFragment;
  @Inject
  public MusicLibraryAdapter() {
    mMusics = new ArrayList<>();
  }

  public void register(MusicLibraryFragment fragment) {
    mFragment = fragment;
  }

  public void setMusics(List<Music> musics) {
    mMusics = musics;
  }

  @Override
  public MusicLibraryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_library_music, parent, false);
    return new MusicLibraryViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(MusicLibraryViewHolder holder, int position) {
    Music music = mMusics.get(position);
    holder.bindMusic(music);
  }

  @Override
  public int getItemCount() {
    return mMusics.size();
  }

  class MusicLibraryViewHolder extends RecyclerView.ViewHolder {
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
      mMusicButton.setOnClickListener(v -> mFragment.showMusicCutDialog(music));
    }

    public MusicLibraryViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

}
