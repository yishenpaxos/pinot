package com.linkedin.thirdeye.taskexecution.dataflow.writer;

import com.google.common.base.Preconditions;
import com.linkedin.thirdeye.taskexecution.impl.dataflow.InMemoryCollectionWriter;
import com.linkedin.thirdeye.taskexecution.operator.Operator;

public class GenericOutputPort<T> implements OutputPort<T> {
  private Operator operator;
  private Writer<T> writer;

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
    writer = new InMemoryCollectionWriter<>();
  }

  @Override
  public Writer<T> getWriter() {
    return writer;
  }
}
