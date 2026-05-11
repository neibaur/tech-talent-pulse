package com.techtalentpulse.ingestion.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.techtalentpulse.ingestion.domain.IngestionProvider;
import com.techtalentpulse.ingestion.domain.SignalType;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RawTechnologySignalRepositoryTest {

  @Container
  static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  @DynamicPropertySource
  static void postgresProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  private final RawTechnologySignalRepository repository;

  @Autowired
  RawTechnologySignalRepositoryTest(RawTechnologySignalRepository repository) {
    this.repository = repository;
  }

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
