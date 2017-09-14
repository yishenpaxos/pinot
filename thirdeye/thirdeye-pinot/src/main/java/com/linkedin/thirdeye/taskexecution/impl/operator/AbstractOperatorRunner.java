package com.linkedin.thirdeye.taskexecution.impl.operator;

import com.linkedin.thirdeye.taskexecution.dataflow.reader.Reader;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.ExecutionStatus;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.NodeConfig;
import com.linkedin.thirdeye.taskexecution.operator.Operator;
import com.linkedin.thirdeye.taskexecution.operator.OperatorConfig;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractOperatorRunner {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractOperatorRunner.class);

  protected NodeIdentifier nodeIdentifier = new NodeIdentifier();
  protected Class operatorClass;
  protected NodeConfig nodeConfig = new NodeConfig();
  private Map<NodeIdentifier, Reader> incomingReaderMap = new HashMap<>();
  protected ExecutionStatus executionStatus = ExecutionStatus.RUNNING;

  public AbstractOperatorRunner(NodeIdentifier nodeIdentifier, NodeConfig nodeConfig, Class operatorClass) {
    this.nodeIdentifier = nodeIdentifier;
    this.nodeConfig = nodeConfig;
    this.operatorClass = operatorClass;
  }

  public void addInput(NodeIdentifier nodeIdentifier, Reader reader) {
    incomingReaderMap.put(nodeIdentifier, reader);
  }

  public Map<NodeIdentifier, Reader> getIncomingReaderMap() {
    return incomingReaderMap;
  }

  public ExecutionStatus getExecutionStatus() {
    return executionStatus;
  }

  protected void setFailure(Exception e) {
    LOG.error("Failed to execute node: {}.", nodeIdentifier, e);
    if (nodeConfig.skipAtFailure()) {
      executionStatus = ExecutionStatus.SKIPPED;
    } else {
      executionStatus = ExecutionStatus.FAILED;
    }
  }

  // TODO: Implement this method
  static OperatorConfig convertNodeConfigToOperatorConfig(NodeConfig nodeConfig) {
    return null;
  }

  static Operator initiateOperatorInstance(Class operatorClass)
      throws IllegalAccessException, InstantiationException {
    try {
      return (Operator) operatorClass.newInstance();
    } catch (Exception e) {
      // We cannot do anything if something bad happens here excepting rethrow the exception.
      LOG.warn("Failed to initialize {}", operatorClass.getName());
      throw e;
    }
  }
}
