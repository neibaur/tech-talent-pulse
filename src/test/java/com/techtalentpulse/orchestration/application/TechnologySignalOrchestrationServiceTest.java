package com.techtalentpulse.orchestration.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.techtalentpulse.ingestion.application.StackOverflowIngestionService;
import com.techtalentpulse.ingestion.domain.IngestionProvider;
import com.techtalentpulse.ingestion.domain.IngestionRunStatus;
import com.techtalentpulse.ingestion.infrastructure.persistence.IngestionRunEntity;
import com.techtalentpulse.transformation.application.TechnologyTrendTransformationService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class TechnologySignalOrchestrationServiceTest {

  private static final Clock FIXED_CLOCK =
      Clock.fixed(Instant.parse("2026-01-05T12:00:00Z"), ZoneOffset.UTC);

  private final StackOverflowIngestionService ingestionService =
      mock(StackOverflowIngestionService.class);

  private final TechnologyTrendTransformationService transformationService =
      mock(TechnologyTrendTransformationService.class);

  private final TechnologySignalOrchestrationService orchestrationService =
      new TechnologySignalOrchestrationService(
          ingestionService, transformationService, FIXED_CLOCK);

  @Test
  void runIngestionReturnsOperationalCounts() {
    when(ingestionService.ingestConfiguredTags()).thenReturn(completedRun(3, 2, 1));

    OrchestrationResult result = orchestrationService.runIngestion();

    assertThat(result.status()).isEqualTo(OrchestrationStatus.COMPLETED);
    assertThat(result.provider()).isEqualTo(IngestionProvider.STACK_OVERFLOW);
    assertThat(result.fetchedCount()).isEqualTo(3);
    assertThat(result.persistedCount()).isEqualTo(2);
    assertThat(result.duplicateCount()).isEqualTo(1);
    assertThat(result.transformedSnapshotCount()).isZero();
    verify(transformationService, never()).transformStackOverflowQuestionSignals();
  }

  @Test
  void runTransformationReturnsSnapshotCount() {
    when(transformationService.transformStackOverflowQuestionSignals()).thenReturn(4);

    OrchestrationResult result = orchestrationService.runTransformation();

    assertThat(result.status()).isEqualTo(OrchestrationStatus.COMPLETED);
    assertThat(result.provider()).isEqualTo(IngestionProvider.STACK_OVERFLOW);
    assertThat(result.transformedSnapshotCount()).isEqualTo(4);
    assertThat(result.startedAt()).isEqualTo(Instant.parse("2026-01-05T12:00:00Z"));
    assertThat(result.completedAt()).isEqualTo(Instant.parse("2026-01-05T12:00:00Z"));
  }

  @Test
  void runIngestionAndTransformationReturnsCombinedResult() {
    when(ingestionService.ingestConfiguredTags()).thenReturn(completedRun(5, 3, 2));
    when(transformationService.transformStackOverflowQuestionSignals()).thenReturn(2);

    OrchestrationResult result = orchestrationService.runIngestionAndTransformation();

    assertThat(result.status()).isEqualTo(OrchestrationStatus.COMPLETED);
    assertThat(result.fetchedCount()).isEqualTo(5);
    assertThat(result.persistedCount()).isEqualTo(3);
    assertThat(result.duplicateCount()).isEqualTo(2);
    assertThat(result.transformedSnapshotCount()).isEqualTo(2);
  }

  @Test
  void runIngestionReportsZeroRecords() {
    when(ingestionService.ingestConfiguredTags()).thenReturn(completedRun(2, 0, 2));

    OrchestrationResult result = orchestrationService.runIngestion();

    assertThat(result.status()).isEqualTo(OrchestrationStatus.COMPLETED_ZERO_RECORDS);
    assertThat(result.message()).contains("zero persisted records");
  }

  @Test
  void runIngestionAndTransformationStopsWhenIngestionFails() {
    when(ingestionService.ingestConfiguredTags()).thenThrow(new IllegalStateException("boom"));

    OrchestrationResult result = orchestrationService.runIngestionAndTransformation();

    assertThat(result.status()).isEqualTo(OrchestrationStatus.FAILED);
    assertThat(result.message()).contains("Ingestion failed");
    verify(transformationService, never()).transformStackOverflowQuestionSignals();
  }

  @Test
  void runIngestionAndTransformationReportsTransformationFailureWithIngestionCounts() {
    when(ingestionService.ingestConfiguredTags()).thenReturn(completedRun(5, 3, 2));
    when(transformationService.transformStackOverflowQuestionSignals())
        .thenThrow(new IllegalStateException("parse failure"));

    OrchestrationResult result = orchestrationService.runIngestionAndTransformation();

    assertThat(result.status()).isEqualTo(OrchestrationStatus.FAILED);
    assertThat(result.fetchedCount()).isEqualTo(5);
    assertThat(result.persistedCount()).isEqualTo(3);
    assertThat(result.duplicateCount()).isEqualTo(2);
    assertThat(result.message()).contains("Transformation failed");
  }

  private IngestionRunEntity completedRun(int fetched, int persisted, int duplicates) {
    IngestionRunEntity run =
        new IngestionRunEntity(
            IngestionProvider.STACK_OVERFLOW,
            IngestionRunStatus.STARTED,
            Instant.parse("2026-01-05T11:59:00Z"));
    run.addFetched(fetched);
    run.addCaptured(persisted);
    run.addDuplicateSkipped(duplicates);
    run.complete(Instant.parse("2026-01-05T12:00:00Z"));
    return run;
  }
}
