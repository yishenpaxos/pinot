package com.linkedin.thirdeye.taskpipeline.datatype.timeseries;

import com.linkedin.thirdeye.dataframe.DataFrame;
import java.util.List;

public interface TimeSeriesHandler {

  /**
   * Gets time series of the given time series request; the returned time series is stored in a {@link DataFrame}.
   *
   * @param timeSeriesRequest the request to retrieve time series.
   *
   * @return a data frame that store the time series of the given request.
   */
  DataFrame handle(TimeSeriesRequest timeSeriesRequest) throws Exception;

  /**
   * Gets multiple time series of the given time series requests; the returned time series are stored in a
   * {@link DataFrame}.
   *
   * @param timeSeriesRequests the list of requests to retrieve multiple time series.
   *
   * @return a data frame that store the time series of the given requests.
   */
  List<DataFrame> handle(List<TimeSeriesRequest> timeSeriesRequests) throws Exception;
}
