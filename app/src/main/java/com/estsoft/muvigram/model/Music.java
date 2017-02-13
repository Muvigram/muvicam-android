package com.estsoft.muvigram.model;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

/**
 *
 * Created by jaylim on 12/13/2016.
 */

@AutoValue
public abstract class Music implements Comparable<Music>, Parcelable {

  public abstract Uri uri();
  public abstract String title();
  public abstract String artist();
  @Nullable public abstract Bitmap thumbnail();

  public static Music create(Music music) {
    return music;
  }

  public static Builder builder() {
    return new AutoValue_Music.Builder();
  }

  public static TypeAdapter<Music> typeAdapter(Gson gson) {
    return new AutoValue_Music.GsonTypeAdapter(gson);
  }


  @Override
  public int compareTo(@NonNull Music another) {
    return uri().compareTo(another.uri());
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setUri(Uri uri);
    public abstract Builder setTitle(String title);
    public abstract Builder setArtist(String artist);
    public abstract Builder setThumbnail(Bitmap thumbnail);
    public abstract Music build();
  }
}
