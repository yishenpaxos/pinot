package com.linkedin.thirdeye.taskpipeline.anomalydetection;

import com.linkedin.thirdeye.anomalydetection.context.AnomalyResult;
import com.linkedin.thirdeye.api.DimensionMap;
import com.linkedin.thirdeye.dataframe.DataFrame;
import com.linkedin.thirdeye.datalayer.dto.MergedAnomalyResultDTO;
import com.linkedin.thirdeye.taskexecution.dag.DAG;
import com.linkedin.thirdeye.taskexecution.dag.NodeIdentifier;
import com.linkedin.thirdeye.taskexecution.dataflow.reader.Reader;
import com.linkedin.thirdeye.taskexecution.executor.DAGConfig;
import com.linkedin.thirdeye.taskexecution.executor.NodeConfig;
import com.linkedin.thirdeye.taskexecution.impl.executor.SystemContext;
import com.linkedin.thirdeye.taskexecution.impl.operator.BaseOperatorConfig;
import com.linkedin.thirdeye.taskexecution.impl.operator.Operator0x1;
import com.linkedin.thirdeye.taskexecution.impl.operator.Operator1x1;
import com.linkedin.thirdeye.taskexecution.impl.operator.Operator2x1;
import com.linkedin.thirdeye.taskexecution.impl.physicaldag.PhysicalDAGBuilder;
import com.linkedin.thirdeye.taskexecution.operator.OperatorConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration.Configuration;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class for prototyping a DAG for anomaly detection.
 *
 * TODO: Generalize to JobPlan or PlanNode
 */
public class AnomalyDetectionPipelinePrototype {
  private static final Logger LOG = LoggerFactory.getLogger(AnomalyDetectionPipelinePrototype.class);

  /**
   * Returns the following DAG:
   *
   * TimeSeriesFetcher ---> AnomalyDetectionOperator --> AnomalyMerger --> TimeSeriesFetcher --> AnomalyUpdater
   *                    ^                                              \                      ^
   *                   /                                                \                    /
   * AnomalyFetcher --/                                                  \- AnomalyFetcher -/
   */
  public static DAG getDAG() {
    PhysicalDAGBuilder dagBuilder = new PhysicalDAGBuilder();

    TimeSeriesFetcher timeSeriesFetcher =
        dagBuilder.addOperator(new NodeIdentifier("TimeSeriesFetcher"), new TimeSeriesFetcher());

    AnomalyFetcher anomalyFetcher = dagBuilder.addOperator(new NodeIdentifier("AnomalyFetcher"), AnomalyFetcher.class);

    AnomalyDetectionOperator detectionOperator =
        dagBuilder.addOperator(new NodeIdentifier("AnomalyDetector"), new AnomalyDetectionOperator());

    AnomalyMerger merger = dagBuilder.addOperator(new NodeIdentifier("Merger"), AnomalyMerger.class);

    dagBuilder.addChannels(timeSeriesFetcher, anomalyFetcher, detectionOperator);
    dagBuilder.addChannel(detectionOperator, merger);


    return dagBuilder.build();
  }

  public static DAGConfig getDagConfig() {
    DAGConfig dagConfig = new DAGConfig();

    NodeConfig timeSeriesFetcherNodeConfig = new NodeConfig();
    dagConfig.putNodeConfig(new NodeIdentifier("TimeSeriesFetcher"), timeSeriesFetcherNodeConfig);

    return dagConfig;
  }

  /**
   * Dummy Time Series Fetcher
   */
  public static class TimeSeriesFetcher extends Operator0x1<DataFrame> {
    public TimeSeriesFetcher() {
      // Runtime configuration goes here?
      //   Parameters that are generated according to job context such as monitoring window, time range intervals, etc.
    }

    @Override
    public OperatorConfig newOperatorConfigInstance() {
      return new TimeSeriesFetcherConfig();
    }

