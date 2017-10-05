package com.linkedin.thirdeye.taskpipeline.datatype.timeseries;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.linkedin.thirdeye.api.TimeGranularity;
import java.util.Map;
import org.joda.time.DateTime;

public class TimeSeriesMetaData {
  private long metricId;
  // Inclusive
  private DateTime startTime;
  // Exclusive
  private DateTime endTime;
  private TimeGranularity granularity;
  // This filter defines the sub-cube C' at which the time series is located
  private ImmutableMultimap<String, String> filter = ImmutableMultimap.of();
  // (dimension name, dimension value) pair
  // This filter defines the dimension of the time series at the sub-cube C'
  private ImmutableMap<String, String> groupByFilter = ImmutableMap.of();
  // Combination of filter + groupByFilter, which defines a sub-cube C'' that is explored by the Group-By query
  // starting from the sub-cube C'. In other words, this filter gives the sub-cube C'' which is the final sub-cube at
  // which the time series is located.
  private ImmutableMultimap<String, String> timeSeriesFilter = null;

  public ImmutableMultimap<String, String> getTimeSeriesFilter() {
    if (timeSeriesFilter == null) {
      ImmutableMultimap.Builder<String, String> multimapBuilder = ImmutableMultimap.builder();
      for (Map.Entry<String, String> entry : groupByFilter.entrySet()) {
        String dimensionName = entry.getKey();
        String dimensionValue = entry.getValue();
        multimapBuilder.put(dimensionName, dimensionValue);
      }
      for (String key : filter.keySet()) {
        if (!groupByFilter.containsKey(key)) {
          ImmutableCollection<String> dimensionValues = filter.get(key);
          multimapBuilder.putAll(key, dimensionValues);
        }
      }
      timeSeriesFilter = multimapBuilder.build();
    }
    return timeSeriesFilter;
  }
}
