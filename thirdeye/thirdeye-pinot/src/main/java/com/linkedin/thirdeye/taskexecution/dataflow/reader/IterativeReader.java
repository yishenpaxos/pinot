package com.linkedin.thirdeye.taskexecution.dataflow.reader;

import java.util.Collection;

public interface IterativeReader<V> extends SimpleReader<Collection<V>> {
  boolean hasNext();

  V next();
}
