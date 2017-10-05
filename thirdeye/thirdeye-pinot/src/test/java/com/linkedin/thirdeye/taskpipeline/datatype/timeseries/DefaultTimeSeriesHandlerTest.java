package com.linkedin.thirdeye.taskpipeline.datatype.timeseries;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.linkedin.pinot.common.data.TimeGranularitySpec;
import com.linkedin.thirdeye.api.TimeGranularity;
import com.linkedin.thirdeye.api.TimeSpec;
import com.linkedin.thirdeye.constant.MetricAggFunction;
import com.linkedin.thirdeye.dataframe.DataFrame;
import com.linkedin.thirdeye.dataframe.DoubleSeries;
import com.linkedin.thirdeye.dataframe.Series;
import com.linkedin.thirdeye.dataframe.util.DataFrameUtils;
import com.linkedin.thirdeye.datalayer.bao.AbstractManagerTestBase;
import com.linkedin.thirdeye.datalayer.dto.DatasetConfigDTO;
import com.linkedin.thirdeye.datalayer.dto.MetricConfigDTO;
import com.linkedin.thirdeye.datalayer.pojo.MetricConfigBean;
import com.linkedin.thirdeye.datasource.MetricExpression;
import com.linkedin.thirdeye.datasource.MetricFunction;
import com.linkedin.thirdeye.datasource.ThirdEyeCacheRegistry;
import com.linkedin.thirdeye.datasource.ThirdEyeDataSource;
import com.linkedin.thirdeye.datasource.ThirdEyeRequest;
import com.linkedin.thirdeye.datasource.ThirdEyeResponse;
import com.linkedin.thirdeye.datasource.ThirdEyeResponseRow;
import com.linkedin.thirdeye.datasource.cache.QueryCache;
import com.linkedin.thirdeye.datasource.pinot.PinotThirdEyeDataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.joda.time.DateTime;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DefaultTimeSeriesHandlerTest extends AbstractManagerTestBase {
  private ThirdEyeCacheRegistry cacheRegistry = ThirdEyeCacheRegistry.getInstance();

  private static final String DATA_SOURCE_NAME = PinotThirdEyeDataSource.class.getSimpleName();

  private static final String DATASET_NAME = "dataset";
  private static final String METRIC_ID1_NAME = "one";
  private static final String METRIC_ID2_NAME = "two";
  private static final String METRIC_ID3_NAME = "three";
  private static final String METRIC_EXPRESSION = "id1+id2";

  private static final String TIME_COLUMN_NAME = "timestamp";

  private static final String DIMENSION_ONE = "country";
  private static final String COUNTRY_ONE = "US";
  private static final String COUNTRY_TWO = "IN";
  private static final String COUNTRY_THREE = "CN";

  private static final String DIMENSION_TWO = "os";
  private static final String OS_ONE = "iOS";
  private static final String OS_TWO = "Android";

  @BeforeClass
  void beforeClass() throws Exception {
    super.init();
    initCacheAndDAORegistry();
    Assert.assertNotNull(daoRegistry.getMetricConfigDAO());
    Assert.assertNotNull(daoRegistry.getDatasetConfigDAO());
  }

  private void initCacheAndDAORegistry() throws Exception {
    // Mock query cache
    ThirdEyeDataSource mockThirdeyeDataSource = Mockito.mock(ThirdEyeDataSource.class);
    Mockito.when(mockThirdeyeDataSource.execute(Matchers.any(ThirdEyeRequest.class)))
        .thenAnswer(new Answer<ThirdEyeResponse>() {

          @Override
          public ThirdEyeResponse answer(InvocationOnMock invocation) throws Throwable {
            Object[] args = invocation.getArguments();
            ThirdEyeRequest request = (ThirdEyeRequest) args[0];
            ThirdEyeResponse response = buildMockedThirdEyeResponse(request);
            return response;
          }
        });
    Map<String, ThirdEyeDataSource> dataSourceMap = new HashMap<>();
    dataSourceMap.put(DATA_SOURCE_NAME, mockThirdeyeDataSource);

    QueryCache mockQueryCache = new QueryCache(dataSourceMap, Executors.newFixedThreadPool(10));
    cacheRegistry.registerQueryCache(mockQueryCache);

    // Creates metric config in MetricConfigDAO
    MetricConfigDTO metricConfig1 = getTestMetricConfig(DATASET_NAME, METRIC_ID1_NAME, null);
    MetricConfigDTO metricConfig2 = getTestMetricConfig(DATASET_NAME, METRIC_ID2_NAME, null);
    MetricConfigDTO metricConfig3 = getTestMetricConfig(DATASET_NAME, METRIC_ID3_NAME, null);
    metricConfig3.setDerivedMetricExpression(METRIC_EXPRESSION);
    metricConfig3.setDerived(true);

    daoRegistry.getMetricConfigDAO().save(metricConfig1);
    daoRegistry.getMetricConfigDAO().save(metricConfig2);
    daoRegistry.getMetricConfigDAO().save(metricConfig3);

    // Creates dataset config in cache
    LoadingCache<String, DatasetConfigDTO> mockDatasetConfigCache = Mockito.mock(LoadingCache.class);
    Mockito.when(mockDatasetConfigCache.get(DATASET_NAME)).thenReturn(getTestDatasetConfig(DATASET_NAME));
    cacheRegistry.registerDatasetConfigCache(mockDatasetConfigCache);
  }

  @Test
  public void testGetThirdEyeRequestMetaData() {
    DefaultTimeSeriesHandler timeSeriesHandler = new DefaultTimeSeriesHandler(cacheRegistry.getQueryCache());
    DefaultTimeSeriesHandler.ThirdEyeRequestMetaData actualThirdEyeRequestMetaData =
        timeSeriesHandler.getThirdEyeRequestMetaData(buildTimeSeriesRequest());

    MetricExpression expectedMetricExpression = new MetricExpression(METRIC_ID3_NAME, METRIC_EXPRESSION, DATASET_NAME);
    List<MetricExpression> expectedExpressionList = Collections.singletonList(expectedMetricExpression);

    MetricFunction expectedMetricFunction1 =
        new MetricFunction(MetricAggFunction.SUM, MetricConfigBean.DERIVED_METRIC_ID_PREFIX + Long.toString(1L), 1L,
            DATASET_NAME, null, null);
    MetricFunction expectedMetricFunction2 =
        new MetricFunction(MetricAggFunction.SUM, MetricConfigBean.DERIVED_METRIC_ID_PREFIX + Long.toString(2L), 2L,
            DATASET_NAME, null, null);
    List<MetricFunction> expectedMetricFunctionList = Arrays.asList(expectedMetricFunction1, expectedMetricFunction2);
    Set<String> expectedNameSet =
        new HashSet<>(Arrays.asList(expectedMetricFunction1.getMetricName(), expectedMetricFunction2.getMetricName()));

    Assert.assertNotNull(actualThirdEyeRequestMetaData);
    Assert.assertEquals(actualThirdEyeRequestMetaData.getMetricExpressions(), expectedExpressionList);
    Set<MetricFunction> actualMetricFunctions = new HashSet<>(actualThirdEyeRequestMetaData.getMetricFunctions());
    Set<MetricFunction> expectedMetricFunctionSet = new HashSet<>(expectedMetricFunctionList);
    Assert.assertEquals(actualMetricFunctions, expectedMetricFunctionSet);
    Assert.assertEquals(actualThirdEyeRequestMetaData.getMetricNameSet(), expectedNameSet);
  }

  @Test
  public void testBuildThirdEyeRequest() {
    TimeSeriesRequest timeSeriesRequest = buildTimeSeriesRequest();
    DefaultTimeSeriesHandler timeSeriesHandler = new DefaultTimeSeriesHandler(cacheRegistry.getQueryCache());
    DefaultTimeSeriesHandler.ThirdEyeRequestMetaData thirdEyeRequestMetaData =
        timeSeriesHandler.getThirdEyeRequestMetaData(timeSeriesRequest);
    ThirdEyeRequest thirdEyeRequest =
        timeSeriesHandler.buildThirdEyeRequest(timeSeriesRequest, thirdEyeRequestMetaData.getMetricFunctions());
    Assert.assertNotNull(thirdEyeRequest);
    Assert.assertEquals(thirdEyeRequest.getStartTimeInclusive(), timeSeriesRequest.getStartTime());
    Assert.assertEquals(thirdEyeRequest.getEndTimeExclusive(), timeSeriesRequest.getEndTime());
    Assert.assertEquals(thirdEyeRequest.getGroupBy(), timeSeriesRequest.getGroupByDimensionKeys());
    Assert.assertEquals(thirdEyeRequest.getGroupByTimeGranularity(), timeSeriesRequest.getGranularity());
    Assert.assertEquals(thirdEyeRequest.getFilterSet(), timeSeriesRequest.getFilter());
    Set<MetricFunction> actualMetricFunctionSet = new HashSet<>(thirdEyeRequest.getMetricFunctions());
    Set<MetricFunction> expectedMetricFunctionSet = new HashSet<>(thirdEyeRequestMetaData.getMetricFunctions());
    Assert.assertEquals(actualMetricFunctionSet, expectedMetricFunctionSet);
    Set<String> actualMetricNames = new HashSet<>(thirdEyeRequest.getMetricNames());
    Set<String> expectedMetricNames = new HashSet<>();
    for (MetricFunction expectedMetricFunction : thirdEyeRequestMetaData.getMetricFunctions()) {
      expectedMetricNames.add(expectedMetricFunction.toString());
    }
    Assert.assertEquals(actualMetricNames, expectedMetricNames);
    Assert.assertEquals(thirdEyeRequest.getDataSource(), DATA_SOURCE_NAME);
  }

  @Test
  public void testParseResponse() throws Exception {
    TimeSeriesRequest timeSeriesRequest = buildTimeSeriesRequest();
    DefaultTimeSeriesHandler timeSeriesHandler = new DefaultTimeSeriesHandler(cacheRegistry.getQueryCache());
    DefaultTimeSeriesHandler.ThirdEyeRequestMetaData thirdEyeRequestMetaData =
        timeSeriesHandler.getThirdEyeRequestMetaData(timeSeriesRequest);
    ThirdEyeRequest thirdEyeRequest =
        timeSeriesHandler.buildThirdEyeRequest(timeSeriesRequest, thirdEyeRequestMetaData.getMetricFunctions());
    MockedThirdEyeResponse mockedThirdEyeResponse = buildMockedThirdEyeResponse(thirdEyeRequest);
    DataFrame actualDataFrame = timeSeriesHandler.parseResponse(thirdEyeRequestMetaData, mockedThirdEyeResponse);

    Assert.assertEquals(actualDataFrame, buildExpectedDataFrame());
  }

  @Test
  public void testSingleHandle() throws Exception {
    TimeSeriesRequest timeSeriesRequest = buildTimeSeriesRequest();
    DefaultTimeSeriesHandler timeSeriesHandler = new DefaultTimeSeriesHandler(cacheRegistry.getQueryCache());
    DataFrame actualDataFrame = timeSeriesHandler.handle(timeSeriesRequest);

    Assert.assertEquals(actualDataFrame, buildExpectedDataFrame());
  }

  @Test
  public void testListHandle() throws Exception {
    List<TimeSeriesRequest> timeSeriesRequests = Arrays
        .asList(buildTimeSeriesRequest(), buildTimeSeriesRequest(), buildTimeSeriesRequest(), buildTimeSeriesRequest());
    DefaultTimeSeriesHandler timeSeriesHandler = new DefaultTimeSeriesHandler(cacheRegistry.getQueryCache());
    List<DataFrame> actualDataFrames = timeSeriesHandler.handle(timeSeriesRequests);
    for (DataFrame actualDataFrame : actualDataFrames) {
      Assert.assertEquals(actualDataFrame, buildExpectedDataFrame());
    }
  }

  private static TimeSeriesRequest buildTimeSeriesRequest() {
    TimeSeriesRequest.Builder requestBuilder = TimeSeriesRequest.builder();
    requestBuilder.setMetricId(3);
    requestBuilder.setStartTime(new DateTime(0));
    requestBuilder.setEndTime(new DateTime(9));
    Multimap<String, String> filter = ArrayListMultimap.create();
    filter.putAll(DIMENSION_ONE, Arrays.asList(COUNTRY_ONE, COUNTRY_TWO, COUNTRY_THREE));
    filter.putAll(DIMENSION_TWO, Arrays.asList(OS_ONE, OS_TWO));
    requestBuilder.setFilter(filter);
    requestBuilder.setGroupByDimensionKeys(getGroupByDimensionNames());
    requestBuilder.setGranularity(new TimeGranularity(5, TimeUnit.MINUTES));

    return requestBuilder.build();
  }

  private DataFrame buildExpectedDataFrame() {
    DataFrame dataFrame = new DataFrame();

    final String atomicMetric1 = "SUM_id1";
    final String atomicMetric2 = "SUM_id2";

    dataFrame.addSeries(TIME_COLUMN_NAME, 0L, 0L, 1L, 1L, 2L, 2L, 0L, 0L, 1L, 1L, 2L, 2L, 0L, 0L, 1L, 1L, 2L, 2L);
    dataFrame.addSeries(DIMENSION_ONE, COUNTRY_ONE, COUNTRY_ONE, COUNTRY_ONE, COUNTRY_ONE, COUNTRY_ONE, COUNTRY_ONE, COUNTRY_TWO, COUNTRY_TWO, COUNTRY_TWO, COUNTRY_TWO, COUNTRY_TWO, COUNTRY_TWO, COUNTRY_THREE, COUNTRY_THREE, COUNTRY_THREE, COUNTRY_THREE, COUNTRY_THREE, COUNTRY_THREE);
    dataFrame.addSeries(DIMENSION_TWO, OS_ONE, OS_TWO, OS_ONE, OS_TWO, OS_ONE, OS_TWO, OS_ONE, OS_TWO, OS_ONE, OS_TWO, OS_ONE, OS_TWO, OS_ONE, OS_TWO, OS_ONE, OS_TWO, OS_ONE, OS_TWO);
    dataFrame.addSeries(atomicMetric2, 100d, 200d, 101d, 201d, 102d, 202d, 10d, 20d, 10.1, 20.1, 10.2, 20.2, 1.0, 2.0, 1.01, 2.01, 1.02, 2.02);
    dataFrame.addSeries(atomicMetric1, 110d, 210d, 111d, 211d, 112d, 212d, 11.0, 21.0, 11.1, 21.1, 11.2, 21.2, 1.10, 2.10, 1.11, 2.11, 1.12, 2.12);

    Series sum_id1 = dataFrame.getSeries().get(atomicMetric1);
    Series sum_id2 = dataFrame.getSeries().get(atomicMetric2);
    DoubleSeries.Builder builder = DoubleSeries.builder();
    for (int i = 0; i < sum_id1.size(); i++) {
      builder.addValues(sum_id1.getDouble(i) + sum_id2.getDouble(i));
    }
    dataFrame.addSeries(DataFrameUtils.COL_VALUE, builder.build());
    dataFrame = dataFrame.sortedBy(TIME_COLUMN_NAME);

    return dataFrame;
  }

  private MockedThirdEyeResponse buildMockedThirdEyeResponse(ThirdEyeRequest thirdEyeRequest) {
    MockedThirdEyeResponse thirdEyeResponse = new MockedThirdEyeResponse();

    thirdEyeResponse.setThirdEyeRequest(thirdEyeRequest);
    thirdEyeResponse.setMetricFunctions(thirdEyeRequest.getMetricFunctions());

    List<String> groupKeyColumns = new ArrayList<>();
    groupKeyColumns.add(TIME_COLUMN_NAME);
    groupKeyColumns.addAll(getGroupByDimensionNames());
    thirdEyeResponse.setGroupKeyColumns(groupKeyColumns);

    {
      int timeBucketId = 0;
      thirdEyeResponse.addRow(new ThirdEyeResponseRow(timeBucketId, Arrays.asList(COUNTRY_ONE, OS_ONE), Arrays.asList(100d, 110d)));
      thirdEyeResponse.addRow(new ThirdEyeResponseRow(timeBucketId, Arrays.asList(COUNTRY_ONE, OS_TWO), Arrays.asList(200d, 210d)));
      ++timeBucketId;
      thirdEyeResponse.addRow(new ThirdEyeResponseRow(timeBucketId, Arrays.asList(COUNTRY_ONE, OS_ONE), Arrays.asList(101d, 111d)));
      thirdEyeResponse.addRow(new ThirdEyeResponseRow(timeBucketId, Arrays.asList(COUNTRY_ONE, OS_TWO), Arrays.asList(201d, 211d)));
      ++timeBucketId;
      thirdEyeResponse.addRow(new ThirdEyeResponseRow(timeBucketId, Arrays.asList(COUNTRY_ONE, OS_ONE), Arrays.asList(102d, 112d)));
      thirdEyeResponse.addRow(new ThirdEyeResponseRow(timeBucketId, Arrays.asList(COUNTRY_ONE, OS_TWO), Arrays.asList(202d, 212d)));
    }

    {
      int timeBucketId = 0;
      thirdEyeResponse.addRow(new ThirdEyeResponseRow(timeBucketId, Arrays.asList(COUNTRY_TWO, OS_ONE), Arrays.asList(10.0d, 11.0d)));
      thirdEyeResponse.addRow(new ThirdEyeResponseRow(timeBucketId, Arrays.asList(COUNTRY_TWO, OS_TWO), Arrays.asList(20.0d, 21.0d)));
      ++timeBucketId;
      thirdEyeResponse.addRow(new ThirdEyeResponseRow(timeBucketId, Arrays.asList(COUNTRY_TWO, OS_ONE), Arrays.asList(10.1d, 11.1d)));
      thirdEyeResponse.addRow(new ThirdEyeResponseRow(timeBucketId, Arrays.asList(COUNTRY_TWO, OS_TWO), Arrays.asList(20.1d, 21.1d)));
      ++timeBucketId;
      thirdEyeResponse.addRow(new ThirdEyeResponseRow(timeBucketId, Arrays.asList(COUNTRY_TWO, OS_ONE), Arrays.asList(10.2d, 11.2d)));
      thirdEyeResponse.addRow(new ThirdEyeResponseRow(timeBucketId, Arrays.asList(COUNTRY_TWO, OS_TWO), Arrays.asList(20.2d, 21.2d)));
    }

    {
      int timeBucketId = 0;
      thirdEyeResponse.addRow(new ThirdEyeResponseRow(timeBucketId, Arrays.asList(COUNTRY_THREE, OS_ONE), Arrays.asList(1.00d, 1.10d)));
      thirdEyeResponse.addRow(new ThirdEyeResponseRow(timeBucketId, Arrays.asList(COUNTRY_THREE, OS_TWO), Arrays.asList(2.00d, 2.10d)));
      ++timeBucketId;
      thirdEyeResponse.addRow(new ThirdEyeResponseRow(timeBucketId, Arrays.asList(COUNTRY_THREE, OS_ONE), Arrays.asList(1.01d, 1.11d)));
      thirdEyeResponse.addRow(new ThirdEyeResponseRow(timeBucketId, Arrays.asList(COUNTRY_THREE, OS_TWO), Arrays.asList(2.01d, 2.11d)));
      ++timeBucketId;
      thirdEyeResponse.addRow(new ThirdEyeResponseRow(timeBucketId, Arrays.asList(COUNTRY_THREE, OS_ONE), Arrays.asList(1.02d, 1.12d)));
      thirdEyeResponse.addRow(new ThirdEyeResponseRow(timeBucketId, Arrays.asList(COUNTRY_THREE, OS_TWO), Arrays.asList(2.02d, 2.12d)));
    }

    return thirdEyeResponse;
  }

  private static List<String> getGroupByDimensionNames() {
    return Arrays.asList(DIMENSION_ONE, DIMENSION_TWO);
  }

  static class MockedThirdEyeResponse implements ThirdEyeResponse {
    private List<ThirdEyeResponseRow> responseRows = new ArrayList<>();
    private List<MetricFunction> metricFunctions = new ArrayList<>();
    private ThirdEyeRequest thirdEyeRequest;
    private List<String> groupKeyColumns = new ArrayList<>();

    @Override
    public List<MetricFunction> getMetricFunctions() {
      return metricFunctions;
    }

    @Override
    public int getNumRows() {
      return responseRows.size();
    }

    @Override
    public ThirdEyeResponseRow getRow(int rowId) {
      return responseRows.get(rowId);
    }

    @Override
    public int getNumRowsFor(MetricFunction metricFunction) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> getRow(MetricFunction metricFunction, int rowId) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ThirdEyeRequest getRequest() {
      return thirdEyeRequest;
    }

    @Override
    public TimeSpec getDataTimeSpec() {
      return new TimeSpec(TIME_COLUMN_NAME, new TimeGranularity(5, TimeUnit.MINUTES),
          TimeGranularitySpec.TimeFormat.EPOCH.toString());
    }

    @Override
    public List<String> getGroupKeyColumns() {
      return groupKeyColumns;
    }

    void addRow(ThirdEyeResponseRow row) {
      responseRows.add(row);
    }

    void setMetricFunctions(List<MetricFunction> metricFunctions) {
      this.metricFunctions = metricFunctions;
    }

    void setThirdEyeRequest(ThirdEyeRequest thirdEyeRequest) {
      this.thirdEyeRequest = thirdEyeRequest;
    }

    void setGroupKeyColumns(List<String> groupKeyColumns) {
      this.groupKeyColumns = groupKeyColumns;
    }
  }

}
