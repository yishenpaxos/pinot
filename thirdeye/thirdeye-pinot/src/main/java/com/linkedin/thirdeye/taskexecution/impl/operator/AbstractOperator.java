package com.linkedin.thirdeye.taskexecution.impl.operator;

import com.google.common.base.Preconditions;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.operator.Operator;
import com.linkedin.thirdeye.taskexecution.operator.OperatorConfig;

public abstract class AbstractOperator implements Operator {
  private NodeIdentifier nodeIdentifier;

  public AbstractOperator() {
  }

  public AbstractOperator(NodeIdentifier nodeIdentifier) {
    setNodeIdentifier(nodeIdentifier);
  }

  public final void setNodeIdentifier(NodeIdentifier nodeIdentifier) {
    Preconditions.checkNotNull(nodeIdentifier);
    this.nodeIdentifier = nodeIdentifier;
  }

  @Override
  final public NodeIdentifier getNodeIdentifier() {
    return nodeIdentifier;
  }

  @Override
  public OperatorConfig newOperatorConfigInstance() {
    return new SimpleOperatorConfig();
  }

  @Override
  public void initializeIOPorts() {

  }
}
