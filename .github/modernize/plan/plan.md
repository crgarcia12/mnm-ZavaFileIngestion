# Modernization Plan: Zava Application Azure Modernization

**Project**: ZavaFileIngestion

---

## Technical Framework

- **Language**: Java 8 (target modernization to latest supported Java)
- **Framework**: Plain Java application (no web framework detected)
- **Build Tool**: Gradle
- **Database**: Microsoft SQL Server (JDBC driver)
- **Key Dependencies**: RabbitMQ AMQP Client, Microsoft SQL Server JDBC

---

## Overview

> This migration modernizes the Zava application and prepares it for Azure deployment.
> The application currently runs on an older Java baseline with non-cloud-native
> messaging and database access patterns. The modernized architecture will:
>
> - Upgrade runtime and project baseline to the latest supported Java/framework
>   level to improve supportability and long-term maintainability.
> - Replace existing integration patterns with Azure-native managed services for
>   messaging and data connectivity to improve operational reliability.
> - Deploy to Azure using a cloud-native target that supports scalable,
>   container-based operations.
>
> The migration follows a phased approach: baseline upgrade, Azure service
> transformations, security/CVE remediation, and deployment.

---

## Migration Impact Summary

| Application | Original Service | New Azure Service | Authentication | Comments |
|-------------|------------------|-------------------|----------------|----------|
| ZavaFileIngestion | RabbitMQ | Azure Service Bus | Managed Identity | Move messaging to Azure-native broker |
| ZavaFileIngestion | SQL Server access | Azure SQL Database | Managed Identity | Modernize DB auth for cloud-native ops |
| ZavaFileIngestion | Current runtime | Latest Java runtime | N/A | Upgrade to current supported baseline |
| ZavaFileIngestion | Local/legacy deploy | Azure Container Apps | Managed Identity | Deploy as cloud-native container workload |

---

## Security Compliance

Dependency vulnerability scanning and CVE remediation are included and run after
upgrade/transform tasks and before deployment.

---

## Open Questions & Questionnaire

- [x] Q: Should the plan include environment/infrastructure provisioning? → A: No — focus on application modernization and deployment using existing Azure resources.
- [x] Q: Should the plan include integration testing? → A: No explicit request; keep testing within each task's success criteria.
- [x] Q: Should the plan include security scan and CVE remediation? → A: Yes — include security/CVE remediation.
- [x] Q: Which Azure deployment target should the plan use? → A: Azure Container Apps (default).
- [x] Q: Should the plan include a separate containerization task? → A: No — deployment task covers containerization for Azure Container Apps.
- [ ] Q: Confirm preferred Azure region, subscription, and resource group for deployment.
