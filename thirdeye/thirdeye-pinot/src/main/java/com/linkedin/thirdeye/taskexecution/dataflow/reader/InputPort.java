package com.linkedin.thirdeye.taskexecution.dataflow.reader;

import com.linkedin.thirdeye.taskexecution.operator.Operator;

public interface InputPort<T> {
  Operator getOperator();

  // Used by executor
  void initialize();

  // Used by executor
  void addContext(Reader<T> reader);

  // Used by Operator
  Reader<T> getReader();
}
