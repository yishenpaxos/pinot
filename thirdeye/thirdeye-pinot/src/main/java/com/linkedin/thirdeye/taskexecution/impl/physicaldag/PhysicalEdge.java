package com.linkedin.thirdeye.taskexecution.impl.physicaldag;

import com.google.common.base.Preconditions;
import com.linkedin.thirdeye.taskexecution.dag.Edge;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.InputPort;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.Reader;
import com.linkedin.thirdeye.taskexecution.dataflow.writer.OutputPort;

public class PhysicalEdge implements Edge {
  private NodeIdentifier sourceIdentifier;
  private NodeIdentifier sinkIdentifier;
  private OutputPort sourcePort;
  private InputPort sinkPort;
  // TODO: Add edge property

  public PhysicalEdge connect(PhysicalNode source, PhysicalNode sink) {
    Preconditions.checkNotNull(source.getIdentifier());
    Preconditions.checkNotNull(sink.getIdentifier());
    this.sourceIdentifier = source.getIdentifier();
    this.sinkIdentifier = sink.getIdentifier();
    this.sourcePort = null;
    this.sinkPort = null;

    return this;
  }

  public <T> PhysicalEdge connect(OutputPort<? extends T> sourcePort, InputPort<? super T> sinkPort) {
    Preconditions.checkNotNull(sourcePort.getOperator().getNodeIdentifier());
    Preconditions.checkNotNull(sinkPort.getOperator().getNodeIdentifier());
    this.sourceIdentifier = sourcePort.getOperator().getNodeIdentifier();
    this.sinkIdentifier = sinkPort.getOperator().getNodeIdentifier();
    this.sourcePort = sourcePort;
    this.sinkPort = sinkPort;

    return this;
  }

  /**
   * This method is supposed to be invoked by the operator runner that runs the source operator.
   */
  public void flush() {
  }

  /**
   * This method is supposed to be invoked by the operator runner that runs the sink operator.
   */
  public void initRead() {
    if (sourcePort != null && sinkPort != null) {
      Reader reader = sourcePort.getWriter().toReader();
      sinkPort.addContext(reader);
    }
  }

  public NodeIdentifier getSourceIdentifier() {
    return sourceIdentifier;
  }

  public NodeIdentifier getSinkIdentifier() {
    return sinkIdentifier;
  }

  public OutputPort getSourcePort() {
    return sourcePort;
  }

  public InputPort getSinkPort() {
    return sinkPort;
  }
}
