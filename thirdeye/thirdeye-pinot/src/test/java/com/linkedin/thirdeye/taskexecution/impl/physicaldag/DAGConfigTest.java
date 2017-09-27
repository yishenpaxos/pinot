package com.linkedin.thirdeye.taskexecution.impl.physicaldag;

import com.linkedin.thirdeye.taskexecution.executor.DAGConfig;
import com.linkedin.thirdeye.taskexecution.executor.NodeConfig;
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
