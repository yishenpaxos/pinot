package com.linkedin.thirdeye.taskexecution.impl.executor;

import com.google.common.base.Preconditions;
import com.linkedin.thirdeye.taskexecution.dag.Edge;
import com.linkedin.thirdeye.taskexecution.dag.Node;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.executor.ExecutionContext;
import com.linkedin.thirdeye.taskexecution.executor.ExecutionEngine;
import com.linkedin.thirdeye.taskexecution.executor.ExecutionResult;
import com.linkedin.thirdeye.taskexecution.executor.NodeConfig;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;

public class DefaultExecutionEngine implements ExecutionEngine {

  private final ExecutorCompletionService<ExecutionResult> executorCompletionService;

  private Map<NodeIdentifier, OperatorRunner> runningNodes = new HashMap<>();

  public DefaultExecutionEngine(ExecutorService executorService) {
    Preconditions.checkNotNull(executorService);
    this.executorCompletionService = new ExecutorCompletionService<>(executorService);
  }

  @Override
  public void submit(ExecutionContext executionContext) {
    Preconditions.checkNotNull(executionContext);

    final Node node = executionContext.getNode();
    final NodeIdentifier nodeIdentifier = node.getIdentifier();
    final NodeConfig nodeConfig = executionContext.getNodeConfig();
    Preconditions.checkNotNull(node);
    Preconditions.checkNotNull(nodeIdentifier);
    Preconditions.checkNotNull(nodeConfig);

    OperatorRunner runner = createOperatorRunner(node, nodeConfig);
    executorCompletionService.submit(runner);
    runningNodes.put(nodeIdentifier, runner);
  }

  @Override
  public ExecutionResult take() throws InterruptedException, ExecutionException {
    ExecutionResult executionResult = executorCompletionService.take().get();
    runningNodes.remove(executionResult.getNodeIdentifier());
    return executionResult;
  }

  private OperatorRunner createOperatorRunner(Node<?, ? extends Edge> node, NodeConfig nodeConfig) {
    // Set up incoming channels for the runner
    OperatorRunner runner = new OperatorRunner(node.getOperator(), nodeConfig);
    Set<OperatorIOChannel> incomingChannels = new HashSet<>();
    for (Edge edge : node.getIncomingEdges()) {
      if (edge.getSourcePort() != null && edge.getSinkPort() != null) {
        OperatorIOChannel operatorIOChannel = new OperatorIOChannel();
        operatorIOChannel.connect(edge.getSourcePort(), edge.getSinkPort());
        incomingChannels.add(operatorIOChannel);
      }
    }
    runner.setIncomingChannels(incomingChannels);

    // Set up outgoing channels for the runner
    Set<OperatorIOChannel> outgoingChannels = new HashSet<>();
    for (Edge edge : node.getOutgoingEdges()) {
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
