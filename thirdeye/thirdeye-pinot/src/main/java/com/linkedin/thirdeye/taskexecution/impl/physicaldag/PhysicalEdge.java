package com.linkedin.thirdeye.taskexecution.impl.physicaldag;

import com.google.common.base.Preconditions;
import com.linkedin.thirdeye.taskexecution.dag.Edge;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.InputPort;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.Reader;
import com.linkedin.thirdeye.taskexecution.dataflow.writer.OutputPort;
import com.linkedin.thirdeye.taskexecution.operator.Operator;

public class PhysicalEdge implements Edge {
  private PhysicalNode source;
  private PhysicalNode sink;
  private Operator sourceOperator;
  private Operator sinkOperator;
  private OutputPort sourcePort;
  private InputPort sinkPort;

  public PhysicalEdge connect(PhysicalNode source, PhysicalNode sink) {
    Preconditions.checkNotNull(source);
    Preconditions.checkNotNull(sink);

    this.source = source;
    this.sink = sink;
    return this;
  }

  public <T> PhysicalEdge connect(PhysicalNode source, OutputPort<? extends T> sourcePort, PhysicalNode sink,
      InputPort<? super T> sinkPort) {
    Preconditions.checkArgument(source.getOperator() == sourcePort.getOperator());
    Preconditions.checkArgument(sink.getOperator() == sinkPort.getOperator());
    this.source = source;
    this.sink = sink;
    sourceOperator = source.getOperator();
    sinkOperator = sink.getOperator();
    this.sourcePort = sourcePort;
    this.sinkPort = sinkPort;

    return this;
  }

  public void flush() {
  }

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

  public Operator getSourceOperator() {
    return sourceOperator;
  }

  public Operator getSinkOperator() {
    return sinkOperator;
  }

  public OutputPort getSourcePort() {
    return sourcePort;
  }

  public InputPort getSinkPort() {
    return sinkPort;
  }
}
