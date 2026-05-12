package com.techtalentpulse.ingestion.application;

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
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StackOverflowIngestionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(StackOverflowIngestionService.class);

  private final StackOverflowQuestionClient questionClient;
  private final StackExchangeApiProperties properties;
  private final IngestionRunRepository ingestionRunRepository;
  private final RawTechnologySignalRepository rawTechnologySignalRepository;
  private final Clock clock;

  public StackOverflowIngestionService(
      StackOverflowQuestionClient questionClient,
      StackExchangeApiProperties properties,
      IngestionRunRepository ingestionRunRepository,
      RawTechnologySignalRepository rawTechnologySignalRepository,
      Clock clock) {
    this.questionClient = questionClient;
    this.properties = properties;
    this.ingestionRunRepository = ingestionRunRepository;
    this.rawTechnologySignalRepository = rawTechnologySignalRepository;
    this.clock = clock;
  }

  @Transactional
  public IngestionRunEntity ingestConfiguredTags() {
    IngestionRunEntity run =
        ingestionRunRepository.save(
            new IngestionRunEntity(
                IngestionProvider.STACK_OVERFLOW, IngestionRunStatus.STARTED, Instant.now(clock)));
    LOGGER.info(
        "stack_overflow_ingestion_started runId={} tags={} pageSize={}",
        run.getId(),
        properties.targetTags().size(),
        properties.defaultPageSize());

    try {
      for (String tag : properties.targetTags()) {
        TagIngestionCounts tagCounts = ingestTag(tag);
        run.addRequested(properties.defaultPageSize());
        run.addFetched(tagCounts.fetchedCount());
        run.addCaptured(tagCounts.persistedCount());
        run.addDuplicateSkipped(tagCounts.duplicateCount());
        LOGGER.info(
            "stack_overflow_ingestion_tag_completed tag={} fetched={} persisted={} duplicates={}",
            tag,
            tagCounts.fetchedCount(),
            tagCounts.persistedCount(),
            tagCounts.duplicateCount());
      }

      run.complete(Instant.now(clock));
      LOGGER.info(
          "stack_overflow_ingestion_completed runId={} status={} requested={} fetched={} persisted={} duplicates={}",
          run.getId(),
          run.getStatus(),
          run.getItemsRequested(),
          run.getItemsFetched(),
          run.getItemsCaptured(),
          run.getItemsDuplicateSkipped());
      return ingestionRunRepository.save(run);
    } catch (RuntimeException exception) {
      run.fail(exception.getMessage(), Instant.now(clock));
      ingestionRunRepository.save(run);
      LOGGER.warn(
          "stack_overflow_ingestion_failed runId={} requested={} fetched={} persisted={} duplicates={}",
          run.getId(),
          run.getItemsRequested(),
          run.getItemsFetched(),
          run.getItemsCaptured(),
          run.getItemsDuplicateSkipped(),
          exception);
      throw exception;
    }
  }

  private TagIngestionCounts ingestTag(String tag) {
    LOGGER.info(
        "stack_overflow_ingestion_tag_started tag={} pageSize={}",
        tag,
        properties.defaultPageSize());
    List<StackOverflowQuestionPayload> questions =
        questionClient.fetchRecentQuestions(tag, properties.defaultPageSize());
    int persisted = 0;
    int duplicates = 0;

    for (StackOverflowQuestionPayload question : questions) {
      if (rawTechnologySignalRepository.existsByProviderAndProviderIdAndSignalType(
          IngestionProvider.STACK_OVERFLOW, question.providerId(), SignalType.QUESTION)) {
        duplicates++;
        continue;
      }

      RawTechnologySignalEntity rawSignal =
          new RawTechnologySignalEntity(
              IngestionProvider.STACK_OVERFLOW,
              question.providerId(),
              SignalType.QUESTION,
              tag,
              question.payload(),
              Instant.now(clock));
      try {
        rawTechnologySignalRepository.save(rawSignal);
        persisted++;
      } catch (DataIntegrityViolationException duplicate) {
        duplicates++;
        LOGGER.debug(
            "stack_overflow_ingestion_duplicate providerId={}", question.providerId(), duplicate);
      }
    }

    return new TagIngestionCounts(questions.size(), persisted, duplicates);
  }

  private record TagIngestionCounts(int fetchedCount, int persistedCount, int duplicateCount) {}
}
