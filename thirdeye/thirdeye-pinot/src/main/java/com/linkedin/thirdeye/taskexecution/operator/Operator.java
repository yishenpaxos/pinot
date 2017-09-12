package com.linkedin.thirdeye.taskexecution.operator;

public interface Operator {

  void initialize(OperatorConfig operatorConfig);

  ExecutionResult run(OperatorContext operatorContext);
}
