package com.linkedin.thirdeye.taskexecution.dataflow.reader;

public interface Reader<V> {

  boolean hasPayload();

  V read();
}
