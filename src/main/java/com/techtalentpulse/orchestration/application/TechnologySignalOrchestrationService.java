package com.techtalentpulse.orchestration.application;

import com.techtalentpulse.ingestion.application.StackOverflowIngestionService;
import com.techtalentpulse.ingestion.domain.IngestionProvider;
import com.techtalentpulse.ingestion.domain.IngestionRunStatus;
import com.techtalentpulse.ingestion.infrastructure.persistence.IngestionRunEntity;
import com.techtalentpulse.transformation.application.TechnologyTrendTransformationService;
import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TechnologySignalOrchestrationService {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(TechnologySignalOrchestrationService.class);

  private final StackOverflowIngestionService ingestionService;
  private final TechnologyTrendTransformationService transformationService;
  private final Clock clock;

  public TechnologySignalOrchestrationService(
      StackOverflowIngestionService ingestionService,
      TechnologyTrendTransformationService transformationService,
      Clock clock) {
    this.ingestionService = ingestionService;
    this.transformationService = transformationService;
    this.clock = clock;
  }

  public OrchestrationResult runIngestion() {
    Instant startedAt = Instant.now(clock);
    LOGGER.info("technology_signal_orchestration_ingestion_started provider={}", provider());
    try {
      IngestionRunEntity run = ingestionService.ingestConfiguredTags();
      OrchestrationResult result =
          new OrchestrationResult(
              toOrchestrationStatus(run.getStatus()),
              run.getProvider(),
              run.getStartedAt(),
              run.getCompletedAt(),
              run.getItemsFetched(),
              run.getItemsCaptured(),
              run.getItemsDuplicateSkipped(),
              0,
              ingestionMessage(run));
      LOGGER.info(
          "technology_signal_orchestration_ingestion_completed provider={} status={} fetched={} persisted={} duplicates={}",
          result.provider(),
          result.status(),
          result.fetchedCount(),
          result.persistedCount(),
          result.duplicateCount());
      return result;
    } catch (RuntimeException exception) {
      Instant completedAt = Instant.now(clock);
      LOGGER.warn(
          "technology_signal_orchestration_ingestion_failed provider={}", provider(), exception);
      return failedResult(startedAt, completedAt, "Ingestion failed: " + exception.getMessage());
    }
  }

  public OrchestrationResult runTransformation() {
    Instant startedAt = Instant.now(clock);
    LOGGER.info("technology_signal_orchestration_transformation_started provider={}", provider());
    try {
      int snapshotCount = transformationService.transformStackOverflowQuestionSignals();
      Instant completedAt = Instant.now(clock);
      OrchestrationStatus status =
          snapshotCount == 0
              ? OrchestrationStatus.COMPLETED_ZERO_RECORDS
              : OrchestrationStatus.COMPLETED;
      OrchestrationResult result =
          new OrchestrationResult(
              status,
              provider(),
              startedAt,
              completedAt,
              0,
              0,
              0,
              snapshotCount,
              "Transformation completed with %d snapshot(s).".formatted(snapshotCount));
      LOGGER.info(
          "technology_signal_orchestration_transformation_completed provider={} status={} snapshots={}",
          result.provider(),
          result.status(),
          result.transformedSnapshotCount());
      return result;
    } catch (RuntimeException exception) {
      Instant completedAt = Instant.now(clock);
      LOGGER.warn(
          "technology_signal_orchestration_transformation_failed provider={}",
          provider(),
          exception);
      return failedResult(
          startedAt, completedAt, "Transformation failed: " + exception.getMessage());
    }
  }

  public OrchestrationResult runIngestionAndTransformation() {
    Instant startedAt = Instant.now(clock);
    LOGGER.info("technology_signal_orchestration_pipeline_started provider={}", provider());
    OrchestrationResult ingestion = runIngestion();
    if (ingestion.status() == OrchestrationStatus.FAILED) {
      LOGGER.warn(
          "technology_signal_orchestration_pipeline_failed provider={} stage=ingestion",
          provider());
      return new OrchestrationResult(
          OrchestrationStatus.FAILED,
          provider(),
          startedAt,
          ingestion.completedAt(),
          ingestion.fetchedCount(),
          ingestion.persistedCount(),
          ingestion.duplicateCount(),
          0,
          ingestion.message());
    }

    OrchestrationResult transformation = runTransformation();
    OrchestrationStatus status =
        transformation.status() == OrchestrationStatus.FAILED
            ? OrchestrationStatus.FAILED
            : combinedStatus(ingestion, transformation);
    String message =
        transformation.status() == OrchestrationStatus.FAILED
            ? transformation.message()
            : "Ingestion and transformation completed.";
    OrchestrationResult result =
        new OrchestrationResult(
            status,
            provider(),
            startedAt,
            transformation.completedAt(),
            ingestion.fetchedCount(),
            ingestion.persistedCount(),
            ingestion.duplicateCount(),
            transformation.transformedSnapshotCount(),
            message);
    LOGGER.info(
        "technology_signal_orchestration_pipeline_completed provider={} status={} fetched={} persisted={} duplicates={} snapshots={}",
        result.provider(),
        result.status(),
        result.fetchedCount(),
        result.persistedCount(),
        result.duplicateCount(),
        result.transformedSnapshotCount());
    return result;
  }

  private OrchestrationResult failedResult(Instant startedAt, Instant completedAt, String message) {
    return new OrchestrationResult(
        OrchestrationStatus.FAILED, provider(), startedAt, completedAt, 0, 0, 0, 0, message);
  }

  private OrchestrationStatus toOrchestrationStatus(IngestionRunStatus status) {
    return switch (status) {
      case COMPLETED -> OrchestrationStatus.COMPLETED;
      case COMPLETED_ZERO_RECORDS -> OrchestrationStatus.COMPLETED_ZERO_RECORDS;
      case FAILED -> OrchestrationStatus.FAILED;
      case RUNNING, STARTED -> OrchestrationStatus.FAILED;
    };
  }

  private OrchestrationStatus combinedStatus(
      OrchestrationResult ingestion, OrchestrationResult transformation) {
    if (ingestion.status() == OrchestrationStatus.COMPLETED_ZERO_RECORDS
        && transformation.status() == OrchestrationStatus.COMPLETED_ZERO_RECORDS) {
      return OrchestrationStatus.COMPLETED_ZERO_RECORDS;
    }
    return OrchestrationStatus.COMPLETED;
  }

  private String ingestionMessage(IngestionRunEntity run) {
    if (run.getStatus() == IngestionRunStatus.COMPLETED_ZERO_RECORDS) {
      return "Ingestion completed with zero persisted records.";
    }
    return "Ingestion completed.";
  }

  private IngestionProvider provider() {
    return IngestionProvider.STACK_OVERFLOW;
  }
}
