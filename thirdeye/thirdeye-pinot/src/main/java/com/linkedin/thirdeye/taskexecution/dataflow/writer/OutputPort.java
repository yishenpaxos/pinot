package com.linkedin.thirdeye.taskexecution.dataflow.writer;

import com.linkedin.thirdeye.taskexecution.operator.Operator;

public interface OutputPort<T> {
  Operator getOperator();

  void initialize();

  Writer<T> getWriter();
}
