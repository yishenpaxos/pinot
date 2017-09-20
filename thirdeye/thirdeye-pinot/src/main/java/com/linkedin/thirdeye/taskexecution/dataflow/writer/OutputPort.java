package com.linkedin.thirdeye.taskexecution.dataflow.writer;

import com.linkedin.thirdeye.taskexecution.dataflow.reader.Reader;
import com.linkedin.thirdeye.taskexecution.operator.Operator;

public interface OutputPort<T> {
  Operator getOperator();

  // Used by executor
  void initialize();

  // Used by Operator
  Writer<T> getWriter();

  // Used by executor
  Reader<T> getReader();
}
