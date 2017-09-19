package com.linkedin.thirdeye.taskexecution.impl;

import com.linkedin.thirdeye.taskexecution.operator.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DAGUtils {
  private static final Logger LOG = LoggerFactory.getLogger(DAGUtils.class);

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
