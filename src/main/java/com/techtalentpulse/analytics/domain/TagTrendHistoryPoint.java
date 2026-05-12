package com.techtalentpulse.analytics.domain;

import java.time.LocalDate;

public record TagTrendHistoryPoint(
    LocalDate snapshotDate, int signalCount, double averageScore, double averageAnswerCount) {}
