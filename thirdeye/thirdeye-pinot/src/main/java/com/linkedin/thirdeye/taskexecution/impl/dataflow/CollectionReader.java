package com.linkedin.thirdeye.taskexecution.impl.dataflow;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class CollectionReader<T> implements Reader<T> {
  private Collection<T> storage;
  private Iterator<T> iterator;

  public CollectionReader() {
  }

  public CollectionReader(Collection<T> storage) {
    Preconditions.checkNotNull(storage);
    this.storage = storage;
  }

  @Override
  public boolean hasNext() {
    initIterator();
    return iterator.hasNext();
  }

  @Override
  public T next() {
    initIterator();
    return iterator.next();
  }

  private void initIterator() {
    if (iterator == null) {
      if (storage != null) {
        iterator = storage.iterator();
      } else {
        iterator = (Iterator<T>) Collections.emptyList().iterator();
      }
    }
  }

  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  public static class Builder<T> {
    private ImmutableList.Builder<T> builder = ImmutableList.builder();

    public Builder<T> add(T o) {
      builder.add(o);
      return this;
    }

    public Builder<T> addAll(Collection<T> collection) {
      builder.addAll(collection);
      return this;
    }

    public CollectionReader<T> build() {
      return new CollectionReader<>(builder.build());
    }
  }
}
