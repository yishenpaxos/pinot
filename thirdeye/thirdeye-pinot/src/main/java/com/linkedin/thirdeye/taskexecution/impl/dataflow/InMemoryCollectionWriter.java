package com.linkedin.thirdeye.taskexecution.impl.dataflow;

import com.linkedin.thirdeye.taskexecution.dataflow.reader.Reader;
import com.linkedin.thirdeye.taskexecution.dataflow.writer.Writer;
import java.util.ArrayList;
import java.util.List;

public class InMemoryCollectionWriter<T> implements Writer<T> {
  private List<T> storage = new ArrayList<>();

  @Override
  public void write(T o) {
    storage.add(o);
  }

  public Reader<T> toReader() {
    return InMemoryCollectionReader.<T>builder().addAll(storage).build();
  }
}
