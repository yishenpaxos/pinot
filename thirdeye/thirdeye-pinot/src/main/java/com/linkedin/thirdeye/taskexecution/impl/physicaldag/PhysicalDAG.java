package com.linkedin.thirdeye.taskexecution.impl.physicaldag;

import com.google.common.base.Preconditions;
import com.linkedin.thirdeye.taskexecution.dag.DAG;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

public class PhysicalDAG implements DAG<PhysicalNode> {
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

  void addNodeDependency(PhysicalNode source, PhysicalNode sink) {
    Preconditions.checkNotNull(source);
    Preconditions.checkNotNull(sink);

    source = getOrAdd(source);
    sink = getOrAdd(sink);
    source.addOutgoingNode(sink);
    if (leafNodes.containsKey(source.getIdentifier())) {
      leafNodes.remove(source.getIdentifier());
    }
    sink.addIncomingNode(source);
    if (rootNodes.containsKey(sink.getIdentifier())) {
      rootNodes.remove(sink.getIdentifier());
    }
  }

  void addChannel(Channel edge) {
    PhysicalNode source = getNode(edge.getSourceIdentifier());
    PhysicalNode sink = getNode(edge.getSinkIdentifier());
    Preconditions.checkNotNull(source, "Source node '%s' doesn't exist in DAG.", edge.getSourceIdentifier());
    Preconditions.checkNotNull(sink, "Sink node '%s' doesn't exist in DAG.", edge.getSinkIdentifier());
    Preconditions.checkArgument(!Objects.equals(source, sink), "Source and sink node cannot be the same.");

    source.addOutgoingNode(sink);
    source.addOutgoingEdge(edge);
    if (leafNodes.containsKey(source.getIdentifier())) {
      leafNodes.remove(source.getIdentifier());
    }

    sink.addIncomingNode(source);
    sink.addIncomingEdge(edge);
    if (rootNodes.containsKey(sink.getIdentifier())) {
        rootNodes.remove(sink.getIdentifier());
    }
  }

  @Override
  public PhysicalNode getNode(NodeIdentifier nodeIdentifier) {
    return nodes.get(nodeIdentifier);
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
  public int size() {
    return nodes.size();
  }

  @Override
  public Collection<PhysicalNode> getRootNodes() {
    return new HashSet<>(rootNodes.values());
  }

  @Override
  public Collection<PhysicalNode> getLeafNodes() {
    return new HashSet<>(leafNodes.values());
  }

  @Override
  public Collection<PhysicalNode> getAllNodes() {
    return new HashSet<>(nodes.values());
  }
}
