package com.techtalentpulse.analytics.domain;

import java.time.LocalDate;

public record TrendDelta(
    String tag,
    LocalDate currentSnapshotDate,
    LocalDate previousSnapshotDate,
    int currentSignalCount,
    int previousSignalCount,
    int absoluteDelta,
    Double percentChange,
    int currentRank,
    Integer previousRank,
    Integer rankMovement) {}
