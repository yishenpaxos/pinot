package com.linkedin.thirdeye.taskexecution.operator;

import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.impl.operator.AbstractOperator;
import com.linkedin.thirdeye.taskexecution.impl.operator.OperatorUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OperatorUtilsTest {

    @Test
    public void testSuccessInitializeOperator() throws InstantiationException, IllegalAccessException {
      OperatorUtils.initiateOperatorInstance(DummyOperator.class);
      OperatorUtils.initiateOperatorInstance(new NodeIdentifier("Dummy"), DummyOperator.class);
    }

    @Test
    public void testFailureInitializeOperator() {
      try {
        OperatorUtils.initiateOperatorInstance(FailedInitializedOperator.class);
      } catch (Exception e) {
        return;
      }
      Assert.fail();
    }

  public static class DummyOperator extends AbstractOperator {
    @Override
    public void initialize(OperatorConfig operatorConfig) {
    }

    @Override
    public void run() {
    }
  }

  public static class FailedInitializedOperator extends AbstractOperator {
    private FailedInitializedOperator() {
    }

    @Override
    public void initialize(OperatorConfig operatorConfig) {
      throw new UnsupportedOperationException("Failed during initialization IN PURPOSE.");
    }

    @Override
    public void run() {
    }
  }

}
