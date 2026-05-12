package com.techtalentpulse.dashboard.domain;

import com.techtalentpulse.ingestion.domain.IngestionProvider;
import java.time.LocalDate;

public record TrendSnapshot(
    String tag,
    IngestionProvider provider,
    LocalDate snapshotDate,
    int signalCount,
    double averageScore,
    double averageAnswerCount) {}
