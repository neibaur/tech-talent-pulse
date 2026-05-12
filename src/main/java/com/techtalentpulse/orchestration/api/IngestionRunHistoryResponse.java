package com.techtalentpulse.orchestration.api;

import com.techtalentpulse.ingestion.domain.IngestionProvider;
import com.techtalentpulse.ingestion.domain.IngestionRunStatus;
import com.techtalentpulse.orchestration.application.IngestionRunHistory;
import java.time.Instant;
import java.util.UUID;

public record IngestionRunHistoryResponse(
    UUID id,
    IngestionProvider provider,
    IngestionRunStatus status,
    Instant startedAt,
    Instant completedAt,
    String errorMessage,
    int itemsRequested,
    int itemsCaptured,
    int itemsFetched,
    int itemsDuplicateSkipped) {

  public static IngestionRunHistoryResponse from(IngestionRunHistory history) {
    return new IngestionRunHistoryResponse(
        history.id(),
        history.provider(),
        history.status(),
        history.startedAt(),
        history.completedAt(),
        history.errorMessage(),
        history.itemsRequested(),
        history.itemsCaptured(),
        history.itemsFetched(),
        history.itemsDuplicateSkipped());
  }
}
