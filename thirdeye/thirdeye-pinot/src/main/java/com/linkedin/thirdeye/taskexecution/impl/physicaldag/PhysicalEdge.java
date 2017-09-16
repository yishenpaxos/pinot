package com.linkedin.thirdeye.taskexecution.impl.physicaldag;

import com.google.common.base.Preconditions;
import com.linkedin.thirdeye.taskexecution.dag.Edge;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.InputPort;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.Reader;
import com.linkedin.thirdeye.taskexecution.dataflow.writer.OutputPort;
import com.linkedin.thirdeye.taskexecution.operator.SimpleIOOperator;

public class PhysicalEdge implements Edge {
  private PhysicalNode source;
  private PhysicalNode sink;
  private OutputPort sourcePort;
  private InputPort sinkPort;

  public PhysicalEdge connect(PhysicalNode source, PhysicalNode sink) {
    Preconditions.checkNotNull(source);
    Preconditions.checkNotNull(sink);

    this.source = source;
    this.sink = sink;

    return this;
  }

  public <T> PhysicalEdge connectPort(PhysicalNode<? extends SimpleIOOperator<?, ? extends T>> source,
      PhysicalNode<? extends SimpleIOOperator<? super T, ?>> sink) {

    return connectPort(source, source.getOperator().getOutputPort(), sink, sink.getOperator().getInputPort());
  }

  public <T> PhysicalEdge connectPort(PhysicalNode source, OutputPort<? extends T> sourcePort, PhysicalNode sink,
      InputPort<? super T> sinkPort) {
    Preconditions.checkArgument(source.getOperator() == sourcePort.getOperator());
    Preconditions.checkArgument(sink.getOperator() == sinkPort.getOperator());

    this.source = source;
    this.sink = sink;
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
      sinkPort.initializeReader(reader);
    }
  }

  public PhysicalNode getSource() {
    return source;
  }

  public PhysicalNode getSink() {
    return sink;
  }

  public OutputPort getSourcePort() {
    return sourcePort;
  }

  public InputPort getSinkPort() {
    return sinkPort;
  }
}
