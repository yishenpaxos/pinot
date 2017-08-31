package com.linkedin.thirdeye.taskexecution.impl.operator;

import com.linkedin.thirdeye.taskexecution.dataflow.ExecutionResult;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.dataflow.ExecutionResults;
import com.linkedin.thirdeye.taskexecution.dataflow.ExecutionResultsReader;
import com.linkedin.thirdeye.taskexecution.impl.dag.ExecutionStatus;
import com.linkedin.thirdeye.taskexecution.impl.dataflow.InMemoryExecutionResultsReader;
import com.linkedin.thirdeye.taskexecution.impl.dag.NodeConfig;
import com.linkedin.thirdeye.taskexecution.processor.Processor;
import com.linkedin.thirdeye.taskexecution.processor.ProcessorConfig;
import com.linkedin.thirdeye.taskexecution.processor.ProcessorContext;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections.MapUtils;
import org.testng.Assert;
import org.testng.annotations.Test;


public class ProcessorRunnerTest {

  @Test
  public void testCreation() {
    try {
      new ProcessorRunner(new NodeIdentifier(), new NodeConfig(), DummyProcessor.class);
    } catch (Exception e) {
      Assert.fail();
    }
  }

  @Test
  public void testBuildInputOperatorContext() {
    Map<NodeIdentifier, ExecutionResultsReader> incomingResultsReader = new HashMap<>();
    NodeIdentifier node1Identifier = new NodeIdentifier("node1");
    NodeIdentifier node2Identifier = new NodeIdentifier("node2");
    NodeIdentifier node3Identifier = new NodeIdentifier("node3");
    String key11 = "result11";
    String key12 = "result12";
    String key21 = "result21";
    String key22 = "result22";
    {
      ExecutionResults<String, Integer> executionResults1 = new ExecutionResults<>(node1Identifier);
      executionResults1.addResult(new ExecutionResult<>(key11, 11));
      executionResults1.addResult(new ExecutionResult<>(key12, 12));
      ExecutionResultsReader<String, Integer> reader1 = new InMemoryExecutionResultsReader<>(executionResults1);
      incomingResultsReader.put(node1Identifier, reader1);
    }
    {
      ExecutionResults<String, Integer> executionResults2 = new ExecutionResults<>(node2Identifier);
      executionResults2.addResult(new ExecutionResult<>(key21, 21));
      executionResults2.addResult(new ExecutionResult<>(key22, 22));
      ExecutionResultsReader<String, Integer> reader2 = new InMemoryExecutionResultsReader<>(executionResults2);
      incomingResultsReader.put(node2Identifier, reader2);
    }
    {
      ExecutionResults<String, Integer> executionResults3 = new ExecutionResults<>(node3Identifier);
      ExecutionResultsReader<String, Integer> reader3 = new InMemoryExecutionResultsReader<>(executionResults3);
      incomingResultsReader.put(node3Identifier, reader3);
    }

    final boolean allowEmptyInput = false;
    ProcessorContext processorContext = ProcessorRunner
        .buildInputOperatorContext(new NodeIdentifier("OperatorContextBuilder"), incomingResultsReader,
            allowEmptyInput);
    Assert.assertNotNull(processorContext);

    Assert.assertEquals(processorContext.getNodeIdentifier(), new NodeIdentifier("OperatorContextBuilder"));
    Map<NodeIdentifier, ExecutionResults> inputs = processorContext.getInputs();
    Assert.assertTrue(MapUtils.isNotEmpty(inputs));
    ExecutionResults<String, Integer> executionResults1 = inputs.get(node1Identifier);
    Assert.assertEquals(executionResults1.keySet().size(), 2);
    Assert.assertEquals(executionResults1.getResult(key11).result(), Integer.valueOf(11));
    Assert.assertEquals(executionResults1.getResult(key12).result(), Integer.valueOf(12));
    ExecutionResults<String, Integer> executionResults2 = inputs.get(node2Identifier);
    Assert.assertEquals(executionResults2.keySet().size(), 2);
    Assert.assertEquals(executionResults2.getResult(key21).result(), Integer.valueOf(21));
    Assert.assertEquals(executionResults2.getResult(key22).result(), Integer.valueOf(22));
    ExecutionResults<String, Integer> executionResults3 = inputs.get(node3Identifier);
    Assert.assertNull(executionResults3);
  }

