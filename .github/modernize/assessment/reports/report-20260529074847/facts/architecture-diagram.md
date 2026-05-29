# Architecture Diagram

This application is a single-process Java file-ingestion worker that polls a shared directory, writes parsed records to SQL Server, and publishes ingestion events to RabbitMQ.

## Application Architecture

```mermaid
flowchart TD
    subgraph Input["Input Layer"]
        Files["Shared File Drop Folder"]
    end
    subgraph App["Application Layer - Java 8"]
        Poller["File Polling Loop"]
        Parser["File Type Parsing"]
        Ingestor["Record Ingestion Service"]
        EventPublisher["RabbitMQ Event Publisher"]
    end
    subgraph Data["Data Layer"]
        SqlClient["JDBC SQL Server Client"]
        SqlDb[("SQL Server")]
    end
    subgraph External["External Services"]
        Rabbit[("RabbitMQ Exchange")]
    end

    Files -->|"poll files"| Poller
    Poller -->|"route by type"| Parser
    Parser -->|"parsed records"| Ingestor
    Ingestor -->|"parameterized inserts"| SqlClient
    SqlClient -->|"SQL write"| SqlDb
    Ingestor -->|"emit status event"| EventPublisher
    EventPublisher -->|"publish message"| Rabbit
```

### Technology Stack Summary

| Layer | Technology | Version | Purpose |
|---|---|---|---|
| Application | Java SE | 8 | Batch-style ingestion runtime |
| Build | Gradle + Shadow | 7.1.2 plugin | Build and fat-jar packaging |
| Data Access | JDBC (mssql-jdbc) | 12.6.1.jre8 | Inserts staging rows into SQL Server |
| Messaging | RabbitMQ Java Client | 5.21.0 | Publishes ingestion lifecycle events |

### Data Storage & External Services

The service writes ingestion rows into SQL Server staging tables and publishes processing status notifications to RabbitMQ. It relies on filesystem directories for inbound, processed, and error file movement.

### Key Architectural Decisions

- Uses a single executable worker loop instead of HTTP controllers.
- Uses prepared JDBC statements for database writes.
- Uses RabbitMQ topic-style routing for downstream event processing.

## Component Relationships

```mermaid
flowchart LR
    subgraph Presentation["Presentation"]
        Scheduler["Main Polling Loop"]
    end
    subgraph Business["Business Logic"]
        TypeDetector["identifyFileType"]
        CheckProcessor["processCheckImageMetadata"]
        WireProcessor["processWireConfirmations"]
        RegProcessor["processRegulatoryFeed"]
        EventLogic["publishIngestionEvent"]
    end
    subgraph DataAccess["Data Access"]
        InsertExec["executeInsert"]
        SqlServer["SQL Server"]
    end
    subgraph Infrastructure["Infrastructure"]
        ConfigLoader["loadConfig"]
        FileMover["moveFile"]
        RabbitConn["RabbitMQ ConnectionFactory"]
    end

    Scheduler -->|"loads settings"| ConfigLoader
    Scheduler -->|"processes file"| TypeDetector
    TypeDetector -->|"dispatch"| CheckProcessor
    TypeDetector -->|"dispatch"| WireProcessor
    TypeDetector -->|"dispatch"| RegProcessor
    CheckProcessor -->|"insert"| InsertExec
    WireProcessor -->|"insert"| InsertExec
    RegProcessor -->|"insert"| InsertExec
    InsertExec -->|"write rows"| SqlServer
    CheckProcessor -->|"publish status"| EventLogic
    WireProcessor -->|"publish status"| EventLogic
    RegProcessor -->|"publish status"| EventLogic
    EventLogic -->|"AMQP"| RabbitConn
    Scheduler -->|"archive/error move"| FileMover
```

### Component Inventory

| Component | Layer | Type | Responsibility |
|---|---|---|---|
| Main polling loop | Presentation | Entry-point loop | Polls watch directory and orchestrates ingestion |
| identifyFileType | Business Logic | Routing helper | Determines parser path by filename conventions |
| processCheckImageMetadata/processWireConfirmations/processRegulatoryFeed | Business Logic | Workflow handlers | Parse file lines and map to staging inserts |
| executeInsert | Data Access | JDBC helper | Executes parameterized SQL inserts |
| publishIngestionEvent | Business Logic | Integration method | Emits processed/failed notifications |
| loadConfig / moveFile | Infrastructure | Utility methods | Loads configuration and manages file movement |
