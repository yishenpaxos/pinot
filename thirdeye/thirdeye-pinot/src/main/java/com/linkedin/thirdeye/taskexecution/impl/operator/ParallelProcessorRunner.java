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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ParallelProcessorRunner<K, V> extends AbstractProcessorRunner {

  private ExecutionResults<K, V> executionResults;

  public ParallelProcessorRunner(NodeIdentifier nodeIdentifier, NodeConfig nodeConfig, Class operatorClass) {
    this(nodeIdentifier, nodeConfig, operatorClass, null);
  }

  public ParallelProcessorRunner(NodeIdentifier nodeIdentifier, NodeConfig nodeConfig, Class operatorClass,
      FrameworkNode logicalNode) {
    super(nodeIdentifier, nodeConfig, operatorClass, logicalNode);
    this.executionResults = new ExecutionResults<>(nodeIdentifier);
  }

  @Override
  public ExecutionResultsReader getExecutionResultsReader() {
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
      List<ProcessorContext> processorContexts = buildInputOperatorContext(nodeIdentifier, incomingResultsReaderMap);
      // TODO: Submit each context to an individual thread
      for (ProcessorContext processorContext : processorContexts) {
        for (int i = 0; i <= numRetry; ++i) {
          try {
            ProcessorConfig processorConfig = convertNodeConfigToOperatorConfig(nodeConfig);
            Processor processor = initializeOperator(operatorClass, processorConfig);
            ExecutionResult<K, V> operatorResult = processor.run(processorContext);
            // Assume that each processor generates a result with non-duplicated key
            executionResults.addResult(operatorResult);
          } catch (Exception e) {
            if (i == numRetry) {
              setFailure(e);
            }
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

  static List<ProcessorContext> buildInputOperatorContext(NodeIdentifier nodeIdentifier,
      Map<NodeIdentifier, ExecutionResultsReader> incomingResultsReader) {
    // Experimental code for considering multi-threading
    Set keys = new HashSet();
    for (ExecutionResultsReader resultsReader : incomingResultsReader.values()) {
      while (resultsReader.hasNext()) {
        keys.add(resultsReader.next().key());
      }
    }

    List<ProcessorContext> processorContexts = new ArrayList<>();
    for (Object key : keys) {
      ProcessorContext processorContext = new ProcessorContext();
      processorContext.setNodeIdentifier(nodeIdentifier);
      for (NodeIdentifier pNodeIdentifier : incomingResultsReader.keySet()) {
        ExecutionResultsReader reader = incomingResultsReader.get(pNodeIdentifier);
        ExecutionResult executionResult = reader.get(key);
        ExecutionResults executionResults = new ExecutionResults(pNodeIdentifier);
        if (executionResult != null) {
          executionResults.addResult(executionResult);
        }
        processorContext.addResults(pNodeIdentifier, executionResults);
      }
      processorContexts.add(processorContext);
    }

    // TODO: Refine the design to decide if empty input still generate one context in order to trigger the operator
    if (processorContexts.isEmpty()) {
      processorContexts.add(new ProcessorContext(nodeIdentifier));
    }

    return processorContexts;
  }
}
