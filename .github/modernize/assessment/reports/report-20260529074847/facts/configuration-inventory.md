# Configuration & Externalized Settings Inventory

This project uses a compact configuration model centered on a single properties file plus environment variable overrides for runtime-sensitive values.

## Configuration Sources

| Source | Type | Path/Location | Notes |
|---|---|---|---|
| fileingestion.properties | Properties file | src/main/resources/fileingestion.properties | Primary defaults for RabbitMQ, DB, and file paths |
| Environment variables | Runtime overrides | Process environment | Overrides selected keys via `applyEnvOverride` |
| Gradle build file | Build configuration | build.gradle | Declares runtime dependencies and Java target |
| Dockerfile | Container build/runtime config | Dockerfile | Builds fat JAR and runs Java process |

## Build Profiles

| Profile | Activation | Purpose | Key Dependencies/Plugins |
|---|---|---|---|
| default | Automatic | Build executable ingestion app | Java plugin, Application plugin, Shadow plugin |

## Runtime Profiles

| Profile | Activation Method | Config Files | Key Overrides |
|---|---|---|---|
| default | Application startup | fileingestion.properties | RabbitMQ, DB, and ingestion path defaults |
| env override | Environment variables | N/A | SQLSERVER_URL/USERNAME/PASSWORD and ingestion path overrides |

## Properties Inventory

| Property Key | Default | Profiles | Source |
|---|---|---|---|
| rabbitmq.host | localhost | default/env | fileingestion.properties |
| rabbitmq.port | 5672 | default/env | fileingestion.properties |
| rabbitmq.username | guest | default/env | fileingestion.properties |
| rabbitmq.password | [MASKED] | default/env | fileingestion.properties |
| rabbitmq.vhost | /zavabank | default/env | fileingestion.properties |
| rabbitmq.events.exchange | zava.events | default/env | fileingestion.properties |
| rabbitmq.ingestion.routingKey | file.ingested | default/env | fileingestion.properties |
| db.url | jdbc:sqlserver://sqlserver:1433;databaseName=ZavaBankCore;encrypt=false;trustServerCertificate=true | default/env | fileingestion.properties |
| db.username | sa | default/env | fileingestion.properties |
| db.password | [MASKED] | default/env | fileingestion.properties |
| ingestion.watch.path | /shared/file-ingestion | default/env | fileingestion.properties |
| ingestion.processed.path | /shared/file-ingestion/processed | default/env | fileingestion.properties |
| ingestion.error.path | /shared/file-ingestion/error | default/env | fileingestion.properties |
| ingestion.poll.interval.ms | 5000 | default/env | fileingestion.properties |

## Startup Parameters & Resource Requirements

| Service | JVM/Runtime Options | Memory | Instance Count |
|---|---|---|---|
| ZavaFileIngestion | No explicit JVM flags detected | Not specified | 1 (single process expected) |

## Startup Dependency Chain

1. ZavaFileIngestion starts and loads local properties.
2. It requires filesystem paths to exist (creates directories if missing).
3. It requires SQL Server and RabbitMQ connectivity during processing and event publication.

## Secrets & Sensitive Configuration

| Secret Reference | Type | Storage (masked) |
|---|---|---|
| rabbitmq.password | Message broker credential | file property / env override ([MASKED]) |
| db.password | Database credential | file property / env override ([MASKED]) |

### Secrets Provisioning Workflow

Secrets are provided either as defaults in `fileingestion.properties` or overridden through environment variables at runtime. The process reads environment variables at startup and replaces sensitive defaults before connecting to external systems.

## Feature Flags

| Flag Name | Default | Controlled By |
|---|---|---|
| None detected | N/A | N/A |

## Framework & Runtime Versions

| Component | Version | Source |
|---|---|---|
| Java target | 1.8 | build.gradle |
| Gradle Shadow plugin | 7.1.2 | build.gradle |
| RabbitMQ Java client | 5.21.0 | build.gradle |
| Microsoft SQL JDBC | 12.6.1.jre8 | build.gradle |
| Docker build image | gradle:7.6-jdk8 | Dockerfile |
| Docker runtime image | eclipse-temurin:8-jre | Dockerfile |
