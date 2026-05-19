package com.techtalentpulse;

import com.techtalentpulse.ingestion.application.StackOverflowQuestionClient;
import com.techtalentpulse.ingestion.infrastructure.persistence.IngestionRunRepository;
import com.techtalentpulse.ingestion.infrastructure.persistence.RawTechnologySignalRepository;
import com.techtalentpulse.transformation.infrastructure.persistence.TechnologyTrendSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
    properties = {
      "spring.autoconfigure.exclude="
          + "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
          + "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
          + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration",
      "tech-talent-pulse.stack-exchange.scheduler-enabled=false",
      "tech-talent-pulse.stack-exchange.base-url=https://api.stackexchange.com/2.3",
      "tech-talent-pulse.stack-exchange.site=stackoverflow",
      "tech-talent-pulse.stack-exchange.default-page-size=1",
      "tech-talent-pulse.stack-exchange.target-tags[0]=java"
    })
class TechTalentPulseApplicationTests {

  @MockitoBean private StackOverflowQuestionClient questionClient;

  @MockitoBean private IngestionRunRepository ingestionRunRepository;

  @MockitoBean private RawTechnologySignalRepository rawTechnologySignalRepository;

  @MockitoBean private TechnologyTrendSnapshotRepository technologyTrendSnapshotRepository;

  @Test
  void contextLoads() {}
}
