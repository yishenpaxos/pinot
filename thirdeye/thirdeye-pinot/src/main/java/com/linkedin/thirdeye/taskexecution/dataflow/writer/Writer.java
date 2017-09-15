package com.linkedin.thirdeye.taskexecution.dataflow.writer;

import com.linkedin.thirdeye.taskexecution.dataflow.reader.Reader;

public interface Writer<T> {
  void write(T payload);

  Reader<T> toReader();
}
