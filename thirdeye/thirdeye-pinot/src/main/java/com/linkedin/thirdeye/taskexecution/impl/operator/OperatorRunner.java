package com.linkedin.thirdeye.taskexecution.impl.operator;

import com.linkedin.thirdeye.taskexecution.dag.physical.FrameworkNode;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.dataflow.ExecutionResult;
import com.linkedin.thirdeye.taskexecution.dataflow.ExecutionResults;
import com.linkedin.thirdeye.taskexecution.dataflow.ExecutionResultsReader;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.ExecutionStatus;
import com.linkedin.thirdeye.taskexecution.impl.dataflow.InMemoryExecutionResultsReader;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.NodeConfig;
import com.linkedin.thirdeye.taskexecution.operator.Operator;
import com.linkedin.thirdeye.taskexecution.operator.OperatorConfig;
import com.linkedin.thirdeye.taskexecution.operator.OperatorContext;
import java.util.Map;

/**
 * OperatorRunner considers multi-threading.
 */
public class OperatorRunner<K, V> extends AbstractOperatorRunner {

  private ExecutionResults<K, V> executionResults;


  public OperatorRunner(NodeIdentifier nodeIdentifier, NodeConfig nodeConfig, Class operatorClass) {
    this(nodeIdentifier, nodeConfig, operatorClass, null);
  }

  private OperatorRunner(NodeIdentifier nodeIdentifier, NodeConfig nodeConfig, Class operatorClass,
      FrameworkNode logicalNode) {
    super(ensureNonNullNodeIdentifier(nodeIdentifier), nodeConfig, operatorClass, logicalNode);
    this.executionResults = new ExecutionResults<>(nodeIdentifier);
  }

  private static NodeIdentifier ensureNonNullNodeIdentifier(NodeIdentifier nodeIdentifier) {
    if (nodeIdentifier == null) {
      nodeIdentifier = new NodeIdentifier("Null Identifier");
    }
    return nodeIdentifier;
  }

  @Override
  public ExecutionStatus getExecutionStatus() {
    return executionStatus;
  }

  @Override
  public ExecutionResultsReader<K, V> getExecutionResultsReader() {
    return new InMemoryExecutionResultsReader<>(executionResults);
  }

  /**
   * Invokes the execution of the operator that is define for the corresponding node in the DAG and returns its node
   * identifier.
   *
   * @return the node identifier of this node (i.e., OperatorRunner).
   */
  @Override
  public NodeIdentifier call() {
    NodeIdentifier identifier = null;
    try {
      identifier = getIdentifier();
      if (identifier == null) {
        throw new IllegalArgumentException("Node identifier cannot be null");
      }
      int numRetry = nodeConfig.numRetryAtError();
      for (int i = 0; i <= numRetry; ++i) {
        try {
          OperatorConfig operatorConfig = convertNodeConfigToOperatorConfig(nodeConfig);
          Operator operator = initializeOperator(operatorClass, operatorConfig);
          OperatorContext operatorContext =
              buildInputOperatorContext(nodeIdentifier, incomingResultsReaderMap, nodeConfig.runWithEmptyInput());
          if (operatorContext != null) {
            ExecutionResult<K, V> operatorResult = operator.run(operatorContext);
            executionResults.addResult(operatorResult);
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
    return identifier;
  }

  static OperatorContext buildInputOperatorContext(NodeIdentifier nodeIdentifier,
      Map<NodeIdentifier, ExecutionResultsReader> incomingResultsReader, boolean allowEmptyIncomingResult) {

    OperatorContext operatorContext = new OperatorContext();
    operatorContext.setNodeIdentifier(nodeIdentifier);
    for (Map.Entry<NodeIdentifier, ExecutionResultsReader> nodeReadersEntry : incomingResultsReader.entrySet()) {
      ExecutionResults executionResults = new ExecutionResults<>(nodeReadersEntry.getKey());
      ExecutionResultsReader reader = nodeReadersEntry.getValue();
      while (reader.hasNext()) {
        executionResults.addResult(reader.next());
      }
      if (executionResults.size() > 0) {
        operatorContext.addResults(nodeReadersEntry.getKey(), executionResults);
      }
    }
    if (operatorContext.size() != 0 || allowEmptyIncomingResult) {
      return operatorContext;
    } else {
      return null;
    }
  }
}
