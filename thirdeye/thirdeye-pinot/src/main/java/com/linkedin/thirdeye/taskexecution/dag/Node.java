package com.linkedin.thirdeye.taskexecution.dag;

import java.util.Collection;

public interface Node<T extends Node, E extends Edge> {

  NodeIdentifier getIdentifier();

  //// Topology Related Methods ////
  void addIncomingNode(T node);

  void addOutgoingNode(T node);

  Collection<T> getIncomingNodes();

  Collection<T> getOutgoingNodes();

  void addIncomingEdge(E edge);

  void addOutgoingEdge(E edge);

  Collection<E> getIncomingEdges();

  Collection<E> getOutgoingEdges();
}
