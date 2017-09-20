package com.linkedin.thirdeye.taskexecution.impl.dataflow;

import com.google.common.base.Preconditions;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.Reader;
import com.linkedin.thirdeye.taskexecution.dataflow.writer.OutputPort;
import com.linkedin.thirdeye.taskexecution.dataflow.writer.Writer;
import com.linkedin.thirdeye.taskexecution.operator.Operator;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GenericOutputPort<T> implements OutputPort<T> {
  private Operator operator;
  private Collection<T> storage;

  public GenericOutputPort() {
  }

  public GenericOutputPort(Operator operator) {
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
    storage = new ConcurrentLinkedQueue<>();
  }

  @Override
  public Writer<T> getWriter() {
    Preconditions.checkNotNull(storage, "This port is not initialized; Please invoke initialize before this method.");
    return new CollectionWriter<>(storage);
  }

  @Override
  public Reader<T> getReader() {
    Preconditions.checkNotNull(storage, "This port is not initialized; Please invoke initialize before this method.");
    return new CollectionReader<>(storage);
  }
}
