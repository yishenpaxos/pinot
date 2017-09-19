package com.linkedin.thirdeye.taskexecution.impl.physicalplan;

import com.google.common.base.Preconditions;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.InputPort;
import com.linkedin.thirdeye.taskexecution.dataflow.writer.OutputPort;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.PhysicalEdge;
import com.linkedin.thirdeye.taskexecution.impl.DAGUtils;
import com.linkedin.thirdeye.taskexecution.operator.Operator;
import java.util.HashMap;
import java.util.Map;

public class PhysicalPlan {
  private Map<NodeIdentifier, Operator> operators = new HashMap<>();

  public <T extends Operator> T addOperator(NodeIdentifier nodeIdentifier, Class<T> operatorClaz) {
    Preconditions.checkNotNull(nodeIdentifier);
    if (operators.containsKey(nodeIdentifier)) {
      return (T) operators.get(nodeIdentifier);
    } else {
      T operator = DAGUtils.initiateOperatorInstance(operatorClaz);
      operators.put(nodeIdentifier, operator);
      return operator;
    }
  }

  public <T> PhysicalEdge addChannel(OutputPort<? extends T> sourcePort, InputPort<? super T> sinkPort) {
    return null;
  }
}
