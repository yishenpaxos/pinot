package com.linkedin.thirdeye.taskexecution.impl.operator;

import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.Reader;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.ExecutionStatus;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.NodeConfig;
import com.linkedin.thirdeye.taskexecution.operator.ExecutionResult;
import com.linkedin.thirdeye.taskexecution.operator.Operator;
import com.linkedin.thirdeye.taskexecution.operator.OperatorConfig;
import com.linkedin.thirdeye.taskexecution.operator.OperatorContext;
import java.util.Map;

/**
 * OperatorRunner is a wrapper class to setup input and gather the output data of a operator.
 */
public class OperatorRunner<V> extends AbstractOperatorRunner {
  private ExecutionResult<V> operatorResult;

  public OperatorRunner(NodeIdentifier nodeIdentifier, NodeConfig nodeConfig, Class operatorClass) {
    super(ensureNonNullNodeIdentifier(nodeIdentifier), nodeConfig, operatorClass);
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

  public ExecutionResult<V> getOperatorResult() {
    return operatorResult;
  }

  /**
   * Invokes the execution of the operator that is define for the corresponding node in the DAG and returns its node
   * identifier.
   *
   * @return the node identifier of this node (i.e., OperatorRunner).
   */
  public NodeIdentifier call() {
    try {
      if (nodeIdentifier == null) {
        throw new IllegalArgumentException("Node identifier cannot be null");
      }
      int numRetry = nodeConfig.numRetryAtError();
      for (int i = 0; i <= numRetry; ++i) {
        try {
          OperatorConfig operatorConfig = convertNodeConfigToOperatorConfig(nodeConfig);
          Operator operator = initializeOperator(operatorClass, operatorConfig);
          OperatorContext operatorContext =
              buildInputOperatorContext(nodeIdentifier, getIncomingReaderMap(), nodeConfig.runWithEmptyInput());
          if (operatorContext != null) {
            operatorResult = operator.run(operatorContext);
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

  static OperatorContext buildInputOperatorContext(NodeIdentifier nodeIdentifier,
      Map<NodeIdentifier, Reader> incomingReader, boolean allowEmptyIncomingResult) {

    OperatorContext operatorContext = new OperatorContext(nodeIdentifier);
    for (Map.Entry<NodeIdentifier, Reader> nodeReadersEntry : incomingReader.entrySet()) {
        operatorContext.addReader(nodeReadersEntry.getKey(), nodeReadersEntry.getValue());
    }

    if (operatorContext.size() != 0 || allowEmptyIncomingResult) {
      return operatorContext;
    } else {
      return null;
    }
  }
}
