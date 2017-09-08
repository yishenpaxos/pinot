/**
 * Copyright (C) 2014-2016 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.linkedin.pinot.core.data.manager.realtime;

import com.linkedin.pinot.common.metrics.ServerGauge;
import com.linkedin.pinot.common.metrics.ServerMeter;
import com.linkedin.pinot.common.metrics.ServerMetrics;
import com.linkedin.pinot.core.data.GenericRow;
import com.linkedin.pinot.core.data.extractors.PlainFieldExtractor;
import com.linkedin.pinot.core.realtime.impl.RealtimeSegmentImpl;
import com.yammer.metrics.core.Meter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KafkaMessageProcessor {
  public long getCurrentOffset() {
    return _currentOffset;
  }

  private long _currentOffset;
  private final int _maxRowsToConusme;
  private int _numRowsConsumed;
  private final Boolean _shouldStop;
  private final ServerMetrics _serverMetrics;
  private final String _metricKeyName;
  private final PlainFieldExtractor _fieldExtractor;
  GenericRow transformedRow = null;
  private RealtimeSegmentImpl _realtimeSegment;
  Meter realtimeRowsConsumedMeter = null;
  Meter realtimeRowsDroppedMeter = null;

  public static Logger LOGGER = LoggerFactory.getLogger(KafkaMessageProcessor.class);

  public KafkaMessageProcessor(int maxRowsToConsume,
      final Boolean shouldStop, ServerMetrics serverMetrics,
      String metricKeyName, PlainFieldExtractor fieldExtractor,
      RealtimeSegmentImpl realtimeSegment) {
    _maxRowsToConusme = maxRowsToConsume;
    _shouldStop = shouldStop;
    _serverMetrics = serverMetrics;
    _metricKeyName = metricKeyName;
    _fieldExtractor = fieldExtractor;
    _realtimeSegment = realtimeSegment;
  }

  public int getNumRowsConsumed() {
    return _numRowsConsumed;
  }

  // Returns false if we cannot take anymore rows.
  public boolean processMessage(GenericRow decodedRow, long offset, long highWaterMark, int totalMsgCount) {
    if (_shouldStop) {
      return false;
    }
    boolean result = true;
    if (_numRowsConsumed == 0) {
      long offsetDifference = highWaterMark - offset;
      _serverMetrics.setValueOfTableGauge(_metricKeyName, ServerGauge.KAFKA_PARTITION_OFFSET_LAG, offsetDifference);
    }
    if (decodedRow != null) {
      transformedRow = GenericRow.createOrReuseRow(transformedRow);
      transformedRow = _fieldExtractor.transform(decodedRow, transformedRow);

      if (transformedRow != null) {
        realtimeRowsConsumedMeter = _serverMetrics.addMeteredTableValue(_metricKeyName, ServerMeter.REALTIME_ROWS_CONSUMED, 1, realtimeRowsConsumedMeter);
      } else {
        realtimeRowsDroppedMeter = _serverMetrics.addMeteredTableValue(_metricKeyName, ServerMeter.INVALID_REALTIME_ROWS_DROPPED, 1, realtimeRowsDroppedMeter);
      }

      result = _realtimeSegment.index(transformedRow);
    } else {
      realtimeRowsDroppedMeter = _serverMetrics.addMeteredTableValue(_metricKeyName, ServerMeter.INVALID_REALTIME_ROWS_DROPPED, 1, realtimeRowsDroppedMeter);
    }


    if (result) {
      _currentOffset = offset;
      _numRowsConsumed++;
    }
    return result;
  }

  public void messageProcessingCompleted(int totalMsgCount) {
    LOGGER.debug("Indexed {} messages ({} messages read from Kafka) current offset {}", _numRowsConsumed, totalMsgCount,
        _currentOffset);
  }

  private boolean indexMessage(GenericRow avroRecord, long offset) {
    return true;
  }
}
