package com.estsoft.muvicam.util;

import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import rx.Observable;

/**
 * Created by estsoft on 2017-02-03.
 */

public class CursorObservableVideo {
    private static final String TAG = "CursorObservableVideo";

    public static Observable<Cursor> create(Cursor cursor, boolean autoClose) {

        return Observable.create(sub -> {
            if (sub.isUnsubscribed()) return;
            if(cursor == null || !cursor.moveToFirst()){
                sub.onCompleted();
                return;
            }
            try {

                do {
                    if (sub.isUnsubscribed()) {
                        sub.onCompleted();
                        return;
                    }
                    sub.onNext( cursor );
                } while( cursor.moveToNext() );

                sub.onCompleted();
            } catch (Exception e) {
                sub.onError(e);
            } finally {
                if (autoClose && !cursor.isClosed()) {
                    cursor.close();
                }
            }
        });
    }

}
