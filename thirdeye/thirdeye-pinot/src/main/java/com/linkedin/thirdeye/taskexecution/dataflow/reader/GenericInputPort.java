package com.linkedin.thirdeye.taskexecution.dataflow.reader;

import com.google.common.base.Preconditions;
import com.linkedin.thirdeye.taskexecution.impl.dataflow.InMemoryCollectionReader;
import com.linkedin.thirdeye.taskexecution.operator.Operator;

public class GenericInputPort<T> implements InputPort<T> {
  private Operator operator;
  private InMemoryCollectionReader.Builder<T> builder;

  public GenericInputPort() {
  }

  public GenericInputPort(Operator operator) {
    setOperator(operator);
  }

  public void setOperator(Operator operator) {
    Preconditions.checkNotNull(operator);
    this.operator = operator;
  }

  @Override
  public Operator getOperator() {
    return operator;
  }

  @Override
  public void addContext(Reader<T> reader) {
    if (reader != null) {
      initializeBuilder();
      while (reader.hasNext()) {
        builder.add(reader.next());
      }
    }
  }

  @Override
  public Reader<T> getReader() {
    initializeBuilder();
    return builder.build();
  }

  private void initializeBuilder() {
    if (builder == null) {
      builder = InMemoryCollectionReader.builder();
    }
  }
}
