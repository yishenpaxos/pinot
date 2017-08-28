package com.linkedin.thirdeye.taskexecution.operator;

import com.linkedin.thirdeye.taskexecution.dataflow.ExecutionResult;

public interface Processor {

  void initialize(ProcessorConfig processorConfig);

  ExecutionResult run(OperatorContext operatorContext);
}
