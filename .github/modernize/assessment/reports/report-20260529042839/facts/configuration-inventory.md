# Configuration & Externalized Settings Inventory

The application uses a single properties file with environment-variable overrides for runtime behavior and external connections.

## Configuration Sources

| Source | Type | Path/Location | Notes |
|---|---|---|---|
| fileingestion.properties | Properties | src/main/resources/fileingestion.properties | Primary runtime config |
| Environment Variables | OS env vars | runtime environment | Overrides RabbitMQ, SQL Server, and ingestion paths |
| build.gradle | Gradle build config | build.gradle | Declares dependencies and Java version |

## Build Profiles

| Profile | Activation | Purpose | Key Dependencies/Plugins |
|---|---|---|---|
| default | Always | Build runnable shaded JAR | java/application/shadow plugins |

## Runtime Profiles

| Profile | Activation Method | Config Files | Key Overrides |
|---|---|---|---|
| default | App startup | fileingestion.properties | db.*, rabbitmq.*, ingestion.* |

## Properties Inventory

| Property Key | Default | Profiles | Source |
|---|---|---|---|
| rabbitmq.host | localhost | default | fileingestion.properties / RABBITMQ_HOST |
| rabbitmq.port | 5672 | default | fileingestion.properties / RABBITMQ_PORT |
| rabbitmq.username | guest | default | fileingestion.properties / RABBITMQ_USERNAME |
| rabbitmq.password | [MASKED] | default | fileingestion.properties / RABBITMQ_PASSWORD |
| db.url | jdbc:sqlserver://sqlserver:1433... | default | fileingestion.properties / SQLSERVER_URL |
| db.username | sa | default | fileingestion.properties / SQLSERVER_USERNAME |
| db.password | [MASKED] | default | fileingestion.properties / SQLSERVER_PASSWORD |
| ingestion.watch.path | /shared/file-ingestion | default | fileingestion.properties / FILE_INGESTION_PATH |
| ingestion.processed.path | /shared/file-ingestion/processed | default | fileingestion.properties / FILE_INGESTION_PROCESSED_PATH |
| ingestion.error.path | /shared/file-ingestion/error | default | fileingestion.properties / FILE_INGESTION_ERROR_PATH |
| ingestion.poll.interval.ms | 5000 | default | fileingestion.properties / FILE_INGESTION_POLL_MS |

## Startup Parameters & Resource Requirements

| Service | JVM/Runtime Options | Memory | Instance Count |
|---|---|---|---|
| mnm-ZavaFileIngestion | None specified in repo | Not specified | Not specified |

## Startup Dependency Chain

- mnm-ZavaFileIngestion → waits for filesystem path availability and SQL Server/RabbitMQ reachability (implicit at runtime).

## Secrets & Sensitive Configuration

| Secret Reference | Type | Storage (masked) |
|---|---|---|
| rabbitmq.password | Credential | Properties/env var ([MASKED]) |
| db.password | Credential | Properties/env var ([MASKED]) |

### Secrets Provisioning Workflow

Secrets are provided via environment variables or the default properties file. The service reads these values at startup and applies env var overrides before opening SQL Server and RabbitMQ connections.

## Feature Flags

| Flag Name | Default | Controlled By |
|---|---|---|
| None detected | N/A | N/A |

## Framework & Runtime Versions

| Component | Version | Source |
|---|---|---|
| Java | 8 | build.gradle |
| Gradle Shadow Plugin | 7.1.2 | build.gradle |
| RabbitMQ Java Client | 5.21.0 | build.gradle |
| SQL Server JDBC Driver | 12.6.1.jre8 | build.gradle |
