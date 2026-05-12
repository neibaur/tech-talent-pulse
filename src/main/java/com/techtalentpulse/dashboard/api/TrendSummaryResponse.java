package com.techtalentpulse.dashboard.api;

import com.techtalentpulse.dashboard.domain.TrendSummary;
import java.time.LocalDate;
import java.util.List;

public record TrendSummaryResponse(
    LocalDate mostRecentSnapshotDate, List<TopTagTrendResponse> topTags) {

  static TrendSummaryResponse from(TrendSummary summary) {
    return new TrendSummaryResponse(
        summary.mostRecentSnapshotDate(),
        summary.topTags().stream().map(TopTagTrendResponse::from).toList());
  }
}
