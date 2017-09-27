package com.linkedin.thirdeye.taskexecution.executor;

import com.google.common.base.Preconditions;
import java.util.Collections;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;

public class NodeConfig {
  private static final Configuration EMPTY_CONFIG = new MapConfiguration(Collections.emptyMap());

  private int numRetryAtError = 0;
  private boolean skipAtFailure = false;
  private Configuration rawOperatorConfig = EMPTY_CONFIG;

  public int numRetryAtError() {
    return numRetryAtError;
  }

  public void setNumRetryAtError(int numRetryAtError) {
    this.numRetryAtError = numRetryAtError;
  }

  public boolean skipAtFailure() {
    return skipAtFailure;
  }

  public void setSkipAtFailure(boolean skipAtFailure) {
    this.skipAtFailure = skipAtFailure;
  }

  public Configuration getRawOperatorConfig() {
    return rawOperatorConfig;
  }

  public void setRawOperatorConfig(Configuration rawOperatorConfig) {
    // TODO: Add null processing logic since this value might be read from a config file
    Preconditions.checkNotNull(rawOperatorConfig);
    this.rawOperatorConfig = rawOperatorConfig;
  }
}
