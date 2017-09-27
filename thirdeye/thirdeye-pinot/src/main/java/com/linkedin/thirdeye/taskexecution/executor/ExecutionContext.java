package com.linkedin.thirdeye.taskexecution.executor;

import com.google.common.base.Preconditions;
import com.linkedin.thirdeye.taskexecution.dag.Node;

public class ExecutionContext {
  private Node node;
  private NodeConfig nodeConfig;

  public ExecutionContext(Node node, NodeConfig nodeConfig) {
    setNode(node);
    setNodeConfig(nodeConfig);
  }

  public Node getNode() {
    return node;
  }

  public void setNode(Node node) {
    Preconditions.checkNotNull(node);
    this.node = node;
  }

  public NodeConfig getNodeConfig() {
    return nodeConfig;
  }

  public void setNodeConfig(NodeConfig nodeConfig) {
    Preconditions.checkNotNull(nodeConfig);
    this.nodeConfig = nodeConfig;
  }
}
