package com.linkedin.thirdeye.taskexecution.impl.operator;

import com.google.common.base.Preconditions;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.ExecutionStatus;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.NodeConfig;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.PhysicalEdge;
import com.linkedin.thirdeye.taskexecution.operator.Operator;
import com.linkedin.thirdeye.taskexecution.operator.OperatorConfig;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractOperatorRunner implements Callable<NodeIdentifier> {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractOperatorRunner.class);

  protected NodeIdentifier nodeIdentifier = new NodeIdentifier();
  protected Operator operator;
  protected NodeConfig nodeConfig = new NodeConfig();
  protected ExecutionStatus executionStatus = ExecutionStatus.RUNNING;
  Set<PhysicalEdge> incomingEdge = Collections.emptySet();
  Set<PhysicalEdge> outgoingEdge = Collections.emptySet();

  public AbstractOperatorRunner(NodeConfig nodeConfig, Operator operator) {
    Preconditions.checkNotNull(nodeConfig);
    Preconditions.checkNotNull(operator.getNodeIdentifier());

    this.nodeIdentifier = operator.getNodeIdentifier();
    this.nodeConfig = nodeConfig;
    this.operator = operator;
  }

  public void setIncomingEdge(Set<PhysicalEdge> incomingEdge) {
    Preconditions.checkNotNull(incomingEdge);
    this.incomingEdge = incomingEdge;
  }

  public void setOutgoingEdge(Set<PhysicalEdge> outgoingEdge) {
    Preconditions.checkNotNull(outgoingEdge);
    this.outgoingEdge = outgoingEdge;
  }

  public ExecutionStatus getExecutionStatus() {
    return executionStatus;
  }

  protected void setFailure(Exception e) {
    LOG.error("Failed to execute node: {}.", nodeIdentifier, e);
    if (nodeConfig.skipAtFailure()) {
      executionStatus = ExecutionStatus.SKIPPED;
    } else {
      executionStatus = ExecutionStatus.FAILED;
    }
  }

  // TODO: Implement this method
  static OperatorConfig convertNodeConfigToOperatorConfig(NodeConfig nodeConfig) {
    return null;
  }
}
