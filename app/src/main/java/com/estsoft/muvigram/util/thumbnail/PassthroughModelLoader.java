package com.estsoft.muvigram.util.thumbnail;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;

/**
 *
 * Created by jaylim on 20/02/2017.
 *
 * @param <R> Result
 * @param <S> Source
 */
final public class PassthroughModelLoader<R, S extends R> implements ModelLoader<S, R> {
  @Override
  public DataFetcher<R> getResourceFetcher(final S model, int width, int height) {
    return new CastingDataFetcher<>(model); // Intended up-casting
  }

  /**
   * Extremely unsafe, use with care.
   *
   * @param <R> Result
   * @param <S> Source
   */
  private static class CastingDataFetcher<S, R> implements DataFetcher<R> {
    private final S model;

    public CastingDataFetcher(S model) {
      this.model = model;
    }

    @SuppressWarnings("unchecked")
    @Override
    public R loadData(Priority priority) {
      return (R) model;
    }

    @Override
    public void cleanup() {
    }

    @Override
    public String getId() {
      return model.toString();
    }

    @Override
    public void cancel() {
    }
  }
}
