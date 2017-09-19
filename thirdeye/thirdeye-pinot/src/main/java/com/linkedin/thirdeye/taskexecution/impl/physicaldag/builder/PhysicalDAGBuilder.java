package com.linkedin.thirdeye.taskexecution.impl.physicaldag.builder;

import com.google.common.base.Preconditions;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.InputPort;
import com.linkedin.thirdeye.taskexecution.dataflow.writer.OutputPort;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.PhysicalDAG;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.PhysicalEdge;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.PhysicalNode;
import com.linkedin.thirdeye.taskexecution.operator.AbstractOperator;
import com.linkedin.thirdeye.taskexecution.operator.Operator;
import com.linkedin.thirdeye.taskexecution.operator.Operator0x1;
import com.linkedin.thirdeye.taskexecution.operator.Operator1x1;
import com.linkedin.thirdeye.taskexecution.operator.Operator2x1;
import com.linkedin.thirdeye.taskexecution.operator.OperatorUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

  public <T1, T2> Set<Channel> addChannels(Operator0x1<? extends T1> source1, Operator0x1<? extends T2> source2,
      Operator2x1<? super T1, ? super T2, ?> sink) {
    Set<Channel> channels = new HashSet<>();
    channels.add(addChannel(source1.getOutputPort(), sink.getInputPort1()));
    channels.add(addChannel(source2.getOutputPort(), sink.getInputPort2()));
    return channels;
  }

  public <T> Channel<T> addChannel(Operator0x1<? extends T> source, Operator1x1<? super T, ?> sink) {
    return addChannel(source.getOutputPort(), sink.getInputPort());
  }

  public <T> Channel<T> addChannel(OutputPort<? extends T> sourcePort, InputPort<? super T> sinkPort) {
    Channel.ChannelBuilder<T> builder = new Channel.ChannelBuilder<>();
    builder.setSourcePort(sourcePort).setSourceIdentify(sourcePort.getOperator().getNodeIdentifier())
        .setSinkPort(sinkPort).setSinkIdentity(sinkPort.getOperator().getNodeIdentifier());
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
      dag.addNode(new PhysicalNode(identifierOperatorPair.getKey(), identifierOperatorPair.getValue()));
    }
    for (Channel channel : channels.values()) {
      NodeIdentifier sourceIdentifier = channel.getSourceIdentify();
      NodeIdentifier sinkIdentifier = channel.getSinkIdentity();
      OutputPort sourcePort = channel.getSourcePort();
      InputPort sinkPort = channel.getSinkPort();

      PhysicalEdge edge = new PhysicalEdge();
      edge.connectPort(dag.getNode(sourceIdentifier), sourcePort, dag.getNode(sinkIdentifier), sinkPort);
      dag.addEdge(edge);
    }

    initBuild();

    return dag;
  }


}
