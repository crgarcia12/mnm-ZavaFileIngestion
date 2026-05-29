# Security Assessment Report

**Generated:** 2026-05-29T01:07:32.0000000Z

## Summary

| Metric | Count |
|--------|-------|
| Total Findings | 3 |
| CVE Vulnerabilities | 0 |
| CWE Vulnerabilities | 3 |
| Total Rules Assessed | 59 |
| Rules Passed | 56 |

### By Severity

| Severity | Count |
|----------|-------|
| mandatory | 0 |
| optional | 2 |
| potential | 1 |

## CVE Findings (Dependency Vulnerabilities)

No CVE findings above the configured severity threshold.

## CWE Findings (Code-Level Vulnerabilities)

### CWE-259: Use of Hard-coded Password
- **Category:** Credentials & Secrets
- **Severity:** optional
- **Story Points:** 5
- **Files:** src/main/resources/fileingestion.properties:4, src/main/resources/fileingestion.properties:11, src/main/java/com/zavabank/fileingestion/Main.java:252

Hard-coded messaging and database passwords are present in fileingestion.properties (rabbitmq.password and db.password), and Main.publishIngestionEvent uses a built-in default password value via config.getProperty(..., "guest").

### CWE-778: Insufficient Logging
- **Category:** Credentials & Secrets
- **Severity:** potential
- **Story Points:** 3
- **Files:** src/main/java/com/zavabank/fileingestion/Main.java:225, src/main/java/com/zavabank/fileingestion/Main.java:268

Security-relevant failures (database insert and RabbitMQ publish errors) are logged only as generic message text without structured security context or event metadata, limiting auditability of credential- or auth-related failures.

### CWE-798: Use of Hard-coded Credentials
- **Category:** Credentials & Secrets
- **Severity:** optional
- **Story Points:** 5
- **Files:** src/main/resources/fileingestion.properties:3, src/main/resources/fileingestion.properties:4, src/main/resources/fileingestion.properties:10, src/main/resources/fileingestion.properties:11, src/main/java/com/zavabank/fileingestion/Main.java:251, src/main/java/com/zavabank/fileingestion/Main.java:252

Hard-coded credential values are present for RabbitMQ and SQL Server in configuration defaults and in code-level fallback values (guest username/password).

