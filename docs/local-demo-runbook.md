# Local Demo Runbook

This runbook shows how a reviewer can start PostgreSQL, run the API locally, load explicit demo data,
and verify the read-only dashboard endpoints.

## Prerequisites

- Java 21
- Maven
- Docker Desktop or another Docker environment with Compose support

## 1. Configure Local Environment Variables

The repository does not contain database credential values. Set these environment variables in your
local shell using values that are only for your machine:

- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `TECH_TALENT_PULSE_DATASOURCE_URL`
- `TECH_TALENT_PULSE_DATASOURCE_USERNAME`
- `TECH_TALENT_PULSE_DATASOURCE_PASSWORD`

The `TECH_TALENT_PULSE_DATASOURCE_*` values should point the Spring Boot app at the PostgreSQL
container started by Docker Compose.

## 2. Start PostgreSQL

```bash
docker compose up -d postgres
```

Confirm the Compose file is valid:

```bash
docker compose config
```

## 3. Run The Spring Boot App With Demo Data

The demo data seeder is disabled by default. It runs only when the `demo` Spring profile is active
and `tech-talent-pulse.demo-data.enabled=true` is loaded from `application-demo.yml`.

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

On startup, Flyway applies the production-safe schema migrations, then the demo seeder inserts
sample Stack Overflow-like raw question signals and runs the existing analytics transformation.
The seeded records are clearly marked in raw payload titles as demo sample data.

The seeder is idempotent for its provider IDs, so restarting the app with the `demo` profile should
not duplicate raw demo signals.

## 4. Call The API

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

## 5. Lightweight API Smoke Test

After the app starts with the `demo` profile, these commands should return HTTP 200:

```bash
curl -o /dev/null -s -w "%{http_code}\n" "http://localhost:8080/api/trends?limit=5"
curl -o /dev/null -s -w "%{http_code}\n" "http://localhost:8080/api/trends/java?limit=3"
curl -o /dev/null -s -w "%{http_code}\n" "http://localhost:8080/api/trends/summary?limit=5"
```

## 6. Run Validation

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
- No Flyway migration inserts demo data.
- No external API calls are required for the demo workflow.
- No secrets or credential values are stored in demo configuration.
- The demo seeder uses repository and transformation paths instead of raw SQL inserts.
