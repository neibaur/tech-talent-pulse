package com.techtalentpulse.transformation.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.techtalentpulse.ingestion.domain.IngestionProvider;
import com.techtalentpulse.ingestion.domain.SignalType;
import com.techtalentpulse.ingestion.infrastructure.persistence.RawTechnologySignalEntity;
import com.techtalentpulse.ingestion.infrastructure.persistence.RawTechnologySignalRepository;
import com.techtalentpulse.transformation.application.TechnologyTrendTransformationService;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = "tech-talent-pulse.stack-exchange.scheduler-enabled=false")
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class TechnologyTrendSnapshotRepositoryTest {

  @Container
  static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  @DynamicPropertySource
  static void registerPostgresProperties(DynamicPropertyRegistry registry) {
    registerPostgresProperties(registry, postgres);
  }

  @Autowired private RawTechnologySignalRepository rawTechnologySignalRepository;

  @Autowired private TechnologyTrendSnapshotRepository snapshotRepository;

  @Autowired private TechnologyTrendTransformationService transformationService;

  @Test
  void transformsRawSignalsIntoUniqueDailySnapshot() {
    rawTechnologySignalRepository.saveAllAndFlush(
        List.of(
            rawSignal(
                "301", "java", "{\"creation_date\":1767268800,\"score\":10,\"answer_count\":2}"),
            rawSignal(
                "302", "java", "{\"creation_date\":1767272400,\"score\":4,\"answer_count\":1}")));

    int snapshots = transformationService.transformStackOverflowQuestionSignals();

    TechnologyTrendSnapshotEntity snapshot =
        snapshotRepository
            .findByProviderAndTagAndSnapshotDate(
                IngestionProvider.STACK_OVERFLOW, "java", LocalDate.parse("2026-01-01"))
            .orElseThrow();
    assertThat(snapshots).isEqualTo(1);
    assertThat(snapshotRepository.count()).isEqualTo(1);
    assertThat(snapshot.getSignalCount()).isEqualTo(2);
    assertThat(snapshot.getAverageScore()).isEqualTo(7.0);
    assertThat(snapshot.getAverageAnswerCount()).isEqualTo(1.5);

    rawTechnologySignalRepository.saveAndFlush(
        rawSignal("303", "java", "{\"creation_date\":1767276000,\"score\":1,\"answer_count\":0}"));

    int updatedSnapshots = transformationService.transformStackOverflowQuestionSignals();

    TechnologyTrendSnapshotEntity updatedSnapshot =
        snapshotRepository
            .findByProviderAndTagAndSnapshotDate(
                IngestionProvider.STACK_OVERFLOW, "java", LocalDate.parse("2026-01-01"))
            .orElseThrow();
    assertThat(updatedSnapshots).isEqualTo(1);
    assertThat(snapshotRepository.count()).isEqualTo(1);
    assertThat(updatedSnapshot.getSignalCount()).isEqualTo(3);
    assertThat(updatedSnapshot.getAverageScore()).isEqualTo(5.0);
    assertThat(updatedSnapshot.getAverageAnswerCount()).isEqualTo(1.0);
  }

  @Test
  void findsRecentSnapshotsTagHistoryAndTopTags() {
    snapshotRepository.saveAllAndFlush(
        List.of(
            snapshot("java", "2026-01-02", 8),
            snapshot("docker", "2026-01-03", 3),
            snapshot("java", "2026-01-04", 5)));

    List<TechnologyTrendSnapshotEntity> recentSnapshots =
        snapshotRepository.findAllByOrderBySnapshotDateDescTagAsc(PageRequest.of(0, 2));
    List<TechnologyTrendSnapshotEntity> javaHistory =
        snapshotRepository.findByTagIgnoreCaseOrderBySnapshotDateDesc(
            "JAVA", PageRequest.of(0, 10));
    List<TechnologyTrendSnapshotRepository.TagSignalTotal> topTags =
        snapshotRepository.findTopTagsBySignalCount(PageRequest.of(0, 2));

    assertThat(recentSnapshots)
        .extracting(TechnologyTrendSnapshotEntity::getTag)
        .containsExactly("java", "docker");
    assertThat(javaHistory)
        .extracting(TechnologyTrendSnapshotEntity::getSnapshotDate)
        .containsExactly(LocalDate.parse("2026-01-04"), LocalDate.parse("2026-01-02"));
    assertThat(snapshotRepository.findFirstByOrderBySnapshotDateDesc())
        .map(TechnologyTrendSnapshotEntity::getSnapshotDate)
        .contains(LocalDate.parse("2026-01-04"));
    assertThat(topTags)
        .extracting(TechnologyTrendSnapshotRepository.TagSignalTotal::getTag)
        .containsExactly("java", "docker");
    assertThat(topTags.getFirst().getSignalCount()).isEqualTo(13);
  }

  private RawTechnologySignalEntity rawSignal(String providerId, String tag, String payload) {
    return new RawTechnologySignalEntity(
        IngestionProvider.STACK_OVERFLOW,
        providerId,
        SignalType.QUESTION,
        tag,
        payload,
        Instant.parse("2026-01-02T00:00:00Z"));
  }

  private TechnologyTrendSnapshotEntity snapshot(String tag, String snapshotDate, int signalCount) {
    return new TechnologyTrendSnapshotEntity(
        new com.techtalentpulse.transformation.domain.TechnologyTrendMetric(
            LocalDate.parse(snapshotDate),
            tag,
            IngestionProvider.STACK_OVERFLOW,
            signalCount,
            3.0,
            1.0),
        Instant.parse("2026-01-05T00:00:00Z"));
  }

  private static void registerPostgresProperties(
      DynamicPropertyRegistry registry, PostgreSQLContainer<?> postgres) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.flyway.url", postgres::getJdbcUrl);
    registry.add("spring.flyway.user", postgres::getUsername);
    registry.add("spring.flyway.password", postgres::getPassword);
    registry.add("spring.flyway.enabled", () -> true);
    registry.add("spring.flyway.schemas", () -> "tech_talent_pulse");
    registry.add("spring.flyway.default-schema", () -> "tech_talent_pulse");
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    registry.add("spring.jpa.properties.hibernate.default_schema", () -> "tech_talent_pulse");
    registry.add("tech-talent-pulse.stack-exchange.scheduler-enabled", () -> false);
  }
}
