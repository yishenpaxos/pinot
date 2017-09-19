package com.linkedin.thirdeye.taskexecution.operator;

import com.google.common.base.Preconditions;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperatorUtils {
  private static final Logger LOG = LoggerFactory.getLogger(OperatorUtils.class);

  private OperatorUtils() {

  }

  public static <T extends AbstractOperator> T initiateOperatorInstance(NodeIdentifier nodeIdentifier,
      Class<T> operatorClass) {
    Preconditions.checkNotNull(nodeIdentifier);
    T operator = initiateOperatorInstance(operatorClass);
    operator.setNodeIdentifier(nodeIdentifier);
    return operator;
  }

  public static <T extends Operator> T initiateOperatorInstance(Class<T> operatorClass) {
    try {
      return operatorClass.newInstance();
    } catch (Exception e) {
      // We cannot do anything if something bad happens here excepting rethrow the exception.
      LOG.warn("Failed to initialize {}", operatorClass.getName());
      throw new IllegalArgumentException(e);
    }
  }
}
