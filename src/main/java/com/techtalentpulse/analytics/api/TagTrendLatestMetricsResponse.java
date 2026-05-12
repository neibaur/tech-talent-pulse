package com.techtalentpulse.analytics.api;

import com.techtalentpulse.analytics.domain.TagTrendLatestMetrics;
import java.time.LocalDate;

public record TagTrendLatestMetricsResponse(
    LocalDate snapshotDate,
    int signalCount,
    double averageScore,
    double averageAnswerCount,
    Integer currentRank) {

  public static TagTrendLatestMetricsResponse from(TagTrendLatestMetrics metrics) {
    return new TagTrendLatestMetricsResponse(
        metrics.snapshotDate(),
        metrics.signalCount(),
        metrics.averageScore(),
        metrics.averageAnswerCount(),
        metrics.currentRank());
  }
}
