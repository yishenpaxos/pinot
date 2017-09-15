package com.linkedin.thirdeye.taskexecution.impl.dataflow;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class InMemoryCollectionReader<T> implements Reader<T> {
  private ImmutableList<T> storage = ImmutableList.<T>builder().build();
  private Iterator<T> iterator;

  public InMemoryCollectionReader() {
  }

  public InMemoryCollectionReader(List<T> storage) {
    Preconditions.checkNotNull(storage);
    this.storage = ImmutableList.<T>builder().addAll(storage).build();
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
      iterator = storage.iterator();
    }
  }

  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  public static class Builder<T> {
    private List<T> storage = new ArrayList<>();

    public Builder<T> add(T o) {
      storage.add(o);
      return this;
    }

    public Builder<T> addAll(Collection<T> collection) {
      storage.addAll(collection);
      return this;
    }

    public InMemoryCollectionReader<T> build() {
      return new InMemoryCollectionReader<>(storage);
    }
  }
}
