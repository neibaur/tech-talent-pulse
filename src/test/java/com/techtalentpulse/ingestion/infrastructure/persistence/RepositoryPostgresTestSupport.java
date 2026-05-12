package com.techtalentpulse.ingestion.infrastructure.persistence;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;

final class RepositoryPostgresTestSupport {

  private static final String APPLICATION_SCHEMA = "tech_talent_pulse";

  private RepositoryPostgresTestSupport() {}

  static void registerPostgresProperties(
      DynamicPropertyRegistry registry, PostgreSQLContainer<?> postgres) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.flyway.url", postgres::getJdbcUrl);
    registry.add("spring.flyway.user", postgres::getUsername);
    registry.add("spring.flyway.password", postgres::getPassword);
    registry.add("spring.flyway.enabled", () -> true);
    registry.add("spring.flyway.schemas", () -> APPLICATION_SCHEMA);
    registry.add("spring.flyway.default-schema", () -> APPLICATION_SCHEMA);
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    registry.add("spring.jpa.properties.hibernate.default_schema", () -> APPLICATION_SCHEMA);
    registry.add("tech-talent-pulse.stack-exchange.scheduler-enabled", () -> false);
  }
}
