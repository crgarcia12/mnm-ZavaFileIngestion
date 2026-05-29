# Modernization Plan: Zava File Ingestion — Migrate to Azure

**Project**: ZavaFileIngestion

---

## Technical Framework

- **Language**: Java 8
- **Framework**: Plain Java (no application framework)
- **Build Tool**: Gradle 7.6 (Shadow JAR plugin)
- **Database**: Microsoft SQL Server (password-based JDBC authentication)
- **Messaging**: RabbitMQ (AMQP client `com.rabbitmq:amqp-client:5.21.0`)
- **File Storage**: Local filesystem (`/shared/file-ingestion`)
- **Key Dependencies**: `mssql-jdbc:12.6.1.jre8`, `amqp-client:5.21.0`

---

## Overview

This migration modernizes the ZavaFileIngestion service — a file polling daemon that ingests
banking files (check images, wire confirmations, regulatory feeds) — to run natively on Azure.

The application currently:
- Watches a local shared directory for incoming files
- Inserts parsed data into SQL Server using password-based JDBC authentication
- Publishes file ingestion events to RabbitMQ via AMQP
- Runs as a containerized JVM process

The new architecture will:

- **Replace RabbitMQ with Azure Service Bus** — cloud-native managed messaging removes the
  need to operate a broker and enables durable, scalable event delivery.
- **Secure SQL Server connections with Azure Managed Identity** — eliminates hardcoded
  database passwords, enforcing credential-free, auditable database access via Azure SQL.
- **Mount file ingestion paths via Azure Storage** — replaces the local shared directory
  with an Azure-mounted storage path, enabling scalable, durable file drop without
  managing persistent volumes manually.
- **Remediate known CVEs** — scans all Gradle dependencies and upgrades vulnerable
  libraries before deployment.
- **Deploy to Azure Container Apps** — packages and deploys the containerized service to
  Azure Container Apps, leveraging the existing Dockerfile.

---

## Migration Impact Summary

| Application         | Original Service      | New Azure Service              | Authentication     | Comments                              |
|---------------------|-----------------------|--------------------------------|--------------------|---------------------------------------|
| ZavaFileIngestion   | RabbitMQ (AMQP)       | Azure Service Bus              | Managed Identity   | Replace amqp-client with Service Bus  |
| ZavaFileIngestion   | SQL Server (password) | Azure SQL Database             | Managed Identity   | Remove hardcoded db credentials       |
| ZavaFileIngestion   | Local filesystem      | Azure Mounted Storage          | N/A                | Map ingestion path to Azure mount     |
| ZavaFileIngestion   | N/A                   | Azure Container Apps           | Managed Identity   | Deploy containerized service          |

---

## Open Questions & Questionnaire

- [x] Q: Should the plan include infrastructure provisioning? → A: No — focus on code migration only; no existing IaC found in the repository.
- [x] Q: Should the plan include integration testing? → A: No — not explicitly requested by the user.
- [x] Q: Should the plan include a security/CVE remediation task? → A: Yes — default security scan and CVE remediation included.
- [x] Q: Which Azure deployment target should the plan use? → A: Azure Container Apps (default); Dockerfile already present in the repository.
