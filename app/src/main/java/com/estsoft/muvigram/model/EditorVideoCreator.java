package com.estsoft.muvigram.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2016-12-08.
 */
public class EditorVideoCreator implements Parcelable.Creator<EditorVideo> {

    @Override
    public EditorVideo[] newArray(int i) {
        return new EditorVideo[i];
    }

    @Override
    public EditorVideo createFromParcel(Parcel parcel) {
        return new EditorVideo(parcel);
    }
}
