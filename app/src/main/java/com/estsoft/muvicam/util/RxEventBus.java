package com.estsoft.muvicam.util;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by jaylim on 26/01/2017.
 */

public class RxEventBus {

  private final Subject<Event, Event> mBusSubject = new SerializedSubject<>(PublishSubject.create());

  public Observable<Event> register() {
    return mBusSubject;
  }

  public <T extends Event> Subscription register(final Class<T> eventClass, Action1<T> onNext, Action1<Throwable> onError, Action0 onComplete) {
    //noinspection unchecked
    return mBusSubject
        .filter(event -> event.getClass().equals(eventClass))
        .map(event -> (T) event)
        .subscribe(onNext, onError, onComplete);
  }

  public void unregister(Subscription subscription) {
    RxUtil.unsubscribe(subscription);
  }

  public void post(Event event) {
    mBusSubject.onNext(event);
  }

  /////

//  private final Subject<Object, Object> bus = new SerializedSubject<>(PublishSubject.create());
//
//  public void send(Object o) {
//    bus.onNext(o);
//  }
//
//  public Observable<Object> toObserverable() {
//    return bus;
//  }
//
//  public boolean hasObservers() {
//    return bus.hasObservers();
//  }

  public static class Event {

  }
}
