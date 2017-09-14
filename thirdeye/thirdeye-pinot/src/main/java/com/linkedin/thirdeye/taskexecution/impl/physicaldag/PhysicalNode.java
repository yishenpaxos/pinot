package com.linkedin.thirdeye.taskexecution.impl.physicaldag;

import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.Reader;
import com.linkedin.thirdeye.taskexecution.impl.dataflow.InMemorySimpleReader;
import com.linkedin.thirdeye.taskexecution.impl.operator.OperatorRunner;
import com.linkedin.thirdeye.taskexecution.operator.ExecutionResult;

/**
 * A PhysicalNode that executes work using one partition.
 */
public class PhysicalNode<V> extends AbstractPhysicalNode<PhysicalNode> {

  private OperatorRunner<V> runner;

  public PhysicalNode(String name, Class operatorClass) {
    this(new NodeIdentifier(name), operatorClass);
  }

  public PhysicalNode(NodeIdentifier nodeIdentifier, Class operatorClass) {
    super(nodeIdentifier, operatorClass);
  }

  @Override
  public ExecutionStatus getExecutionStatus() {
    if (runner != null) {
      return runner.getExecutionStatus();
    } else {
      new InMemorySimpleReader<>();
    }
    return ExecutionStatus.SKIPPED;
  }

  @Override
  public Reader getOutputReader() {
    if (runner != null) {
      ExecutionResult operatorResult = runner.getOperatorResult();
      if (operatorResult != null) {
        return new InMemorySimpleReader<>(operatorResult.result());
      }
    }
    return new InMemorySimpleReader<>();
  }

  @Override
  public NodeIdentifier call() throws Exception {
    runner = new OperatorRunner<>(nodeIdentifier, nodeConfig, operatorClass);

    for (PhysicalNode pNode : this.getIncomingNodes()) {
      // TODO: Get output (writer) from parents and construct inputs (readers)

      // TODO: Add node identifier to port identifier mapping
      runner.addInput(pNode.getIdentifier(), pNode.getOutputReader());
    }

    return runner.call();
  }
}
