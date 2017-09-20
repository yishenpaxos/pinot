package com.linkedin.thirdeye.taskexecution.impl.executor;

import com.linkedin.thirdeye.taskexecution.dag.DAG;
import com.linkedin.thirdeye.taskexecution.dag.Node;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.impl.operator.OperatorRunner;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.Channel;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.DAGConfig;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.ExecutionStatus;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.NodeConfig;
import com.linkedin.thirdeye.taskexecution.impl.operator.OperatorIOChannel;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.PhysicalNode;
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
public class LocalDAGExecutor {
  private static final Logger LOG = LoggerFactory.getLogger(LocalDAGExecutor.class);
  private ExecutorCompletionService<NodeIdentifier> executorCompletionService;

  // TODO: Persistent the following status to a DB in case of executor unexpectedly dies
  private Set<NodeIdentifier> processedNodes = new HashSet<>();
  private Map<NodeIdentifier, OperatorRunner> runningNodes = new HashMap<>();


  public LocalDAGExecutor(ExecutorService executorService) {
    this.executorCompletionService = new ExecutorCompletionService<>(executorService);
  }

  public <N extends PhysicalNode> void execute(DAG<N> dag, DAGConfig dagConfig) {
    Collection<N> nodes = dag.getRootNodes();
    for (PhysicalNode node : nodes) {
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
        PhysicalNode node = dag.getNode(nodeIdentifier);
        for (PhysicalNode outGoingNode : node.getOutgoingNodes()) {
          processNode(outGoingNode, dagConfig);
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

  private void processNode(PhysicalNode node, DAGConfig dagConfig) {
    if (!isProcessed(node) && parentsAreProcessed(node)) {
      NodeConfig nodeConfig = dagConfig.getNodeConfig(node.getIdentifier());
      node.setNodeConfig(nodeConfig);

      LOG.info("Submitting node -- {} -- for execution.", node.getIdentifier().toString());

      // Set up incoming channels for the runner
      OperatorRunner runner = new OperatorRunner(nodeConfig, node.getOperator());
      Set<OperatorIOChannel> incomingChannels = new HashSet<>();
      for (Channel edge : node.getIncomingEdges()) {
        if (edge.getSourcePort() != null && edge.getSinkPort() != null) {
          OperatorIOChannel operatorIOChannel = new OperatorIOChannel();
          operatorIOChannel.connect(edge.getSourcePort(), edge.getSinkPort());
          incomingChannels.add(operatorIOChannel);
        }
      }
      runner.setIncomingChannels(incomingChannels);

      // Set up outgoing channels for the runner
      Set<OperatorIOChannel> outgoingChannels = new HashSet<>();
      for (Channel edge : node.getOutgoingEdges()) {
        if (edge.getSourcePort() != null && edge.getSinkPort() != null) {
          OperatorIOChannel operatorIOChannel = new OperatorIOChannel();
          operatorIOChannel.connect(edge.getSourcePort(), edge.getSinkPort());
          outgoingChannels.add(operatorIOChannel);
        }
      }
      runner.setOutgoingChannels(outgoingChannels);

      // Submit runner
      executorCompletionService.submit(runner);

      runningNodes.put(node.getIdentifier(), runner);
    }
  }

  private boolean isProcessed(Node node) {
    return processedNodes.contains(node.getIdentifier());
  }

  private boolean parentsAreProcessed(PhysicalNode node) {
    for (PhysicalNode pNode : node.getIncomingNodes()) {
      if (!processedNodes.contains(pNode.getIdentifier())) {
        return false;
      }
    }
    return true;
  }
}
