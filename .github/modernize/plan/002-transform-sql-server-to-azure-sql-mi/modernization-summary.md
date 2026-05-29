# Modernization Summary

- Replaced password-based SQL Server connections in `Main.executeInsert` with `SQLServerDataSource` using Azure Active Directory Managed Identity.
- Added managed-identity JDBC URL normalization that removes embedded credential segments, ensures `authentication=ActiveDirectoryMSI`, and appends `msiClientId` from `azure.client.id` when provided.
- Removed `db.username` and `db.password` from the tracked SQL configuration files and added `azure.client.id` with `AZURE_CLIENT_ID` environment-variable support.
- Updated the Microsoft SQL Server JDBC driver from `12.6.1.jre8` to `12.8.1.jre8`.
- Kept the three staging-table INSERT statements functionally equivalent.
- Validation: `gradle --no-daemon clean test` passed; consistency check reported zero Critical and zero Major issues.
