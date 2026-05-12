package com.techtalentpulse.analytics.api;

import com.techtalentpulse.analytics.domain.TagTrendComparison;
import com.techtalentpulse.analytics.domain.TagTrendComparisonResult;
import java.util.List;

public record TagTrendComparisonResponse(List<TagComparisonResponse> tags) {

  public static TagTrendComparisonResponse from(TagTrendComparisonResult result) {
    return new TagTrendComparisonResponse(
        result.tags().stream().map(TagComparisonResponse::from).toList());
  }

  public record TagComparisonResponse(
      String requestedTag,
      String normalizedTag,
      boolean found,
      TagTrendLatestMetricsResponse latestMetrics,
      TagTrendDeltaMetricsResponse deltaMetrics,
      List<TagTrendHistoryPointResponse> history) {

    private static TagComparisonResponse from(TagTrendComparison comparison) {
      return new TagComparisonResponse(
          comparison.requestedTag(),
          comparison.normalizedTag(),
          comparison.found(),
          comparison.latestMetrics() == null
              ? null
              : TagTrendLatestMetricsResponse.from(comparison.latestMetrics()),
          comparison.deltaMetrics() == null
              ? null
              : TagTrendDeltaMetricsResponse.from(comparison.deltaMetrics()),
          comparison.history().stream().map(TagTrendHistoryPointResponse::from).toList());
    }
  }
}
