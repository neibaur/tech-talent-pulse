package com.techtalentpulse.dashboard.api;

import com.techtalentpulse.dashboard.domain.TrendSnapshot;
import java.time.LocalDate;

public record TrendSnapshotResponse(
    String tag,
    String provider,
    LocalDate snapshotDate,
    int signalCount,
    double averageScore,
    double averageAnswerCount) {

  static TrendSnapshotResponse from(TrendSnapshot snapshot) {
    return new TrendSnapshotResponse(
        snapshot.tag(),
        snapshot.provider().name(),
        snapshot.snapshotDate(),
        snapshot.signalCount(),
        snapshot.averageScore(),
        snapshot.averageAnswerCount());
  }
}
