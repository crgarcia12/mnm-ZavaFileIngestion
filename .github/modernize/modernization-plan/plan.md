# Modernization Plan: ZavaFileIngestion Azure Migration

**Project**: ZavaFileIngestion

---

## Technical Framework

- **Language**: Java 8 (Java SE)
- **Framework**: Java SE (no application framework)
- **Build Tool**: Gradle 7.x (Shadow JAR plugin)
- **Database**: SQL Server (mssql-jdbc:12.6.1.jre8)
- **Key Dependencies**: RabbitMQ AMQP Client (amqp-client:5.21.0),
  Microsoft SQL Server JDBC Driver

---

## Overview

> This migration modernizes ZavaFileIngestion from an on-premises Java
> application to a cloud-native service on Azure. The application currently
> monitors a local file system directory, processes financial files
> (check images, wire confirmations, regulatory feeds), stores records in
> a SQL Server database using password-based authentication, and publishes
> ingestion events to RabbitMQ using hardcoded credentials. The new
> architecture will:
>
> - Replace RabbitMQ AMQP messaging with Azure Service Bus for managed,
>   scalable, and serverless event publishing
> - Enable Managed Identity for Azure SQL Database access, eliminating
>   password-based database authentication
> - Migrate plaintext credentials from the properties file to Azure Key
>   Vault for secure, centralized secrets management
> - Transition local file system paths to mounted Azure Blob Storage paths
>   for cloud-native file ingestion
> - Remediate known CVEs and security vulnerabilities identified in the
>   assessment report
>
> The migration follows a phased approach: service migrations first,
> followed by security hardening, and finally containerized deployment
> to Azure Container Apps.

---

## Migration Impact Summary

| Application       | Original Service      | New Azure Service        | Authentication    | Comments                         |
|-------------------|-----------------------|--------------------------|-------------------|----------------------------------|
| ZavaFileIngestion | RabbitMQ AMQP         | Azure Service Bus        | Managed Identity  | Event publishing migration       |
| ZavaFileIngestion | SQL Server (password) | Azure SQL Database       | Managed Identity  | Remove password-based auth       |
| ZavaFileIngestion | Plaintext credentials | Azure Key Vault          | Managed Identity  | Secure remaining secrets         |
| ZavaFileIngestion | Local file system     | Azure Blob Storage       | Managed Identity  | Mounted paths for file ingestion |

---

## Open Questions & Questionnaire

- [x] Q: Should the plan include infrastructure provisioning? → A: No —
  focus on code migration only; no existing IaC found in the repository.
- [x] Q: Should the plan include integration testing? → A: No — user did
  not explicitly request integration tests.
- [x] Q: Should the plan include security/CVE remediation? → A: Yes —
  assessment report identifies CVE-2025-59250 as a mandatory blocker.
- [x] Q: Which Azure deployment target should be used? → A: Azure Container
  Apps (default) — the project already has a Dockerfile.
