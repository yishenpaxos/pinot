package com.linkedin.thirdeye.taskexecution.impl.physicaldag;

import com.google.common.base.Preconditions;
import com.linkedin.thirdeye.taskexecution.dag.Edge;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.InputPort;
import com.linkedin.thirdeye.taskexecution.dataflow.writer.OutputPort;
import java.util.Objects;

public class Channel<T> implements Edge {
  private NodeIdentifier sourceIdentify;
  private NodeIdentifier sinkIdentity;
  private OutputPort<? extends T> sourcePort;
  private InputPort<? super T> sinkPort;

  Channel(OutputPort<? extends T> sourcePort, InputPort<? super T> sinkPort, NodeIdentifier sourceIdentify,
      NodeIdentifier sinkIdentity) {
    this.sourcePort = sourcePort;
    this.sinkPort = sinkPort;
    this.sourceIdentify = sourceIdentify;
    this.sinkIdentity = sinkIdentity;
  }

  public OutputPort<? extends T> getSourcePort() {
    return sourcePort;
  }

  public InputPort<? super T> getSinkPort() {
    return sinkPort;
  }

  public NodeIdentifier getSourceIdentifier() {
    return sourceIdentify;
  }

  public NodeIdentifier getSinkIdentifier() {
    return sinkIdentity;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Channel<?> channel = (Channel<?>) o;
    return Objects.equals(getSourcePort(), channel.getSourcePort()) && Objects
        .equals(getSinkPort(), channel.getSinkPort()) && Objects
        .equals(getSourceIdentifier(), channel.getSourceIdentifier()) && Objects
        .equals(getSinkIdentifier(), channel.getSinkIdentifier());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getSourcePort(), getSinkPort(), getSourceIdentifier(), getSinkIdentifier());
  }

  public static class ChannelBuilder<T> {
    private OutputPort<? extends T> sourcePort;
    private InputPort<? super T> sinkPort;
    private NodeIdentifier sourceIdentify;
    private NodeIdentifier sinkIdentity;

    public ChannelBuilder<T> setSourcePort(OutputPort<? extends T> sourcePort) {
      Preconditions.checkNotNull(sourcePort);
      this.sourcePort = sourcePort;
      return this;
    }

    public ChannelBuilder<T> setSinkPort(InputPort<? super T> sinkPort) {
      Preconditions.checkNotNull(sinkPort);
      this.sinkPort = sinkPort;
      return this;
    }

    public ChannelBuilder<T> setSourceIdentify(NodeIdentifier sourceIdentify) {
      Preconditions.checkNotNull(sourceIdentify);
      this.sourceIdentify = sourceIdentify;
      return this;
    }

    public ChannelBuilder<T> setSinkIdentity(NodeIdentifier sinkIdentity) {
      Preconditions.checkNotNull(sinkIdentity);
      this.sinkIdentity = sinkIdentity;
      return this;
    }

    public Channel<T> createChannel() {
      Preconditions.checkNotNull(sourcePort);
      Preconditions.checkNotNull(sinkPort);
      Preconditions.checkNotNull(sourceIdentify);
      Preconditions.checkNotNull(sinkIdentity);
      return new Channel<>(sourcePort, sinkPort, sourceIdentify, sinkIdentity);
    }
  }
}
