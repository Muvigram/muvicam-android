package com.estsoft.muvigram.util;

import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by jaylim on 13/02/2017.
 */

public class BitmapViewUtil {
  public static void clearViewGroup(View view) {
    if(view instanceof ImageView) {
      BitmapDrawable drawable = (BitmapDrawable) ((ImageView) view).getDrawable();
      drawable.getBitmap().recycle();
    }

    if (view instanceof ViewGroup) {
      for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
        clearViewGroup(((ViewGroup) view).getChildAt(i));
      }
      ((ViewGroup) view).removeAllViews();
    }
  }
}
