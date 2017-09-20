package com.linkedin.thirdeye.taskexecution.impl.executor;

import com.linkedin.thirdeye.anomaly.utils.AnomalyUtils;
import com.linkedin.thirdeye.taskexecution.dag.DAG;
import com.linkedin.thirdeye.taskexecution.dag.Edge;
import com.linkedin.thirdeye.taskexecution.dag.Node;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.DAGConfig;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.ExecutionStatus;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.NodeConfig;
import java.util.Collection;
import java.util.Collections;
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
 * An executor that goes through the DAG and submit the nodes, whose upstream dependency (parents) are finished, to
 * the given execution service. An executor only submits the tasks depending on the flow of the DAG and and execution
 * status of nodes.
 *
 * (Future work) The actual execution should be run on other classes.
 */
public class DAGExecutor {
  private static final Logger LOG = LoggerFactory.getLogger(DAGExecutor.class);
  private ExecutorCompletionService<NodeIdentifier> executorCompletionService;

  // TODO: Persistent the following status to a DB in case of executor unexpectedly dies
  private Set<NodeIdentifier> processedNodes = new HashSet<>();
  private Map<NodeIdentifier, OperatorRunner> runningNodes = new HashMap<>();

  public DAGExecutor(ExecutorService executorService) {
    this.executorCompletionService = new ExecutorCompletionService<>(executorService);
  }

  public <N extends Node<N, E>, E extends Edge> void execute(DAG<N, E> dag, DAGConfig dagConfig) {
    Collection<N> nodes = dag.getStartNodes();
    for (Node node : nodes) {
      checkAndRunNode(node, dagConfig, Collections.<Node>emptySet());
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

  private <N extends Node<N, E>, E extends Edge> void checkAndRunNode(Node<N, E> node, DAGConfig dagConfig,
      Set<? extends Node> upstreamDependency) {
    if (!isFinished(node) && parentsAreFinished(upstreamDependency)) {
      LOG.info("Submitting node -- {} -- for execution.", node.getIdentifier().toString());
      NodeConfig nodeConfig = dagConfig.getNodeConfig(node.getIdentifier());
      OperatorRunner runner = createOperatorRunner(node, nodeConfig);
      // Submit runner
      executorCompletionService.submit(runner);
      runningNodes.put(node.getIdentifier(), runner);
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

  // TODO: Decouple OperatorRunner (which is run on local machine) from DAG Executor
  // TODO: Simplify method signature
  private <N extends Node<N, E>, E extends Edge> OperatorRunner createOperatorRunner(Node<N, E> node,
      NodeConfig nodeConfig) {
    // Set up incoming channels for the runner
    OperatorRunner runner = new OperatorRunner(nodeConfig, node.getOperator());
    Set<OperatorIOChannel> incomingChannels = new HashSet<>();
    for (E edge : node.getIncomingEdges()) {
      if (edge.getSourcePort() != null && edge.getSinkPort() != null) {
        OperatorIOChannel operatorIOChannel = new OperatorIOChannel();
        operatorIOChannel.connect(edge.getSourcePort(), edge.getSinkPort());
        incomingChannels.add(operatorIOChannel);
      }
    }
    runner.setIncomingChannels(incomingChannels);

    // Set up outgoing channels for the runner
    Set<OperatorIOChannel> outgoingChannels = new HashSet<>();
    for (E edge : node.getOutgoingEdges()) {
      if (edge.getSourcePort() != null && edge.getSinkPort() != null) {
        OperatorIOChannel operatorIOChannel = new OperatorIOChannel();
        operatorIOChannel.connect(edge.getSourcePort(), edge.getSinkPort());
        outgoingChannels.add(operatorIOChannel);
      }
    }
    runner.setOutgoingChannels(outgoingChannels);

    return runner;
  }
}
