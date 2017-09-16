package com.linkedin.thirdeye.taskexecution.impl.physicaldag;

import com.linkedin.thirdeye.taskexecution.dataflow.reader.Reader;
import com.linkedin.thirdeye.taskexecution.operator.OperatorConfig;
import com.linkedin.thirdeye.taskexecution.operator.OperatorContext;
import com.linkedin.thirdeye.taskexecution.operator.SimpleIOOperator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class DAGExecutorBasicTest {
  private static final Logger LOG = LoggerFactory.getLogger(DAGExecutorBasicTest.class);
  private ExecutorService threadPool = Executors.newFixedThreadPool(10);

  /**
   * DAG: root
   */
  @Test
  public void testOneNodeExecution() {
    PhysicalPlan dag = new PhysicalPlan();
    PhysicalNode<LogOperator> start = new PhysicalNode<>("start", new LogOperator());
    dag.addNode(start);

    DAGExecutor<PhysicalNode, PhysicalEdge> dagExecutor = new DAGExecutor<>(threadPool);
    DAGConfig dagConfig = new DAGConfig();
    dagConfig.setStopAtFailure(true);
    dagExecutor.execute(dag, dagConfig);
    List<String> executionLog = checkAndGetFinalResult(dag.getNode(start.getIdentifier()));
    List<String> expectedLog = new ArrayList<String>() {{
      add("start");
    }};
    Assert.assertEquals(executionLog, expectedLog);
  }

  /**
   * DAG: 1 -> 2 -> 3
   */
  @Test
  public void testOneNodeChainExecution() {
    PhysicalPlan dag = new PhysicalPlan();
    PhysicalNode<LogOperator> node1 = new PhysicalNode<>("1", new LogOperator());
    PhysicalNode<LogOperator> node2 = new PhysicalNode<>("2", new LogOperator());
    PhysicalNode<LogOperator> node3 = new PhysicalNode<>("3", new LogOperator());
    dag.addEdge((new PhysicalEdge()).connect(node1, node1.getOperator().output, node2, node2.getOperator().input));
    dag.addEdge((new PhysicalEdge()).connect(node2, node2.getOperator().output, node3, node3.getOperator().input));

    DAGExecutor<PhysicalNode, PhysicalEdge> dagExecutor = new DAGExecutor<>(threadPool);
    dagExecutor.execute(dag, new DAGConfig());

    List<String> executionLog = checkAndGetFinalResult(dag.getNode(node3.getIdentifier()));
    List<String> expectedResult = new ArrayList<String>() {{
      add("1");
      add("2");
      add("3");
    }};
    List<List<String>> expectedResults = new ArrayList<>();
    expectedResults.add(expectedResult);

    Assert.assertTrue(checkLinearizability(executionLog, expectedResults));
  }

  /**
   * DAG:
   *     start1 -> node12 -> end1
   *
   *     start2 -> node22 ---> node23 ----> end2
   *                     \             /
   *                      \-> node24 -/
   */
  @Test
  public void testTwoNodeChainsExecution() {
    PhysicalPlan dag = new PhysicalPlan();
    PhysicalNode<LogOperator> start1 = new PhysicalNode<>("start1", new LogOperator());
    PhysicalNode<LogOperator> node12 = new PhysicalNode<>("node12", new LogOperator());
    PhysicalNode<LogOperator> end1 = new PhysicalNode<>("end1", new LogOperator());
    dag.addEdge((new PhysicalEdge()).connect(start1, start1.getOperator().output, node12, node12.getOperator().input));
    dag.addEdge((new PhysicalEdge()).connect(node12, node12.getOperator().output, end1, end1.getOperator().input));

    PhysicalNode<LogOperator> start2 = new PhysicalNode<>("start2", new LogOperator());
    PhysicalNode<LogOperator> node22 = new PhysicalNode<>("node22", new LogOperator());
    PhysicalNode<LogOperator> node23 = new PhysicalNode<>("node23", new LogOperator());
    PhysicalNode<LogOperator> node24 = new PhysicalNode<>("node24", new LogOperator());
    PhysicalNode<LogOperator> end2 = new PhysicalNode<>("end2", new LogOperator());
    dag.addEdge((new PhysicalEdge()).connect(start2, start2.getOperator().output, node22, node22.getOperator().input));
    dag.addEdge((new PhysicalEdge()).connect(node22, node22.getOperator().output, node23, node23.getOperator().input));
    dag.addEdge((new PhysicalEdge()).connect(node23, node23.getOperator().output, end2, end2.getOperator().input));
    dag.addEdge((new PhysicalEdge()).connect(node22, node22.getOperator().output, node24, node24.getOperator().input));
    dag.addEdge((new PhysicalEdge()).connect(node24, node24.getOperator().output, end2, end2.getOperator().input));


    DAGExecutor<PhysicalNode, PhysicalEdge> dagExecutor = new DAGExecutor<>(threadPool);
    dagExecutor.execute(dag, new DAGConfig());

    // Check path 1
    {
      List<String> executionLog = checkAndGetFinalResult(dag.getNode(end1.getIdentifier()));
      List<String> expectedResult = new ArrayList<String>() {{
        add("start1");
        add("node12");
        add("end1");
      }};
      List<List<String>> expectedResults = new ArrayList<>();
      expectedResults.add(expectedResult);
      Assert.assertTrue(checkLinearizability(executionLog, expectedResults));
    }
    // Check path 2
    {
      List<String> executionLog = checkAndGetFinalResult(dag.getNode(end2.getIdentifier()));
      List<String> expectedResult1 = new ArrayList<String>() {{
        add("start2");
        add("node22");
        add("node23");
        add("end2");
      }};
      List<String> expectedResult2 = new ArrayList<String>() {{
        add("start2");
        add("node22");
        add("node24");
        add("end2");
      }};
      List<List<String>> expectedResults = new ArrayList<>();
      expectedResults.add(expectedResult1);
      expectedResults.add(expectedResult2);
      Assert.assertTrue(checkLinearizability(executionLog, expectedResults));
    }
  }

  /**
   * DAG:
   *           /---------> 12 -------------\
   *         /                              \
   *       /    /---------> 23 ------------\ \
   * start-> 22                           -----> end
   *            \-> 24 --> 25 -----> 27 -/
   *                   \         /
   *                    \-> 26 -/
   */
  @Test
  public void testComplexGraphExecution() {
    PhysicalPlan dag = new PhysicalPlan();
    PhysicalNode<LogOperator> start = new PhysicalNode<>("start", new LogOperator());
    PhysicalNode<LogOperator> end = new PhysicalNode<>("end", new LogOperator());

    // sub-path 2
    PhysicalNode<LogOperator> node22 = new PhysicalNode<>("22", new LogOperator());
    PhysicalNode<LogOperator> node23 = new PhysicalNode<>("23", new LogOperator());
    PhysicalNode<LogOperator> node24 = new PhysicalNode<>("24", new LogOperator());
    PhysicalNode<LogOperator> node25 = new PhysicalNode<>("25", new LogOperator());
    PhysicalNode<LogOperator> node26 = new PhysicalNode<>("26", new LogOperator());
    PhysicalNode<LogOperator> node27 = new PhysicalNode<>("27", new LogOperator());
    dag.addEdge((new PhysicalEdge()).connect(start, start.getOperator().output, node22, node22.getOperator().input));
    dag.addEdge((new PhysicalEdge()).connect(node22, node22.getOperator().output, node23, node23.getOperator().input));
    dag.addEdge((new PhysicalEdge()).connect(node22, node22.getOperator().output, node24, node24.getOperator().input));
    dag.addEdge((new PhysicalEdge()).connect(node24, node24.getOperator().output, node25, node25.getOperator().input));
    dag.addEdge((new PhysicalEdge()).connect(node24, node24.getOperator().output, node26, node26.getOperator().input));
    dag.addEdge((new PhysicalEdge()).connect(node25, node25.getOperator().output, node27, node27.getOperator().input));
    dag.addEdge((new PhysicalEdge()).connect(node26, node26.getOperator().output, node27, node27.getOperator().input));
    dag.addEdge((new PhysicalEdge()).connect(node23, node23.getOperator().output, end, end.getOperator().input));
    dag.addEdge((new PhysicalEdge()).connect(node27, node27.getOperator().output, end, end.getOperator().input));

    PhysicalEdge edge = new PhysicalEdge();
    edge.connect(start, start.getOperator().output, end, end.getOperator().input);

    // sub-path 1
    PhysicalNode<LogOperator> node12 = new PhysicalNode<>("12", new LogOperator());
    dag.addEdge((new PhysicalEdge()).connect(start, start.getOperator().output, node12, node12.getOperator().input));
    dag.addEdge((new PhysicalEdge()).connect(node12, node12.getOperator().output, end, end.getOperator().input));

    DAGExecutor<PhysicalNode, PhysicalEdge> dagExecutor = new DAGExecutor<>(threadPool);
    dagExecutor.execute(dag, new DAGConfig());

    List<String> executionLog = checkAndGetFinalResult(dag.getNode(end.getIdentifier()));
    List<String> expectedOrder1 = new ArrayList<String>() {{
      add("start");
      add("12");
      add("end");
    }};
    List<String> expectedOrder2 = new ArrayList<String>() {{
      add("start");
      add("22");
      add("23");
      add("end");
    }};
    List<String> expectedOrder3 = new ArrayList<String>() {{
      add("start");
      add("22");
      add("24");
      add("25");
      add("27");
      add("end");
    }};
    List<String> expectedOrder4 = new ArrayList<String>() {{
      add("start");
      add("22");
      add("24");
      add("26");
      add("27");
      add("end");
    }};
    List<List<String>> expectedOrders = new ArrayList<>();
    expectedOrders.add(expectedOrder1);
    expectedOrders.add(expectedOrder2);
    expectedOrders.add(expectedOrder3);
    expectedOrders.add(expectedOrder4);
    Assert.assertTrue(checkLinearizability(executionLog, expectedOrders));
  }

  /**
   * DAG: 1 -> 2 -> 3
   */
  @Test
  public void testFailedChainExecution() {
    PhysicalPlan dag = new PhysicalPlan();
    PhysicalNode<LogOperator> node1 = new PhysicalNode<>("1", new LogOperator());
    PhysicalNode<FailedOperator> node2 = new PhysicalNode<>("2", new FailedOperator());
    PhysicalNode<LogOperator> node3 = new PhysicalNode<>("3", new LogOperator());
    dag.addEdge((new PhysicalEdge()).connect(node1, node1.getOperator().output, node2, node2.getOperator().input));
    dag.addEdge((new PhysicalEdge()).connect(node2, node2.getOperator().output, node3, node3.getOperator().input));

    DAGConfig dagConfig = new DAGConfig();
    dagConfig.setStopAtFailure(false);
    DAGExecutor<PhysicalNode, PhysicalEdge> dagExecutor = new DAGExecutor<>(threadPool);
    dagExecutor.execute(dag, dagConfig);

    List<String> executionLog = checkAndGetFinalResult(dag.getNode(node3.getIdentifier()));
    List<String> expectedResult = new ArrayList<String>() {{
      add("3");
    }};
    List<List<String>> expectedResults = new ArrayList<>();
    expectedResults.add(expectedResult);

    Assert.assertTrue(checkLinearizability(executionLog, expectedResults));
  }

  /**
   * An operator that appends node name to a list, which is passed in from its incoming nodes.
   */
  public static class LogOperator extends SimpleIOOperator<List<String>, List<String>> {
    private static final Logger LOG = LoggerFactory.getLogger(LogOperator.class);

    @Override
    public void initialize(OperatorConfig operatorConfig) {
    }

    @Override
    public void run(OperatorContext operatorContext) {
      LOG.info("Running node: {}", operatorContext.getNodeIdentifier().getName());
      List<String> executionLog = new ArrayList<>();
      Reader<List<String>> reader = input.getReader();
      while (reader.hasNext()) {
        List<String> list = reader.next();
        for (String s : list) {
          if (!executionLog.contains(s)) {
            executionLog.add(s);
          }
        }
      }
      executionLog.add(operatorContext.getNodeIdentifier().getName());
      output.getWriter().write(executionLog);
    }
  }

  /**
   * An operator that always fails.
   */
  public static class FailedOperator extends SimpleIOOperator<List<String>, List<String>> {
    @Override
    public void initialize(OperatorConfig operatorConfig) {
    }

    @Override
    public void run(OperatorContext operatorContext) {
      throw new UnsupportedOperationException("Failed in purpose.");
    }
  }

  /**
   * Returns the execution log of the given the node.
   *
   * @param node the last node in the DAG.
   *
   * @return the final execution log of the DAG.
   */
  private static List<String> checkAndGetFinalResult(PhysicalNode<LogOperator> node) {
    Reader<List<String>> reader = node.getOperator().output.getWriter().toReader();
    Assert.assertTrue(reader.hasNext());
    return reader.next();
  }

  /**
   * Utility method to check if the given execution log, which is a partially-ordered execution that is logged as a
   * sequence of execution, satisfies the expected linearizations, which is given by multiple totally-ordered
   * sub-executions.
   *
   * For example, this DAG execution:
   *
   *     root2 -> node22 ---> node23 ----> leaf2
   *                     \             /
   *                      \-> node24 -/
   *
   * contains two totally-ordered sub-executions:
   *     1. root2 -> node22 -> node23 -> leaf2, and
   *     2. root2 -> node22 -> node24 -> leaf2.
   *
   * Therefore, the input (i.e., executionLog) could be:
   *     1. root2 -> node22 -> node23 -> node24 -> leaf2 or
   *     2. root2 -> node22 -> node24 -> node23 -> leaf2 (node24 is swapped with node23).
   * The expected orders are the two totally-ordered sub-executions.
   *
   * This method checks if the execution order in the log contains all sub-execution in the expected orders.
   *
   * @param executionLog the execution log, which could be a partially-ordered execution.
   *
   * @param expectedOrders the expected sub-executions, which are totally-ordered executions.
   * @return true if the execution log satisfies the expected linearizations.
   */
  private boolean checkLinearizability(List<String> executionLog, List<List<String>> expectedOrders) {
    Set<String> checkedNodes = new HashSet<>();
    for (int i = 0; i < expectedOrders.size(); i++) {
      List<String> expectedOrder = expectedOrders.get(i);
      if (expectedOrder.size() > executionLog.size()) {
        throw new IllegalArgumentException("Execution log is shorter than the " + i + "th expected order.");
      }
      int expectedIdx = 0;
      int logIdx = 0;
      while (expectedIdx < expectedOrder.size() && logIdx < executionLog.size()) {
        if (expectedOrder.get(expectedIdx).equals(executionLog.get(logIdx))) {
          checkedNodes.add(expectedOrder.get(expectedIdx));
          ++expectedIdx;
        }
        ++logIdx;
      }
      if (expectedIdx < expectedOrder.size()-1) {
        LOG.warn("The location {} in the {}th expected order is not found or out of order.", expectedIdx, i);
        LOG.warn("The expected order: {}", expectedOrder.toString());
        ArrayList<String> subExecutionLog = new ArrayList<>();
        for (String s : executionLog) {
          if (expectedOrder.contains(s)) {
            subExecutionLog.add(s);
          }
        }
        LOG.warn("The execution log: {}", subExecutionLog.toString());
        return false;
      }
    }
    if (checkedNodes.size() < executionLog.size()) {
      LOG.warn("Execution log contains more nodes than expected logs.");
      LOG.warn("Num nodes in expected log: {}, execution log: {}", checkedNodes.size(), executionLog.size());
      Iterator<String> ite = executionLog.iterator();
      while(ite.hasNext()) {
        String s = ite.next();
        if (checkedNodes.contains(s)) {
          checkedNodes.remove(s);
          ite.remove();
        }
      }
      LOG.warn("Additional nodes in execution log: {}", executionLog.toString());
      return false;
    }
    return true;
  }

  /// The followings are tests for the method {@link checkLinearizability}.
  @Test(dataProvider = "ExpectedOrders")
  public void testCheckLegalLinearizability(List<List<String>> expectedOrders) {
    List<String> executionLog = new ArrayList<String>() {{
      add("1");
      add("4");
      add("2");
      add("5");
      add("3");
    }};

    Assert.assertTrue(checkLinearizability(executionLog, expectedOrders));
  }

  @Test(dataProvider = "ExpectedOrders")
  public void testCheckIllegalLinearizability(List<List<String>> expectedOrders) {
    List<String> executionLog = new ArrayList<String>() {{
      add("1");
      add("5");
      add("2");
      add("4");
      add("3");
    }};

    Assert.assertFalse(checkLinearizability(executionLog, expectedOrders));
  }

  @Test(dataProvider = "ExpectedOrders")
  public void testCheckNonExistNodeLinearizability(List<List<String>> expectedOrders) {
    List<String> executionLog = new ArrayList<String>() {{
      add("1");
      add("4");
      add("5");
      add("2");
      add("5");
      add("3");
      add("6");
    }};

    Assert.assertFalse(checkLinearizability(executionLog, expectedOrders));
  }

  @DataProvider(name = "ExpectedOrders")
  public static Object[][] expectedOrders() {
    List<List<String>> expectedOrders = new ArrayList<>();
    List<String> expectedOrder1 = new ArrayList<String>() {{
      add("1");
      add("2");
      add("3");
    }};
    List<String> expectedOrder2 = new ArrayList<String>() {{
      add("1");
      add("4");
      add("5");
      add("3");
    }};
    expectedOrders.add(expectedOrder1);
    expectedOrders.add(expectedOrder2);

    return new Object[][] { { expectedOrders } };
  }
}
