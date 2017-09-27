package com.linkedin.thirdeye.taskexecution.impl.executor;

import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.Reader;
import com.linkedin.thirdeye.taskexecution.executor.ExecutionResult;
import com.linkedin.thirdeye.taskexecution.executor.ExecutionStatus;
import com.linkedin.thirdeye.taskexecution.executor.NodeConfig;
import com.linkedin.thirdeye.taskexecution.impl.operator.AbstractOperator;
import com.linkedin.thirdeye.taskexecution.impl.operator.Operator1x1;
import com.linkedin.thirdeye.taskexecution.operator.OperatorConfig;
import java.util.Collections;
import org.testng.Assert;
import org.testng.annotations.Test;


public class OperatorRunnerTest {

  @Test
  public void testCreation() {
    try {
      new OperatorRunner(new DummyOperator(), new NodeConfig());
    } catch (Exception e) {
      Assert.fail();
    }
  }

  @Test
  public void testSuccessRunOfOperator() {
    NodeConfig nodeConfig = new NodeConfig();
    nodeConfig.setSkipAtFailure(false);
    nodeConfig.setNumRetryAtError(0);

    DummyOperator operator1 = new DummyOperator();
    OperatorRunner runner = new OperatorRunner(operator1, nodeConfig);
    ExecutionResult executionResult = runner.call();
    Assert.assertEquals(executionResult.getExecutionStatus(), ExecutionStatus.SUCCESS);

    Reader<Integer> integerReader = operator1.getOutputPort().getReader();
    Assert.assertTrue(integerReader.hasNext());
    Assert.assertEquals((int) integerReader.next(), 1);
  }

  @Test
  public void testSuccessRunOfChainOperators() {
    NodeConfig nodeConfig = new NodeConfig();
    nodeConfig.setSkipAtFailure(false);
    nodeConfig.setNumRetryAtError(0);

    DummyOperator operator1 = new DummyOperator(new NodeIdentifier("operator1"));
    DummyOperator operator2 = new DummyOperator(new NodeIdentifier("operator2"));
    OperatorRunner runner1 = new OperatorRunner(operator1, nodeConfig);
    OperatorRunner runner2 = new OperatorRunner(operator2, nodeConfig);

    OperatorIOChannel channel = new OperatorIOChannel();
    channel.connect(operator1.getOutputPort(), operator2.getInputPort());
    runner1.setOutgoingChannels(Collections.singleton(channel));
    runner2.setIncomingChannels(Collections.singleton(channel));

    ExecutionResult executionResult1 = runner1.call();
    Assert.assertEquals(executionResult1.getExecutionStatus(), ExecutionStatus.SUCCESS);
    Reader<Integer> integerReader = operator1.getOutputPort().getReader();
    Assert.assertTrue(integerReader.hasNext());
    Assert.assertEquals((int) integerReader.next(), 1);

    ExecutionResult executionResult2 = runner2.call();
    Assert.assertEquals(executionResult2.getExecutionStatus(), ExecutionStatus.SUCCESS);
    Reader<Integer> integerReader2 = operator2.getOutputPort().getReader();
    Assert.assertTrue(integerReader2.hasNext());
    Assert.assertEquals((int) integerReader2.next(), 2);
  }

  @Test
  public void testFailureRunOfOperator() {
    NodeConfig nodeConfig = new NodeConfig();
    nodeConfig.setSkipAtFailure(false);
    nodeConfig.setNumRetryAtError(1);
    OperatorRunner runner = new OperatorRunner(new FailedRunOperator(), nodeConfig);
    ExecutionResult executionResult = runner.call();
    Assert.assertEquals(executionResult.getExecutionStatus(), ExecutionStatus.FAILED);
  }

  @Test
  public void testSkippedRunOfOperator() {
    NodeConfig nodeConfig = new NodeConfig();
    nodeConfig.setSkipAtFailure(true);
    nodeConfig.setNumRetryAtError(2);
    OperatorRunner runner = new OperatorRunner(new FailedRunOperator(), nodeConfig);
    ExecutionResult executionResult = runner.call();
    Assert.assertEquals(executionResult.getExecutionStatus(), ExecutionStatus.SKIPPED);
  }

  @Test
  public void testNullIdentifier() {
    OperatorRunner runner = new OperatorRunner(new DummyOperator(), new NodeConfig());
    ExecutionResult executionResult = runner.call();
    Assert.assertEquals(executionResult.getExecutionStatus(), ExecutionStatus.SUCCESS);
    Assert.assertNotNull(executionResult.getNodeIdentifier());
    Assert.assertNotNull(executionResult.getNodeIdentifier().getName());
  }

  public static class DummyOperator extends Operator1x1<Integer, Integer> {

    public DummyOperator() {
      super(new NodeIdentifier());
    }

    public DummyOperator(NodeIdentifier nodeIdentifier) {
      super(nodeIdentifier);
    }

    @Override
    public void initialize(OperatorConfig operatorConfig) {
    }

    @Override
    public void run() {
      Reader<Integer> reader = getInputPort().getReader();
      int sum = 1;
      if (reader.hasNext()) {
        sum += reader.next();
      }
      getOutputPort().getWriter().write(sum);
    }
  }

  public static class FailedRunOperator extends AbstractOperator {
    public FailedRunOperator() {
      super(new NodeIdentifier());
    }

    @Override
    public void initialize(OperatorConfig operatorConfig) {
    }

    @Override
    public void run() {
      throw new UnsupportedOperationException("Failed during running IN PURPOSE.");
    }
  }
}
