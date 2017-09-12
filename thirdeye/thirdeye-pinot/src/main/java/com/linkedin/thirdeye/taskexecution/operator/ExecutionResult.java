package com.linkedin.thirdeye.taskexecution.operator;

import java.util.Objects;

public class ExecutionResult<V> {
  private V result;

  public ExecutionResult() {
  }

  public ExecutionResult(V result) {
    this.result = result;
  }

  public void setResult(V result) {
    this.result = result;
  }

  public V result() {
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExecutionResult<?> that = (ExecutionResult<?>) o;
    return Objects.equals(result, that.result);
  }

  @Override
  public int hashCode() {
    return Objects.hash(result);
  }
}
