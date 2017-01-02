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
    cursor.moveToFirst();
    return Observable.create(sub -> {
      if (sub.isUnsubscribed()) return;

      try {
        while (cursor.moveToNext()) {
          if (sub.isUnsubscribed()) return;
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
