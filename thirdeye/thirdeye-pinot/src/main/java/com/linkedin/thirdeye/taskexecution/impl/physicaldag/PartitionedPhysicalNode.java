package com.linkedin.thirdeye.taskexecution.impl.physicaldag;

import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.impl.operator.PartitionOperatorRunner;

@Deprecated
public class PartitionedPhysicalNode<K, V> extends PhysicalNode {

  public PartitionedPhysicalNode(String name, Class operatorClass) {
    super(name, operatorClass);
  }

  @Override
  public NodeIdentifier call() throws Exception {
    PartitionOperatorRunner<K, V> runner = new PartitionOperatorRunner<>(nodeIdentifier, nodeConfig, operatorClass);
    getPhysicalNode().add(runner);

    for (FrameworkNode pNode : this.getIncomingNodes()) {
      runner.addInput(pNode.getIdentifier(), pNode.getOutputReader());
    }

    return runner.call();
  }
}
