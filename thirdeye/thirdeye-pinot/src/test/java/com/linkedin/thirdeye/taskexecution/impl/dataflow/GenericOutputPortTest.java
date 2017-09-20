package com.linkedin.thirdeye.taskexecution.impl.dataflow;

import com.linkedin.thirdeye.taskexecution.dataflow.reader.Reader;
import com.linkedin.thirdeye.taskexecution.dataflow.writer.Writer;
import org.testng.Assert;
import org.testng.annotations.Test;


public class GenericOutputPortTest {
  @Test
  public void testCreation() {
    GenericOutputPort<Integer> port = new GenericOutputPort<>();
    port.initialize();

    Writer<Integer> writer1 = port.getWriter();
    writer1.write(1);

    Writer<Integer> writer2 = port.getWriter();
    writer2.write(2);

    Reader<Integer> reader = port.getReader();
    Assert.assertTrue(reader.hasNext());
    Assert.assertEquals((int) reader.next(), 1);
    Assert.assertTrue(reader.hasNext());
    Assert.assertEquals((int) reader.next(), 2);
  }

}
