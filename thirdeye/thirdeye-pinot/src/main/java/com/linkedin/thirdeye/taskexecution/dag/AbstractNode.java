package com.linkedin.thirdeye.taskexecution.dag;

import com.google.common.base.Preconditions;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.ExecutionStatus;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;

public abstract class AbstractNode<N extends AbstractNode, E extends Edge> implements Node<N, E>, Callable<NodeIdentifier> {
  protected NodeIdentifier nodeIdentifier = new NodeIdentifier();

  private Set<N> incomingNode = new HashSet<>();
  private Set<N> outgoingNode = new HashSet<>();
  protected Set<E> incomingEdge = new HashSet<>();
  protected Set<E> outgoingEdge = new HashSet<>();

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

  public Collection<N> getIncomingNodes() {
    return incomingNode;
  }

  public Collection<N> getOutgoingNodes() {
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
  public Collection<E> getIncomingEdges() {
    return incomingEdge;
  }

  @Override
  public Collection<E> getOutgoingEdges() {
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
