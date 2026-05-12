package com.techtalentpulse.orchestration.application;

import com.techtalentpulse.ingestion.domain.IngestionProvider;
import java.time.Instant;

public record OrchestrationResult(
    OrchestrationStatus status,
    IngestionProvider provider,
    Instant startedAt,
    Instant completedAt,
    int fetchedCount,
    int persistedCount,
    int duplicateCount,
    int transformedSnapshotCount,
    String message) {}