  @Test
  public void testBuildEmptyInputOperatorContext() {
    Map<NodeIdentifier, ExecutionResultsReader> incomingResultsReader = new HashMap<>();

    boolean allowEmptyInput = false;
    ProcessorContext processorContextNull = ProcessorRunner
        .buildInputOperatorContext(new NodeIdentifier("OperatorContextBuilder"), incomingResultsReader,
            allowEmptyInput);
    Assert.assertNull(processorContextNull);

    allowEmptyInput = true;
    ProcessorContext processorContextNotNull = ProcessorRunner
        .buildInputOperatorContext(new NodeIdentifier("OperatorContextBuilder"), incomingResultsReader,
            allowEmptyInput);
    Assert.assertNotNull(processorContextNotNull);
  }

  @Test
  public void testSuccessRunOfOperator() {
    NodeConfig nodeConfig = new NodeConfig();
    nodeConfig.setSkipAtFailure(false);
    nodeConfig.setNumRetryAtError(0);

    ExecutionResults<String, Integer> executionResults = new ExecutionResults<>(new NodeIdentifier("DummyParent"));
    ExecutionResult<String, Integer> executionResult = new ExecutionResult<>("TestDummy", 123);
    executionResults.addResult(executionResult);
    ExecutionResultsReader reader = new InMemoryExecutionResultsReader<>(executionResults);

    ProcessorRunner runner = new ProcessorRunner(new NodeIdentifier(), nodeConfig, DummyProcessor.class);
    runner.addIncomingExecutionResultReader(new NodeIdentifier("DummyNode"), reader);
    runner.call();
    Assert.assertEquals(runner.getExecutionStatus(), ExecutionStatus.SUCCESS);

    ExecutionResultsReader executionResultsReader = runner.getExecutionResultsReader();
    Assert.assertTrue(executionResultsReader.hasNext());

    Assert.assertEquals(executionResultsReader.next().result(), 0);
  }

  @Test
  public void testFailureRunOfOperator() {
    NodeConfig nodeConfig = new NodeConfig();
    nodeConfig.setSkipAtFailure(false);
    nodeConfig.setNumRetryAtError(1);
    ProcessorRunner runner = new ProcessorRunner(new NodeIdentifier(), nodeConfig, FailedRunProcessor.class);
    runner.call();
    Assert.assertEquals(runner.getExecutionStatus(), ExecutionStatus.FAILED);
  }

  @Test
  public void testSkippedRunOfOperator() {
    NodeConfig nodeConfig = new NodeConfig();
    nodeConfig.setSkipAtFailure(true);
    nodeConfig.setNumRetryAtError(2);
    ProcessorRunner runner = new ProcessorRunner(new NodeIdentifier(), nodeConfig, FailedRunProcessor.class);
    runner.call();
    Assert.assertEquals(runner.getExecutionStatus(), ExecutionStatus.SKIPPED);
  }

  @Test
  public void testNullIdentifier() {
    ProcessorRunner runner = new ProcessorRunner(null, new NodeConfig(), DummyProcessor.class);
    NodeIdentifier nodeIdentifier = runner.call();
    Assert.assertEquals(runner.getExecutionStatus(), ExecutionStatus.SUCCESS);
    Assert.assertNotNull(nodeIdentifier);
    Assert.assertNotNull(nodeIdentifier.getName());
  }

  public static class DummyProcessor implements Processor {
    @Override
    public void initialize(ProcessorConfig processorConfig) {
    }

    @Override
    public ExecutionResult run(ProcessorContext processorContext) {
      return new ExecutionResult<>("I am a Dummy", 0);
    }
  }

  public static class FailedRunProcessor implements Processor {
    @Override
    public void initialize(ProcessorConfig processorConfig) {
    }

    @Override
    public ExecutionResult run(ProcessorContext processorContext) {
      throw new UnsupportedOperationException("Failed during running IN PURPOSE.");
    }
  }
}
