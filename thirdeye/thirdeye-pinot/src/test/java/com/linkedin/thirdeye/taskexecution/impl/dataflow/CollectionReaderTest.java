package com.linkedin.thirdeye.taskexecution.impl.dataflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CollectionReaderTest {
  @Test
  public void testEmptyCreation() {
    CollectionReader<Integer> reader = new CollectionReader<>();
    Assert.assertFalse(reader.hasNext());
  }

  @Test
  public void testCreation() {
    Collection<Integer> storage = new ArrayList<>(Arrays.asList(1, 3));
    CollectionReader<Integer> reader = new CollectionReader<>(storage);
    Assert.assertTrue(reader.hasNext());
    Assert.assertEquals((int) reader.next(), 1);
    Assert.assertTrue(reader.hasNext());
    Assert.assertEquals((int) reader.next(), 3);
  }

  @Test
  public void testEmptyBuilder() {
    CollectionReader.Builder<Integer> builder = CollectionReader.builder();
    CollectionReader<Integer> reader = builder.build();
    Assert.assertFalse(reader.hasNext());
  }

  @Test
  public void testBuilderCollectionInsertion() {
    Collection<Integer> storage = new ArrayList<>(Arrays.asList(1, 3));
    CollectionReader.Builder<Integer> builder = CollectionReader.builder();
    CollectionReader<Integer> reader = builder.addAll(storage).add(2).build();
    Assert.assertTrue(reader.hasNext());
    Assert.assertEquals((int) reader.next(), 1);
    Assert.assertTrue(reader.hasNext());
    Assert.assertEquals((int) reader.next(), 3);
    Assert.assertTrue(reader.hasNext());
    Assert.assertEquals((int) reader.next(), 2);
  }
}
