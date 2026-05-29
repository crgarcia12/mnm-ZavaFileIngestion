# Modernization Summary

- Updated `ingestion.watch.path`, `ingestion.processed.path`, and `ingestion.error.path` defaults to use `${AZURE_MOUNT_PATH:/mnt/azure}` in `src/main/resources/fileingestion.properties`.
- Added ingestion path normalization in `Main.java` so configured values, placeholder-based values, and relative env override values resolve correctly under the Azure mount path.
- Preserved existing file operations (`listFiles`, `createDirectories`, `move`) against the mounted Azure Storage filesystem.
- Verified the project builds successfully and unit tests pass.
