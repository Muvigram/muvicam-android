package com.estsoft.muvicam.util;

import android.database.Cursor;

import rx.Observable;

/**
 * Created by jaylim on 12/13/2016.
 */

public class CursorObservable {

  public static Observable<Cursor> create(Cursor cursor) {
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
        if (!cursor.isClosed()) {
          cursor.close();
        }
      }
    });
  }


}
