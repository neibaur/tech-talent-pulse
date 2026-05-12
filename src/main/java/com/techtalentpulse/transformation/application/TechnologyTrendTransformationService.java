package com.techtalentpulse.transformation.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techtalentpulse.ingestion.domain.IngestionProvider;
import com.techtalentpulse.ingestion.domain.SignalType;
import com.techtalentpulse.ingestion.infrastructure.persistence.RawTechnologySignalEntity;
import com.techtalentpulse.ingestion.infrastructure.persistence.RawTechnologySignalRepository;
import com.techtalentpulse.transformation.domain.TechnologyTrendMetric;
import com.techtalentpulse.transformation.infrastructure.persistence.TechnologyTrendSnapshotEntity;
import com.techtalentpulse.transformation.infrastructure.persistence.TechnologyTrendSnapshotRepository;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TechnologyTrendTransformationService {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(TechnologyTrendTransformationService.class);

  private final RawTechnologySignalRepository rawTechnologySignalRepository;
  private final TechnologyTrendSnapshotRepository snapshotRepository;
  private final ObjectMapper objectMapper;
  private final Clock clock;

  public TechnologyTrendTransformationService(
      RawTechnologySignalRepository rawTechnologySignalRepository,
      TechnologyTrendSnapshotRepository snapshotRepository,
      ObjectMapper objectMapper,
      Clock clock) {
    this.rawTechnologySignalRepository = rawTechnologySignalRepository;
    this.snapshotRepository = snapshotRepository;
    this.objectMapper = objectMapper;
    this.clock = clock;
  }

  @Transactional
  public int transformStackOverflowQuestionSignals() {
    List<RawTechnologySignalEntity> rawSignals =
        rawTechnologySignalRepository.findByProviderAndSignalType(
            IngestionProvider.STACK_OVERFLOW, SignalType.QUESTION);
    Map<MetricKey, MetricAccumulator> groupedMetrics = new HashMap<>();

    for (RawTechnologySignalEntity rawSignal : rawSignals) {
      parseQuestion(rawSignal)
          .ifPresent(
              question -> {
                MetricKey key =
                    new MetricKey(
                        rawSignal.getProvider(), rawSignal.getSourceTag(), question.snapshotDate());
                groupedMetrics
                    .computeIfAbsent(key, ignored -> new MetricAccumulator())
                    .add(question);
              });
    }

    Instant capturedAt = Instant.now(clock);
    groupedMetrics.forEach((key, accumulator) -> saveSnapshot(key, accumulator, capturedAt));
    LOGGER.info("technology_trend_transformation_completed snapshots={}", groupedMetrics.size());
    return groupedMetrics.size();
  }

  private Optional<QuestionMetricInput> parseQuestion(RawTechnologySignalEntity rawSignal) {
    try {
      JsonNode root = objectMapper.readTree(rawSignal.getPayload());
      long creationDateEpochSeconds = root.path("creation_date").asLong(-1);
      if (creationDateEpochSeconds < 0) {
        LOGGER.warn(
            "technology_trend_transformation_missing_creation_date providerId={}",
            rawSignal.getProviderId());
        return Optional.empty();
      }

      LocalDate snapshotDate =
          Instant.ofEpochSecond(creationDateEpochSeconds).atZone(ZoneOffset.UTC).toLocalDate();
      return Optional.of(
          new QuestionMetricInput(
              snapshotDate, root.path("score").asInt(0), root.path("answer_count").asInt(0)));
    } catch (IOException exception) {
      LOGGER.warn(
          "technology_trend_transformation_invalid_payload providerId={}",
          rawSignal.getProviderId(),
          exception);
      return Optional.empty();
    }
  }

  private void saveSnapshot(MetricKey key, MetricAccumulator accumulator, Instant capturedAt) {
    TechnologyTrendMetric metric =
        new TechnologyTrendMetric(
            key.snapshotDate(),
            key.tag(),
            key.provider(),
            accumulator.signalCount(),
            accumulator.averageScore(),
            accumulator.averageAnswerCount());

    TechnologyTrendSnapshotEntity snapshot =
        snapshotRepository
            .findByProviderAndTagAndSnapshotDate(
                metric.provider(), metric.tag(), metric.snapshotDate())
            .map(
                existing -> {
                  existing.updateFrom(metric, capturedAt);
                  return existing;
                })
            .orElseGet(() -> new TechnologyTrendSnapshotEntity(metric, capturedAt));

    snapshotRepository.save(snapshot);
  }

  private record MetricKey(IngestionProvider provider, String tag, LocalDate snapshotDate) {}

  private record QuestionMetricInput(LocalDate snapshotDate, int score, int answerCount) {}

  private static final class MetricAccumulator {

    private int signalCount;
    private int totalScore;
    private int totalAnswerCount;

    void add(QuestionMetricInput question) {
      signalCount++;
      totalScore += question.score();
      totalAnswerCount += question.answerCount();
    }

    int signalCount() {
      return signalCount;
    }

    double averageScore() {
      return (double) totalScore / signalCount;
    }

    double averageAnswerCount() {
      return (double) totalAnswerCount / signalCount;
    }
  }
}
