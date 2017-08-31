package com.linkedin.thirdeye.taskexecution.impl.operator;

import com.linkedin.thirdeye.taskexecution.dataflow.ExecutionResult;
import com.linkedin.thirdeye.taskexecution.processor.Processor;
import com.linkedin.thirdeye.taskexecution.processor.ProcessorConfig;
import com.linkedin.thirdeye.taskexecution.processor.ProcessorContext;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AbstractProcessorRunnerTest {

  @Test
  public void testSuccessInitializeOperator() throws InstantiationException, IllegalAccessException {
    AbstractProcessorRunner.initializeOperator(ProcessorRunnerTest.DummyProcessor.class, new ProcessorConfig());
  }

  @Test
  public void testFailureInitializeOperator() {
    try {
      AbstractProcessorRunner.initializeOperator(FailedInitializedProcessor.class, new ProcessorConfig());
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
    public ExecutionResult run(ProcessorContext processorContext) {
      return new ExecutionResult();
    }
  }

}
