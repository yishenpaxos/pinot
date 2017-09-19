package com.linkedin.thirdeye.taskexecution.operator;

import com.linkedin.thirdeye.taskexecution.dataflow.reader.GenericInputPort;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.InputPort;
import com.linkedin.thirdeye.taskexecution.dataflow.writer.GenericOutputPort;
import com.linkedin.thirdeye.taskexecution.dataflow.writer.OutputPort;

public abstract class SimpleIOOperator<IN, OUT> extends AbstractOperator {
  private final InputPort<IN> inputPort = new GenericInputPort<>(this);
  private final OutputPort<OUT> outputPort = new GenericOutputPort<>(this);

  public InputPort<IN> getInputPort() {
    return inputPort;
  }

  public OutputPort<OUT> getOutputPort() {
    return outputPort;
  }
}
