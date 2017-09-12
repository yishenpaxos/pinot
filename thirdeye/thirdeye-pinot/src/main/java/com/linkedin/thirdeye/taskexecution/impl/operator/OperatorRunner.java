package com.linkedin.thirdeye.taskexecution.impl.operator;

import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.Reader;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.SimpleReader;
import com.linkedin.thirdeye.taskexecution.impl.dataflow.InMemorySimpleReader;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.ExecutionStatus;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.FrameworkNode;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.NodeConfig;
import com.linkedin.thirdeye.taskexecution.operator.ExecutionResult;
import com.linkedin.thirdeye.taskexecution.operator.Operator;
import com.linkedin.thirdeye.taskexecution.operator.OperatorConfig;
import com.linkedin.thirdeye.taskexecution.operator.OperatorContext;
import java.util.HashMap;
import java.util.Map;

/**
 * OperatorRunner considers multi-threading.
 */
public class OperatorRunner<V> extends AbstractOperatorRunner {
  ExecutionResult<V> operatorResult = new ExecutionResult<>();

  public OperatorRunner(NodeIdentifier nodeIdentifier, NodeConfig nodeConfig, Class operatorClass) {
    this(nodeIdentifier, nodeConfig, operatorClass, null);
  }

  private OperatorRunner(NodeIdentifier nodeIdentifier, NodeConfig nodeConfig, Class operatorClass,
      FrameworkNode logicalNode) {
    super(ensureNonNullNodeIdentifier(nodeIdentifier), nodeConfig, operatorClass, logicalNode);
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
  public Reader getOutputReader() {
    return new InMemorySimpleReader<>(operatorResult.result());
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
          Map<NodeIdentifier, SimpleReader> incomingSimpleReaderMap = new HashMap<>();
          for (Map.Entry<NodeIdentifier, Reader> nodeIdentifierReaderEntry : getIncomingReaderMap().entrySet()) {
            incomingSimpleReaderMap.put(nodeIdentifierReaderEntry.getKey(),
                (SimpleReader) nodeIdentifierReaderEntry.getValue());
          }
          OperatorContext operatorContext =
              buildInputOperatorContext(nodeIdentifier, incomingSimpleReaderMap, nodeConfig.runWithEmptyInput());
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
    return identifier;
  }

  static OperatorContext buildInputOperatorContext(NodeIdentifier nodeIdentifier,
      Map<NodeIdentifier, SimpleReader> incomingReader, boolean allowEmptyIncomingResult) {

    OperatorContext operatorContext = new OperatorContext(nodeIdentifier);
    for (Map.Entry<NodeIdentifier, SimpleReader> nodeReadersEntry : incomingReader.entrySet()) {
        operatorContext.addReader(nodeReadersEntry.getKey(), nodeReadersEntry.getValue());
    }

    if (operatorContext.size() != 0 || allowEmptyIncomingResult) {
      return operatorContext;
    } else {
      return null;
    }
  }
}
