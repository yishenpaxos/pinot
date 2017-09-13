package com.linkedin.thirdeye.taskexecution.impl.dataflow;

import com.linkedin.thirdeye.taskexecution.dataflow.reader.SimpleReader;

public class InMemorySimpleReader<V> implements SimpleReader<V> {
  private V payload;
  private boolean isPayloadSet = false;

  public InMemorySimpleReader() {
  }

  public InMemorySimpleReader(V payload) {
    setPayload(payload);
  }

  public void setPayload(V payload) {
    this.payload = payload;
    isPayloadSet = true;
  }

  @Override
  public boolean hasPayload() {
    return isPayloadSet;
  }

  @Override
  public V read() {
    return payload;
  }
}
