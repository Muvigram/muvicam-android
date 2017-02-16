package com.estsoft.muvigram.util;

import android.database.Cursor;
import android.support.annotation.Nullable;

import rx.Observable;

/**
 * Created by jaylim on 12/13/2016.
 */

public class CursorObservable {

  public static Observable<Cursor> create(@Nullable Cursor cursor) {
    if (cursor == null || !cursor.moveToFirst()) {
      return Observable.empty();
    }

    return Observable.<Cursor>create(sub -> {
      if (sub.isUnsubscribed()) return;
      try {
        do {
          if (sub.isUnsubscribed()) {
            sub.onCompleted();
            return;
          }
          sub.onNext(cursor);
        } while (cursor.moveToNext());

        sub.onCompleted();

      } catch (Exception e) {
        sub.onError(e);
      }
    }).doOnCompleted(cursor::close);
  }


}
