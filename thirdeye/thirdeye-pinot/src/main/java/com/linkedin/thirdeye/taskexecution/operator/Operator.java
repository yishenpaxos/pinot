package com.linkedin.thirdeye.taskexecution.operator;

import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;

public interface Operator {

  NodeIdentifier getNodeIdentifier();

  void initialize(OperatorConfig operatorConfig);

  void run(OperatorContext operatorContext);

  void initialInputPorts();

  void initialOutputPorts();
}
