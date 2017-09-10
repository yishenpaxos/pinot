package com.linkedin.thirdeye.taskexecution.impl.physicaldag;

import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import java.util.HashMap;
import java.util.Map;

public class DAGConfig {
  private boolean stopAtFailure = true;
  private Map<NodeIdentifier, NodeConfig> nodeConfigs = new HashMap<>();

  public boolean stopAtFailure() {
    return stopAtFailure;
  }

  public void setStopAtFailure(boolean stopAtFailure) {
    this.stopAtFailure = stopAtFailure;
  }

  public void putNodeConfig(NodeIdentifier nodeIdentifier, NodeConfig nodeConfig) {
    nodeConfigs.put(nodeIdentifier, nodeConfig);
  }

  public NodeConfig getNodeConfig(NodeIdentifier nodeIdentifier) {
    if (nodeConfigs.containsKey(nodeIdentifier)) {
      return nodeConfigs.get(nodeIdentifier);
    } else {
      return new NodeConfig();
    }
  }
}
