package com.estsoft.muvicam.model;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

/**
 * Created by jaylim on 12/01/2017.
 */

@AutoValue
public abstract class Video implements Comparable<Video>, Parcelable {

  private boolean isSelected = false;
  private int selectionOrder = -1;

  public boolean isSelected() {
    return isSelected;
  }

  public void selected() {
    isSelected = true;
  }

  public void released() {
    isSelected = false;
  }

  public int getSelectionOrder() {
    return selectionOrder;
  }

  public void setSelectionOrder(int selectionOrder) {
    this.selectionOrder = selectionOrder;
  }

  public abstract Uri uri();
  public abstract int width();
  public abstract int height();
  public abstract int duration();
  @Nullable public abstract Bitmap thumbnail();

  public static Video create(Video video) {
    return video;
  }

  public static Builder builder() {
    return new AutoValue_Video.Builder();
  }

  public static TypeAdapter<Video> typeAdapter(Gson gson) {
    return new AutoValue_Video.GsonTypeAdapter(gson);
  }


  @Override
  public int compareTo(@NonNull Video another) {
    return uri().compareTo(another.uri());
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setUri(Uri uri);
    public abstract Builder setWidth(int width);
    public abstract Builder setHeight(int height);
    public abstract Builder setDuration(int duration);
    public abstract Builder setThumbnail(Bitmap thumbnail);
    public abstract Video build();
  }
}
