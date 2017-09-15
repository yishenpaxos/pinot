package com.linkedin.thirdeye.taskexecution.impl.physicaldag;

import com.linkedin.thirdeye.taskexecution.dag.DAG;
import com.linkedin.thirdeye.taskexecution.dag.Node;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An single machine executor that goes through the DAG and submit the nodes, whose parents are finished, to execution
 * service. An executor takes care of only logical execution (control flow). The physical execution is done by
 * OperatorRunner, which could be executed on other machines.
 */
public class DAGExecutor<N extends PhysicalNode, E extends PhysicalEdge> {
  private static final Logger LOG = LoggerFactory.getLogger(DAGExecutor.class);
  private ExecutorCompletionService<NodeIdentifier> executorCompletionService;

  // TODO: Persistent the following status to a DB in case of executor unexpectedly dies
  private Set<NodeIdentifier> processedNodes = new HashSet<>();
  private Map<NodeIdentifier, N> runningNodes = new HashMap<>();


  public DAGExecutor(ExecutorService executorService) {
    this.executorCompletionService = new ExecutorCompletionService<>(executorService);
  }

  public void execute(DAG<N, E> dag, DAGConfig dagConfig) {
    Collection<N> nodes = dag.getRootNodes();
    for (N node : nodes) {
      processNode(node, dagConfig);
    }
    while (runningNodes.size() > processedNodes.size()) {
      try {
        LOG.info("Getting next completed node.");
        NodeIdentifier nodeIdentifier = executorCompletionService.take().get();
        ExecutionStatus executionStatus = runningNodes.get(nodeIdentifier).getExecutionStatus();
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
        N node = dag.getNode(nodeIdentifier);
        for (Object outGoingNode : node.getOutgoingNodes()) {
          processNode((N) outGoingNode, dagConfig);
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

  private void processNode(N node, DAGConfig dagConfig) {
    if (!isProcessed(node) && parentsAreProcessed(node)) {
      NodeConfig nodeConfig = dagConfig.getNodeConfig(node.getIdentifier());
      node.setNodeConfig(nodeConfig);

      LOG.info("Submitting node -- {} -- for execution.", node.getIdentifier().toString());
      executorCompletionService.submit(node);
      runningNodes.put(node.getIdentifier(), node);
    }
  }

  private boolean isProcessed(Node node) {
    return processedNodes.contains(node.getIdentifier());
  }

  private boolean parentsAreProcessed(N node) {
    for (Object pNode : node.getIncomingNodes()) {
      if (!processedNodes.contains(((N) pNode).getIdentifier())) {
        return false;
      }
    }
    return true;
  }

  public N getNode(NodeIdentifier nodeIdentifier) {
    return runningNodes.get(nodeIdentifier);
  }
}
