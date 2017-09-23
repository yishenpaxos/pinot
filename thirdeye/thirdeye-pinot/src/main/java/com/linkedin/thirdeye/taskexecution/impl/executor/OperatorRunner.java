package com.linkedin.thirdeye.taskexecution.impl.executor;

import com.google.common.base.Preconditions;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.ExecutionStatus;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.NodeConfig;
import com.linkedin.thirdeye.taskexecution.operator.Operator;
import com.linkedin.thirdeye.taskexecution.operator.OperatorConfig;
import com.linkedin.thirdeye.taskexecution.operator.OperatorContext;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OperatorRunner is a wrapper class to setup input and gather the output data of a operator.
 */
public class OperatorRunner implements Callable<NodeIdentifier> {
  private static final Logger LOG = LoggerFactory.getLogger(OperatorRunner.class);

  private NodeIdentifier nodeIdentifier = new NodeIdentifier();
  private Operator operator;
  private NodeConfig nodeConfig = new NodeConfig();
  private ExecutionStatus executionStatus = ExecutionStatus.RUNNING;
  private Set<OperatorIOChannel> incomingChannels = Collections.emptySet();
  private Set<OperatorIOChannel> outgoingChannels = Collections.emptySet();

  public OperatorRunner(NodeConfig nodeConfig, Operator operator) {
    Preconditions.checkNotNull(nodeConfig);
    Preconditions.checkNotNull(operator.getNodeIdentifier());

    this.nodeIdentifier = operator.getNodeIdentifier();
    this.nodeConfig = nodeConfig;
    this.operator = operator;
  }

  public void setIncomingChannels(Set<OperatorIOChannel> incomingChannels) {
    Preconditions.checkNotNull(incomingChannels);
    this.incomingChannels = incomingChannels;
  }

  public void setOutgoingChannels(Set<OperatorIOChannel> outgoingChannels) {
    Preconditions.checkNotNull(outgoingChannels);
    this.outgoingChannels = outgoingChannels;
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
    return new OperatorConfig();
  }

  /**
   * Invokes the execution of the operator that is define for the corresponding node in the DAG and returns its node
   * identifier.
   *
   * @return the node identifier of this node (i.e., OperatorRunner).
   */
  @Override
  public NodeIdentifier call() {
    try {
      Preconditions.checkNotNull(nodeIdentifier, "Node identifier cannot be null");
      int numRetry = nodeConfig.numRetryAtError();
      for (int i = 0; i <= numRetry; ++i) {
        try {
          // Initialize local input and output ports
          operator.initializeIOPorts();
          operator.initializeOutputPorts();

          // Read context from remote output ports to local input ports
          for (OperatorIOChannel edge : incomingChannels) {
            edge.initInputPort();
          }

          // Initialize operator
          OperatorConfig operatorConfig = convertNodeConfigToOperatorConfig(nodeConfig);
          operator.initialize(operatorConfig);
          // Run operator
          operator.run(new OperatorContext(nodeIdentifier));

          // Flush context in local output ports, which may write context to a remote DB.
          for (OperatorIOChannel edge : outgoingChannels) {
            edge.flushOutputPort();
          }
        } catch (Exception e) {
          if (i == numRetry) {
            setFailure(e);
          }
        }
      }
      if (ExecutionStatus.RUNNING.equals(executionStatus)) {
        executionStatus = ExecutionStatus.SUCCESS;
      }
    } catch (Exception e) {
      setFailure(e);
    }
    return nodeIdentifier;
  }
}
