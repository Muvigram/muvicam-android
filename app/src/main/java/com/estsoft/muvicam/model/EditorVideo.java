package com.estsoft.muvicam.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class EditorVideo implements Parcelable {

    private String videoPath;
    private long durationMiliSec;
    private int start;
    private int end;

    // will be changed real videothumbnail from sdcard;
    private Bitmap thumbnailBitmap;
    private long presentationTimeUs;
    private boolean resolutionAcceptable;
    private boolean isSelected;
    private boolean isLast;
    private int numSelected;



    public EditorVideo() {
        isSelected = false;
        numSelected = -1;
        resolutionAcceptable = false;
        start = 0;
        end = 0;
    }

    public EditorVideo(Parcel in) {
        readFromParcel(in);

    }

    public EditorVideo(Bitmap thumbnailBitmap, long presentationTimeUs, boolean isLast, int numSelected) {
        this.thumbnailBitmap = thumbnailBitmap;
        this.presentationTimeUs = presentationTimeUs;
        this.isLast = isLast;
        this.numSelected = numSelected;
    }

    public Bitmap getThumbnailBitmap() {
        return thumbnailBitmap;
    }

    public void setThumbnailBitmap(Bitmap thumbnailBitmap) {
        this.thumbnailBitmap = thumbnailBitmap;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }


    public int getNumSelected() {
        return numSelected;
    }

    public void setNumSelected(int numSelected) {
        this.numSelected = numSelected;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public long getDurationMiliSec() {
        return durationMiliSec;
    }

    public void setDurationMiliSec(long durationMiliSec) {
        this.durationMiliSec = durationMiliSec;
    }



    public boolean isLast() {
        return isLast;
    }

    public void setLast(boolean last) {
        isLast = last;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public boolean isResolutionAcceptable() {
        return resolutionAcceptable;
    }

    public void setResolutionAcceptable(boolean resolutionAcceptable) {
        this.resolutionAcceptable = resolutionAcceptable;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(videoPath);
        parcel.writeLong(durationMiliSec);
        parcel.writeInt(start);
        parcel.writeInt(end);
    }

    public void readFromParcel(Parcel in) {
        videoPath = in.readString();
        durationMiliSec = in.readLong();
        start = in.readInt();
        end = in.readInt();
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator<EditorVideo>(){
        @Override
        public EditorVideo[] newArray(int i) {
            return new EditorVideo[i];
        }

        @Override
        public EditorVideo createFromParcel(Parcel parcel) {
            return new EditorVideo(parcel);
        }
    };
}
