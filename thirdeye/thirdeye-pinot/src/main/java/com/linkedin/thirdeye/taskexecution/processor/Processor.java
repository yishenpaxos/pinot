package com.linkedin.thirdeye.taskexecution.processor;

import com.linkedin.thirdeye.taskexecution.dataflow.ExecutionResult;

public interface Processor {

  void initialize(ProcessorConfig processorConfig);

  ExecutionResult run(ProcessorContext processorContext);
}
