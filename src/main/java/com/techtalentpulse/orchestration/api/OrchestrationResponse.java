package com.techtalentpulse.orchestration.api;

import com.techtalentpulse.ingestion.domain.IngestionProvider;
import com.techtalentpulse.orchestration.application.OrchestrationResult;
import com.techtalentpulse.orchestration.application.OrchestrationStatus;
import java.time.Instant;

public record OrchestrationResponse(
    OrchestrationStatus status,
    IngestionProvider provider,
    Instant startedAt,
    Instant completedAt,
    int fetchedCount,
    int persistedCount,
    int duplicateCount,
    int transformedSnapshotCount,
    String message) {

  public static OrchestrationResponse from(OrchestrationResult result) {
    return new OrchestrationResponse(
        result.status(),
        result.provider(),
        result.startedAt(),
        result.completedAt(),
        result.fetchedCount(),
        result.persistedCount(),
        result.duplicateCount(),
        result.transformedSnapshotCount(),
        result.message());
  }
}
