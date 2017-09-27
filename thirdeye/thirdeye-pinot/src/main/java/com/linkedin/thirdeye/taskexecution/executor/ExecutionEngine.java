package com.linkedin.thirdeye.taskexecution.executor;

import java.util.concurrent.ExecutionException;

public interface ExecutionEngine {
  void submit(ExecutionContext executionContext);

  ExecutionResult take() throws InterruptedException, ExecutionException;
}
