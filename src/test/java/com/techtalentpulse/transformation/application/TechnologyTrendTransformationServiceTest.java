package com.techtalentpulse.transformation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techtalentpulse.ingestion.domain.IngestionProvider;
import com.techtalentpulse.ingestion.domain.SignalType;
import com.techtalentpulse.ingestion.infrastructure.persistence.RawTechnologySignalEntity;
import com.techtalentpulse.ingestion.infrastructure.persistence.RawTechnologySignalRepository;
import com.techtalentpulse.transformation.infrastructure.persistence.TechnologyTrendSnapshotEntity;
import com.techtalentpulse.transformation.infrastructure.persistence.TechnologyTrendSnapshotRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class TechnologyTrendTransformationServiceTest {

  private static final Clock FIXED_CLOCK =
      Clock.fixed(Instant.parse("2026-01-04T10:15:30Z"), ZoneOffset.UTC);

  private final RawTechnologySignalRepository rawTechnologySignalRepository =
      mock(RawTechnologySignalRepository.class);
  private final TechnologyTrendSnapshotRepository snapshotRepository =
      mock(TechnologyTrendSnapshotRepository.class);

  @Test
  void aggregatesStackOverflowQuestionsIntoDailyTagSnapshot() {
    TechnologyTrendTransformationService service =
        new TechnologyTrendTransformationService(
            rawTechnologySignalRepository, snapshotRepository, new ObjectMapper(), FIXED_CLOCK);
    when(rawTechnologySignalRepository.findByProviderAndSignalType(
            IngestionProvider.STACK_OVERFLOW, SignalType.QUESTION))
        .thenReturn(
            List.of(
                rawSignal(
                    "100", "java", "{\"creation_date\":1767225600,\"score\":4,\"answer_count\":1}"),
                rawSignal(
                    "101",
                    "java",
                    "{\"creation_date\":1767229200,\"score\":8,\"answer_count\":3}")));
    when(snapshotRepository.findByProviderAndTagAndSnapshotDate(
            IngestionProvider.STACK_OVERFLOW, "java", LocalDate.parse("2026-01-01")))
        .thenReturn(Optional.empty());

    int snapshots = service.transformStackOverflowQuestionSignals();

    ArgumentCaptor<TechnologyTrendSnapshotEntity> snapshotCaptor =
        ArgumentCaptor.forClass(TechnologyTrendSnapshotEntity.class);
    verify(snapshotRepository).save(snapshotCaptor.capture());
    TechnologyTrendSnapshotEntity snapshot = snapshotCaptor.getValue();
    assertThat(snapshots).isEqualTo(1);
    assertThat(snapshot.getProvider()).isEqualTo(IngestionProvider.STACK_OVERFLOW);
    assertThat(snapshot.getTag()).isEqualTo("java");
    assertThat(snapshot.getSnapshotDate()).isEqualTo(LocalDate.parse("2026-01-01"));
    assertThat(snapshot.getSignalCount()).isEqualTo(2);
    assertThat(snapshot.getAverageScore()).isEqualTo(6.0);
    assertThat(snapshot.getAverageAnswerCount()).isEqualTo(2.0);
    assertThat(snapshot.getCapturedAt()).isEqualTo(Instant.parse("2026-01-04T10:15:30Z"));
  }

  @Test
  void skipsSignalsWithoutStackOverflowCreationDate() {
    TechnologyTrendTransformationService service =
        new TechnologyTrendTransformationService(
            rawTechnologySignalRepository, snapshotRepository, new ObjectMapper(), FIXED_CLOCK);
    when(rawTechnologySignalRepository.findByProviderAndSignalType(
            IngestionProvider.STACK_OVERFLOW, SignalType.QUESTION))
        .thenReturn(List.of(rawSignal("200", "docker", "{\"score\":4,\"answer_count\":1}")));

    int snapshots = service.transformStackOverflowQuestionSignals();

    assertThat(snapshots).isZero();
    verify(snapshotRepository, never()).save(any());
  }

  private RawTechnologySignalEntity rawSignal(String providerId, String tag, String payload) {
    return new RawTechnologySignalEntity(
        IngestionProvider.STACK_OVERFLOW,
        providerId,
        SignalType.QUESTION,
        tag,
        payload,
        Instant.parse("2026-01-04T00:00:00Z"));
  }
}
