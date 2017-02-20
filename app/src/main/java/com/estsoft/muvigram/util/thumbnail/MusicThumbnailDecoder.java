package com.estsoft.muvigram.util.thumbnail;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;

import java.io.IOException;

/**
 * Created by jaylim on 20/02/2017.
 */

final public class MusicThumbnailDecoder implements ResourceDecoder<Uri, Bitmap> {

  // be careful if you change the Generator implementation you have to change this
  // otherwise the users may see a cached image; or clear cache on app update
  private static final String ID = MusicThumbnailDecoder.class.getName();

  // Use Bitmap pool to avoid continuous allocation and de-allocation of memory in application
  // and reduce GC overhead so that the application will run more smooth.
  private final BitmapPool pool;

  public MusicThumbnailDecoder(Context context) {
    pool = Glide.get(context).getBitmapPool();
  }

  @Override
  public Resource<Bitmap> decode(Uri source, int width, int height) throws IOException {

    MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
    metaRetriever.setDataSource(source.toString());
    byte[] art = metaRetriever.getEmbeddedPicture();
    if (art == null) {
      throw new IOException("There is no album art.");
    }
    Bitmap thumbnail = BitmapFactory.decodeByteArray(art, 0, art.length);

    // create and return the bitmap resource
    return BitmapResource.obtain(thumbnail, pool);
  }

  @Override
  public String getId() {
    return ID;
  }
}