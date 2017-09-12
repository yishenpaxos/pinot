package com.linkedin.thirdeye.taskexecution.impl.operator;

import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.IterativeReader;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.KVReader;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.Reader;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.ExecutionStatus;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.FrameworkNode;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.NodeConfig;
import com.linkedin.thirdeye.taskexecution.operator.Operator;
import com.linkedin.thirdeye.taskexecution.operator.OperatorConfig;
import com.linkedin.thirdeye.taskexecution.operator.OperatorContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Deprecated
public class PartitionOperatorRunner<K, V> extends AbstractOperatorRunner {

  public PartitionOperatorRunner(NodeIdentifier nodeIdentifier, NodeConfig nodeConfig, Class operatorClass) {
    this(nodeIdentifier, nodeConfig, operatorClass, null);
  }

  public PartitionOperatorRunner(NodeIdentifier nodeIdentifier, NodeConfig nodeConfig, Class operatorClass,
      FrameworkNode logicalNode) {
    super(nodeIdentifier, nodeConfig, operatorClass, logicalNode);
  }

  @Override
  public KVReader getOutputReader() {
    return null;
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
      Map<NodeIdentifier, IterativeReader> incomingIterativeReaderMap = new HashMap<>();
      for (Map.Entry<NodeIdentifier, Reader> nodeIdentifierReaderEntry : getIncomingReaderMap().entrySet()) {
        incomingIterativeReaderMap.put(nodeIdentifierReaderEntry.getKey(),
            (IterativeReader) nodeIdentifierReaderEntry.getValue());
      }
      List<OperatorContext> operatorContexts = buildInputOperatorContext(nodeIdentifier, incomingIterativeReaderMap);
      // TODO: Submit each context to an individual thread
      for (OperatorContext operatorContext : operatorContexts) {
        for (int i = 0; i <= numRetry; ++i) {
          try {
            OperatorConfig operatorConfig = convertNodeConfigToOperatorConfig(nodeConfig);
            Operator operator = initializeOperator(operatorClass, operatorConfig);
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

  static List<OperatorContext> buildInputOperatorContext(NodeIdentifier nodeIdentifier,
      Map<NodeIdentifier, IterativeReader> incomingResultsReader) {
    // Experimental code for considering multi-threading
    Set keys = new HashSet();
//    for (KVReader resultsReader : incomingResultsReader.values()) {
//      while (resultsReader.hasNext()) {
//        keys.add(resultsReader.next().key());
//      }
//    }

    List<OperatorContext> operatorContexts = new ArrayList<>();
    for (Object key : keys) {
      OperatorContext operatorContext = new OperatorContext();
      operatorContext.setNodeIdentifier(nodeIdentifier);
//      for (NodeIdentifier pNodeIdentifier : incomingResultsReader.keySet()) {
//        KVReader reader = incomingResultsReader.get(pNodeIdentifier);
//        ExecutionResult executionResult = reader.get(key);
//        ExecutionResults executionResults = new ExecutionResults(pNodeIdentifier);
//        if (executionResult != null) {
//          executionResults.addResult(executionResult);
//        }
//        operatorContext.addReader(pNodeIdentifier, executionResults);
//      }
      operatorContexts.add(operatorContext);
    }

    // TODO: Refine the design to decide if empty input still generate one context in order to trigger the operator
    if (operatorContexts.isEmpty()) {
      operatorContexts.add(new OperatorContext(nodeIdentifier));
    }

    return operatorContexts;
  }
}
