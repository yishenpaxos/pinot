package com.linkedin.thirdeye.taskpipeline.anomalydetection;

import com.google.common.collect.ImmutableMultimap;

public class MetricIdentifier {
  private long metricId;
  private ImmutableMultimap<String, String> filter = ImmutableMultimap.of();
}
