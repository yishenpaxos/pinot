package com.linkedin.thirdeye.taskexecution.impl.operator;

import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.Reader;
import com.linkedin.thirdeye.taskexecution.impl.dataflow.InMemoryCollectionReader;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.ExecutionStatus;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.NodeConfig;
import com.linkedin.thirdeye.taskexecution.operator.Operator;
import com.linkedin.thirdeye.taskexecution.operator.OperatorConfig;
import com.linkedin.thirdeye.taskexecution.operator.OperatorContext;
import java.util.HashMap;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;


public class OperatorRunnerTest {

  @Test
  public void testCreation() {
    try {
      new OperatorRunner(new NodeIdentifier(), new NodeConfig(), new DummyOperator());
    } catch (Exception e) {
      Assert.fail();
    }
  }

  @Test
  public void testBuildInputOperatorContext() {
    Map<NodeIdentifier, Reader<Map<String, Integer>>> incomingReaders = new HashMap<>();
    NodeIdentifier node1Identifier = new NodeIdentifier("node1");
    NodeIdentifier node2Identifier = new NodeIdentifier("node2");
    NodeIdentifier node3Identifier = new NodeIdentifier("node3");
    String key11 = "result11";
    String key12 = "result12";
    String key21 = "result21";
    String key22 = "result22";

    Map<String, Integer> expectedContext1 = new HashMap<>();
    expectedContext1.put(key11, 11);
    expectedContext1.put(key12, 12);
    Reader<Map<String, Integer>> reader1 = InMemoryCollectionReader.<Map<String, Integer>>builder().add(expectedContext1).build();
    incomingReaders.put(node1Identifier, reader1);

    Map<String, Integer> expectedContext2 = new HashMap<>();
    expectedContext2.put(key21, 21);
    expectedContext2.put(key22, 22);
    Reader<Map<String, Integer>> reader2 = InMemoryCollectionReader.<Map<String, Integer>>builder().add(expectedContext2).build();
    incomingReaders.put(node2Identifier, reader2);

    Map<String, Integer> expectedContext3 = new HashMap<>();
    Reader<Map<String, Integer>> reader3 = InMemoryCollectionReader.<Map<String, Integer>>builder().add(expectedContext3).build();
    incomingReaders.put(node3Identifier, reader3);

    //TODO: create new tests for checking the dataflow

//    OperatorContext operatorContext = OperatorRunner
//        .buildInputOperatorContext(new NodeIdentifier("OperatorContextBuilder"), incomingReaders);
//    Assert.assertNotNull(operatorContext);
//
//    Assert.assertEquals(operatorContext.getNodeIdentifier(), new NodeIdentifier("OperatorContextBuilder"));
//    Map<NodeIdentifier, Reader> inputs = operatorContext.getInputs();
//    Assert.assertTrue(MapUtils.isNotEmpty(inputs));
//
//    Map<String, Integer> actualContext1 = (Map<String, Integer>) inputs.get(node1Identifier).read();
//    Assert.assertEquals(actualContext1.size(), 2);
//    for (Map.Entry<String, Integer> entry : actualContext1.entrySet()) {
//      Assert.assertEquals(entry.getValue(), expectedContext1.get(entry.getKey()));
//    }
//
//    Map<String, Integer> actualContext2 = (Map<String, Integer>) inputs.get(node2Identifier).read();
//    Assert.assertEquals(actualContext2.size(), 2);
//    for (Map.Entry<String, Integer> entry : actualContext2.entrySet()) {
//      Assert.assertEquals(entry.getValue(), expectedContext2.get(entry.getKey()));
//    }
//
//    Map<String, Integer> actualContext3 = (Map<String, Integer>) inputs.get(node3Identifier).read();
//    Assert.assertEquals(actualContext3.size(), 0);
  }

  @Test
  public void testSuccessRunOfOperator() {
    NodeConfig nodeConfig = new NodeConfig();
    nodeConfig.setSkipAtFailure(false);
    nodeConfig.setNumRetryAtError(0);

    OperatorRunner runner = new OperatorRunner(new NodeIdentifier(), nodeConfig, new DummyOperator());
    runner.call();
    Assert.assertEquals(runner.getExecutionStatus(), ExecutionStatus.SUCCESS);

//    ExecutionResult operatorResult = runner.getOperatorResult();
//    Assert.assertEquals(operatorResult.result(), 0);
  }

  @Test
  public void testFailureRunOfOperator() {
    NodeConfig nodeConfig = new NodeConfig();
    nodeConfig.setSkipAtFailure(false);
    nodeConfig.setNumRetryAtError(1);
    OperatorRunner runner = new OperatorRunner(new NodeIdentifier(), nodeConfig, new FailedRunOperator());
    runner.call();
    Assert.assertEquals(runner.getExecutionStatus(), ExecutionStatus.FAILED);
  }

  @Test
  public void testSkippedRunOfOperator() {
    NodeConfig nodeConfig = new NodeConfig();
    nodeConfig.setSkipAtFailure(true);
    nodeConfig.setNumRetryAtError(2);
    OperatorRunner runner = new OperatorRunner(new NodeIdentifier(), nodeConfig, new FailedRunOperator());
    runner.call();
    Assert.assertEquals(runner.getExecutionStatus(), ExecutionStatus.SKIPPED);
  }

  @Test
  public void testNullIdentifier() {
    OperatorRunner runner = new OperatorRunner(null, new NodeConfig(), new DummyOperator());
    NodeIdentifier nodeIdentifier = runner.call();
    Assert.assertEquals(runner.getExecutionStatus(), ExecutionStatus.SUCCESS);
    Assert.assertNotNull(nodeIdentifier);
    Assert.assertNotNull(nodeIdentifier.getName());
  }

  public static class DummyOperator extends Operator {
    @Override
    public void initialize(OperatorConfig operatorConfig) {
    }

    @Override
    public void run(OperatorContext operatorContext) {
    }
  }

  public static class FailedRunOperator extends Operator {
    @Override
    public void initialize(OperatorConfig operatorConfig) {
    }

    @Override
    public void run(OperatorContext operatorContext) {
      throw new UnsupportedOperationException("Failed during running IN PURPOSE.");
    }
  }
}