    @Override
    public void initialize(OperatorConfig operatorConfig) {
      // Static configuration goes here
      //   Parameters that are declare in the configuration file that are stored in some file system.
      TimeSeriesFetcherConfig config = (TimeSeriesFetcherConfig) operatorConfig;
    }

    @Override
    public void run() {
      // Runtime configuration goes here?
      // Job context (monitoring window) could go here.
      // Time range intervals, which is derived from Anomaly Function, is difficult to get.

      LOG.info("Running {}...", getNodeIdentifier());

      DataFrame dataFrame = new DataFrame(3).addSeries("testDoubles", 1.0, 2.0, 3.0);
      //      DataFrame next = new DataFrame();
      //      next.addSeries(COL_TIMESTAMP, 0, 1, 2, 3, 4);
      //      next.addSeries(COL_VALUE, 1.0, 2.0, 3.0, 4.0);
      //      next.setIndex(COL_TIMESTAMP);
      //      dataFrame.addSeries(next, "testIntegers");
      //      dataFrame.groupByValue("index", "testInteger").aggregate("index:FIRST", "testInteger:FIRST", "testDouble:SUM");

      LOG.info("{} fetched time series {}: {}", getNodeIdentifier(), "testDoubles",
          dataFrame.toString());
      getOutputPort().getWriter().write(dataFrame);
    }

    public static class TimeSeriesFetcherConfig extends BaseOperatorConfig {
      List<Interval> dataRanges = Collections.emptyList();

      @Override
      public void initialize(Configuration operatorRawConfig, SystemContext systemContext) {
        super.initialize(operatorRawConfig, systemContext);

      }
    }
  }

  /**
   * Dummy Anomaly Fetcher
   */
  public static class AnomalyFetcher extends Operator0x1<Map<DimensionMap, List<AnomalyResult>>> {
    @Override
    public void initialize(OperatorConfig operatorConfig) {

    }

    @Override
    public void run() {
      NodeIdentifier nodeIdentifier = getNodeIdentifier();
      LOG.info("Running {}...", nodeIdentifier);

      Map<DimensionMap, List<AnomalyResult>> oldAnomalies = new HashMap<>();
      // Create US anomalies
      AnomalyResult USAnomaly1 = new MergedAnomalyResultDTO();
      USAnomaly1.setStartTime(1);
      USAnomaly1.setEndTime(2);
      AnomalyResult USAnomaly2 = new MergedAnomalyResultDTO();
      USAnomaly2.setStartTime(3);
      USAnomaly2.setEndTime(4);
      DimensionMap dimensionMapUS = new DimensionMap();
      dimensionMapUS.put("country", "US");
      oldAnomalies.put(dimensionMapUS, new ArrayList<>(Arrays.asList(USAnomaly1, USAnomaly2)));

      // Create IN anomalies
      AnomalyResult INAnomaly1 = new MergedAnomalyResultDTO();
      INAnomaly1.setStartTime(2);
      INAnomaly1.setEndTime(3);
      AnomalyResult INAnomaly2 = new MergedAnomalyResultDTO();
      INAnomaly2.setStartTime(4);
      INAnomaly2.setEndTime(6);
      DimensionMap dimensionMapIN = new DimensionMap();
      dimensionMapIN.put("country", "IN");
      oldAnomalies.put(dimensionMapIN, new ArrayList<>(Arrays.asList(INAnomaly1, INAnomaly2)));

      LOG.info("{} fetched old anomalies {}", nodeIdentifier, oldAnomalies.toString());
      getOutputPort().getWriter().write(oldAnomalies);
    }
  }

