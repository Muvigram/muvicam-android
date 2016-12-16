package com.estsoft.muvicam.util;

import rx.Subscription;

/**
 * Created by jaylim on 12/13/2016.
 */

public class RxUtil {

  public static void unsubscribe(Subscription subscription) {
    if (subscription != null && !subscription.isUnsubscribed()) {
      subscription.unsubscribe();
    }
  }
}
