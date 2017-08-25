package com.linkedin.thirdeye.taskexecution.impl.dag;

import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class DAGConfigTest {
  @Test
  public void testGetNodeConfig() throws Exception {
    DAGConfig dagConfig = new DAGConfig();
    NodeConfig nodeConfig = dagConfig.getNodeConfig(null);
    Assert.assertNotNull(nodeConfig);
  }

}
