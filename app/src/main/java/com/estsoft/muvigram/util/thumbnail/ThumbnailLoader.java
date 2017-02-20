package com.estsoft.muvigram.util.thumbnail;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.BitmapEncoder;
import com.bumptech.glide.load.resource.bitmap.StreamBitmapDecoder;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;
import com.bumptech.glide.load.resource.transcode.BitmapToGlideDrawableTranscoder;
import com.estsoft.muvigram.R;
import com.estsoft.muvigram.model.Music;
import com.estsoft.muvigram.util.thumbnail.MusicThumbnailDecoder;
import com.estsoft.muvigram.util.thumbnail.PassthroughModelLoader;
import com.estsoft.muvigram.util.thumbnail.UriToFileModelLoader;
import com.estsoft.muvigram.util.thumbnail.VideoThumbnailDecoder;

import java.io.File;

/**
 * Created by jaylim on 20/02/2017.
 */

final public class ThumbnailLoader {

  public static GenericRequestBuilder<Uri, Uri, Bitmap, GlideDrawable> musicThumbnailLoader(Context context) {
    return Glide
        .with(context)

        .using(new PassthroughModelLoader<>(), Uri.class) // Model: Uri -> Data: File
        .from(Uri.class)


        .as(Bitmap.class) // Resource: Bitmap -> Transcode: GlideDrawable
        .transcode(new BitmapToGlideDrawableTranscoder(context), GlideDrawable.class)

        .decoder(new MusicThumbnailDecoder(context)) // Data: File -> Resource: Bitmap
        .encoder(new BitmapEncoder(Bitmap.CompressFormat.PNG, 0))

        .cacheDecoder(new FileToStreamDecoder<>(new StreamBitmapDecoder(context)))

        .placeholder(R.drawable.music_item_loading)  // pre-set placeholder
        .error(R.drawable.music_item_no_album_art)        // on error behavior
        //.diskCacheStrategy(DiskCacheStrategy.NONE)  // For debugging
        //.skipMemoryCache(true)                      // For debugging
        ;
  }

  public static Bitmap getThumbnail(Music music) {
    MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
    metaRetriever.setDataSource(music.uri().toString());
    byte[] art = metaRetriever.getEmbeddedPicture();
    if (art == null) {
      return null;
    }
    return BitmapFactory.decodeByteArray(art, 0, art.length);
  }

  public static GenericRequestBuilder<Uri, File, Bitmap, GlideDrawable> videoThumbnailLoader(Context context) {
    return Glide
        .with(context)

        .using(new UriToFileModelLoader(), File.class) // Model: Uri -> Data: File
        .from(Uri.class)

        .as(Bitmap.class) // Resource: Bitmap -> Transcode: GlideDrawable
        .transcode(new BitmapToGlideDrawableTranscoder(context), GlideDrawable.class)

        .decoder(new VideoThumbnailDecoder(context)) // Data: File -> Resource: Bitmap
        .encoder(new BitmapEncoder(Bitmap.CompressFormat.PNG, 0))

        .cacheDecoder(new FileToStreamDecoder<Bitmap>(new StreamBitmapDecoder(context)))

        .placeholder(R.drawable.music_item_loading)  // pre-set placeholder
        .error(R.drawable.music_item_no_album_art)        // on error behavior
        //.diskCacheStrategy(DiskCacheStrategy.NONE)  // For debugging
        //.skipMemoryCache(true)                      // For debugging
        ;
  }
}
