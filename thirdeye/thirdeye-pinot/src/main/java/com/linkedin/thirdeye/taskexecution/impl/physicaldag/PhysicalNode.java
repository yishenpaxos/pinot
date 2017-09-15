package com.linkedin.thirdeye.taskexecution.impl.physicaldag;

import com.linkedin.thirdeye.taskexecution.dag.AbstractNode;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.impl.dataflow.InMemoryCollectionReader;
import com.linkedin.thirdeye.taskexecution.impl.operator.OperatorRunner;
import com.linkedin.thirdeye.taskexecution.operator.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A PhysicalNode that executes work using one partition.
 */
public class PhysicalNode<OP extends Operator> extends AbstractNode<PhysicalNode, PhysicalEdge> {
  private static final Logger LOG = LoggerFactory.getLogger(PhysicalNode.class);

  private OP operator;
  private NodeConfig nodeConfig = new NodeConfig();

  private OperatorRunner runner;

  public PhysicalNode(String name, Class operatorClass) throws IllegalAccessException, InstantiationException {
    this(new NodeIdentifier(name), operatorClass);
  }

  public PhysicalNode(NodeIdentifier nodeIdentifier, Class operatorClass)
      throws InstantiationException, IllegalAccessException {
    this(nodeIdentifier, (OP) initiateOperatorInstance(operatorClass));
  }

  public PhysicalNode(String name, OP operator) {
    this(new NodeIdentifier(name), operator);
  }

  public PhysicalNode(NodeIdentifier nodeIdentifier, OP operator) {
    super(nodeIdentifier);
    this.operator = operator;
  }

  private static Operator initiateOperatorInstance(Class operatorClass)
      throws IllegalAccessException, InstantiationException {
    try {
      return (Operator) operatorClass.newInstance();
    } catch (Exception e) {
      // We cannot do anything if something bad happens here excepting rethrow the exception.
      LOG.warn("Failed to initialize {}", operatorClass.getName());
      throw e;
    }
  }

  public NodeConfig getNodeConfig() {
    return nodeConfig;
  }

  public void setNodeConfig(NodeConfig nodeConfig) {
    this.nodeConfig = nodeConfig;
  }

  public OP getOperator() {
    return operator;
  }

  @Override
  public ExecutionStatus getExecutionStatus() {
    if (runner != null) {
      return runner.getExecutionStatus();
    } else {
      new InMemoryCollectionReader<>();
    }
    return ExecutionStatus.SKIPPED;
  }

  @Override
  public NodeIdentifier call() throws Exception {
    runner = new OperatorRunner<>(nodeIdentifier, nodeConfig, operator);
    runner.setIncomingEdge(incomingEdge);
    runner.setOutgoingEdge(outgoingEdge);

    return runner.call();
  }
}
