package com.linkedin.thirdeye.taskexecution.processor;

import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.dataflow.ExecutionContext;
import com.linkedin.thirdeye.taskexecution.dataflow.ExecutionResults;
import java.util.HashMap;
import java.util.Map;

public class ProcessorContext implements ExecutionContext<ExecutionResults> {
  private NodeIdentifier nodeIdentifier;
  private Map<NodeIdentifier, ExecutionResults> inputs = new HashMap<>();

  public ProcessorContext() {
  }

  public ProcessorContext(NodeIdentifier nodeIdentifier) {
    this.nodeIdentifier = nodeIdentifier;
  }

  @Override
  public NodeIdentifier getNodeIdentifier() {
    return nodeIdentifier;
  }

  @Override
  public void setNodeIdentifier(NodeIdentifier nodeIdentifier) {
    this.nodeIdentifier = nodeIdentifier;
  }

  @Override
  public Map<NodeIdentifier, ExecutionResults> getInputs() {
    return inputs;
  }

  @Override
  public void addResults(NodeIdentifier identifier, ExecutionResults operatorResult) {
    inputs.put(identifier, operatorResult);
  }

  @Override
  public int size() {
    return inputs.size();
  }
}
