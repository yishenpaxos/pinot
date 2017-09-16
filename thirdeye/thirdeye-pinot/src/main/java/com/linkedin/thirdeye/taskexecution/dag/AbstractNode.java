package com.linkedin.thirdeye.taskexecution.dag;

import com.google.common.base.Preconditions;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.ExecutionStatus;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


public abstract class AbstractNode<N extends AbstractNode, E extends Edge> implements Node<N, E> {
  protected NodeIdentifier nodeIdentifier = new NodeIdentifier();

  private Set<N> incomingNode = new HashSet<>();
  private Set<N> outgoingNode = new HashSet<>();
  private Set<E> incomingEdge = new HashSet<>();
  private Set<E> outgoingEdge = new HashSet<>();

  protected AbstractNode() {
  }

  protected AbstractNode(NodeIdentifier nodeIdentifier) {
    Preconditions.checkNotNull(nodeIdentifier);
    this.nodeIdentifier = nodeIdentifier;
  }

  public NodeIdentifier getIdentifier() {
    return nodeIdentifier;
  }

  public void setIdentifier(NodeIdentifier nodeIdentifier) {
    Preconditions.checkNotNull(nodeIdentifier);
    this.nodeIdentifier = nodeIdentifier;
  }

  public void addIncomingNode(N node) {
    Preconditions.checkNotNull(node);
    incomingNode.add(node);
  }

  public void addOutgoingNode(N node) {
    Preconditions.checkNotNull(node);
    outgoingNode.add(node);
  }

  public Set<N> getIncomingNodes() {
    return incomingNode;
  }

  public Set<N> getOutgoingNodes() {
    return outgoingNode;
  }

  @Override
  public void addIncomingEdge(E edge) {
    Preconditions.checkNotNull(edge);
    incomingEdge.add(edge);
  }

  @Override
  public void addOutgoingEdge(E edge) {
    Preconditions.checkNotNull(edge);
    outgoingEdge.add(edge);
  }

  @Override
  public Set<E> getIncomingEdges() {
    return incomingEdge;
  }

  @Override
  public Set<E> getOutgoingEdges() {
    return outgoingEdge;
  }

  public abstract ExecutionStatus getExecutionStatus();

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AbstractNode<?, ?> that = (AbstractNode<?, ?>) o;
    return Objects.equals(getIdentifier(), that.getIdentifier()) && Objects
        .equals(incomingNode, that.incomingNode) && Objects.equals(outgoingNode, that.outgoingNode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getIdentifier());
  }
}
