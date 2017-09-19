package com.linkedin.thirdeye.taskexecution.operator;

import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.dataflow.writer.GenericOutputPort;
import com.linkedin.thirdeye.taskexecution.dataflow.writer.OutputPort;

public abstract class Operator0x1<OUT> extends AbstractOperator {
  private final OutputPort<OUT> outputPort = new GenericOutputPort<>(this);

  public Operator0x1() {
  }

  public Operator0x1(NodeIdentifier nodeIdentifier) {
    super(nodeIdentifier);
  }

  public OutputPort<OUT> getOutputPort() {
    return outputPort;
  }

  @Override
  public void initialInputPorts() {

  }

  @Override
  public void initialOutputPorts() {
    outputPort.initialize();
  }
}
