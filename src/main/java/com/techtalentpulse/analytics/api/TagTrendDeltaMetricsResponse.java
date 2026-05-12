package com.techtalentpulse.analytics.api;

import com.techtalentpulse.analytics.domain.TagTrendDeltaMetrics;
import java.time.LocalDate;

public record TagTrendDeltaMetricsResponse(
    LocalDate previousSnapshotDate,
    int previousSignalCount,
    int absoluteDelta,
    Double percentChange,
    Integer previousRank,
    Integer rankMovement) {

  public static TagTrendDeltaMetricsResponse from(TagTrendDeltaMetrics metrics) {
    return new TagTrendDeltaMetricsResponse(
        metrics.previousSnapshotDate(),
        metrics.previousSignalCount(),
        metrics.absoluteDelta(),
        metrics.percentChange(),
        metrics.previousRank(),
        metrics.rankMovement());
  }
}
