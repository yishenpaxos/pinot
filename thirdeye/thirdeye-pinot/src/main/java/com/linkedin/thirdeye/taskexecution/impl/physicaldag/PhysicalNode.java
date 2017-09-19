package com.linkedin.thirdeye.taskexecution.impl.physicaldag;

import com.linkedin.thirdeye.taskexecution.dag.AbstractNode;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
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

  public PhysicalNode(String name, OP operator) {
    this(new NodeIdentifier(name), operator);
  }

  public PhysicalNode(NodeIdentifier nodeIdentifier, OP operator) {
    super(nodeIdentifier);
    this.operator = operator;
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
    throw new UnsupportedOperationException();
  }
}
