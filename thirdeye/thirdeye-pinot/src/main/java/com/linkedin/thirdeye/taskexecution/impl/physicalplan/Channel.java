package com.linkedin.thirdeye.taskexecution.impl.physicalplan;

import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.InputPort;
import com.linkedin.thirdeye.taskexecution.dataflow.writer.OutputPort;

public class Channel<T> {
  private InputPort<T> source;
  private OutputPort<T> sink;
  private NodeIdentifier sourceIdentify;
  private NodeIdentifier sinkIdentity;

  Channel(InputPort<T> source, OutputPort<T> sink, NodeIdentifier sourceIdentify, NodeIdentifier sinkIdentity) {
    this.source = source;
    this.sink = sink;
    this.sourceIdentify = sourceIdentify;
    this.sinkIdentity = sinkIdentity;
  }

  public InputPort<T> getSource() {
    return source;
  }

  public OutputPort<T> getSink() {
    return sink;
  }

  public NodeIdentifier getSourceIdentify() {
    return sourceIdentify;
  }

  public NodeIdentifier getSinkIdentity() {
    return sinkIdentity;
  }

  public static class ChannelBuilder<T> {
    private InputPort<T> source;
    private OutputPort<T> sink;
    private NodeIdentifier sourceIdentify;
    private NodeIdentifier sinkIdentity;

    public ChannelBuilder setSource(InputPort<T> source) {
      this.source = source;
      return this;
    }

    public ChannelBuilder setSink(OutputPort<T> sink) {
      this.sink = sink;
      return this;
    }

    public ChannelBuilder setSourceIdentify(NodeIdentifier sourceIdentify) {
      this.sourceIdentify = sourceIdentify;
      return this;
    }

    public ChannelBuilder setSinkIdentity(NodeIdentifier sinkIdentity) {
      this.sinkIdentity = sinkIdentity;
      return this;
    }

    public Channel<T> createChannel() {
      return new Channel<>(source, sink, sourceIdentify, sinkIdentity);
    }
  }
}
