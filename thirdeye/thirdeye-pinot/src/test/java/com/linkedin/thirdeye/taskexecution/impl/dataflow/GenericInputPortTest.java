package com.linkedin.thirdeye.taskexecution.impl.dataflow;

import com.linkedin.thirdeye.taskexecution.dataflow.reader.Reader;
import org.testng.Assert;
import org.testng.annotations.Test;


public class GenericInputPortTest {
  @Test
  public void testEmptyCreation() {
    GenericInputPort<Integer> port = new GenericInputPort<>();
    port.initialize();

    Reader<Integer> reader = port.getReader();
    Assert.assertFalse(reader.hasNext());
  }

  @Test
  public void testCreation() {
    GenericInputPort<Integer> port = new GenericInputPort<>();
    port.initialize();

    CollectionReader.Builder<Integer> builder1 = CollectionReader.builder();
    builder1.add(1);
    Reader<Integer> reader1 = builder1.build();

    CollectionReader.Builder<Integer> builder2 = CollectionReader.builder();
    builder2.add(2);
    Reader<Integer> reader2 = builder2.build();

    port.addContext(reader1);
    port.addContext(reader2);

    Reader<Integer> reader = port.getReader();
    Assert.assertTrue(reader.hasNext());
    Assert.assertEquals((int) reader.next(), 1);
    Assert.assertTrue(reader.hasNext());
    Assert.assertEquals((int) reader.next(), 2);
  }

}
