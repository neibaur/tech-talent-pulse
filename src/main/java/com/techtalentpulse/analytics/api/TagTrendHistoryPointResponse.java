package com.techtalentpulse.analytics.api;

import com.techtalentpulse.analytics.domain.TagTrendHistoryPoint;
import java.time.LocalDate;

public record TagTrendHistoryPointResponse(
    LocalDate snapshotDate, int signalCount, double averageScore, double averageAnswerCount) {

  public static TagTrendHistoryPointResponse from(TagTrendHistoryPoint point) {
    return new TagTrendHistoryPointResponse(
        point.snapshotDate(),
        point.signalCount(),
        point.averageScore(),
        point.averageAnswerCount());
  }
}
