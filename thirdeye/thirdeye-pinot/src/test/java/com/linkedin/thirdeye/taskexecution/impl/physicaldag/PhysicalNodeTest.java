package com.linkedin.thirdeye.taskexecution.impl.physicaldag;

import com.linkedin.thirdeye.taskexecution.dataflow.reader.Reader;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.SimpleReader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PhysicalNodeTest {
  private PhysicalNode node;
  @Test
  public void testCreation() throws Exception {
    node = new PhysicalNode("Test", null);
  }

  @Test (dependsOnMethods = "testCreation")
  public void testEmptyLogicalNode() throws Exception {
    Assert.assertEquals(node.getExecutionStatus(), ExecutionStatus.SKIPPED);
    Reader reader = node.getOutputReader();
    Assert.assertTrue(reader instanceof SimpleReader);
    SimpleReader simpleReader = (SimpleReader) reader;
//    Assert.assertFalse(reader.hasNext());
    Assert.assertNull(simpleReader.read());

    Assert.assertNull(node.getLogicalNode());
    Assert.assertNotNull(node.getPhysicalNode());
    Assert.assertEquals(node.getPhysicalNode().size(), 0);
  }

}
