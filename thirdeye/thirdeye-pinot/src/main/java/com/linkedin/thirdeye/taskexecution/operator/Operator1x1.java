package com.linkedin.thirdeye.taskexecution.operator;

import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.GenericInputPort;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.InputPort;

public abstract class Operator1x1<IN, OUT> extends Operator0x1<OUT> {
  private final InputPort<IN> inputPort = new GenericInputPort<>(this);

  public Operator1x1() {
  }

  public Operator1x1(NodeIdentifier nodeIdentifier) {
    super(nodeIdentifier);
  }

  public InputPort<IN> getInputPort() {
    return inputPort;
  }

  @Override
  public void initialInputPorts() {
    inputPort.initialize();
  }
}
