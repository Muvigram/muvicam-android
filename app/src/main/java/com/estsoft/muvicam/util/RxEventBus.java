package com.estsoft.muvicam.util;

import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by jaylim on 26/01/2017.
 */

public class RxEventBus {

  private PublishSubject<Object> mSbject = PublishSubject.create();

  private final Subject<Object, Object> mBusSubject = new SerializedSubject<>(PublishSubject.create());

  public <T> Subscription register(final Class<T> eventClass, Action1<T> onNext) {
    return mBusSubject
        .filter(event -> event.getClass().equals(eventClass))
        .map(obj -> (T) obj)
        .subscribe(onNext);
  }

  public void post(Object event) {
    mBusSubject.onNext(event);
  }
}
