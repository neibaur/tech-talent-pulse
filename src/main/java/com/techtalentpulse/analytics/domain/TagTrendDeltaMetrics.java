package com.techtalentpulse.analytics.domain;

import java.time.LocalDate;

public record TagTrendDeltaMetrics(
    LocalDate previousSnapshotDate,
    int previousSignalCount,
    int absoluteDelta,
    Double percentChange,
    Integer previousRank,
    Integer rankMovement) {}
