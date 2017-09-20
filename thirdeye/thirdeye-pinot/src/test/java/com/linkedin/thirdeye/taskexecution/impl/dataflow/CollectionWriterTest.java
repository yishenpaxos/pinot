package com.linkedin.thirdeye.taskexecution.impl.dataflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CollectionWriterTest {

  @Test
  public void testEmptyCreation() {
    CollectionWriter<Integer> writer = new CollectionWriter<>();
    // Write to a black hole
    writer.write(1);
    writer.write(2);
  }

  @Test
  public void testCreation() {
    Collection<Integer> storage = new ArrayList<>();
    CollectionWriter<Integer> writer = new CollectionWriter<>(storage);
    writer.write(1);
    writer.write(2);
    Assert.assertTrue(storage.size() == 2);
    Iterator<Integer> iterator = storage.iterator();
    Assert.assertTrue(iterator.hasNext());
    Assert.assertEquals((int) iterator.next(), 1);
    Assert.assertTrue(iterator.hasNext());
    Assert.assertEquals((int) iterator.next(), 2);
  }

}
