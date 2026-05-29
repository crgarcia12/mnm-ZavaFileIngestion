# Modernization Summary

- Replaced the RabbitMQ Java client dependency with Azure SDK dependencies for Azure Service Bus and Managed Identity authentication.
- Updated `Main.java` to publish ingestion events with `ServiceBusClientBuilder`, `DefaultAzureCredentialBuilder`, and `ServiceBusSenderClient` against a Service Bus topic.
- Replaced RabbitMQ configuration properties and environment overrides with Azure Service Bus namespace, topic, subject, and managed identity client ID settings.
- Removed remaining RabbitMQ references from the application source and configuration files.
- Verified the migration with the consistency check plus `gradle --no-daemon clean test shadowJar`.
