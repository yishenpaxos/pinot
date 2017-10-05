package com.linkedin.thirdeye.taskpipeline.datatype.timeseries;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.linkedin.thirdeye.dataframe.DataFrame;
import com.linkedin.thirdeye.dataframe.util.DataFrameUtils;
import com.linkedin.thirdeye.datalayer.dto.MetricConfigDTO;
import com.linkedin.thirdeye.datasource.MetricExpression;
import com.linkedin.thirdeye.datasource.MetricFunction;
import com.linkedin.thirdeye.datasource.ThirdEyeRequest;
import com.linkedin.thirdeye.datasource.ThirdEyeResponse;
import com.linkedin.thirdeye.datasource.cache.QueryCache;
import com.linkedin.thirdeye.util.ThirdEyeUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class DefaultTimeSeriesHandler implements TimeSeriesHandler {

  private static final int TIMEOUT_SIZE = 5;
  private static final TimeUnit TIMEOUT_UNIT = TimeUnit.MINUTES;

  private final QueryCache queryCache;

  public DefaultTimeSeriesHandler(QueryCache queryCache) {
    Preconditions.checkNotNull(queryCache);
    this.queryCache = queryCache;
  }

  @Override
  public DataFrame handle(TimeSeriesRequest timeSeriesRequest) throws Exception {
    ThirdEyeRequestMetaData thirdEyeRequestMetaData = getThirdEyeRequestMetaData(timeSeriesRequest);
    ThirdEyeRequest thirdEyeRequest =
        buildThirdEyeRequest(timeSeriesRequest, thirdEyeRequestMetaData.getMetricFunctions());
    Future<ThirdEyeResponse> queryResultFuture = queryCache.getQueryResultAsync(thirdEyeRequest);
    ThirdEyeResponse thirdEyeResponse = queryResultFuture.get(TIMEOUT_SIZE, TIMEOUT_UNIT);
    return parseResponse(thirdEyeRequestMetaData, thirdEyeResponse);
  }

  /**
   * Gets multiple time series of the given time series requests; the returned time series are stored in a
   * {@link DataFrame}. The requests are sent out concurrently.
   *
   * @param timeSeriesRequests the list of requests to retrieve multiple time series.
   *
   * @return a data frame that store the time series of the given requests.
   */
  @Override
  public List<DataFrame> handle(List<TimeSeriesRequest> timeSeriesRequests) throws Exception {
    List<ThirdEyeRequestMetaData> thirdEyeRequestMetaDataList = new ArrayList<>();
    List<Future<ThirdEyeResponse>> responseFutures = new ArrayList<>();
    for (TimeSeriesRequest timeSeriesRequest : timeSeriesRequests) {
      ThirdEyeRequestMetaData thirdEyeRequestMetaData = getThirdEyeRequestMetaData(timeSeriesRequest);
      ThirdEyeRequest thirdEyeRequest =
          buildThirdEyeRequest(timeSeriesRequest, thirdEyeRequestMetaData.getMetricFunctions());
      Future<ThirdEyeResponse> queryResult = queryCache.getQueryResultAsync(thirdEyeRequest);

      thirdEyeRequestMetaDataList.add(thirdEyeRequestMetaData);
      responseFutures.add(queryResult);
    }

    List<DataFrame> dataFrames = new ArrayList<>();
    for (int idx = 0; idx < responseFutures.size(); idx++) {
      Future<ThirdEyeResponse> responseFuture = responseFutures.get(idx);
      ThirdEyeResponse thirdEyeResponse = responseFuture.get(TIMEOUT_SIZE, TIMEOUT_UNIT);
      dataFrames.add(parseResponse(thirdEyeRequestMetaDataList.get(idx), thirdEyeResponse));
    }

    return dataFrames;
  }

  /**
   * Calculates MetricFunctions from the MetricExpressions of the given time series request and returns the results in a
   * {@link ThirdEyeRequestMetaData}.
   *
   * @param timeSeriesRequest the time series request to be processed.
   *
   * @return A {@link ThirdEyeRequestMetaData} that contains the MetricExpressions, MetricFunction, and unique metric
   * names of the given time series request.
   */
  ThirdEyeRequestMetaData getThirdEyeRequestMetaData(TimeSeriesRequest timeSeriesRequest) {
    MetricConfigDTO metricConfig = ThirdEyeUtils.getMetricConfigFromId(timeSeriesRequest.getMetricId());

    // The assumption of ThirdEyeUtils.getMetricExpressionFromMetricConfig is that a derived metric is constructed
    // using atomic metrics (non-derived metrics). Thus, it always return one metric expression.
    // However, this assumption may be changed when we want to support nested metric expression (i.e., a metric
    // expression could be composed of one or multiple derived metric expressions). Hence, this handler's implementation
    // handles lists of metric expressions instead of one for future expansion.
    MetricExpression metricExpression = ThirdEyeUtils.getMetricExpressionFromMetricConfig(metricConfig);
    List<MetricExpression> metricExpressions = Collections.singletonList(metricExpression);

    // 1. Constructs the mapping from a expression to list of metric functions
    // 2. Calculates unique metric functions from the list of metric expressions
    Multimap<MetricExpression, MetricFunction> expressionToFunctions = ArrayListMultimap.create();
    Set<MetricFunction> uniqueMetricFunctions = new HashSet<>();
    for (MetricExpression expression : metricExpressions) {
      List<MetricFunction> metricFunctions = expression.computeMetricFunctions();
      expressionToFunctions.putAll(expression, metricFunctions);
      uniqueMetricFunctions.addAll(metricFunctions);
    }
    List<MetricFunction> metricFunctions = new ArrayList<>(uniqueMetricFunctions);

    // Calculates unique metric names
    HashSet<String> metricNameSet = new HashSet<>();
    for (MetricFunction function : metricFunctions) {
      metricNameSet.add(function.getMetricName());
    }

    return new ThirdEyeRequestMetaData(metricExpressions, metricFunctions, expressionToFunctions, metricNameSet);
  }

  /**
   * Converts a time series request to a {@link ThirdEyeRequest}.
   *
   * @param timeSeriesRequest the time series request to be converted.
   * @param metricFunctions   the metric functions of the atomic metrics (non-derived metrics) that are used to query
   *                          the response in order to construct the result of the time series request.
   *
   * @return a ThirdEyeRequest that could be used to query the atomic metrics.
   */
  ThirdEyeRequest buildThirdEyeRequest(TimeSeriesRequest timeSeriesRequest,
      List<MetricFunction> metricFunctions) {

    ThirdEyeRequest.ThirdEyeRequestBuilder thirdEyeRequestBuilder =
        ThirdEyeRequest.newBuilder().setStartTimeInclusive(timeSeriesRequest.getStartTime())
            .setEndTimeExclusive(timeSeriesRequest.getEndTime())
            .setGroupByTimeGranularity(timeSeriesRequest.getGranularity()).setFilterSet(timeSeriesRequest.getFilter())
            .setGroupBy(timeSeriesRequest.getGroupByDimensionKeys()).setMetricFunctions(metricFunctions)
            .setDataSource(ThirdEyeUtils.getDataSourceFromMetricFunctions(metricFunctions));

    return thirdEyeRequestBuilder.build("timeSeriesRequest");
  }

  /**
   * Given a {@link ThirdEyeResponse} and the meta data of the time series request, to which the response corresponding,
   * returns a data frame that contains all intermediate and derived metrics.
   *
   * @param thirdEyeRequestMetaData the meta data of the original time series request.
   * @param thirdEyeResponse        the query response.
   *
   * @return a data frame that contains all intermediate and derived metrics.
   *
   * @throws Exception is thrown when evaluate of the expression is failed.
   */
  DataFrame parseResponse(ThirdEyeRequestMetaData thirdEyeRequestMetaData, ThirdEyeResponse thirdEyeResponse)
      throws Exception {
    // Gets the data frame that contains the values of non-derived metrics
    DataFrame dataFrame = DataFrameUtils.parseResponse(thirdEyeResponse);

    // Extracts derived metric expressions
    List<MetricExpression> derivedMetricExpressions = new ArrayList<>();
    List<MetricExpression> metricExpressions = thirdEyeRequestMetaData.getMetricExpressions();
    Set<String> metricNameSet = thirdEyeRequestMetaData.getMetricNameSet();
    for (MetricExpression metricExpression : metricExpressions) {
      if (!metricNameSet.contains(metricExpression.getExpressionName())) {
        derivedMetricExpressions.add(metricExpression);
      }
    }

    // Calculates the value of derived metrics
    if (derivedMetricExpressions.size() > 0) {
      for (MetricExpression expression : derivedMetricExpressions) {
        DataFrameUtils.evaluateExpressions(dataFrame, Collections.singletonList(expression));
      }
    }

    return dataFrame;
  }

  /**
   * The auxiliary data that are not stored with {@link ThirdEyeRequest} when the request is converted from a {@link
   * TimeSeriesRequest}. This meta data are useful when processing or parsing the returned {@link ThirdEyeResponse}.
   */
  static class ThirdEyeRequestMetaData {
    private ImmutableList<MetricExpression> metricExpressions;
    private ImmutableList<MetricFunction> metricFunctions;
    private ImmutableMultimap<MetricExpression, MetricFunction> expressionToFunctions;
    private ImmutableSet<String> metricNameSet;

    ThirdEyeRequestMetaData(List<MetricExpression> metricExpressions, List<MetricFunction> metricFunctions,
        Multimap<MetricExpression, MetricFunction> expressionToFunctions, Set<String> metricNameSet) {
      this.metricExpressions = ImmutableList.copyOf(metricExpressions);
      this.metricFunctions = ImmutableList.copyOf(metricFunctions);
      this.expressionToFunctions = ImmutableMultimap.copyOf(expressionToFunctions);
      this.metricNameSet = ImmutableSet.copyOf(metricNameSet);
    }

    ImmutableList<MetricExpression> getMetricExpressions() {
      return metricExpressions;
    }

    ImmutableList<MetricFunction> getMetricFunctions() {
      return metricFunctions;
    }

    ImmutableMultimap<MetricExpression, MetricFunction> getExpressionToFunctions() {
      return expressionToFunctions;
    }

    ImmutableSet<String> getMetricNameSet() {
      return metricNameSet;
    }
  }
}
