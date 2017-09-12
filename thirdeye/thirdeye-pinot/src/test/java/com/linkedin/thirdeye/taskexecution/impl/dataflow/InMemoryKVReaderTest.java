package com.linkedin.thirdeye.taskexecution.impl.dataflow;

import com.linkedin.thirdeye.taskexecution.dataflow.reader.KVReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class InMemoryKVReaderTest {
  @Test
  public void testCreation() throws Exception {
    new InMemoryKVReader();
  }

  @Test(expectedExceptions = NoSuchElementException.class)
  public void testEmptyReader() throws Exception {
    KVReader reader = new InMemoryKVReader();
    Assert.assertFalse(reader.hasNext());
    reader.next();
  }

  @Test(expectedExceptions = NoSuchElementException.class)
  public void testEmptyResult() throws Exception {
    KVReader reader = new InMemoryKVReader(Collections.emptyMap());
    Assert.assertFalse(reader.hasNext());
    reader.next();
  }

  @Test
  public void testOneResult() {
    Map<String, String> context = new HashMap<>();
    context.put("key", "value");
    KVReader<String, String> reader = new InMemoryKVReader(context);
    Assert.assertTrue(reader.hasNext());
    Map.Entry<String, String> entry = reader.next();
    Assert.assertEquals(entry.getKey(), "key");
    Assert.assertEquals(entry.getValue(), "value");
    Assert.assertFalse(reader.hasNext());
    try {
      reader.next();
    } catch (NoSuchElementException e) {
      return;
    }
    Assert.fail();
  }
}
