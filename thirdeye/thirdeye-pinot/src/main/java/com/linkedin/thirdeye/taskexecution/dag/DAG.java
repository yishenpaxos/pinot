package com.linkedin.thirdeye.taskexecution.dag;

import java.util.Collection;
import java.util.Set;

public interface DAG {
  /**
   * Returns the node with the given {@link NodeIdentifier}.
   *
   * @param nodeIdentifier the node identifier.
   *
   * @return null if no such node exists.
   */
  Node getNode(NodeIdentifier nodeIdentifier);

  /**
   * Returns the parents of the specified node.
   *
   * @param nodeIdentifier the identifier of the specified node.
   *
   * @return an empty set if the specified node does not exist.
   */
  Set<? extends Node> getParents(NodeIdentifier nodeIdentifier);

  /**
   * Returns the children of the specified node.
   *
   * @param nodeIdentifier the identifier of the specified node.
   *
   * @return an empty set if the specified node does not exist.
   */
  Set<? extends Node> getChildren(NodeIdentifier nodeIdentifier);

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
  Collection<? extends Node> getStartNodes();

  /**
   * Returns all nodes that do not have outgoing edges.
   *
   * @return all nodes that do not have outgoing edges.
   */
  Collection<? extends Node> getEndNodes();

  /**
   * Returns all nodes in the DAG.
   *
   * @return all nodes in the DAG.
   */
  Collection<? extends Node> getAllNodes();
}
