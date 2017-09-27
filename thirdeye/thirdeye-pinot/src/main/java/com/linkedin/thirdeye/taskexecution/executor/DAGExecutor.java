package com.linkedin.thirdeye.taskexecution.executor;

import com.linkedin.thirdeye.taskexecution.dag.DAG;

public interface DAGExecutor {
  void execute(DAG dag, DAGConfig dagConfig);
}
