package com.linkedin.thirdeye.taskexecution.impl.executor;

import com.google.common.base.Preconditions;
import com.linkedin.thirdeye.taskexecution.dag.DAG;
import com.linkedin.thirdeye.taskexecution.dag.Edge;
import com.linkedin.thirdeye.taskexecution.dag.Node;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.executor.DAGExecutor;
import com.linkedin.thirdeye.taskexecution.executor.ExecutionContext;
import com.linkedin.thirdeye.taskexecution.executor.ExecutionEngine;
import com.linkedin.thirdeye.taskexecution.executor.ExecutionResult;
import com.linkedin.thirdeye.taskexecution.executor.DAGConfig;
import com.linkedin.thirdeye.taskexecution.executor.ExecutionStatus;
import com.linkedin.thirdeye.taskexecution.executor.NodeConfig;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An executor that goes through the DAG and submit the nodes, whose upstream dependency (parents) are finished, to the
 * given execution service. An executor only submits the tasks depending on the flow of the DAG and and execution status
 * of nodes.
 * (Future work) The actual execution should be run on other classes.
 */
public class DefaultDAGExecutor implements DAGExecutor {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultDAGExecutor.class);

  private ExecutionEngine executionEngine;

  // TODO: Persistent the following status to a DB in case of executor crashes
  private Set<NodeIdentifier> processedNodes = new HashSet<>();
  private Set<NodeIdentifier> runningNodes = new HashSet<>();

  public DefaultDAGExecutor(ExecutionEngine executionEngine) {
    Preconditions.checkNotNull(executionEngine);
    this.executionEngine = executionEngine;
  }

  public void execute(DAG dag, DAGConfig dagConfig) {
    Collection<? extends Node> nodes = dag.getStartNodes();
    for (Node node : nodes) {
      checkAndRunNode(node, dagConfig, Collections.<Node>emptySet());
    }
    while (runningNodes.size() > processedNodes.size()) {
      try {
        LOG.info("Getting next completed node.");
        ExecutionResult executionResult = executionEngine.take();
        NodeIdentifier nodeIdentifier = executionResult.getNodeIdentifier();
        ExecutionStatus executionStatus = executionResult.getExecutionStatus();
        assert (!ExecutionStatus.RUNNING.equals(executionStatus));
        LOG.info("Execution status of node {}: {}.", nodeIdentifier.toString(), executionStatus);
        // Check whether the execution should be stopped upon execution failure
        if (ExecutionStatus.FAILED.equals(executionStatus) && dagConfig.stopAtFailure()) {
          LOG.error("Aborting execution because execution of node {} is failed.", nodeIdentifier.toString());
          abortExecution();
          break;
        }
        processedNodes.add(nodeIdentifier);
        // Search for the next node to execute
        for (Node child : dag.getChildren(nodeIdentifier)) {
          checkAndRunNode(child, dagConfig, dag.getParents(child.getIdentifier()));
        }
      } catch (InterruptedException | ExecutionException e) {
        // The implementation of OperatorRunner needs to guarantee that this block never happens
        LOG.error("Aborting execution because unexpected error.", e);
        abortExecution();
        break;
      }
    }
  }

  private void abortExecution() {
    // TODO: wait all runners are stopped and clean up intermediate data
  }

  private void checkAndRunNode(Node node, DAGConfig dagConfig,
      Set<? extends Node> upstreamDependency) {
    if (!isFinished(node) && parentsAreFinished(upstreamDependency)) {
      LOG.info("Submitting node -- {} -- for execution.", node.getIdentifier().toString());
      NodeConfig nodeConfig = dagConfig.getNodeConfig(node.getIdentifier());
      ExecutionContext executionContext = new ExecutionContext(node, nodeConfig);
      // Submit runner
      executionEngine.submit(executionContext);
      runningNodes.add(node.getIdentifier());
    }
  }

  private boolean isFinished(Node node) {
    return processedNodes.contains(node.getIdentifier());
  }

  private boolean parentsAreFinished(Set<? extends Node> upstreamDependency) {
    for (Node pNode : upstreamDependency) {
      if (!processedNodes.contains(pNode.getIdentifier())) {
        return false;
      }
    }
    return true;
  }
}
