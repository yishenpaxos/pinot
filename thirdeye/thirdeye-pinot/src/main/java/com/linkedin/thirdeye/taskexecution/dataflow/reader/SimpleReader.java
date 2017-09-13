package com.linkedin.thirdeye.taskexecution.dataflow.reader;

public interface SimpleReader<V> extends Reader {

  boolean hasPayload();

  V read();
}
