package com.estsoft.muvicam.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class EditorVideo implements Parcelable {

    private String videoPath;
    // will be changed real videothumbnail from sdcard;
    private Bitmap thumbnailBitmap;
    private long presentationTimeUs;
    private boolean resolutionacceptable;
    private boolean isSelected;
    private boolean isLast;
    private int numSelected;
    private long durationMiliSec;
    private String audioPath;
    private int start;
    private int end;



    public EditorVideo() {
        isSelected = false;
        numSelected = -1;
        resolutionacceptable = false;
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



    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(videoPath);
        parcel.writeLong(durationMiliSec);
        parcel.writeString(audioPath);
        parcel.writeInt(start);
        parcel.writeInt(end);
    }

    public void readFromParcel(Parcel in) {
        videoPath = in.readString();
        durationMiliSec = in.readLong();
        audioPath = in.readString();
        start = in.readInt();
        end = in.readInt();
    }

    public boolean getIsLast() {
        return isLast;
    }

    public void setIsLast(boolean isLast) {
        this.isLast = isLast;
    }

    public long getPresentationTimeUs() {
        return presentationTimeUs;
    }

    public void setPresentationTimeUs(long presentationTimeUs) {
        this.presentationTimeUs = presentationTimeUs;
    }

    public static final Parcelable.Creator CREATOR = new EditorVideoCreator();


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

    public boolean isResolutionacceptable() {
        return resolutionacceptable;
    }

    public void setResolutionacceptable(boolean resolutionacceptable) {
        this.resolutionacceptable = resolutionacceptable;
    }





}
