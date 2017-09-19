package com.linkedin.thirdeye.taskexecution.impl.physicaldag;

import com.linkedin.thirdeye.taskexecution.impl.physicalplan.PhysicalPlan;
import com.linkedin.thirdeye.taskexecution.operator.AbstractOperator;
import com.linkedin.thirdeye.taskexecution.operator.OperatorConfig;
import com.linkedin.thirdeye.taskexecution.operator.OperatorContext;
import org.testng.Assert;
import org.testng.annotations.Test;


public class PhysicalDAGTest {
  private PhysicalDAG dag;
  private PhysicalNode<DummyOperator> start;

  @Test
  public void testCreation() {
    try {
      dag = new PhysicalDAG();
    } catch (Exception e) {
      Assert.fail();
    }
  }

  @Test(dependsOnMethods = {"testCreation"})
  public void testAddRoot() {
    dag = new PhysicalDAG();
    start = new PhysicalNode<>("1", new DummyOperator());
    dag.addNode(start);

    Assert.assertEquals(dag.getRootNodes().size(), 1);
    Assert.assertEquals(dag.getAllNodes().size(), 1);
    Assert.assertEquals(dag.size(), 1);
    Assert.assertEquals(dag.getLeafNodes().size(), 1);
  }

  @Test(dependsOnMethods = {"testCreation", "testAddRoot"})
  public void testAddDuplicatedNode() {
    PhysicalNode node2 = new PhysicalNode<>("1", new DummyOperator());
    PhysicalNode dagNode1 = dag.addNode(node2);

    Assert.assertEquals(dagNode1, start);
    Assert.assertEquals(dag.getRootNodes().size(), 1);
    Assert.assertEquals(dag.getAllNodes().size(), 1);
    Assert.assertEquals(dag.getLeafNodes().size(), 1);
  }

  @Test(dependsOnMethods = {"testCreation", "testAddRoot", "testAddDuplicatedNode"})
  public void testAddNodes() {
    PhysicalNode node2 = new PhysicalNode<>("2", new DummyOperator());
    dag.addNode(node2);
    dag.addEdge((new PhysicalEdge()).connect(start, node2));
    dag.addEdge((new PhysicalEdge()).connect(start, node2));
    Assert.assertEquals(dag.getRootNodes().size(), 1);
    Assert.assertEquals(dag.getAllNodes().size(), 2);
    Assert.assertEquals(dag.getLeafNodes().size(), 1);

    PhysicalNode node3 = new PhysicalNode<>("3", new DummyOperator());
    // The following line should be automatically executed in the addEdge method.
    // dag.addNode(node3);
    dag.addEdge((new PhysicalEdge()).connect(start, node3));
    Assert.assertEquals(dag.getRootNodes().size(), 1);
    Assert.assertEquals(dag.getAllNodes().size(), 3);
    Assert.assertEquals(dag.getLeafNodes().size(), 2);
  }

  @Test
  public void testAddNodeWithNullNodeIdentifier() {
    PhysicalDAG dag = new PhysicalDAG();
    try {
      PhysicalNode node = new PhysicalNode<>("", new DummyOperator());
      node.setIdentifier(null);
      dag.addNode(node);
    } catch (NullPointerException e) {
      return;
    }
    Assert.fail();
  }

  public static class DummyOperator extends AbstractOperator {
    @Override
    public void initialize(OperatorConfig operatorConfig) {
    }

    @Override
    public void run(OperatorContext operatorContext) {
    }
  }
}
