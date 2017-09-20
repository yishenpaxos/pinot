package com.linkedin.thirdeye.taskexecution.dag;

import com.linkedin.thirdeye.taskexecution.operator.Operator;
import java.util.Set;

public interface Node<T extends Node, E extends Edge> {

  /** Execution Related Methods **/
  NodeIdentifier getIdentifier();

  Operator getOperator();

  /** Topology Related Methods **/
  void addIncomingNode(T node);

  void addOutgoingNode(T node);

  Set<T> getIncomingNodes();

  Set<T> getOutgoingNodes();

  void addIncomingEdge(E edge);

  void addOutgoingEdge(E edge);

  Set<E> getIncomingEdges();

  Set<E> getOutgoingEdges();
}
