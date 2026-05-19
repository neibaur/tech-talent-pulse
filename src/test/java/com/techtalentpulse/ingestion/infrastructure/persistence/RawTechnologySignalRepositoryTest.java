package com.techtalentpulse.ingestion.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.techtalentpulse.ingestion.domain.IngestionProvider;
import com.techtalentpulse.ingestion.domain.SignalType;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = "tech-talent-pulse.stack-exchange.scheduler-enabled=false")
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class RawTechnologySignalRepositoryTest {

  @Container static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

  @DynamicPropertySource
  static void registerPostgresProperties(DynamicPropertyRegistry registry) {
    RepositoryPostgresTestSupport.registerPostgresProperties(registry, postgres);
  }

  @Autowired private RawTechnologySignalRepository repository;

  @Test
  void savesRawSignalPayloadAndFindsExistingProviderSignal() {
    RawTechnologySignalEntity signal =
        new RawTechnologySignalEntity(
            IngestionProvider.STACK_OVERFLOW,
            "101",
            SignalType.QUESTION,
            "java",
            "{\"question_id\":101,\"tags\":[\"java\"]}",
            Instant.parse("2026-01-01T00:00:00Z"));

    RawTechnologySignalEntity saved = repository.saveAndFlush(signal);

    assertThat(saved.getId()).isNotNull();
    assertThat(
            repository.existsByProviderAndProviderIdAndSignalType(
                IngestionProvider.STACK_OVERFLOW, "101", SignalType.QUESTION))
        .isTrue();
  }

  @Test
  void rejectsDuplicateProviderSignal() {
    RawTechnologySignalEntity first =
        new RawTechnologySignalEntity(
            IngestionProvider.STACK_OVERFLOW,
            "202",
            SignalType.QUESTION,
            "docker",
            "{\"question_id\":202}",
            Instant.parse("2026-01-01T00:00:00Z"));
    RawTechnologySignalEntity duplicate =
        new RawTechnologySignalEntity(
            IngestionProvider.STACK_OVERFLOW,
            "202",
            SignalType.QUESTION,
            "docker",
            "{\"question_id\":202}",
            Instant.parse("2026-01-01T00:00:01Z"));

    repository.saveAndFlush(first);

    assertThatThrownBy(() -> repository.saveAndFlush(duplicate))
        .isInstanceOf(DataIntegrityViolationException.class);
  }
}
