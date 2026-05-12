package com.techtalentpulse.demo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.techtalentpulse.ingestion.domain.IngestionProvider;
import com.techtalentpulse.ingestion.domain.SignalType;
import com.techtalentpulse.ingestion.infrastructure.persistence.RawTechnologySignalEntity;
import com.techtalentpulse.ingestion.infrastructure.persistence.RawTechnologySignalRepository;
import com.techtalentpulse.transformation.application.TechnologyTrendTransformationService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

class DemoDataSeederTest {

  private final RawTechnologySignalRepository rawTechnologySignalRepository =
      org.mockito.Mockito.mock(RawTechnologySignalRepository.class);

  private final TechnologyTrendTransformationService transformationService =
      org.mockito.Mockito.mock(TechnologyTrendTransformationService.class);

  private final DemoDataSeeder seeder =
      new DemoDataSeeder(
          rawTechnologySignalRepository,
          transformationService,
          Clock.fixed(Instant.parse("2026-05-12T12:00:00Z"), ZoneOffset.UTC));

  @Test
  void insertsMissingDemoSignalsAndRunsTransformation() throws Exception {
    when(rawTechnologySignalRepository.existsByProviderAndProviderIdAndSignalType(
            IngestionProvider.STACK_OVERFLOW, "demo-java-2026-05-08-01", SignalType.QUESTION))
        .thenReturn(true);

    seeder.run(new DefaultApplicationArguments());

    verify(rawTechnologySignalRepository, times(7)).save(any(RawTechnologySignalEntity.class));
    verify(transformationService).transformStackOverflowQuestionSignals();
  }
}
