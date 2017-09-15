package com.linkedin.thirdeye.taskexecution.impl.operator;

import com.google.common.base.Preconditions;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.Reader;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.ExecutionStatus;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.NodeConfig;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.PhysicalEdge;
import com.linkedin.thirdeye.taskexecution.operator.Operator;
import com.linkedin.thirdeye.taskexecution.operator.OperatorConfig;
import com.linkedin.thirdeye.taskexecution.operator.OperatorContext;
import java.util.Map;

/**
 * OperatorRunner is a wrapper class to setup input and gather the output data of a operator.
 */
public class OperatorRunner<V> extends AbstractOperatorRunner {

  public OperatorRunner(NodeIdentifier nodeIdentifier, NodeConfig nodeConfig, Operator operator) {
    super(ensureNonNullNodeIdentifier(nodeIdentifier), nodeConfig, operator);
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

  /**
   * Invokes the execution of the operator that is define for the corresponding node in the DAG and returns its node
   * identifier.
   *
   * @return the node identifier of this node (i.e., OperatorRunner).
   */
  public NodeIdentifier call() {
    try {
      Preconditions.checkNotNull(nodeIdentifier, "Node identifier cannot be null");
      int numRetry = nodeConfig.numRetryAtError();
      for (int i = 0; i <= numRetry; ++i) {
        try {
          for (PhysicalEdge edge : incomingEdge) {
            edge.initRead();
          }

          OperatorConfig operatorConfig = convertNodeConfigToOperatorConfig(nodeConfig);
          operator.initialize(operatorConfig);
          OperatorContext operatorContext = buildInputOperatorContext(nodeIdentifier, getIncomingReaderMap());
          operator.run(operatorContext);

          for (PhysicalEdge edge : outgoingEdge) {
            edge.flush();
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
      Map<NodeIdentifier, Reader> incomingReader) {

    OperatorContext operatorContext = new OperatorContext(nodeIdentifier);
    for (Map.Entry<NodeIdentifier, Reader> nodeReadersEntry : incomingReader.entrySet()) {
      operatorContext.addReader(nodeReadersEntry.getKey(), nodeReadersEntry.getValue());
    }

    return operatorContext;
  }
}
