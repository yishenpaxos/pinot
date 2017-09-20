package com.linkedin.thirdeye.taskexecution.dag;

import com.linkedin.thirdeye.taskexecution.dataflow.reader.InputPort;
import com.linkedin.thirdeye.taskexecution.dataflow.writer.OutputPort;

public interface Edge {
  OutputPort getSourcePort();

  InputPort getSinkPort();
}
