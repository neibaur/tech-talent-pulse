package com.techtalentpulse.transformation.domain;

import com.techtalentpulse.ingestion.domain.IngestionProvider;
import java.time.LocalDate;

public record TechnologyTrendMetric(
    LocalDate snapshotDate,
    String tag,
    IngestionProvider provider,
    int signalCount,
    double averageScore,
    double averageAnswerCount) {}
