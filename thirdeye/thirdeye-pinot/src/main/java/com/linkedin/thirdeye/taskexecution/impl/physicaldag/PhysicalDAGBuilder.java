package com.linkedin.thirdeye.taskexecution.impl.physicaldag;

import com.google.common.base.Preconditions;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.InputPort;
import com.linkedin.thirdeye.taskexecution.dataflow.writer.OutputPort;
import com.linkedin.thirdeye.taskexecution.operator.AbstractOperator;
import com.linkedin.thirdeye.taskexecution.operator.Operator;
import com.linkedin.thirdeye.taskexecution.operator.Operator0x1;
import com.linkedin.thirdeye.taskexecution.operator.Operator1x1;
import com.linkedin.thirdeye.taskexecution.operator.Operator2x1;
import com.linkedin.thirdeye.taskexecution.operator.OperatorUtils;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;

public class PhysicalDAGBuilder {
  private Map<NodeIdentifier, Operator> operators;
  private Map<Channel, Channel> channels;

  public PhysicalDAGBuilder() {
    initBuild();
  }

  private void initBuild() {
    operators = new HashMap<>();
    channels = new HashMap<>();
  }

  public <T extends AbstractOperator> T addOperator(NodeIdentifier nodeIdentifier, Class<T> operatorClaz) {
    Preconditions.checkNotNull(nodeIdentifier);
    if (operators.containsKey(nodeIdentifier)) {
      return (T) operators.get(nodeIdentifier);
    } else {
      T operator = OperatorUtils.initiateOperatorInstance(nodeIdentifier, operatorClaz);
      operators.put(nodeIdentifier, operator);
      return operator;
    }
  }

  public <T1, T2> LinkedHashSet<Channel> addChannels(Operator0x1<? extends T1> source1, Operator0x1<? extends T2> source2,
      Operator2x1<? super T1, ? super T2, ?> sink) {
    LinkedHashSet<Channel> channels = new LinkedHashSet<>();
    channels.add(addChannel(source1.getOutputPort(), sink.getInputPort1()));
    channels.add(addChannel(source2.getOutputPort(), sink.getInputPort2()));
    return channels;
  }

  public <T> Channel<T> addChannel(Operator0x1<? extends T> source, Operator1x1<? super T, ?> sink) {
    return addChannel(source.getOutputPort(), sink.getInputPort());
  }

  public <T> Channel<T> addChannel(OutputPort<? extends T> sourcePort, InputPort<? super T> sinkPort) {
    final NodeIdentifier sourceIdentity = sourcePort.getOperator().getNodeIdentifier();
    final NodeIdentifier sinkIdentify = sinkPort.getOperator().getNodeIdentifier();
    Preconditions.checkArgument(!Objects.equals(sourceIdentity, sinkIdentify), "Source and sink operator cannot be the same.");

    Channel.ChannelBuilder<T> builder = new Channel.ChannelBuilder<>();
    builder.setSourcePort(sourcePort).setSourceIdentify(sourceIdentity)
        .setSinkPort(sinkPort).setSinkIdentity(sinkIdentify);
    Channel<T> channel = builder.createChannel();
    if (channels.containsKey(channel)) {
      return channels.get(channel);
    } else {
      channels.put(channel, channel);
      return channel;
    }
  }

  public PhysicalDAG build() {
    PhysicalDAG dag = new PhysicalDAG();
    for (Map.Entry<NodeIdentifier, Operator> identifierOperatorPair : operators.entrySet()) {
      NodeIdentifier identifier = identifierOperatorPair.getKey();
      Operator operator = identifierOperatorPair.getValue();
      dag.addNode(new PhysicalNode(identifier, operator));
    }
    for (Channel channel : channels.values()) {
      OutputPort sourcePort = channel.getSourcePort();
      InputPort sinkPort = channel.getSinkPort();

      PhysicalEdge edge = new PhysicalEdge();
      edge.connect(sourcePort, sinkPort);
      dag.addEdge(edge);
    }

    initBuild();

    return dag;
  }


}
