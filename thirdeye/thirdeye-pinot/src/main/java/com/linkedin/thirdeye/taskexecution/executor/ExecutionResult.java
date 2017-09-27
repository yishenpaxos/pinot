package com.linkedin.thirdeye.taskexecution.executor;

import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;

public class ExecutionResult {
  private NodeIdentifier nodeIdentifier;
  private ExecutionStatus executionStatus;

  public NodeIdentifier getNodeIdentifier() {
    return nodeIdentifier;
  }

  public void setNodeIdentifier(NodeIdentifier nodeIdentifier) {
    this.nodeIdentifier = nodeIdentifier;
  }

  public ExecutionStatus getExecutionStatus() {
    return executionStatus;
  }

  public void setExecutionStatus(ExecutionStatus executionStatus) {
    this.executionStatus = executionStatus;
  }
}
