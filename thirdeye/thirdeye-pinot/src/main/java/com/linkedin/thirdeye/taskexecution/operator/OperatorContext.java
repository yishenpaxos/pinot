package com.linkedin.thirdeye.taskexecution.operator;

import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.Reader;
import java.util.HashMap;
import java.util.Map;

public class OperatorContext {
  private NodeIdentifier nodeIdentifier;

  public OperatorContext() {
  }

  public OperatorContext(NodeIdentifier nodeIdentifier) {
    setNodeIdentifier(nodeIdentifier);
  }

  public NodeIdentifier getNodeIdentifier() {
    return nodeIdentifier;
  }

  public void setNodeIdentifier(NodeIdentifier nodeIdentifier) {
    this.nodeIdentifier = nodeIdentifier;
  }
}
