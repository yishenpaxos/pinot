package com.linkedin.thirdeye.taskexecution.dataflow.reader;

import java.util.Map;

public interface KVReader<K, V> extends IterativeReader <Map.Entry<K,V>> {

  boolean hasNext();

  Map.Entry<K, V> next();
}
