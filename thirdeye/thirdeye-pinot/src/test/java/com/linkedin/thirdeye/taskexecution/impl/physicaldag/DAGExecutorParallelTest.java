package com.linkedin.thirdeye.taskexecution.impl.physicaldag;

import com.linkedin.thirdeye.taskexecution.dag.DAG;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.dataflow.ExecutionResult;
import com.linkedin.thirdeye.taskexecution.dataflow.ExecutionResults;
import com.linkedin.thirdeye.taskexecution.operator.Operator;
import com.linkedin.thirdeye.taskexecution.operator.OperatorConfig;
import com.linkedin.thirdeye.taskexecution.operator.OperatorContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DAGExecutorParallelTest {
  private ExecutorService threadPool = Executors.newFixedThreadPool(10);
  private final static String EXECUTION_LOG_KEY1 = "key1";
  private final static String EXECUTION_LOG_KEY2 = "key2";

  /**
   * DAG: root
   */
  @Test(enabled = false)
  public void testOneNodeChainExecution() {
    DAG<PhysicalNode> dag = new PhysicalPlan();
    PhysicalNode root = new PhysicalNode("root", LogGeneratorOperator.class);
    PartitionedPhysicalNode loopNode = new PartitionedPhysicalNode("2", LogOperator.class);
    dag.addNode(root);
    dag.addNode(loopNode);
    dag.addEdge(root, loopNode);

    DAGExecutor<PhysicalNode> dagExecutor = new DAGExecutor<>(threadPool);
    dagExecutor.execute(dag, new DAGConfig());

    List<String> executionLog = DAGExecutorBasicTest.checkAndGetFinalResult(dagExecutor.getNode(loopNode.getIdentifier()));
    List<String> expectedLog = new ArrayList<String>() {{
      add("root");
    }};
    Assert.assertEquals(executionLog, expectedLog);
  }

  /**
   * An operator that appends node name to a list, which is passed in from its incoming nodes.
   */
  public static class LogGeneratorOperator implements Operator {
    private static final Logger LOG = LoggerFactory.getLogger(LogGeneratorOperator.class);

    @Override
    public void initialize(OperatorConfig operatorConfig) {
    }

    @Override
    public ExecutionResult run(OperatorContext operatorContext) {
      LOG.info("Running node: {}", operatorContext.getNodeIdentifier().getName());
      Map<String, List<String>> executionLogs = new HashMap<>();
      List<String> list1 = new ArrayList<>();
      list1.add(operatorContext.getNodeIdentifier().getName());
      executionLogs.put(EXECUTION_LOG_KEY1, list1);

      List<String> list2 = new ArrayList<>();
      list2.add(operatorContext.getNodeIdentifier().getName());
      executionLogs.put(EXECUTION_LOG_KEY2, list2);

      ExecutionResult operatorResult = new ExecutionResult();
      operatorResult.setResult(EXECUTION_LOG_KEY1, list1);
      operatorResult.setResult(EXECUTION_LOG_KEY2, list2);
      return operatorResult;
    }
  }

  /**
   * An operator that appends node name to a list, which is passed in from its incoming nodes.
   */
  public static class LogOperator implements Operator {
    private static final Logger LOG = LoggerFactory.getLogger(LogOperator.class);

    @Override
    public void initialize(OperatorConfig operatorConfig) {
    }

    @Override
    public ExecutionResult run(OperatorContext operatorContext) {
      LOG.info("Running node: {}", operatorContext.getNodeIdentifier().getName());
      Map<NodeIdentifier, ExecutionResults> inputs = operatorContext.getInputs();
      List<String> executionLog = new ArrayList<>();
      String uniqueKey = "";
      for (ExecutionResults parentResult : inputs.values()) {
        String key = (String) CollectionUtils.get(parentResult.keySet(), 0);
        Object result = parentResult.getResult(key).result();
        if (result instanceof List) {
          List<String> list = (List<String>) result;
          for (String s : list) {
            if (!executionLog.contains(s)) {
              executionLog.add(s);
            }
          }
        }
        uniqueKey = key;
      }
      executionLog.add(operatorContext.getNodeIdentifier().getName());
      ExecutionResult operatorResult = new ExecutionResult();
      operatorResult.setResult(uniqueKey, executionLog);
      return operatorResult;
    }
  }
}
