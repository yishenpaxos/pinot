package com.linkedin.thirdeye.taskexecution.operator;

import com.linkedin.thirdeye.taskexecution.dataflow.reader.GenericInputPort;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.InputPort;
import com.linkedin.thirdeye.taskexecution.dataflow.writer.GenericOutputPort;
import com.linkedin.thirdeye.taskexecution.dataflow.writer.OutputPort;

public abstract class SimpleIOOperator<IN, OUT> extends Operator {
  public final InputPort<IN> input = new GenericInputPort<>(this);
  public final OutputPort<OUT> output = new GenericOutputPort<>(this);
}
