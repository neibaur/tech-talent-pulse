# Local Demo Runbook

This runbook shows how a reviewer can start PostgreSQL, run the API locally, load explicit demo data,
and verify the read-only dashboard endpoints.

## Prerequisites

- Java 21
- Maven
- Docker Desktop or another Docker environment with Compose support

## 1. Review Local Docker Database Defaults

The local demo workflow works without exporting database environment variables first. Docker Compose
and the Spring Boot `demo` profile share these local-only defaults:

- Database name: `tech_talent_pulse`
- Username: `tech_talent_pulse`
- Password: `tech_talent_pulse`

With the `demo` profile active, the Spring Boot app uses this local JDBC URL by default:

```text
jdbc:postgresql://localhost:5432/tech_talent_pulse
```

These defaults are for local Docker demo use only. They are not production credentials.

Optionally override the Docker database values in your local shell:

- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`

If the Spring Boot app should use a different JDBC URL or separate app credentials, optionally
override these demo-specific environment variables:

- `TECH_TALENT_PULSE_DEMO_DATASOURCE_URL`
- `TECH_TALENT_PULSE_DEMO_DATASOURCE_USERNAME`
- `TECH_TALENT_PULSE_DEMO_DATASOURCE_PASSWORD`

## 2. Start PostgreSQL

```bash
docker compose up -d postgres
```

Confirm the Compose file is valid:

```bash
docker compose config
```

## 3. Run The Spring Boot App Without Demo Data

The `demo` profile can start against local Docker PostgreSQL without seeding demo data:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

## 4. Run The Spring Boot App With Demo Data

The demo data seeder is disabled by default. It runs only when the `demo` Spring profile is active
and `tech-talent-pulse.demo-data.enabled=true` is supplied explicitly.

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo -Dspring-boot.run.arguments="--tech-talent-pulse.demo-data.enabled=true"
```

On startup, Flyway applies the production-safe schema migrations, then the demo seeder inserts
sample Stack Overflow-like raw question signals and runs the existing analytics transformation.
The seeded records are clearly marked in raw payload titles as demo sample data.

The seeder is idempotent for its provider IDs, so restarting the app with the `demo` profile should
not duplicate raw demo signals.

## 5. Call The API

Recent trend snapshots:

```bash
curl "http://localhost:8080/api/trends?limit=5"
```

Trend history for one tag:

```bash
curl "http://localhost:8080/api/trends/java?limit=3"
```

Summary:

```bash
curl "http://localhost:8080/api/trends/summary?limit=5"
```

Health check:

```bash
curl "http://localhost:8080/actuator/health"
```

## 6. Lightweight API Smoke Test

After the app starts with the `demo` profile, these commands should return HTTP 200:

```bash
curl -o /dev/null -s -w "%{http_code}\n" "http://localhost:8080/api/trends?limit=5"
curl -o /dev/null -s -w "%{http_code}\n" "http://localhost:8080/api/trends/java?limit=3"
curl -o /dev/null -s -w "%{http_code}\n" "http://localhost:8080/api/trends/summary?limit=5"
```

## 7. Run Validation

Run the full local validation gate:

```bash
mvn clean verify
```

This executes tests, Spotless checks, JaCoCo report generation, and the JaCoCo coverage gate.

View local coverage reports:

- HTML: `target/site/jacoco/index.html`
- XML: `target/site/jacoco/jacoco.xml`
- CSV: `target/site/jacoco/jacoco.csv`

The current line coverage gate is `80%` at the Maven bundle level.

## Demo Data Safety

- Demo data is not loaded by the default profile.
- Demo data is not loaded by the `demo` profile unless `tech-talent-pulse.demo-data.enabled=true`
  is supplied explicitly.
- Demo datasource defaults are local-only and can be overridden through environment variables.
- No Flyway migration inserts demo data.
- No external API calls are required for the demo workflow.
- No production secrets or credential values are stored in demo configuration.
- The demo seeder uses repository and transformation paths instead of raw SQL inserts.
