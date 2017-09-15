package com.linkedin.thirdeye.taskexecution.dataflow.reader;

public interface Reader<T> {

  boolean hasNext();

  T next();
}
