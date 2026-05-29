# Architecture Diagram

This application is a single Java worker service that ingests files, writes staging records to SQL Server, and emits ingestion events to RabbitMQ.

## Application Architecture

```mermaid
flowchart TD
    subgraph Ingest["Application Layer - Java 8 Worker"]
        Poller["File Poller"]
        Parser["Type-specific Parsers"]
        DbWriter["JDBC Insert Writer"]
        EventPub["RabbitMQ Event Publisher"]
    end
    subgraph Data["Data Layer"]
        SQL[("SQL Server")]
        FS[("Shared File System")]
    end
    subgraph External["External Services"]
        MQ["RabbitMQ"]
    end

    Poller -->|"read files"| FS
    Poller -->|"dispatch"| Parser
    Parser -->|"persist staged rows"| DbWriter
    DbWriter -->|"SQL inserts"| SQL
    Parser -->|"status event"| EventPub
    EventPub -->|"topic publish"| MQ
```

### Technology Stack Summary

| Layer | Technology | Version | Purpose |
|---|---|---|---|
| Application | Java service | 8 | Poll and process inbound files |
| Persistence | SQL Server JDBC | 12.6.1.jre8 | Insert staged ingestion records |
| Messaging | RabbitMQ Java Client | 5.21.0 | Publish processed/failed events |

### Data Storage & External Services

The service reads from filesystem directories and writes records to SQL Server staging tables. RabbitMQ is used as an external integration to publish ingestion outcome notifications.

### Key Architectural Decisions

- Uses a single-process polling loop with configurable interval.
- Uses direct JDBC prepared statements instead of ORM.
- Uses topic-based RabbitMQ publishing for downstream event consumers.

## Component Relationships

```mermaid
flowchart LR
    subgraph Presentation
        Main["Main Runtime Loop"]
    end
    subgraph Business["Business Logic"]
        Detect["identifyFileType"]
        Process["processFile"]
        Check["processCheckImageMetadata"]
        Wire["processWireConfirmations"]
        Reg["processRegulatoryFeed"]
    end
    subgraph DataAccess["Data Access"]
        Exec["executeInsert"]
    end
    subgraph Infra["Infrastructure"]
        Config["loadConfig"]
        MQ["publishIngestionEvent"]
        Move["moveFile"]
    end

    Main -->|"polls and routes"| Detect
    Detect -->|"delegates"| Process
    Process -->|"check flow"| Check
    Process -->|"wire flow"| Wire
    Process -->|"reg flow"| Reg
    Check -->|"insert"| Exec
    Wire -->|"insert"| Exec
    Reg -->|"insert"| Exec
    Process -->|"emit status"| MQ
    Process -->|"archive/error move"| Move
    Main -->|"startup"| Config
```

### Component Inventory

| Component | Layer | Type | Responsibility |
|---|---|---|---|
| Main | Presentation | Entry loop | Polls ingestion path and orchestrates processing |
| identifyFileType | Business Logic | Classifier | Determines file category by naming convention |
| process* methods | Business Logic | Processors | Parse file records and map to staging inserts |
| executeInsert | Data Access | JDBC helper | Executes prepared SQL inserts |
| publishIngestionEvent | Infrastructure | Messaging adapter | Publishes processing status to RabbitMQ |
| loadConfig | Infrastructure | Config loader | Loads properties and environment overrides |
