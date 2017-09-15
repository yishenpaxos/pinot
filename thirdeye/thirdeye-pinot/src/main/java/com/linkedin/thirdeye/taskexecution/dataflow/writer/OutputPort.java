package com.linkedin.thirdeye.taskexecution.dataflow.writer;

import com.linkedin.thirdeye.taskexecution.operator.Operator;

public interface OutputPort<T> {
  void setOperator(Operator operator);

  Operator getOperator();

  void setWriter(Writer<T> writer);

  Writer<T> getWriter();
}
