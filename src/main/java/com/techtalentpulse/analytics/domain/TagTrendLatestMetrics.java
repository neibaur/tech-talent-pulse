package com.techtalentpulse.analytics.domain;

import java.time.LocalDate;

public record TagTrendLatestMetrics(
    LocalDate snapshotDate,
    int signalCount,
    double averageScore,
    double averageAnswerCount,
    Integer currentRank) {}
