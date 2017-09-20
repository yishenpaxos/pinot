package com.linkedin.thirdeye.taskexecution.impl.executor;

import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.Reader;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.DAGConfig;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.PhysicalDAG;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.PhysicalDAGBuilder;
import com.linkedin.thirdeye.taskexecution.operator.Operator2x1;
import com.linkedin.thirdeye.taskexecution.operator.OperatorConfig;
import com.linkedin.thirdeye.taskexecution.operator.OperatorContext;
import com.linkedin.thirdeye.taskexecution.operator.Operator1x1;
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


public class DAGExecutorTest {
  private static final Logger LOG = LoggerFactory.getLogger(DAGExecutorTest.class);
  private ExecutorService threadPool = Executors.newFixedThreadPool(10);

  /**
   * DAG: start
   */
  @Test
  public void testOneNodeExecution() {
    PhysicalDAGBuilder dagBuilder = new PhysicalDAGBuilder();
    LogOperator start = dagBuilder.addOperator(new NodeIdentifier("start"), LogOperator.class);

    PhysicalDAG dag = dagBuilder.build();

    DAGExecutor dagExecutor = new DAGExecutor(threadPool);
    DAGConfig dagConfig = new DAGConfig();
    dagConfig.setStopAtFailure(true);
    dagExecutor.execute(dag, dagConfig);
    List<String> executionLog = checkAndGetFinalResult(start);
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
    PhysicalDAGBuilder dagBuilder = new PhysicalDAGBuilder();
    LogOperator node1 = dagBuilder.addOperator(new NodeIdentifier("1"), LogOperator.class);
    LogOperator node2 = dagBuilder.addOperator(new NodeIdentifier("2"), LogOperator.class);
    LogOperator node3 = dagBuilder.addOperator(new NodeIdentifier("3"), LogOperator.class);
    dagBuilder.addChannel(node1, node2);
    dagBuilder.addChannel(node2.getOutputPort(), node3.getInputPort());

    PhysicalDAG physicalDAG = dagBuilder.build();
    DAGExecutor dagExecutor = new DAGExecutor(threadPool);
    dagExecutor.execute(physicalDAG, new DAGConfig());

    List<String> executionLog = checkAndGetFinalResult(node3);
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
    PhysicalDAGBuilder dagBuilder = new PhysicalDAGBuilder();
    LogOperator start1 = dagBuilder.addOperator(new NodeIdentifier("start1"), LogOperator.class);
    LogOperator node12 = dagBuilder.addOperator(new NodeIdentifier("node12"), LogOperator.class);
    LogOperator end1 = dagBuilder.addOperator(new NodeIdentifier("end1"), LogOperator.class);
    dagBuilder.addChannel(start1, node12);
    dagBuilder.addChannel(node12, end1);

    LogOperator start2 = dagBuilder.addOperator(new NodeIdentifier("start2"), LogOperator.class);
    LogOperator node22 = dagBuilder.addOperator(new NodeIdentifier("node22"), LogOperator.class);
    LogOperator node23 = dagBuilder.addOperator(new NodeIdentifier("node23"), LogOperator.class);
    LogOperator node24 = dagBuilder.addOperator(new NodeIdentifier("node24"), LogOperator.class);
    LogOperator end2 = dagBuilder.addOperator(new NodeIdentifier("end2"), LogOperator.class);
    dagBuilder.addChannel(start2, node22);
    dagBuilder.addChannel(node22, node23);
    dagBuilder.addChannel(node23, end2);
    dagBuilder.addChannel(node22, node24);
    dagBuilder.addChannel(node24, end2);

    PhysicalDAG dag = dagBuilder.build();

    DAGExecutor dagExecutor = new DAGExecutor(threadPool);
    dagExecutor.execute(dag, new DAGConfig());

    // Check path 1
    {
      List<String> executionLog = checkAndGetFinalResult(end1);
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
      List<String> executionLog = checkAndGetFinalResult(end2);
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
    PhysicalDAGBuilder dagBuilder = new PhysicalDAGBuilder();
    LogOperator start = dagBuilder.addOperator(new NodeIdentifier("start"), LogOperator.class);
    LogOperator end = dagBuilder.addOperator(new NodeIdentifier("end"), LogOperator.class);

    // sub-path 2
    LogOperator node22 = dagBuilder.addOperator(new NodeIdentifier("22"), LogOperator.class);
    LogOperator node23 = dagBuilder.addOperator(new NodeIdentifier("23"), LogOperator.class);
    LogOperator node24 = dagBuilder.addOperator(new NodeIdentifier("24"), LogOperator.class);
    LogOperator node25 = dagBuilder.addOperator(new NodeIdentifier("25"), LogOperator.class);
    LogOperator node26 = dagBuilder.addOperator(new NodeIdentifier("26"), LogOperator.class);
    LogOperator node27 = dagBuilder.addOperator(new NodeIdentifier("27"), LogOperator.class);
    dagBuilder.addChannel(start, node22);
    dagBuilder.addChannel(start, node22);
    dagBuilder.addChannel(node22, node23);
    dagBuilder.addChannel(node22, node24);
    dagBuilder.addChannel(node24, node25);
    dagBuilder.addChannel(node24, node26);
    dagBuilder.addChannel(node25, node27);
    dagBuilder.addChannel(node26, node27);
    dagBuilder.addChannel(node23, end);
    dagBuilder.addChannel(node27, end);

    // sub-path 1
    LogOperator node12 = dagBuilder.addOperator(new NodeIdentifier("12"), LogOperator.class);
    dagBuilder.addChannel(start, node12);
    dagBuilder.addChannel(node12, end);

    PhysicalDAG dag = dagBuilder.build();
    DAGExecutor dagExecutor = new DAGExecutor(threadPool);
    dagExecutor.execute(dag, new DAGConfig());

    List<String> executionLog = checkAndGetFinalResult(end);
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
    PhysicalDAGBuilder dagBuilder = new PhysicalDAGBuilder();
    LogOperator node1 = dagBuilder.addOperator(new NodeIdentifier("1"), LogOperator.class);
    FailedOperator node2 = dagBuilder.addOperator(new NodeIdentifier("2"), FailedOperator.class);
    LogOperator node3 = dagBuilder.addOperator(new NodeIdentifier("3"), LogOperator.class);
    dagBuilder.addChannel(node1, node2);
    dagBuilder.addChannel(node2, node3);

    PhysicalDAG dag = dagBuilder.build();
    DAGConfig dagConfig = new DAGConfig();
    dagConfig.setStopAtFailure(false);
    DAGExecutor dagExecutor = new DAGExecutor(threadPool);
    dagExecutor.execute(dag, dagConfig);

    List<String> executionLog = checkAndGetFinalResult(node3);
    List<String> expectedResult = new ArrayList<String>() {{
      add("3");
    }};
    List<List<String>> expectedResults = new ArrayList<>();
    expectedResults.add(expectedResult);

    Assert.assertTrue(checkLinearizability(executionLog, expectedResults));
  }

  @Test
  public void testStaticTypeChecking() {
    PhysicalDAGBuilder dagBuilder = new PhysicalDAGBuilder();
    LogOperator stringStringNode = dagBuilder.addOperator(new NodeIdentifier("stringStringNode"), LogOperator.class);

    // The following addChannels() should NOT compile because of static type checking
    IntIntOperator intIntNode = dagBuilder.addOperator(new NodeIdentifier("intIntNode"), IntIntOperator.class);
//    dagBuilder.addChannel(stringStringNode, intIntNode);
//    dagBuilder.addChannel(stringStringNode.getOutputPort(), intIntNode.getInputPort());

    IntStringNumberOperator
        intStringNumberNode = dagBuilder.addOperator(new NodeIdentifier("intStringNumberNode"), IntStringNumberOperator.class);
    dagBuilder.addChannels(intIntNode, stringStringNode, intStringNumberNode);

    PhysicalDAG dag = dagBuilder.build();
    DAGConfig dagConfig = new DAGConfig();
    dagConfig.setStopAtFailure(true);
    DAGExecutor dagExecutor = new DAGExecutor(threadPool);
    dagExecutor.execute(dag, dagConfig);
  }

  @Test
  public void testDuplicatedNode() {
    PhysicalDAGBuilder dagBuilder = new PhysicalDAGBuilder();
    LogOperator stringStringNode1 = dagBuilder.addOperator(new NodeIdentifier("start"), LogOperator.class);
    LogOperator stringStringNode2 = dagBuilder.addOperator(new NodeIdentifier("start"), LogOperator.class);
    Assert.assertEquals(stringStringNode1, stringStringNode2);
  }

  /**
   * An operator that appends node name to a list, which is passed in from its incoming nodes.
   */
  public static class LogOperator extends Operator1x1<List<String>, List<String>> {
    private static final Logger LOG = LoggerFactory.getLogger(LogOperator.class);

    @Override
    public void initialize(OperatorConfig operatorConfig) {
    }

    @Override
    public void run(OperatorContext operatorContext) {
      LOG.info("Running node: {}", operatorContext.getNodeIdentifier().getName());
      List<String> executionLog = new ArrayList<>();
      Reader<List<String>> reader = getInputPort().getReader();
      while (reader.hasNext()) {
        List<String> list = reader.next();
        for (String s : list) {
          if (!executionLog.contains(s)) {
            executionLog.add(s);
          }
        }
      }
      executionLog.add(operatorContext.getNodeIdentifier().getName());
      getOutputPort().getWriter().write(executionLog);
    }
  }

  /**
   * An operator that always fails.
   */
  public static class FailedOperator extends Operator1x1<List<String>, List<String>> {
    @Override
    public void initialize(OperatorConfig operatorConfig) {
    }

    @Override
    public void run(OperatorContext operatorContext) {
      throw new UnsupportedOperationException("Failed in purpose.");
    }
  }

  /**
   * An operator for testing incompatible input and output port type.
   */
  public static class IntIntOperator extends Operator1x1<Integer, Integer> {
    @Override
    public void initialize(OperatorConfig operatorConfig) {
    }

    @Override
    public void run(OperatorContext operatorContext) {
    }
  }

  public static class IntStringNumberOperator extends Operator2x1<Integer, List<String>, Number> {
    @Override
    public void initialize(OperatorConfig operatorConfig) {
    }

    @Override
    public void run(OperatorContext operatorContext) {
    }
  }


  /**
   * Returns the execution log of the given the node.
   *
   * @param operator the last operator in the DAG.
   *
   * @return the final execution log of the DAG.
   */
  private static List<String> checkAndGetFinalResult(LogOperator operator) {
    Reader<List<String>> reader = operator.getOutputPort().getReader();
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
