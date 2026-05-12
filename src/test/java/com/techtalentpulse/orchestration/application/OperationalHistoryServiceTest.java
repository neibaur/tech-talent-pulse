package com.techtalentpulse.orchestration.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.techtalentpulse.ingestion.domain.IngestionProvider;
import com.techtalentpulse.ingestion.domain.IngestionRunStatus;
import com.techtalentpulse.ingestion.infrastructure.persistence.IngestionRunEntity;
import com.techtalentpulse.ingestion.infrastructure.persistence.IngestionRunRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;

class OperationalHistoryServiceTest {

  private final IngestionRunRepository ingestionRunRepository = mock(IngestionRunRepository.class);
  private final OperationalHistoryService service =
      new OperationalHistoryService(ingestionRunRepository);

  @Test
  void returnsRecentRunsFromRepositoryOrder() {
    IngestionRunEntity newest = run("2026-01-03T00:00:00Z", 4);
    IngestionRunEntity older = run("2026-01-02T00:00:00Z", 2);
    when(ingestionRunRepository.findAllByOrderByStartedAtDescCompletedAtDesc(any(Pageable.class)))
        .thenReturn(List.of(newest, older));

    List<IngestionRunHistory> history = service.getRecentIngestionRuns(5);

    assertThat(history).hasSize(2);
    assertThat(history)
        .extracting(IngestionRunHistory::startedAt)
        .containsExactly(
            Instant.parse("2026-01-03T00:00:00Z"), Instant.parse("2026-01-02T00:00:00Z"));
    assertThat(history.getFirst().itemsFetched()).isEqualTo(4);
  }

  @Test
  void usesDefaultLimitWhenMissingOrInvalid() {
    service.getRecentIngestionRuns(null);
    service.getRecentIngestionRuns(0);

    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    org.mockito.Mockito.verify(ingestionRunRepository, org.mockito.Mockito.times(2))
        .findAllByOrderByStartedAtDescCompletedAtDesc(pageableCaptor.capture());
    assertThat(pageableCaptor.getAllValues())
        .extracting(Pageable::getPageSize)
        .containsExactly(
            OperationalHistoryService.DEFAULT_LIMIT, OperationalHistoryService.DEFAULT_LIMIT);
  }

  @Test
  void capsLimitAtMaximum() {
    service.getRecentIngestionRuns(500);

    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    org.mockito.Mockito.verify(ingestionRunRepository)
        .findAllByOrderByStartedAtDescCompletedAtDesc(pageableCaptor.capture());
    assertThat(pageableCaptor.getValue().getPageSize())
        .isEqualTo(OperationalHistoryService.MAX_LIMIT);
  }

  @Test
  void returnsEmptyHistoryCleanly() {
    when(ingestionRunRepository.findAllByOrderByStartedAtDescCompletedAtDesc(any(Pageable.class)))
        .thenReturn(List.of());

    assertThat(service.getRecentIngestionRuns(10)).isEmpty();
  }

  private IngestionRunEntity run(String startedAt, int fetched) {
    IngestionRunEntity run =
        new IngestionRunEntity(
            IngestionProvider.STACK_OVERFLOW, IngestionRunStatus.STARTED, Instant.parse(startedAt));
    run.addRequested(5);
    run.addFetched(fetched);
    run.addCaptured(Math.max(0, fetched - 1));
    run.addDuplicateSkipped(fetched > 0 ? 1 : 0);
    run.complete(Instant.parse(startedAt).plusSeconds(30));
    return run;
  }
}
