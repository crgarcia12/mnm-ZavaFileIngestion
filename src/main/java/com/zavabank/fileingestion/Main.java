package com.zavabank.fileingestion;

import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.PreparedStatement;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Main {
    private static volatile boolean running = true;
    private static final Map<String, String> FILE_TYPE_HINTS = new HashMap<String, String>();

    static {
        FILE_TYPE_HINTS.put("check", "check_images");
        FILE_TYPE_HINTS.put("wire", "wire_confirmations");
        FILE_TYPE_HINTS.put("reg", "regulatory_feeds");
    }

    public static void main(String[] args) {
        final Properties config = loadConfig("fileingestion.properties");
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                running = false;
                log("Shutdown requested.");
            }
        }));

        String watchPath = config.getProperty("ingestion.watch.path", "/shared/file-ingestion");
        String processedPath = config.getProperty("ingestion.processed.path", "/shared/file-ingestion/processed");
        String errorPath = config.getProperty("ingestion.error.path", "/shared/file-ingestion/error");
        long intervalMs = parseLong(config.getProperty("ingestion.poll.interval.ms", "5000"), 5000L);

        ensureDirectory(watchPath);
        ensureDirectory(processedPath);
        ensureDirectory(errorPath);

        log("ZavaFileIngestion started. Monitoring " + watchPath + " every " + intervalMs + " ms.");
        while (running) {
            ingestPendingFiles(config, watchPath, processedPath, errorPath);
            sleepQuietly(intervalMs);
        }
        log("ZavaFileIngestion stopped.");
    }

    private static void ingestPendingFiles(Properties config, String watchPath, String processedPath, String errorPath) {
        File watchDir = new File(watchPath);
        File[] files = watchDir.listFiles();
        if (files == null || files.length == 0) {
            return;
        }

        int i;
        for (i = 0; i < files.length; i++) {
            File file = files[i];
            if (!file.isFile()) {
                continue;
            }
            String fileType = identifyFileType(file.getName());
            if (fileType == null) {
                continue;
            }
            processFile(config, file, fileType, processedPath, errorPath);
        }
    }

    private static void processFile(Properties config, File file, String fileType, String processedPath, String errorPath) {
        boolean ok;
        if ("check_images".equals(fileType)) {
            ok = processCheckImageMetadata(config, file);
        } else if ("wire_confirmations".equals(fileType)) {
            ok = processWireConfirmations(config, file);
        } else {
            ok = processRegulatoryFeed(config, file);
        }

        if (ok) {
            publishIngestionEvent(config, file.getName(), fileType, "processed");
            moveFile(file.toPath(), Paths.get(processedPath, file.getName()));
        } else {
            publishIngestionEvent(config, file.getName(), fileType, "failed");
            moveFile(file.toPath(), Paths.get(errorPath, file.getName()));
        }
    }

    private static boolean processCheckImageMetadata(Properties config, File file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            String line = reader.readLine();
            if (line == null) {
                return false;
            }
            String[] fields = splitCsv(line);
            String checkNumber = fields.length > 0 ? fields[0] : "";
            String accountNumber = fields.length > 1 ? fields[1] : "";
            String imagePath = fields.length > 2 ? fields[2] : "";
            return insertCheckImageMetadata(config, file.getName(), checkNumber, accountNumber, imagePath);
        } catch (Exception ex) {
            log("Check image parse failed for " + file.getName() + ": " + ex.getMessage());
            return false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static boolean processWireConfirmations(Properties config, File file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            String line;
            boolean insertedAny = false;
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                String reference = safeSlice(line, 0, 16).trim();
                String amount = safeSlice(line, 16, 28).trim();
                String beneficiary = safeSlice(line, 28, 68).trim();
                insertedAny = insertWireConfirmation(config, file.getName(), reference, amount, beneficiary) || insertedAny;
            }
            return insertedAny;
        } catch (Exception ex) {
            log("Wire confirmation parse failed for " + file.getName() + ": " + ex.getMessage());
            return false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static boolean processRegulatoryFeed(Properties config, File file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            String line;
            boolean insertedAny = false;
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                String[] fields = splitCsv(line);
                String feedCode = fields.length > 0 ? fields[0] : "";
                String recordDate = fields.length > 1 ? fields[1] : "";
                String payload = fields.length > 2 ? fields[2] : "";
                insertedAny = insertRegulatoryFeed(config, file.getName(), feedCode, recordDate, payload) || insertedAny;
            }
            return insertedAny;
        } catch (Exception ex) {
            log("Regulatory feed parse failed for " + file.getName() + ": " + ex.getMessage());
            return false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static boolean insertCheckImageMetadata(Properties config, String fileName, String checkNumber, String accountNumber, String imagePath) {
        return executeInsert(
            config,
            "INSERT INTO StagingCheckImage (FileName, CheckNumber, AccountNumber, ImagePath, IngestedAt) VALUES (?, ?, ?, ?, GETDATE())",
            new String[]{fileName, checkNumber, accountNumber, imagePath}
        );
    }

    private static boolean insertWireConfirmation(Properties config, String fileName, String wireReference, String amount, String beneficiary) {
        return executeInsert(
            config,
            "INSERT INTO StagingWireConfirmation (FileName, WireReference, Amount, Beneficiary, IngestedAt) VALUES (?, ?, ?, ?, GETDATE())",
            new String[]{fileName, wireReference, amount, beneficiary}
        );
    }

    private static boolean insertRegulatoryFeed(Properties config, String fileName, String feedCode, String recordDate, String payload) {
        return executeInsert(
            config,
            "INSERT INTO StagingRegulatoryFeed (FileName, FeedCode, RecordDate, Payload, IngestedAt) VALUES (?, ?, ?, ?, GETDATE())",
            new String[]{fileName, feedCode, recordDate, payload}
        );
    }

    private static boolean executeInsert(Properties config, String sql, String[] values) {
        java.sql.Connection conn = null;
        PreparedStatement ps = null;
        try {
            SQLServerDataSource dataSource = new SQLServerDataSource();
            dataSource.setURL(buildAzureSqlManagedIdentityUrl(config));
            conn = dataSource.getConnection();
            ps = conn.prepareStatement(sql);
            int i;
            for (i = 0; i < values.length; i++) {
                ps.setString(i + 1, values[i]);
            }
            ps.executeUpdate();
            return true;
        } catch (Exception ex) {
            log("Insert failed: " + ex.getMessage());
            return false;
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception ignored) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static String buildAzureSqlManagedIdentityUrl(Properties config) {
        String url = stripJdbcCredentialSegments(trimToEmpty(config.getProperty("db.url", "")));
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.indexOf("authentication=activedirectorymsi") < 0) {
            url = appendJdbcProperty(url, "authentication=ActiveDirectoryMSI");
            lowerUrl = url.toLowerCase();
        }
        String managedIdentityClientId = trimToEmpty(config.getProperty("azure.client.id", ""));
        if (managedIdentityClientId.length() > 0 && lowerUrl.indexOf("msiclientid=") < 0) {
            url = appendJdbcProperty(url, "msiClientId=" + managedIdentityClientId);
        }
        return url;
    }

    private static String stripJdbcCredentialSegments(String url) {
        String[] segments = url.split(";");
        StringBuilder sanitized = new StringBuilder();
        int i;
        for (i = 0; i < segments.length; i++) {
            String segment = trimToEmpty(segments[i]);
            String lowerSegment = segment.toLowerCase();
            if (segment.length() == 0
                || lowerSegment.startsWith("user=")
                || lowerSegment.startsWith("username=")
                || lowerSegment.startsWith("uid=")
                || lowerSegment.startsWith("password=")
                || lowerSegment.startsWith("pwd=")) {
                continue;
            }
            if (sanitized.length() > 0) {
                sanitized.append(";");
            }
            sanitized.append(segment);
        }
        return sanitized.toString();
    }

    private static String appendJdbcProperty(String url, String property) {
        if (url.length() == 0) {
            return property;
        }
        if (url.endsWith(";")) {
            return url + property;
        }
        return url + ";" + property;
    }

    private static void publishIngestionEvent(Properties config, String fileName, String fileType, String status) {

        ServiceBusSenderClient senderClient = null;
        try {
            String namespace = normalizeServiceBusNamespace(config.getProperty("servicebus.namespace", ""));
            if (namespace.isEmpty()) {
                log("Azure Service Bus publish skipped: servicebus.namespace is not configured.");
                return;
            }

            String topicName = config.getProperty("servicebus.topic.name", "zava.events");
            String subject = config.getProperty("servicebus.ingestion.subject", "file.ingested").trim();
            String managedIdentityClientId = trimToEmpty(config.getProperty("servicebus.managedIdentityClientId", ""));
            DefaultAzureCredentialBuilder credentialBuilder = new DefaultAzureCredentialBuilder();
            if (managedIdentityClientId.length() > 0) {
                credentialBuilder.managedIdentityClientId(managedIdentityClientId);
            }

            senderClient = new ServiceBusClientBuilder()
                .credential(namespace, credentialBuilder.build())
                .sender()
                .topicName(topicName)
                .buildClient();

            String payload = "{"
                + "\"eventType\":\"file.ingestion\","
                + "\"fileName\":\"" + escape(fileName) + "\","
                + "\"fileType\":\"" + escape(fileType) + "\","
                + "\"status\":\"" + escape(status) + "\","
                + "\"ingestedAt\":\"" + new Date().getTime() + "\""
                + "}";
            ServiceBusMessage message = new ServiceBusMessage(BinaryData.fromString(payload));
            if (subject.length() > 0) {
                message.setSubject(subject);
            }
            senderClient.sendMessage(message);
        } catch (Exception ex) {
            log("Azure Service Bus publish failed: " + ex.getMessage());
        } finally {
            if (senderClient != null) {
                try {
                    senderClient.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static String identifyFileType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.contains("processed") || lower.contains("error")) {
            return null;
        }
        if (lower.endsWith(".csv") && lower.contains("check")) {
            return "check_images";
        }
        if (lower.endsWith(".txt") && lower.contains("wire")) {
            return "wire_confirmations";
        }
        if ((lower.endsWith(".csv") || lower.endsWith(".txt")) && (lower.contains("reg") || lower.contains("feed"))) {
            return "regulatory_feeds";
        }

        int i = 0;
        String[] keys = new String[]{"check", "wire", "reg"};
        while (i < keys.length) {
            if (lower.contains(keys[i])) {
                return FILE_TYPE_HINTS.get(keys[i]);
            }
            i++;
        }
        return null;
    }

    private static String[] splitCsv(String line) {
        return line.split(",", -1);
    }

    private static String safeSlice(String value, int start, int end) {
        if (value == null || value.length() <= start) {
            return "";
        }
        int safeEnd = end;
        if (safeEnd > value.length()) {
            safeEnd = value.length();
        }
        return value.substring(start, safeEnd);
    }

    private static Properties loadConfig(String classpathFile) {
        Properties props = new Properties();
        InputStream stream = null;
        try {
            stream = Main.class.getClassLoader().getResourceAsStream(classpathFile);
            if (stream != null) {
                props.load(stream);
            }
        } catch (Exception ex) {
            log("Could not load config file " + classpathFile + ": " + ex.getMessage());
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                }
            }
        }
        applyEnvOverride(props, "SERVICEBUS_NAMESPACE", "servicebus.namespace");
        applyEnvOverride(props, "SERVICEBUS_TOPIC_NAME", "servicebus.topic.name");
        applyEnvOverride(props, "SERVICEBUS_INGESTION_SUBJECT", "servicebus.ingestion.subject");
        applyEnvOverride(props, "AZURE_CLIENT_ID", "servicebus.managedIdentityClientId");
        applyEnvOverride(props, "AZURE_CLIENT_ID", "azure.client.id");
        applyEnvOverride(props, "SQLSERVER_URL", "db.url");
        applyEnvOverride(props, "FILE_INGESTION_PATH", "ingestion.watch.path");
        applyEnvOverride(props, "FILE_INGESTION_PROCESSED_PATH", "ingestion.processed.path");
        applyEnvOverride(props, "FILE_INGESTION_ERROR_PATH", "ingestion.error.path");
        applyEnvOverride(props, "FILE_INGESTION_POLL_MS", "ingestion.poll.interval.ms");
        return props;
    }

    private static void applyEnvOverride(Properties props, String envName, String key) {
        String value = System.getenv(envName);
        if (value != null && value.trim().length() > 0) {
            props.setProperty(key, value);
        }
    }

    private static void ensureDirectory(String path) {
        try {
            Files.createDirectories(Paths.get(path));
        } catch (Exception ex) {
            log("Could not create directory " + path + ": " + ex.getMessage());
        }
    }

    private static String normalizeServiceBusNamespace(String namespace) {
        String normalized = trimToEmpty(namespace);
        if (normalized.length() == 0) {
            return "";
        }
        if (normalized.indexOf('.') < 0) {
            return normalized + ".servicebus.windows.net";
        }
        return normalized;
    }

    private static String trimToEmpty(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private static void moveFile(Path source, Path target) {
        try {
            if (target.getParent() != null) {
                Files.createDirectories(target.getParent());
            }
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ex) {
            log("File move failed: " + ex.getMessage());
        }
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static long parseLong(String value, long fallback) {
        try {
            return Long.parseLong(value);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static void sleepQuietly(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void log(String message) {
        System.out.println("[ZavaFileIngestion] " + message);
    }
}
