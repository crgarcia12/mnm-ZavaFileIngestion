# Configuration & Externalized Settings Inventory

The project uses a compact configuration model centered on a single properties file with environment variable overrides. Runtime behavior is primarily controlled by messaging, database, and file-ingestion settings.

## Configuration Sources

| Source | Type | Path/Location | Notes |
|---|---|---|---|
| fileingestion.properties | Properties file | src/main/resources/fileingestion.properties | Default runtime configuration |
| Environment variables | Externalized runtime | Process environment | Overrides key RabbitMQ, SQL Server, and file path settings |
| build.gradle | Build config | /build.gradle | Declares dependencies and Java compatibility |
| Dockerfile | Container config | /Dockerfile | Runtime containerization source |

## Build Profiles

| Profile | Activation | Purpose | Key Dependencies/Plugins |
|---|---|---|---|
| default Gradle build | Automatic | Compile/package app | java, application, shadow plugins |

## Runtime Profiles

| Profile | Activation Method | Config Files | Key Overrides |
|---|---|---|---|
| default | Automatic | fileingestion.properties | RabbitMQ, DB, file paths |
| env-override | Environment variables | fileingestion.properties + env | Env vars supersede defaults |

## Properties Inventory

| Property Key | Default | Profiles | Source |
|---|---|---|---|
| rabbitmq.host | localhost | default/env-override | properties/env |
| rabbitmq.port | 5672 | default/env-override | properties/env |
| rabbitmq.username | guest | default/env-override | properties/env |
| rabbitmq.password | [MASKED] | default/env-override | properties/env |
| rabbitmq.vhost | /zavabank | default/env-override | properties/env |
| rabbitmq.events.exchange | zava.events | default | properties |
| rabbitmq.ingestion.routingKey | file.ingested | default | properties |
| db.url | jdbc:sqlserver://sqlserver:1433;... | default/env-override | properties/env |
| db.username | sa | default/env-override | properties/env |
| db.password | [MASKED] | default/env-override | properties/env |
| ingestion.watch.path | /shared/file-ingestion | default/env-override | properties/env |
| ingestion.processed.path | /shared/file-ingestion/processed | default/env-override | properties/env |
| ingestion.error.path | /shared/file-ingestion/error | default/env-override | properties/env |
| ingestion.poll.interval.ms | 5000 | default/env-override | properties/env |

## Startup Parameters & Resource Requirements

| Service | JVM/Runtime Options | Memory | Instance Count |
|---|---|---|---|
| zava-file-ingestion | None explicitly configured | Not specified | Not specified |

## Startup Dependency Chain

1. zava-file-ingestion waits for file path availability and attempts SQL Server/RabbitMQ connections on use.
2. SQL Server should be reachable before inserts can succeed.
3. RabbitMQ should be reachable before event publication can succeed.

## Secrets & Sensitive Configuration

| Secret Reference | Type | Storage (masked) |
|---|---|---|
| db.password | Database credential | properties/env ([MASKED]) |
| rabbitmq.password | Messaging credential | properties/env ([MASKED]) |

### Secrets Provisioning Workflow

Secrets are supplied either from the default properties file or from environment variable overrides at startup. No external secret store integration (e.g., Vault/Key Vault) was detected.

## Feature Flags

| Flag Name | Default | Controlled By |
|---|---|---|
| None detected | N/A | N/A |

## Framework & Runtime Versions

| Component | Version | Source |
|---|---|---|
| Java target | 1.8 | build.gradle |
| Gradle Shadow Plugin | 7.1.2 | build.gradle |
| RabbitMQ Java Client | 5.21.0 | build.gradle |
| SQL Server JDBC | 12.6.1.jre8 | build.gradle |
