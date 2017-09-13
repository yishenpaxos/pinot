package com.linkedin.thirdeye.taskexecution.impl.dataflow;

import com.linkedin.thirdeye.taskexecution.dataflow.reader.KVReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

public class InMemoryKVReader<K, V> implements KVReader<K, V> {
  private Map<K, V> context = Collections.emptyMap();
  private Iterator<Map.Entry<K, V>> iterator;

  public InMemoryKVReader() {
  }

  public InMemoryKVReader(Map<K, V> context) {
    this.context = context;
  }

  private void initIterator() {
    if (iterator == null) {
      iterator = context.entrySet().iterator();
    }
  }

  @Override
  public boolean hasNext() {
    initIterator();
    return iterator.hasNext();
  }

  @Override
  public Map.Entry<K, V> next() {
    initIterator();
    return iterator.next();
  }

  @Override
  public boolean hasPayload() {
    return true;
  }

  @Override
  public Collection<Map.Entry<K, V>> read() {
    return new ArrayList<>(context.entrySet());
  }
}
