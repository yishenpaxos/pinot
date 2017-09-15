package com.linkedin.thirdeye.taskexecution.operator;

public abstract class Operator {

  public abstract void initialize(OperatorConfig operatorConfig);

  public abstract void run(OperatorContext operatorContext);
}
