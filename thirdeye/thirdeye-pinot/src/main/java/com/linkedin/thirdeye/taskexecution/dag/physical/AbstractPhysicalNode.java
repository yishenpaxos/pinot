package com.linkedin.thirdeye.taskexecution.dag.physical;

import com.linkedin.thirdeye.taskexecution.dag.Node;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractPhysicalNode<T extends AbstractPhysicalNode> extends FrameworkNode implements Node<T> {

  private Set<T> incomingEdge = new HashSet<>();
  private Set<T> outgoingEdge = new HashSet<>();

  protected AbstractPhysicalNode() {
  }

  protected AbstractPhysicalNode(NodeIdentifier nodeIdentifier, Class operatorClass) {
    super(nodeIdentifier, operatorClass);
  }

  public void addIncomingNode(T node) {
    incomingEdge.add(node);
  }

  public void addOutgoingNode(T node) {
    outgoingEdge.add(node);
  }

  public Collection<T> getIncomingNodes() {
    return incomingEdge;
  }

  public Collection<T> getOutgoingNodes() {
    return outgoingEdge;
  }
}
