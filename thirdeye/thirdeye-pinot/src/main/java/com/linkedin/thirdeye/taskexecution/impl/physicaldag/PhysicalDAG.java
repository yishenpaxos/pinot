package com.linkedin.thirdeye.taskexecution.impl.physicaldag;

import com.google.common.base.Preconditions;
import com.linkedin.thirdeye.taskexecution.dag.DAG;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class PhysicalDAG implements DAG {
  private Map<NodeIdentifier, PhysicalNode> rootNodes = new HashMap<>();
  private Map<NodeIdentifier, PhysicalNode> leafNodes = new HashMap<>();
  private Map<NodeIdentifier, PhysicalNode> nodes = new HashMap<>();

  /**
   * Add the given node if it has not been inserted to this DAG and returns the node that has the same {@link
   * NodeIdentifier}.
   *
   * @param node the node to be added.
   *
   * @return the node that is just being added or the existing node that has the same {@link NodeIdentifier}.
   */
  PhysicalNode addNode(PhysicalNode node) {
    Preconditions.checkNotNull(node, "Unable to add an null node.");
    return getOrAdd(node);
  }

  /**
   * Add execution dependency from the source to the sink without considering data exchange between them. See {@link
   * #addChannel(Channel)} if there is data needs to be passed from the source to the sink.
   *
   * @param source the source node.
   * @param sink the sink node.
   */
  void addExecutionDependency(PhysicalNode source, PhysicalNode sink) {
    Preconditions.checkNotNull(source, "Unable to add an null source.");
    Preconditions.checkNotNull(sink, "Unable to add an null sink.");
    source = getOrAdd(source);
    sink = getOrAdd(sink);
    addNodeDependency(source, sink);
  }

  /**
   * Add execution dependency from the source to the sink of the channel. Moreover, connect the output port of source
   * to the input port of sink.
   *
   * @param channel the channel that specified the output port of source and input port of sink.
   */
  void addChannel(Channel channel) {
    PhysicalNode source = getNode(channel.getSourceIdentifier());
    PhysicalNode sink = getNode(channel.getSinkIdentifier());
    Preconditions.checkNotNull(source, "Source node '%s' doesn't exist in DAG.", channel.getSourceIdentifier());
    Preconditions.checkNotNull(sink, "Sink node '%s' doesn't exist in DAG.", channel.getSinkIdentifier());
    Preconditions.checkArgument(!Objects.equals(source, sink), "Source and sink node cannot be the same.");

    addNodeDependency(source, sink);
    source.addOutgoingEdge(channel);
    sink.addIncomingEdge(channel);
  }

  /**
   * Internal implementation for adding execution dependency from the source to the sink node.
   *
   * @param source the source node.
   * @param sink the sink node.
   */
  private void addNodeDependency(PhysicalNode source, PhysicalNode sink) {
    source.addOutgoingNode(sink);
    if (leafNodes.containsKey(source.getIdentifier())) {
      leafNodes.remove(source.getIdentifier());
    }
    sink.addIncomingNode(source);
    if (rootNodes.containsKey(sink.getIdentifier())) {
      rootNodes.remove(sink.getIdentifier());
    }
  }

  /**
   * Returns the given node if it has not been inserted to this DAG; otherwise, returns the previous inserted node,
   * which has the same {@link NodeIdentifier}.
   *
   * @param node the node to get or be added.
   *
   * @return the node with the same {@link NodeIdentifier}.
   */
  private PhysicalNode getOrAdd(PhysicalNode node) {
    NodeIdentifier nodeIdentifier = node.getIdentifier();
    if (!nodes.containsKey(nodeIdentifier)) {
      nodes.put(nodeIdentifier, node);
      rootNodes.put(nodeIdentifier, node);
      leafNodes.put(nodeIdentifier, node);
      return node;
    } else {
      return nodes.get(nodeIdentifier);
    }
  }

  @Override
  public PhysicalNode getNode(NodeIdentifier nodeIdentifier) {
    Preconditions.checkNotNull(nodeIdentifier);
    return nodes.get(nodeIdentifier);
  }

  @Override
  public Set<PhysicalNode> getParents(NodeIdentifier nodeIdentifier) {
    PhysicalNode node = getNode(nodeIdentifier);
    if (node != null) {
      return node.getIncomingNodes();
    } else {
      return Collections.emptySet();
    }
  }

  @Override
  public Set<PhysicalNode> getChildren(NodeIdentifier nodeIdentifier) {
    PhysicalNode node = getNode(nodeIdentifier);
    if (node != null) {
      return node.getOutgoingNodes();
    } else {
      return Collections.emptySet();
    }
  }

  @Override
  public int size() {
    return nodes.size();
  }

  @Override
  public Collection<PhysicalNode> getStartNodes() {
    return new HashSet<>(rootNodes.values());
  }

  @Override
  public Collection<PhysicalNode> getEndNodes() {
    return new HashSet<>(leafNodes.values());
  }

  @Override
  public Collection<PhysicalNode> getAllNodes() {
    return new HashSet<>(nodes.values());
  }
}
