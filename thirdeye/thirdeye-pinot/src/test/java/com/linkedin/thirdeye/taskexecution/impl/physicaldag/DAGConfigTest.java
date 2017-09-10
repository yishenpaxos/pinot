package com.linkedin.thirdeye.taskexecution.impl.physicaldag;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DAGConfigTest {
  @Test
  public void testGetNodeConfig() throws Exception {
    DAGConfig dagConfig = new DAGConfig();
    NodeConfig nodeConfig = dagConfig.getNodeConfig(null);
    Assert.assertNotNull(nodeConfig);
  }

}
