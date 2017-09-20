package com.linkedin.thirdeye.taskexecution.dataflow.writer;

public interface Writer<T> {
  void write(T payload);
}
