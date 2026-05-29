# Configuration & Externalized Settings Inventory

This inventory captures externalized configuration across application properties, environment-variable overrides, and container build/runtime settings. Configuration is centralized in a single properties file with env override support.

## Configuration Sources

| Source | Type | Path/Location | Notes |
|---|---|---|---|
| fileingestion.properties | Application properties | src/main/resources/fileingestion.properties | Default runtime configuration |
| Environment variables | Runtime overrides | Process environment | Applied via `applyEnvOverride` |
| Dockerfile | Container build/runtime | Dockerfile | Defines build image and Java runtime image |

## Build Profiles

| Profile | Activation | Purpose | Key Dependencies/Plugins |
|---|---|---|---|
| default Gradle build | Automatic | Compile and package service | `java`, `application`, `shadow` plugins |

## Runtime Profiles

| Profile | Activation Method | Config Files | Key Overrides |
|---|---|---|---|
| default | Implicit | fileingestion.properties | RabbitMQ, SQL Server, ingestion paths |
| environment override | Environment variables | N/A | Overrides host, credentials, file paths, poll interval |

## Properties Inventory

| Property Key | Default | Profiles | Source |
|---|---|---|---|
| rabbitmq.host | localhost | default/env | properties + env |
| rabbitmq.port | 5672 | default/env | properties + env |
| rabbitmq.username | guest | default/env | properties + env |
| rabbitmq.password | [MASKED] | default/env | properties + env |
| rabbitmq.vhost | /zavabank | default/env | properties + env |
| rabbitmq.events.exchange | zava.events | default | properties |
| rabbitmq.ingestion.routingKey | file.ingested | default | properties |
| db.url | jdbc:sqlserver://sqlserver:1433;databaseName=ZavaBankCore;encrypt=false;trustServerCertificate=true | default/env | properties + env |
| db.username | sa | default/env | properties + env |
| db.password | [MASKED] | default/env | properties + env |
| ingestion.watch.path | /shared/file-ingestion | default/env | properties + env |
| ingestion.processed.path | /shared/file-ingestion/processed | default/env | properties + env |
| ingestion.error.path | /shared/file-ingestion/error | default/env | properties + env |
| ingestion.poll.interval.ms | 5000 | default/env | properties + env |

## Startup Parameters & Resource Requirements

| Service | JVM/Runtime Options | Memory | Instance Count |
|---|---|---|---|
| zava-file-ingestion | `java -jar app.jar` | Not explicitly configured | Not explicitly configured |

## Startup Dependency Chain

1. zava-file-ingestion starts and loads properties.
2. Service requires SQL Server and RabbitMQ to be reachable for full processing success.
3. Service polls watch directory continuously; no explicit readiness/wait mechanism is configured.

## Secrets & Sensitive Configuration

| Secret Reference | Type | Storage (masked) |
|---|---|---|
| rabbitmq.password | Credential | Properties/env override [MASKED] |
| db.username | Credential | Properties/env override [MASKED] |
| db.password | Credential | Properties/env override [MASKED] |

### Secrets Provisioning Workflow

Secrets are sourced either from the packaged properties file or process-level environment variables. At startup, values are loaded and environment variables override defaults when present. SQL and RabbitMQ clients consume these values directly during connection initialization.

## Feature Flags

| Flag Name | Default | Controlled By |
|---|---|---|
| None detected | N/A | N/A |

## Framework & Runtime Versions

| Component | Version | Source |
|---|---|---|
| Java | 1.8 target/source | build.gradle |
| Gradle Shadow plugin | 7.1.2 | build.gradle |
| RabbitMQ Java client | 5.21.0 | build.gradle |
| MSSQL JDBC driver | 12.6.1.jre8 | build.gradle |
| Build image | gradle:7.6-jdk8 | Dockerfile |
| Runtime image | eclipse-temurin:8-jre | Dockerfile |
