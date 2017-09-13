package com.linkedin.thirdeye.taskexecution.impl.physicaldag;

import com.linkedin.thirdeye.taskexecution.dag.Node;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.Reader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;

public abstract class AbstractPhysicalNode<T extends AbstractPhysicalNode> implements Node<T>, Callable<NodeIdentifier> {
  protected NodeIdentifier nodeIdentifier = new NodeIdentifier();
  protected Class operatorClass;
  protected NodeConfig nodeConfig = new NodeConfig();

  private Set<T> incomingEdge = new HashSet<>();
  private Set<T> outgoingEdge = new HashSet<>();

  protected AbstractPhysicalNode() {
  }

  protected AbstractPhysicalNode(NodeIdentifier nodeIdentifier, Class operatorClass) {
    this.nodeIdentifier = nodeIdentifier;
    this.operatorClass = operatorClass;
  }

  public NodeIdentifier getIdentifier() {
    return nodeIdentifier;
  }

  public void setIdentifier(NodeIdentifier nodeIdentifier) {
    this.nodeIdentifier = nodeIdentifier;
  }

  public NodeConfig getNodeConfig() {
    return nodeConfig;
  }

  public void setNodeConfig(NodeConfig nodeConfig) {
    this.nodeConfig = nodeConfig;
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

  public abstract ExecutionStatus getExecutionStatus();

  @Deprecated
  public abstract Reader getOutputReader();

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AbstractPhysicalNode<?> that = (AbstractPhysicalNode<?>) o;
    return Objects.equals(getIdentifier(), that.getIdentifier()) && Objects
        .equals(operatorClass, that.operatorClass) && Objects.equals(getNodeConfig(), that.getNodeConfig()) && Objects
        .equals(incomingEdge, that.incomingEdge) && Objects.equals(outgoingEdge, that.outgoingEdge);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getIdentifier(), operatorClass, getNodeConfig());
  }
}
