package com.techtalentpulse.orchestration.application;

import com.techtalentpulse.ingestion.infrastructure.persistence.IngestionRunEntity;
import com.techtalentpulse.ingestion.infrastructure.persistence.IngestionRunRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OperationalHistoryService {

  public static final int DEFAULT_LIMIT = 10;
  public static final int MAX_LIMIT = 100;

  private final IngestionRunRepository ingestionRunRepository;

  public OperationalHistoryService(IngestionRunRepository ingestionRunRepository) {
    this.ingestionRunRepository = ingestionRunRepository;
  }

  @Transactional(readOnly = true)
  public List<IngestionRunHistory> getRecentIngestionRuns(Integer requestedLimit) {
    int limit = normalizeLimit(requestedLimit);
    return ingestionRunRepository
        .findAllByOrderByStartedAtDescCompletedAtDesc(PageRequest.of(0, limit))
        .stream()
        .map(this::toHistory)
        .toList();
  }

  public int effectiveLimit(Integer requestedLimit) {
    return normalizeLimit(requestedLimit);
  }

  private int normalizeLimit(Integer requestedLimit) {
    if (requestedLimit == null || requestedLimit < 1) {
      return DEFAULT_LIMIT;
    }
    return Math.min(requestedLimit, MAX_LIMIT);
  }

  private IngestionRunHistory toHistory(IngestionRunEntity run) {
    return new IngestionRunHistory(
        run.getId(),
        run.getProvider(),
        run.getStatus(),
        run.getStartedAt(),
        run.getCompletedAt(),
        run.getErrorMessage(),
        run.getItemsRequested(),
        run.getItemsCaptured(),
        run.getItemsFetched(),
        run.getItemsDuplicateSkipped());
  }
}
