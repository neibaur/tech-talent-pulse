package com.techtalentpulse.ingestion.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.techtalentpulse.ingestion.domain.IngestionProvider;
import com.techtalentpulse.ingestion.domain.IngestionRunStatus;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = "tech-talent-pulse.stack-exchange.scheduler-enabled=false")
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class IngestionRunRepositoryTest {

  @Container @ServiceConnection
  static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  @Autowired private IngestionRunRepository repository;

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
