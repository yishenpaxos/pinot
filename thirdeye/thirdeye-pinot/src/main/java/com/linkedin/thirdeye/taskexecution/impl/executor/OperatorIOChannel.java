package com.linkedin.thirdeye.taskexecution.impl.executor;

import com.google.common.base.Preconditions;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.InputPort;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.Reader;
import com.linkedin.thirdeye.taskexecution.dataflow.writer.OutputPort;

public class OperatorIOChannel {
  private OutputPort sourcePort;
  private InputPort sinkPort;
  // TODO: Add edge property

  public void connect(OutputPort sourcePort, InputPort sinkPort) {
    Preconditions.checkNotNull(sourcePort);
    Preconditions.checkNotNull(sinkPort);
    this.sourcePort = sourcePort;
    this.sinkPort = sinkPort;
  }

  /**
   * This method is supposed to be invoked by the operator runner that runs the source operator.
   */
  public void flushOutputPort() {
  }

  /**
   * This method is supposed to be invoked by the operator runner that runs the sink operator.
   */
  public void prepareInputPortContext() {
    if (sourcePort != null && sinkPort != null) {
      Reader reader = sourcePort.getReader();
      sinkPort.addContext(reader);
    }
  }


  public OutputPort getSourcePort() {
    return sourcePort;
  }

  public InputPort getSinkPort() {
    return sinkPort;
  }
}