  /**
   * Dummy Anomaly Detector
   */
  public static class AnomalyDetectionOperator
      extends Operator2x1<DataFrame, Map<DimensionMap, List<AnomalyResult>>, Map<DimensionMap, List<AnomalyResult>>> {

    @Override
    public void initialize(OperatorConfig operatorConfig) {

    }

    @Override
    public void run() {
      NodeIdentifier identifier = getNodeIdentifier();
      LOG.info("Running {}...", identifier);

      Reader<DataFrame> reader1 = getInputPort1().getReader();
      while (reader1.hasNext()) {
        DataFrame dataFrame = reader1.next();
        // Combine data frames if necessary, but there should be only one data frame
        LOG.info("{} received time series: {}", identifier, dataFrame);
      }

      Reader<Map<DimensionMap, List<AnomalyResult>>> reader2 = getInputPort2().getReader();
      while (reader2.hasNext()) {
        Map<DimensionMap, List<AnomalyResult>> anomalies = reader2.next();
        LOG.info("{} received old anomalies: {}", identifier, anomalies);
      }

      Map<DimensionMap, List<AnomalyResult>> newAnomalies = new HashMap<>();
      // Create US anomalies
      AnomalyResult USAnomaly1 = new MergedAnomalyResultDTO();
      USAnomaly1.setStartTime(10);
      USAnomaly1.setEndTime(11);
      AnomalyResult USAnomaly2 = new MergedAnomalyResultDTO();
      USAnomaly2.setStartTime(13);
      USAnomaly2.setEndTime(14);
      DimensionMap dimensionMapUS = new DimensionMap();
      dimensionMapUS.put("country", "US");
      newAnomalies.put(dimensionMapUS, new ArrayList<>(Arrays.asList(USAnomaly1, USAnomaly2)));

      // Create IN anomalies
      AnomalyResult INAnomaly1 = new MergedAnomalyResultDTO();
      INAnomaly1.setStartTime(12);
      INAnomaly1.setEndTime(13);
      AnomalyResult INAnomaly2 = new MergedAnomalyResultDTO();
      INAnomaly2.setStartTime(14);
      INAnomaly2.setEndTime(16);
      DimensionMap dimensionMapIN = new DimensionMap();
      dimensionMapIN.put("country", "IN");
      newAnomalies.put(dimensionMapIN, new ArrayList<>(Arrays.asList(INAnomaly1, INAnomaly2)));

      LOG.info("{} detected anomalies: {}", identifier, newAnomalies);
      getOutputPort().getWriter().write(newAnomalies);
    }
  }

  /**
   * Dummy Anomaly Merger
   */
  public static class AnomalyMerger
      extends Operator1x1<Map<DimensionMap, List<AnomalyResult>>, Map<DimensionMap, List<AnomalyResult>>> {

    @Override
    public void initialize(OperatorConfig operatorConfig) {

    }

    @Override
    public void run() {
      NodeIdentifier nodeIdentifier = getNodeIdentifier();
      LOG.info("Running {}...", nodeIdentifier);
      Reader<Map<DimensionMap, List<AnomalyResult>>> reader = getInputPort().getReader();
      while (reader.hasNext()) {
        Map<DimensionMap, List<AnomalyResult>> anomalies = reader.next();
        LOG.info("{} received new anomalies {}", nodeIdentifier, anomalies.toString());
      }

      Map<DimensionMap, List<AnomalyResult>> mergedAnomalies = new HashMap<>();
      // Create US anomalies
      AnomalyResult USAnomaly1 = new MergedAnomalyResultDTO();
      USAnomaly1.setStartTime(10);
      USAnomaly1.setEndTime(13);
      DimensionMap dimensionMapUS = new DimensionMap();
      dimensionMapUS.put("country", "US");
      mergedAnomalies.put(dimensionMapUS, new ArrayList<>(Collections.singletonList(USAnomaly1)));

      // Create IN anomalies
      AnomalyResult INAnomaly1 = new MergedAnomalyResultDTO();
      INAnomaly1.setStartTime(12);
      INAnomaly1.setEndTime(16);
      DimensionMap dimensionMapIN = new DimensionMap();
      dimensionMapIN.put("country", "IN");
      mergedAnomalies.put(dimensionMapIN, new ArrayList<>(Collections.singletonList(INAnomaly1)));

      LOG.info("{} merged anomalies: {}", nodeIdentifier, mergedAnomalies);
      getOutputPort().getWriter().write(mergedAnomalies);
    }
  }
}


