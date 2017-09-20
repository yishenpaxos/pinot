package com.linkedin.thirdeye.taskexecution.impl.physicaldag;

import com.google.common.base.Preconditions;
import com.linkedin.thirdeye.taskexecution.dag.Node;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.operator.Operator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A PhysicalNode that executes work using one partition.
 */
public class PhysicalNode implements Node<PhysicalNode, Channel> {

  protected NodeIdentifier nodeIdentifier = new NodeIdentifier();
  private Operator operator;

  private Set<PhysicalNode> incomingNode = new HashSet<>();
  private Set<PhysicalNode> outgoingNode = new HashSet<>();
  private Set<Channel> incomingEdge = new HashSet<>();
  private Set<Channel> outgoingEdge = new HashSet<>();

  public PhysicalNode(String name, Operator operator) {
    this(new NodeIdentifier(name), operator);
  }

  public PhysicalNode(NodeIdentifier nodeIdentifier, Operator operator) {
    setIdentifier(nodeIdentifier);
    this.operator = operator;
  }

  public NodeIdentifier getIdentifier() {
    return nodeIdentifier;
  }

  public void setIdentifier(NodeIdentifier nodeIdentifier) {
    Preconditions.checkNotNull(nodeIdentifier);
    this.nodeIdentifier = nodeIdentifier;
  }

  public void addIncomingNode(PhysicalNode node) {
    Preconditions.checkNotNull(node);
    Preconditions.checkArgument(node != this);
    incomingNode.add(node);
  }

  public void addOutgoingNode(PhysicalNode node) {
    Preconditions.checkNotNull(node);
    Preconditions.checkArgument(node != this);
    outgoingNode.add(node);
  }

  public Set<PhysicalNode> getIncomingNodes() {
    return incomingNode;
  }

  public Set<PhysicalNode> getOutgoingNodes() {
    return outgoingNode;
  }

  @Override
  public void addIncomingEdge(Channel edge) {
    Preconditions.checkNotNull(edge);
    incomingEdge.add(edge);
  }

  @Override
  public void addOutgoingEdge(Channel edge) {
    Preconditions.checkNotNull(edge);
    outgoingEdge.add(edge);
  }

  @Override
  public Set<Channel> getIncomingEdges() {
    return incomingEdge;
  }

  @Override
  public Set<Channel> getOutgoingEdges() {
    return outgoingEdge;
  }

  public Operator getOperator() {
    return operator;
  }

  public ExecutionStatus getExecutionStatus() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PhysicalNode that = (PhysicalNode) o;
    return Objects.equals(getOperator(), that.getOperator()) && Objects.equals(nodeIdentifier, that.nodeIdentifier)
        && Objects.equals(incomingNode, that.incomingNode) && Objects.equals(outgoingNode, that.outgoingNode) && Objects
        .equals(incomingEdge, that.incomingEdge) && Objects.equals(outgoingEdge, that.outgoingEdge);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nodeIdentifier);
  }
}
