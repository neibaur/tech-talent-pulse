package com.techtalentpulse.orchestration.application;

import com.techtalentpulse.ingestion.domain.IngestionProvider;
import com.techtalentpulse.ingestion.domain.IngestionRunStatus;
import java.time.Instant;
import java.util.UUID;

public record IngestionRunHistory(
    UUID id,
    IngestionProvider provider,
    IngestionRunStatus status,
    Instant startedAt,
    Instant completedAt,
    String errorMessage,
    int itemsRequested,
    int itemsCaptured,
    int itemsFetched,
    int itemsDuplicateSkipped) {}
