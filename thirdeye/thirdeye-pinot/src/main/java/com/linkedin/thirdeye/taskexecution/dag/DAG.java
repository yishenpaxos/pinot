package com.linkedin.thirdeye.taskexecution.dag;

import java.util.Collection;

public interface DAG<N extends Node> {
  /**
   * Returns the node with the given {@link NodeIdentifier}.
   *
   * @param nodeIdentifier the node identifier.
   *
   * @return the node with the given {@link NodeIdentifier}.
   */
  N getNode(NodeIdentifier nodeIdentifier);

  /**
   * Returns the number of nodes in the DAG.
   *
   * @return the number of nodes in the DAG.
   */
  int size();

  /**
   * Returns all nodes that do not have incoming edges.
   *
   * @return all nodes that do not have incoming edges.
   */
  Collection<N> getRootNodes();

  /**
   * Returns all nodes that do not have outgoing edges.
   *
   * @return all nodes that do not have outgoing edges.
   */
  Collection<N> getLeafNodes();

  /**
   * Returns all nodes in the DAG.
   *
   * @return all nodes in the DAG.
   */
  Collection<N> getAllNodes();
}
