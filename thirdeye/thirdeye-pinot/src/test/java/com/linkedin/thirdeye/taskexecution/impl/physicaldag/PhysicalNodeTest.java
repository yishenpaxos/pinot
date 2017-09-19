package com.linkedin.thirdeye.taskexecution.impl.physicaldag;

import com.linkedin.thirdeye.taskexecution.operator.AbstractOperator;
import com.linkedin.thirdeye.taskexecution.operator.OperatorConfig;
import com.linkedin.thirdeye.taskexecution.operator.OperatorContext;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PhysicalNodeTest {
  private PhysicalNode<DummyOperator> node;
  @Test
  public void testCreation() throws Exception {
    node = new PhysicalNode<>("Test", new DummyOperator());
  }

  @Test (enabled = false, dependsOnMethods = "testCreation")
  public void testEmptyNode() throws Exception {
    Assert.assertEquals(node.getExecutionStatus(), ExecutionStatus.SKIPPED);
  }

  public static class DummyOperator extends AbstractOperator {
    @Override
    public void initialize(OperatorConfig operatorConfig) {
    }

    @Override
    public void run(OperatorContext operatorContext) {
    }
  }
}
