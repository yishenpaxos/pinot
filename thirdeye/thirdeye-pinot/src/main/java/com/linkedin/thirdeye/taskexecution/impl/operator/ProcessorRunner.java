package com.linkedin.thirdeye.taskexecution.impl.operator;

import com.linkedin.thirdeye.taskexecution.dag.FrameworkNode;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.dataflow.ExecutionResult;
import com.linkedin.thirdeye.taskexecution.dataflow.ExecutionResults;
import com.linkedin.thirdeye.taskexecution.dataflow.ExecutionResultsReader;
import com.linkedin.thirdeye.taskexecution.impl.dag.ExecutionStatus;
import com.linkedin.thirdeye.taskexecution.impl.dataflow.InMemoryExecutionResultsReader;
import com.linkedin.thirdeye.taskexecution.impl.dag.NodeConfig;
import com.linkedin.thirdeye.taskexecution.processor.Processor;
import com.linkedin.thirdeye.taskexecution.processor.ProcessorConfig;
import com.linkedin.thirdeye.taskexecution.processor.ProcessorContext;
import java.util.Map;

/**
 * ProcessorRunner considers multi-threading.
 */
public class ProcessorRunner<K, V> extends AbstractProcessorRunner {

  private ExecutionResults<K, V> executionResults;


  public ProcessorRunner(NodeIdentifier nodeIdentifier, NodeConfig nodeConfig, Class operatorClass) {
    this(nodeIdentifier, nodeConfig, operatorClass, null);
  }

  private ProcessorRunner(NodeIdentifier nodeIdentifier, NodeConfig nodeConfig, Class operatorClass,
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
   * @return the node identifier of this node (i.e., ProcessorRunner).
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
          ProcessorConfig processorConfig = convertNodeConfigToOperatorConfig(nodeConfig);
          Processor processor = initializeOperator(operatorClass, processorConfig);
          ProcessorContext processorContext =
              buildInputOperatorContext(nodeIdentifier, incomingResultsReaderMap, nodeConfig.runWithEmptyInput());
          if (processorContext != null) {
            ExecutionResult<K, V> operatorResult = processor.run(processorContext);
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

  static ProcessorContext buildInputOperatorContext(NodeIdentifier nodeIdentifier,
      Map<NodeIdentifier, ExecutionResultsReader> incomingResultsReader, boolean allowEmptyIncomingResult) {

    ProcessorContext processorContext = new ProcessorContext();
    processorContext.setNodeIdentifier(nodeIdentifier);
    for (Map.Entry<NodeIdentifier, ExecutionResultsReader> nodeReadersEntry : incomingResultsReader.entrySet()) {
      ExecutionResults executionResults = new ExecutionResults<>(nodeReadersEntry.getKey());
      ExecutionResultsReader reader = nodeReadersEntry.getValue();
      while (reader.hasNext()) {
        executionResults.addResult(reader.next());
      }
      if (executionResults.size() > 0) {
        processorContext.addResults(nodeReadersEntry.getKey(), executionResults);
      }
    }
    if (processorContext.size() != 0 || allowEmptyIncomingResult) {
      return processorContext;
    } else {
      return null;
    }
  }
}
