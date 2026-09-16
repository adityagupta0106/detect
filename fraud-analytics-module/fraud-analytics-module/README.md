# Fraud Analytics Module (FAM)

VismAI (ServicePlus 2.0) — rule-based fraud detection service for Aadhaar
authentication transactions.

Ref: **SP-SRS-FRAUD-001 v1.0** (see the companion SRS document).

## What this is

A Spring Boot microservice that:

1. Ingests Aadhaar authentication events (primarily via Kafka, with a REST
   fallback), and logs transaction metadata — **never** a raw Aadhaar number,
   biometric template, or OTP value; only the Aadhaar Data Vault (ADV)
   reference token (SRS 5.1 / 6.3).
2. Evaluates each event against a configurable set of **MVEL** rule
   expressions — the same expression engine already standardised in the
   ServicePlus Workflow Management Module (SRS 4.2, "reuse before rebuild").
3. Computes a composite risk score and classifies it Normal / Medium / High
   (SRS 5.3).
4. Raises alerts and dispatches notifications (dashboard always; email/SMS
   for Medium/High) (SRS 5.5).
5. Exposes a monitoring dashboard and an investigation workbench (search,
   timeline, remarks, closure with mandatory disposition) (SRS 5.4 / 5.6).

## A note on this sandbox build

This project was authored in an environment without access to Maven Central
(only npm/PyPI/crates/GitHub registries are reachable), so it has **not**
been compiled or dependency-resolved here. The code is written against
Spring Boot 3.3.2 / Java 21 idioms and should build cleanly with a normal
`mvn clean install` on a machine with standard internet or an internal Nexus/
Artifactory mirror. Please run a build and the test suite in your own CI
before deploying.

## Building

```bash
mvn clean package
java -jar target/fraud-analytics-module.jar
```

## Configuration

All configuration is externalised via environment variables (see
`src/main/resources/application.yml`):

| Variable | Purpose |
|---|---|
| `FAM_DB_URL`, `FAM_DB_USER`, `FAM_DB_PASSWORD` | PostgreSQL connection |
| `FAM_REDIS_HOST`, `FAM_REDIS_PORT`, `FAM_REDIS_PASSWORD` | Redis (rolling counters) |
| `FAM_KAFKA_BOOTSTRAP`, `FAM_KAFKA_TOPIC` | Kafka event source (`auth.events` by default) |
| `FAM_KEYCLOAK_ISSUER_URI` | Keycloak realm issuer for JWT validation |
| `FAM_SMTP_HOST/PORT/USER/PASSWORD` | Email alert channel |
| `FAM_EMAIL_ALERTS_ENABLED`, `FAM_EMAIL_ALERTS_RECIPIENTS` | Email alerting toggle/recipients |
| `FAM_SMS_ALERTS_ENABLED` | SMS alerting toggle (stub — wire to VismAI's SMS gateway) |
| `FAM_SERVER_PORT` | HTTP port (default 8085) |

## Database

Schema is Flyway-managed (`src/main/resources/db/migration/V1__init.sql`),
which also seeds the baseline rule set from SRS 5.2. Do not use
`ddl-auto: update` in any environment — Flyway is the single source of
truth for schema.

## Security

All endpoints (other than `/actuator/health`, `/actuator/info`) require a
valid Keycloak-issued JWT. Rule administration (`/api/fraud/rules/**`)
requires the `fam-admin` realm role; other endpoints accept `fam-admin` or
`fam-viewer` (see `SecurityConfig`).

## API surface

See SP-SRS-FRAUD-001 Section 9 for the full list. Representative endpoints:

- `POST /api/fraud/events` — fallback synchronous ingestion
- `GET /api/fraud/dashboard/summary?windowHours=24`
- `GET /api/fraud/alerts?status=OPEN`
- `GET /api/fraud/alerts/{id}`
- `GET /api/fraud/alerts/timeline/user/{userId}`
- `GET /api/fraud/alerts/timeline/ip/{ip}`
- `POST /api/fraud/alerts/{id}/remarks`
- `POST /api/fraud/alerts/{id}/close`
- `GET|POST|PUT|DELETE /api/fraud/rules` — rule administration (`fam-admin` only)

## Integration checklist for the Aadhaar Authentication Integration Layer

- [ ] Publish one `AuthEventDto`-shaped JSON message to the `auth.events`
      Kafka topic per authentication attempt (success and failure both).
- [ ] Populate `aadhaarRefToken` from the platform's Aadhaar Data Vault —
      never the raw Aadhaar number.
- [ ] Ensure an ADV is already provisioned at the platform level (this
      module only ever handles the resulting token, per SRS 11).
- [ ] Confirm Keycloak realm roles `fam-admin` / `fam-viewer` are defined for
      the departments that will use the dashboard/investigation UI.

## What's intentionally out of scope for v1.0

Impossible-travel detection, bot/velocity checks beyond the baseline rules,
behavioural profiling, blacklisted-IP threat feeds, and ML/AI-based anomaly
scoring — see SRS Section 5.7. These are optional future-phase items, not
required for baseline UIDAI compliance.
