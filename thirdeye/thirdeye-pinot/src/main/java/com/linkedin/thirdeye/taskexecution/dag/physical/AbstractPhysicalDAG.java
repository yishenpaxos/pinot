package com.linkedin.thirdeye.taskexecution.dag.physical;

import com.linkedin.thirdeye.taskexecution.dag.DAG;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import java.util.Collection;

public abstract class AbstractPhysicalDAG<T extends AbstractPhysicalNode> implements DAG<T> {

  public abstract T addNode(T node);

  public abstract void addEdge(T source, T sink);

  public abstract T getNode(NodeIdentifier nodeIdentifier);

  public abstract int size();

  public abstract Collection<T> getRootNodes();

  public abstract Collection<T> getLeafNodes();

  public abstract Collection<T> getAllNodes();

}
