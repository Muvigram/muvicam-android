package com.estsoft.muvigram.util.thumbnail;

import android.net.Uri;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;

import java.io.File;

/**
 * Created by jaylim on 20/02/2017.
 */

final public class UriToFileModelLoader implements ModelLoader<Uri, File> {
  @Override
  public DataFetcher<File> getResourceFetcher(Uri model, int width, int height) {
    return new CastingDataFetcher(model);
  }

  private static class CastingDataFetcher implements DataFetcher<File> {
    private final File data;

    public CastingDataFetcher(Uri model) {
      this.data = new File(model.toString());
    }

    @Override
    public File loadData(Priority priority) {
      return data;
    }

    @Override
    public void cleanup() {
    }

    @Override
    public String getId() {
      return data.getAbsolutePath();
    }

    @Override
    public void cancel() {
    }
  }
}
