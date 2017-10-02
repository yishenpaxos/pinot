package com.linkedin.thirdeye.taskexecution.impl.operator;

import com.google.common.base.Preconditions;
import com.linkedin.thirdeye.taskexecution.impl.executor.SystemContext;
import com.linkedin.thirdeye.taskexecution.operator.OperatorConfig;
import java.util.Collections;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;

public abstract class BaseOperatorConfig implements OperatorConfig {
  private static final Configuration EMPTY_CONFIG = new MapConfiguration(Collections.emptyMap());
  private Configuration nodeRawConfig = EMPTY_CONFIG;

  @Override
  public void initialize(Configuration operatorRawConfig, SystemContext systemContext) {
    Preconditions.checkNotNull(operatorRawConfig);
    this.nodeRawConfig = operatorRawConfig;

    // TODO: Use Java Reflection to map fields in configuration to fields in the Config instance
  }

  public Configuration getRawConfiguration() {
    return nodeRawConfig;
  }
}
