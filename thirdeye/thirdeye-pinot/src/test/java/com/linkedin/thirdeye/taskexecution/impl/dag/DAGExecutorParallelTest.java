package com.linkedin.thirdeye.taskexecution.impl.dag;

import com.linkedin.thirdeye.taskexecution.dag.DAG;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.dataflow.ExecutionResult;
import com.linkedin.thirdeye.taskexecution.dataflow.ExecutionResults;
import com.linkedin.thirdeye.taskexecution.operator.Processor;
import com.linkedin.thirdeye.taskexecution.operator.ProcessorConfig;
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
    DAG<LogicalNode> dag = new LogicalPlan();
    LogicalNode root = new LogicalNode("root", LogGeneratorProcessor.class);
    ParallelLogicNode loopNode = new ParallelLogicNode("2", LogProcessor.class);
    dag.addNode(root);
    dag.addNode(loopNode);
    dag.addEdge(root, loopNode);

    DAGExecutor<LogicalNode> dagExecutor = new DAGExecutor<>(threadPool);
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
  public static class LogGeneratorProcessor implements Processor {
    private static final Logger LOG = LoggerFactory.getLogger(LogGeneratorProcessor.class);

    @Override
    public void initialize(ProcessorConfig processorConfig) {
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
  public static class LogProcessor implements Processor {
    private static final Logger LOG = LoggerFactory.getLogger(LogProcessor.class);

    @Override
    public void initialize(ProcessorConfig processorConfig) {
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
