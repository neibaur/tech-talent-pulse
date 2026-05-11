package com.techtalentpulse.ingestion.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.techtalentpulse.ingestion.domain.IngestionProvider;
import com.techtalentpulse.ingestion.domain.IngestionRunStatus;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class IngestionRunRepositoryTest {

  @Container
  static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  @DynamicPropertySource
  static void postgresProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  private final IngestionRunRepository repository;

  @Autowired
  IngestionRunRepositoryTest(IngestionRunRepository repository) {
    this.repository = repository;
  }

  @Test
  void savesCompletedIngestionRun() {
    IngestionRunEntity run =
        new IngestionRunEntity(
            IngestionProvider.STACK_OVERFLOW,
            IngestionRunStatus.RUNNING,
            Instant.parse("2026-01-01T00:00:00Z"));
    run.addRequested(5);
    run.addCaptured(3);
    run.complete(Instant.parse("2026-01-01T00:01:00Z"));

    IngestionRunEntity saved = repository.saveAndFlush(run);

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getStatus()).isEqualTo(IngestionRunStatus.COMPLETED);
    assertThat(saved.getItemsRequested()).isEqualTo(5);
    assertThat(saved.getItemsCaptured()).isEqualTo(3);
  }
}
