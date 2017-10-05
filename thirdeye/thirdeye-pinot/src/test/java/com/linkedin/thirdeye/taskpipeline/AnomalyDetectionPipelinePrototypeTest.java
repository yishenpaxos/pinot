package com.linkedin.thirdeye.taskpipeline;

import com.linkedin.thirdeye.anomaly.utils.AnomalyUtils;
import com.linkedin.thirdeye.taskexecution.dag.DAG;
import com.linkedin.thirdeye.taskexecution.executor.DAGConfig;
import com.linkedin.thirdeye.taskexecution.executor.ExecutionEngine;
import com.linkedin.thirdeye.taskexecution.impl.executor.DefaultDAGExecutor;
import com.linkedin.thirdeye.taskexecution.impl.executor.DefaultExecutionEngine;
import com.linkedin.thirdeye.taskpipeline.anomalydetection.AnomalyDetectionPipelinePrototype;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.testng.annotations.Test;

public class AnomalyDetectionPipelinePrototypeTest {

  @Test
  public void testPipeline() {
    ExecutorService executorService = Executors.newFixedThreadPool(10);
    ExecutionEngine executionEngine = new DefaultExecutionEngine(executorService);
    DefaultDAGExecutor executor = new DefaultDAGExecutor(executionEngine);

    DAGConfig dagConfig = AnomalyDetectionPipelinePrototype.getDagConfig();

    DAG anomalyDetectionPipeline = AnomalyDetectionPipelinePrototype.getDAG();
    executor.execute(anomalyDetectionPipeline, dagConfig);

    AnomalyUtils.safelyShutdownExecutionService(executorService, 30, AnomalyDetectionPipelinePrototype.class);
  }

}
