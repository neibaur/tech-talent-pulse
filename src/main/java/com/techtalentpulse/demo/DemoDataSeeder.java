package com.techtalentpulse.demo;

import com.techtalentpulse.ingestion.domain.IngestionProvider;
import com.techtalentpulse.ingestion.domain.SignalType;
import com.techtalentpulse.ingestion.infrastructure.persistence.RawTechnologySignalEntity;
import com.techtalentpulse.ingestion.infrastructure.persistence.RawTechnologySignalRepository;
import com.techtalentpulse.transformation.application.TechnologyTrendTransformationService;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("demo")
@ConditionalOnProperty(name = "tech-talent-pulse.demo-data.enabled", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(DemoDataSeeder.class);

  private final RawTechnologySignalRepository rawTechnologySignalRepository;
  private final TechnologyTrendTransformationService transformationService;
  private final Clock clock;

  public DemoDataSeeder(
      RawTechnologySignalRepository rawTechnologySignalRepository,
      TechnologyTrendTransformationService transformationService,
      Clock clock) {
    this.rawTechnologySignalRepository = rawTechnologySignalRepository;
    this.transformationService = transformationService;
    this.clock = clock;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    int insertedSignals = 0;
    Instant capturedAt = Instant.now(clock);

    for (DemoSignal signal : demoSignals()) {
      if (rawTechnologySignalRepository.existsByProviderAndProviderIdAndSignalType(
          IngestionProvider.STACK_OVERFLOW, signal.providerId(), SignalType.QUESTION)) {
        continue;
      }

      rawTechnologySignalRepository.save(
          new RawTechnologySignalEntity(
              IngestionProvider.STACK_OVERFLOW,
              signal.providerId(),
              SignalType.QUESTION,
              signal.tag(),
              signal.payload(),
              capturedAt));
      insertedSignals++;
    }

    int transformedSnapshots = transformationService.transformStackOverflowQuestionSignals();
    LOGGER.info(
        "demo_data_seed_completed insertedSignals={} transformedSnapshots={}",
        insertedSignals,
        transformedSnapshots);
  }

  private List<DemoSignal> demoSignals() {
    return List.of(
        signal("demo-java-2026-05-08-01", "java", "2026-05-08T12:00:00Z", 7, 3),
        signal("demo-java-2026-05-09-01", "java", "2026-05-09T12:00:00Z", 11, 4),
        signal("demo-java-2026-05-10-01", "java", "2026-05-10T12:00:00Z", 14, 5),
        signal("demo-spring-boot-2026-05-08-01", "spring-boot", "2026-05-08T12:00:00Z", 5, 2),
        signal("demo-spring-boot-2026-05-09-01", "spring-boot", "2026-05-09T12:00:00Z", 9, 3),
        signal("demo-postgresql-2026-05-09-01", "postgresql", "2026-05-09T12:00:00Z", 6, 2),
        signal("demo-docker-2026-05-10-01", "docker", "2026-05-10T12:00:00Z", 8, 3),
        signal("demo-kubernetes-2026-05-10-01", "kubernetes", "2026-05-10T12:00:00Z", 10, 4));
  }

  private DemoSignal signal(
      String providerId, String tag, String createdAt, int score, int answerCount) {
    long epochSeconds = Instant.parse(createdAt).getEpochSecond();
    String payload =
        """
        {
          "question_id": "%s",
          "title": "DEMO SAMPLE DATA: %s trend signal",
          "creation_date": %d,
          "score": %d,
          "answer_count": %d,
          "tags": ["%s"]
        }
        """
            .formatted(providerId, tag, epochSeconds, score, answerCount, tag);
    return new DemoSignal(providerId, tag, payload);
  }

  private record DemoSignal(String providerId, String tag, String payload) {}
}
