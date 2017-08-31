package com.linkedin.thirdeye.taskexecution.impl.dag;

import com.linkedin.thirdeye.taskexecution.dag.DAG;
import com.linkedin.thirdeye.taskexecution.dataflow.ExecutionResult;
import com.linkedin.thirdeye.taskexecution.processor.Processor;
import com.linkedin.thirdeye.taskexecution.processor.ProcessorConfig;
import com.linkedin.thirdeye.taskexecution.processor.ProcessorContext;
import org.testng.Assert;
import org.testng.annotations.Test;


public class LogicalPlanTest {
  private DAG<LogicalNode> dag;
  private LogicalNode root;

  @Test
  public void testCreation() {
    try {
      dag = new LogicalPlan();
    } catch (Exception e) {
      Assert.fail();
    }
  }

  @Test(dependsOnMethods = {"testCreation"})
  public void testAddRoot() {
    dag = new LogicalPlan();
    root = new LogicalNode("1", DummyProcessor.class);
    dag.addNode(root);

    Assert.assertEquals(dag.getRootNodes().size(), 1);
    Assert.assertEquals(dag.getAllNodes().size(), 1);
    Assert.assertEquals(dag.size(), 1);
    Assert.assertEquals(dag.getLeafNodes().size(), 1);
  }

  @Test(dependsOnMethods = {"testCreation", "testAddRoot"})
  public void testAddDuplicatedNode() {
    LogicalNode node2 = new LogicalNode("1", DummyProcessor.class);
    LogicalNode dagNode1 = dag.addNode(node2);

    Assert.assertEquals(dagNode1, root);
    Assert.assertEquals(dag.getRootNodes().size(), 1);
    Assert.assertEquals(dag.getAllNodes().size(), 1);
    Assert.assertEquals(dag.getLeafNodes().size(), 1);
  }

  @Test(dependsOnMethods = {"testCreation", "testAddRoot", "testAddDuplicatedNode"})
  public void testAddNodes() {
    LogicalNode node2 = new LogicalNode("2", DummyProcessor.class);
    dag.addNode(node2);
    dag.addEdge(root, node2);
    Assert.assertEquals(dag.getRootNodes().size(), 1);
    Assert.assertEquals(dag.getAllNodes().size(), 2);
    Assert.assertEquals(dag.getLeafNodes().size(), 1);

    LogicalNode node3 = new LogicalNode("3", DummyProcessor.class);
    // The following line should be automatically executed in the addEdge method.
    // dag.addNode(node3);
    dag.addEdge(root, node3);
    Assert.assertEquals(dag.getRootNodes().size(), 1);
    Assert.assertEquals(dag.getAllNodes().size(), 3);
    Assert.assertEquals(dag.getLeafNodes().size(), 2);
  }

  @Test
  public void testAddNodeWithNullNodeIdentifier() {
    LogicalPlan dag = new LogicalPlan();
    try {
      LogicalNode node = new LogicalNode("", DummyProcessor.class);
      node.setNodeIdentifier(null);
      dag.addNode(node);
    } catch (IllegalArgumentException e) {
      return;
    }
    Assert.fail();
  }

  public static class DummyProcessor implements Processor {
    @Override
    public void initialize(ProcessorConfig processorConfig) {
    }

    @Override
    public ExecutionResult run(ProcessorContext processorContext) {
      return null;
    }
  }
}
