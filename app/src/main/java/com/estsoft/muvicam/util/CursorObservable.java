package com.estsoft.muvicam.util;

import android.database.Cursor;

import java.util.concurrent.Semaphore;

import rx.Observable;

/**
 *
 * Created by jaylim on 12/13/2016.
 */

public class CursorObservable {

  public static Observable<Cursor> create(Cursor cursor, boolean autoClose) {

    return Observable.create(sub -> {
      if (sub.isUnsubscribed()) return;
      if(cursor == null || !cursor.moveToFirst()){
        sub.onCompleted();
        return;
      }
      try {
        while (cursor.moveToNext()) {
          sub.onNext(cursor);
        }
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
