package com.linkedin.thirdeye.taskexecution.operator;

import com.google.common.base.Preconditions;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;

public abstract class AbstractOperator implements Operator {
  private NodeIdentifier nodeIdentifier;

  public AbstractOperator() {
  }

  public AbstractOperator(NodeIdentifier nodeIdentifier) {
    setNodeIdentifier(nodeIdentifier);
  }

  final public void setNodeIdentifier(NodeIdentifier nodeIdentifier) {
    Preconditions.checkNotNull(nodeIdentifier);
    this.nodeIdentifier = nodeIdentifier;
  }
}
