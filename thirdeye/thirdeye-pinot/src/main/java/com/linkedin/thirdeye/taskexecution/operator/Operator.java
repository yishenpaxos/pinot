package com.linkedin.thirdeye.taskexecution.operator;

import com.google.common.reflect.TypeToken;

public abstract class Operator<V> {
//  TypeToken<V> type = new TypeToken<V>(getClass()) {};

  public abstract void initialize(OperatorConfig operatorConfig);

  public abstract ExecutionResult<V> run(OperatorContext operatorContext);
}
