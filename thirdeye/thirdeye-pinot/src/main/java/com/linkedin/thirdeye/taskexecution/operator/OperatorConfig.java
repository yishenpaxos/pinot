package com.linkedin.thirdeye.taskexecution.operator;

import com.linkedin.thirdeye.taskexecution.impl.executor.SystemContext;
import org.apache.commons.configuration.Configuration;

public interface OperatorConfig {
  void initialize(Configuration configuration, SystemContext systemContext);

  Configuration getRawConfiguration();
}
