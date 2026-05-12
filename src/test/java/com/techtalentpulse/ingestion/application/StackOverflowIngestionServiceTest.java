package com.techtalentpulse.ingestion.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.techtalentpulse.config.StackExchangeApiProperties;
import com.techtalentpulse.ingestion.domain.IngestionProvider;
import com.techtalentpulse.ingestion.domain.IngestionRunStatus;
import com.techtalentpulse.ingestion.domain.SignalType;
import com.techtalentpulse.ingestion.infrastructure.persistence.IngestionRunEntity;
import com.techtalentpulse.ingestion.infrastructure.persistence.IngestionRunRepository;
import com.techtalentpulse.ingestion.infrastructure.persistence.RawTechnologySignalEntity;
import com.techtalentpulse.ingestion.infrastructure.persistence.RawTechnologySignalRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class StackOverflowIngestionServiceTest {

  private static final Clock FIXED_CLOCK =
      Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

  private final StackOverflowQuestionClient questionClient =
      mock(StackOverflowQuestionClient.class);
  private final IngestionRunRepository ingestionRunRepository = mock(IngestionRunRepository.class);
  private final RawTechnologySignalRepository rawTechnologySignalRepository =
      mock(RawTechnologySignalRepository.class);

  @Test
  void persistsNewRawQuestionsAndCompletesRun() {
    StackOverflowIngestionService service = serviceWithTags(List.of("java"));
    when(ingestionRunRepository.save(any(IngestionRunEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(questionClient.fetchRecentQuestions("java", 2))
        .thenReturn(List.of(new StackOverflowQuestionPayload("1001", "{\"question_id\":1001}")));
    when(rawTechnologySignalRepository.existsByProviderAndProviderIdAndSignalType(
            IngestionProvider.STACK_OVERFLOW, "1001", SignalType.QUESTION))
        .thenReturn(false);
    when(rawTechnologySignalRepository.save(any(RawTechnologySignalEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    IngestionRunEntity completedRun = service.ingestConfiguredTags();

    assertThat(completedRun.getStatus()).isEqualTo(IngestionRunStatus.COMPLETED);
    assertThat(completedRun.getItemsRequested()).isEqualTo(2);
    assertThat(completedRun.getItemsFetched()).isEqualTo(1);
    assertThat(completedRun.getItemsCaptured()).isEqualTo(1);
    assertThat(completedRun.getItemsDuplicateSkipped()).isZero();
    verify(rawTechnologySignalRepository).save(any(RawTechnologySignalEntity.class));
  }

  @Test
  void skipsAlreadyCapturedQuestions() {
    StackOverflowIngestionService service = serviceWithTags(List.of("docker"));
    when(ingestionRunRepository.save(any(IngestionRunEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(questionClient.fetchRecentQuestions("docker", 2))
        .thenReturn(List.of(new StackOverflowQuestionPayload("2002", "{\"question_id\":2002}")));
    when(rawTechnologySignalRepository.existsByProviderAndProviderIdAndSignalType(
            IngestionProvider.STACK_OVERFLOW, "2002", SignalType.QUESTION))
        .thenReturn(true);

    IngestionRunEntity completedRun = service.ingestConfiguredTags();

    assertThat(completedRun.getStatus()).isEqualTo(IngestionRunStatus.COMPLETED_ZERO_RECORDS);
    assertThat(completedRun.getItemsFetched()).isEqualTo(1);
    assertThat(completedRun.getItemsCaptured()).isZero();
    assertThat(completedRun.getItemsDuplicateSkipped()).isEqualTo(1);
    verify(rawTechnologySignalRepository, never()).save(any(RawTechnologySignalEntity.class));
  }

  private StackOverflowIngestionService serviceWithTags(List<String> tags) {
    StackExchangeApiProperties properties =
        new StackExchangeApiProperties(
            "https://api.stackexchange.com/2.3", "stackoverflow", 2, tags, false);
    return new StackOverflowIngestionService(
        questionClient,
        properties,
        ingestionRunRepository,
        rawTechnologySignalRepository,
        FIXED_CLOCK);
  }
}
