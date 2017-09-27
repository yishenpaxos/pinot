package com.linkedin.thirdeye.taskexecution.operator;

import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;

public interface Operator {

  NodeIdentifier getNodeIdentifier();

  OperatorConfig newOperatorConfigInstance();

  void initializeIOPorts();

  void initialize(OperatorConfig operatorConfig);

  void run();

}
