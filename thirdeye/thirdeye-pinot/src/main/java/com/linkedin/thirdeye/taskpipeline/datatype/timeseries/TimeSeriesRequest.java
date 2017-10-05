package com.linkedin.thirdeye.taskpipeline.datatype.timeseries;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.linkedin.thirdeye.api.TimeGranularity;
import java.util.List;
import org.joda.time.DateTime;

public class TimeSeriesRequest {
  private long metricId;
  // Inclusive
  private DateTime startTime;
  // Exclusive
  private DateTime endTime;
  private TimeGranularity granularity;
  // This filter defines the sub-cube C' at which the time series is located
  private ImmutableMultimap<String, String> filter = ImmutableMultimap.of();
  // Dimension exploration starting from C'
  private ImmutableList<String> groupByDimensionKeys = ImmutableList.of();

  TimeSeriesRequest(long metricId, DateTime startTime, DateTime endTime, TimeGranularity granularity,
      ImmutableMultimap<String, String> filter, ImmutableList<String> groupByDimensionKeys) {
    this.metricId = metricId;
    this.startTime = startTime;
    this.endTime = endTime;
    this.granularity = granularity;
    this.filter = filter;
    this.groupByDimensionKeys = groupByDimensionKeys;
  }

  public long getMetricId() {
    return metricId;
  }

  public DateTime getStartTime() {
    return startTime;
  }

  public DateTime getEndTime() {
    return endTime;
  }

  public TimeGranularity getGranularity() {
    return granularity;
  }

  public ImmutableMultimap<String, String> getFilter() {
    return filter;
  }

  public ImmutableList<String> getGroupByDimensionKeys() {
    return groupByDimensionKeys;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private long metricId;
    // Inclusive
    private DateTime startTime;
    // Exclusive
    private DateTime endTime;
    private TimeGranularity granularity;
    // This filter defines the sub-cube C' at which the time series is located
    private Multimap<String, String> filter = ImmutableMultimap.of();
    // Dimension exploration starting from C'
    private List<String> groupByDimensionKeys = ImmutableList.of();

    public void setMetricId(long metricId) {
      this.metricId = metricId;
    }

    public void setStartTime(DateTime startTime) {
      this.startTime = startTime;
    }

    public void setEndTime(DateTime endTime) {
      this.endTime = endTime;
    }

    public void setGranularity(TimeGranularity granularity) {
      this.granularity = granularity;
    }

    public void setFilter(Multimap<String, String> filter) {
      this.filter = filter;
    }

    public void setGroupByDimensionKeys(List<String> groupByDimensionKeys) {
      this.groupByDimensionKeys = groupByDimensionKeys;
    }

    public TimeSeriesRequest build() {
      Preconditions.checkNotNull(metricId);
      Preconditions.checkNotNull(startTime);
      Preconditions.checkNotNull(endTime);
      Preconditions.checkNotNull(granularity);
      Preconditions.checkNotNull(filter);
      Preconditions.checkNotNull(groupByDimensionKeys);

      return new TimeSeriesRequest(metricId, startTime, endTime, granularity, ImmutableMultimap.copyOf(filter),
          ImmutableList.copyOf(groupByDimensionKeys));
    }
  }
}
