package com.linkedin.thirdeye.taskexecution.impl.operator;

import com.linkedin.thirdeye.taskexecution.dataflow.ExecutionResult;
import com.linkedin.thirdeye.taskexecution.operator.Processor;
import com.linkedin.thirdeye.taskexecution.operator.ProcessorConfig;
import com.linkedin.thirdeye.taskexecution.operator.OperatorContext;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AbstractProcessorRunnerTest {

  @Test
  public void testSuccessInitializeOperator() throws InstantiationException, IllegalAccessException {
    AbstractOperatorRunner.initializeOperator(ProcessorRunnerTest.DummyProcessor.class, new ProcessorConfig());
  }

  @Test
  public void testFailureInitializeOperator() {
    try {
      AbstractOperatorRunner.initializeOperator(FailedInitializedProcessor.class, new ProcessorConfig());
    } catch (Exception e) {
      return;
    }
    Assert.fail();
  }

  public static class FailedInitializedProcessor implements Processor {
    @Override
    public void initialize(ProcessorConfig processorConfig) {
      throw new UnsupportedOperationException("Failed during initialization IN PURPOSE.");
    }

    @Override
    public ExecutionResult run(OperatorContext operatorContext) {
      return new ExecutionResult();
    }
  }

}
