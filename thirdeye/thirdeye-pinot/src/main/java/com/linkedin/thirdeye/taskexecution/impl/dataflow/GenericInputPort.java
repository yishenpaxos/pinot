package com.linkedin.thirdeye.taskexecution.impl.dataflow;

import com.google.common.base.Preconditions;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.InputPort;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.Reader;
import com.linkedin.thirdeye.taskexecution.operator.Operator;

public class GenericInputPort<T> implements InputPort<T> {
  private Operator operator;
  private CollectionReader.Builder<T> builder;

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
  public void initialize() {
    builder = CollectionReader.builder();
  }

  @Override
  public void addContext(Reader<T> reader) {
    Preconditions.checkNotNull(builder, "This port is not initialized; please invoke initialize before this method.");
    if (reader != null) {
      while (reader.hasNext()) {
        builder.add(reader.next());
      }
    }
  }

  @Override
  public Reader<T> getReader() {
    Preconditions.checkNotNull(builder, "This port is not initialized; please invoke initialize before this method.");
    return builder.build();
  }
}
