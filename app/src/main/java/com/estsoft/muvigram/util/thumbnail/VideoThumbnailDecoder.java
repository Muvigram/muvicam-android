package com.estsoft.muvigram.util.thumbnail;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;

import java.io.File;
import java.io.IOException;

/**
 * Created by jaylim on 20/02/2017.
 */

final public class VideoThumbnailDecoder implements ResourceDecoder<File, Bitmap> {

  // be careful if you change the Generator implementation you have to change this
  // otherwise the users may see a cached image; or clear cache on app update
  private static final String ID = VideoThumbnailDecoder.class.getName();

  // Use Bitmap pool to avoid continuous allocation and de-allocation of memory in application
  // and reduce GC overhead so that the application will run more smooth.
  private final BitmapPool pool;

  public VideoThumbnailDecoder(Context context) {
    pool = Glide.get(context).getBitmapPool();
  }

  @Override
  public Resource<Bitmap> decode(File source, int width, int height) throws IOException {
    String path = source.getAbsolutePath();
    int kind = MediaStore.Video.Thumbnails.MINI_KIND;
    if (width <= 96 && height <= 96) {
      kind = MediaStore.Video.Thumbnails.MICRO_KIND;
    }
    // Decode bitmap
    Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(path, kind);

    // create and return the bitmap resource
    return BitmapResource.obtain(thumbnail, pool);
  }

  @Override
  public String getId() {
    return ID;
  }
}