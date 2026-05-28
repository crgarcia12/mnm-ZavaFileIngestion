# Security Assessment Report

**Generated:** 2026-05-28T23:00:00Z

## Summary

| Metric | Count |
|--------|-------|
| Total Findings | 4 |
| CVE Vulnerabilities | 1 |
| CWE Vulnerabilities | 3 |
| Total Rules Assessed | 59 |
| Rules Passed | 56 |

### By Severity

| Severity | Count |
|----------|-------|
| mandatory | 1 |
| optional | 2 |
| potential | 1 |

## CVE Findings (Dependency Vulnerabilities)

### CVE-2025-59250: JDBC Driver for SQL Server has improper input validation issue
- **Severity:** mandatory
- **Story Points:** 1
- **Files:** build.gradle:13

[CVE-2025-59250](https://github.com/advisories/GHSA-m494-w24q-6f7w): JDBC Driver for SQL Server has improper input validation issue

Severity: HIGH (CVSS 8.1)

Improper input validation in JDBC Driver for SQL Server allows an unauthorized attacker to perform spoofing over a network.

Affected dependencies:
  - com.microsoft.sqlserver:mssql-jdbc:12.6.1.jre8 (declared at build.gradle:13)

Recommended fix:
  - Upgrade com.microsoft.sqlserver:mssql-jdbc to 12.6.5.jre8 or later

## CWE Findings (Code-Level Vulnerabilities)

### CWE-259: Use of Hard-coded Password
- **Category:** Credentials & Secrets
- **Severity:** optional
- **Story Points:** 5
- **Files:** src/main/resources/fileingestion.properties

Hard-coded passwords are present in the bundled properties file. The SQL Server SA password is set at `db.password`, and the RabbitMQ password `guest` is set at `rabbitmq.password`. These credentials are committed into the repository and bundled into the application artifact, making them visible to anyone with access to the source code or the built JAR.

### CWE-778: Insufficient Logging
- **Category:** Credentials & Secrets
- **Severity:** potential
- **Story Points:** 3
- **Files:** src/main/java/com/zavabank/fileingestion/Main.java

In the Main class, security-critical failures are logged with minimal context. The `executeInsert()` method (line 225) logs only `'Insert failed: '` + exception message without identifying which file or operation failed. The `publishIngestionEvent()` method (line 268) logs only `'RabbitMQ publish failed: '` + message without including the file name or file type. For a financial application processing wire confirmations, check images, and regulatory feeds, there is no structured security audit logging, no recording of authentication failures, and no traceability between failed database events and the source files being processed.

### CWE-798: Use of Hard-coded Credentials
- **Category:** Credentials & Secrets
- **Severity:** optional
- **Story Points:** 5
- **Files:** src/main/resources/fileingestion.properties

Hard-coded credentials are present in the bundled properties file committed to source control. The SQL Server SA account credentials are hard-coded (`db.username=sa`, `db.****** and RabbitMQ credentials are hard-coded (`rabbitmq.username=guest`, `rabbitmq.****** These values are loaded at runtime via `Main.loadConfig()` and applied as defaults even when environment variable overrides are not provided, meaning the application may silently connect with these insecure hard-coded credentials in environments where environment variables are not set.
