package com.estsoft.muvicam.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class EditorVideo implements Parcelable {
    private static int index = 0;
    private static final String TAG = "EditorVideo";

    private String videoPath;
    private int durationMiliSec;
    private int start;
    private int end;

    // will be changed real videothumbnail from sdcard;
    private Bitmap thumbnailBitmap;
    private int presentationTimeUs;
    private boolean resolutionAcceptable;
    private boolean isSelected;
    private boolean isLast;
    private int numSelected;



    public EditorVideo() {
        index ++;
        Log.d(TAG, "EditorVideo: " + index);
        isSelected = false;
        numSelected = -1;
        resolutionAcceptable = false;
        start = 0;
        end = 0;

    }

    public EditorVideo(Parcel in) {
        readFromParcel(in);

    }

    public EditorVideo(Bitmap thumbnailBitmap, int presentationTimeUs, boolean isLast, int numSelected) {
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

    public int getDurationMiliSec() {
        return durationMiliSec;
    }

    public void setDurationMiliSec(int durationMiliSec) {
        this.durationMiliSec = durationMiliSec;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(videoPath);

        parcel.writeInt(start);
        parcel.writeInt(end);
        parcel.writeInt(durationMiliSec);
    }

    public void readFromParcel(Parcel in) {
        videoPath = in.readString();
    start = in.readInt();
    end = in.readInt();
        durationMiliSec = in.readInt();
}

    public boolean getIsLast() {
        return isLast;
    }

    public void setIsLast(boolean isLast) {
        this.isLast = isLast;
    }

    public int getPresentationTimeUs() {
        return presentationTimeUs;
    }

    public void setPresentationTimeUs(int presentationTimeUs) {
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

    public boolean isResolutionAcceptable() {
        return resolutionAcceptable;
    }

    public void setResolutionAcceptable(boolean resolutionAcceptable) {
        this.resolutionAcceptable = resolutionAcceptable;
    }

    @Override
    public String toString() {
        return "start: "+start+", end:"+end+", path: "+videoPath;
    }
}
