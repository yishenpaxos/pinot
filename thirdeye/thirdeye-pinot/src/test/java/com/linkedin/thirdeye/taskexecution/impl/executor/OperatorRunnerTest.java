package com.linkedin.thirdeye.taskexecution.impl.executor;

import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.Reader;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.ExecutionStatus;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.NodeConfig;
import com.linkedin.thirdeye.taskexecution.operator.AbstractOperator;
import com.linkedin.thirdeye.taskexecution.operator.Operator1x1;
import com.linkedin.thirdeye.taskexecution.operator.OperatorConfig;
import com.linkedin.thirdeye.taskexecution.operator.OperatorContext;
import java.util.Collections;
import org.testng.Assert;
import org.testng.annotations.Test;


public class OperatorRunnerTest {

  @Test
  public void testCreation() {
    try {
      new OperatorRunner(new NodeConfig(), new DummyOperator());
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
    OperatorRunner runner = new OperatorRunner(nodeConfig, operator1);
    runner.call();
    Assert.assertEquals(runner.getExecutionStatus(), ExecutionStatus.SUCCESS);

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
    OperatorRunner runner1 = new OperatorRunner(nodeConfig, operator1);
    OperatorRunner runner2 = new OperatorRunner(nodeConfig, operator2);

    OperatorIOChannel channel = new OperatorIOChannel();
    channel.connect(operator1.getOutputPort(), operator2.getInputPort());
    runner1.setOutgoingChannels(Collections.singleton(channel));
    runner2.setIncomingChannels(Collections.singleton(channel));

    runner1.call();
    Assert.assertEquals(runner1.getExecutionStatus(), ExecutionStatus.SUCCESS);
    Reader<Integer> integerReader = operator1.getOutputPort().getReader();
    Assert.assertTrue(integerReader.hasNext());
    Assert.assertEquals((int) integerReader.next(), 1);

    runner2.call();
    Assert.assertEquals(runner2.getExecutionStatus(), ExecutionStatus.SUCCESS);
    Reader<Integer> integerReader2 = operator2.getOutputPort().getReader();
    Assert.assertTrue(integerReader2.hasNext());
    Assert.assertEquals((int) integerReader2.next(), 2);
  }

  @Test
  public void testFailureRunOfOperator() {
    NodeConfig nodeConfig = new NodeConfig();
    nodeConfig.setSkipAtFailure(false);
    nodeConfig.setNumRetryAtError(1);
    OperatorRunner runner = new OperatorRunner(nodeConfig, new FailedRunOperator());
    runner.call();
    Assert.assertEquals(runner.getExecutionStatus(), ExecutionStatus.FAILED);
  }

  @Test
  public void testSkippedRunOfOperator() {
    NodeConfig nodeConfig = new NodeConfig();
    nodeConfig.setSkipAtFailure(true);
    nodeConfig.setNumRetryAtError(2);
    OperatorRunner runner = new OperatorRunner(nodeConfig, new FailedRunOperator());
    runner.call();
    Assert.assertEquals(runner.getExecutionStatus(), ExecutionStatus.SKIPPED);
  }

  @Test
  public void testNullIdentifier() {
    OperatorRunner runner = new OperatorRunner(new NodeConfig(), new DummyOperator());
    NodeIdentifier nodeIdentifier = runner.call();
    Assert.assertEquals(runner.getExecutionStatus(), ExecutionStatus.SUCCESS);
    Assert.assertNotNull(nodeIdentifier);
    Assert.assertNotNull(nodeIdentifier.getName());
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
    public void run(OperatorContext operatorContext) {
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
    public void run(OperatorContext operatorContext) {
      throw new UnsupportedOperationException("Failed during running IN PURPOSE.");
    }
  }
}
