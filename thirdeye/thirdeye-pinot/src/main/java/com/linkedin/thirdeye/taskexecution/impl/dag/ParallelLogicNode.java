package com.linkedin.thirdeye.taskexecution.impl.dag;

import com.linkedin.thirdeye.taskexecution.dag.FrameworkNode;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.impl.operator.ParallelProcessorRunner;

public class ParallelLogicNode<K, V> extends LogicalNode<K, V> {

  public ParallelLogicNode(String name, Class operatorClass) {
    super(name, operatorClass);
  }

  @Override
  public NodeIdentifier call() throws Exception {
    ParallelProcessorRunner<K, V> runner = new ParallelProcessorRunner<>(nodeIdentifier, nodeConfig, operatorClass);
    getPhysicalNode().add(runner);

    for (FrameworkNode pNode : this.getIncomingNodes()) {
      runner.addIncomingExecutionResultReader(pNode.getIdentifier(), pNode.getExecutionResultsReader());
    }

    return runner.call();
  }
}
