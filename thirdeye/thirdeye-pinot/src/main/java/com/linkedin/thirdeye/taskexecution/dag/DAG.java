package com.linkedin.thirdeye.taskexecution.dag;

import java.util.Collection;
import java.util.Set;

public interface DAG<N extends Node<N, E>, E extends Edge> {
  /**
   * Returns the node with the given {@link NodeIdentifier}.
   *
   * @param nodeIdentifier the node identifier.
   *
   * @return null if no such node exists.
   */
  N getNode(NodeIdentifier nodeIdentifier);

  /**
   * Returns the parents of the specified node.
   *
   * @param nodeIdentifier the identifier of the specified node.
   *
   * @return an empty set if the specified node does not exist.
   */
  Set<N> getParents(NodeIdentifier nodeIdentifier);

  /**
   * Returns the children of the specified node.
   *
   * @param nodeIdentifier the identifier of the specified node.
   *
   * @return an empty set if the specified node does not exist.
   */
  Set<N> getChildren(NodeIdentifier nodeIdentifier);

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
  Collection<N> getStartNodes();

  /**
   * Returns all nodes that do not have outgoing edges.
   *
   * @return all nodes that do not have outgoing edges.
   */
  Collection<N> getEndNodes();

  /**
   * Returns all nodes in the DAG.
   *
   * @return all nodes in the DAG.
   */
  Collection<N> getAllNodes();
}
