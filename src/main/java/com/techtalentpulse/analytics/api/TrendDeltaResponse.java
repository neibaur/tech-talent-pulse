package com.techtalentpulse.analytics.api;

import com.techtalentpulse.analytics.domain.TrendDelta;
import java.time.LocalDate;

public record TrendDeltaResponse(
    String tag,
    LocalDate currentSnapshotDate,
    LocalDate previousSnapshotDate,
    int currentSignalCount,
    int previousSignalCount,
    int absoluteDelta,
    Double percentChange,
    int currentRank,
    Integer previousRank,
    Integer rankMovement) {

  public static TrendDeltaResponse from(TrendDelta delta) {
    return new TrendDeltaResponse(
        delta.tag(),
        delta.currentSnapshotDate(),
        delta.previousSnapshotDate(),
        delta.currentSignalCount(),
        delta.previousSignalCount(),
        delta.absoluteDelta(),
        delta.percentChange(),
        delta.currentRank(),
        delta.previousRank(),
        delta.rankMovement());
  }
}
