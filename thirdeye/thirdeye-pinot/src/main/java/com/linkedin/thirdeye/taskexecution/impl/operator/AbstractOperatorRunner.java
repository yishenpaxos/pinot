package com.linkedin.thirdeye.taskexecution.impl.operator;

import com.linkedin.thirdeye.taskexecution.dataflow.reader.Reader;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.ExecutionStatus;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.NodeConfig;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.PhysicalEdge;
import com.linkedin.thirdeye.taskexecution.operator.Operator;
import com.linkedin.thirdeye.taskexecution.operator.OperatorConfig;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractOperatorRunner {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractOperatorRunner.class);

  protected NodeIdentifier nodeIdentifier = new NodeIdentifier();
  protected Operator operator;
  protected NodeConfig nodeConfig = new NodeConfig();
  private Map<NodeIdentifier, Reader> incomingReaderMap = new HashMap<>();
  protected ExecutionStatus executionStatus = ExecutionStatus.RUNNING;
  Set<PhysicalEdge> incomingEdge = Collections.emptySet();
  Set<PhysicalEdge> outgoingEdge = Collections.emptySet();

  public AbstractOperatorRunner(NodeIdentifier nodeIdentifier, NodeConfig nodeConfig, Operator operator) {
    this.nodeIdentifier = nodeIdentifier;
    this.nodeConfig = nodeConfig;
    this.operator = operator;
  }

  public void setIncomingEdge(Set<PhysicalEdge> incomingEdge) {
    this.incomingEdge = incomingEdge;
  }

  public void setOutgoingEdge(Set<PhysicalEdge> outgoingEdge) {
    this.outgoingEdge = outgoingEdge;
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
}
