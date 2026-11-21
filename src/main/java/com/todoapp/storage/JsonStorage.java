package com.todoapp.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * JSON-backed persistent storage implementation adhering to strict crash-safety rules.
 * Uses atomic write-and-replace to prevent corruption during unexpected shutdowns
 * and quarantines corrupt data files on startup rather than crashing.
 */
public class JsonStorage implements Storage {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final Path filePath;
    private String lastLoadWarning;

    /**
     * Constructs a JsonStorage pointing to the specified target file path.
     *
     * @param filePath non-null target data file path
     */
    public JsonStorage(Path filePath) {
        this.filePath = Objects.requireNonNull(filePath, "Storage file path must not be null");
    }

    /**
     * Returns the target persistent storage file path.
     *
     * @return file path
     */
    public Path getFilePath() {
        return filePath;
    }

    /**
     * Returns the warning message generated during the most recent load operation, if any.
     *
     * @return Optional containing warning message, or empty if load was completely clean
     */
    public Optional<String> getLastLoadWarning() {
        return Optional.ofNullable(lastLoadWarning);
    }

    @Override
    public AppData load() throws StorageException {
        this.lastLoadWarning = null;

        Path parent = filePath.getParent();
        if (parent != null && !Files.exists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new StorageException("Failed to create storage directory: " + parent, e);
            }
        }

        if (!Files.exists(filePath)) {
            return AppData.empty();
        }

        String content;
        try {
            content = Files.readString(filePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new StorageException("Failed to read storage file: " + filePath, e);
        }

        if (content.trim().isEmpty()) {
            return AppData.empty();
        }

        List<String> warnings = new ArrayList<>();
        try {
            AppData data = JsonMapper.fromRawJson(content, warnings);
            if (!warnings.isEmpty()) {
                this.lastLoadWarning = String.join("; ", warnings);
            }
            return data;
        } catch (Exception e) {
            // Corrupt syntax or unparseable structure -> quarantine the corrupted file
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
            String baseCorruptName = filePath.getFileName().toString() + ".corrupt-" + timestamp;
            Path corruptPath = parent != null ? parent.resolve(baseCorruptName) : Path.of(baseCorruptName);
            int counter = 1;
            while (Files.exists(corruptPath)) {
                String candidate = baseCorruptName + "_" + counter++;
                corruptPath = parent != null ? parent.resolve(candidate) : Path.of(candidate);
            }

            try {
                Files.move(filePath, corruptPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException moveEx) {
                throw new StorageException("Data file is corrupt and quarantine failed: " + e.getMessage(), moveEx);
            }

            this.lastLoadWarning = "Data file was corrupted and quarantined to "
                    + corruptPath.getFileName() + ". Loaded default data.";
            return AppData.empty();
        }
    }

    @Override
    public void save(AppData data) throws StorageException {
        Objects.requireNonNull(data, "AppData to save must not be null");

        Path parent = filePath.getParent();
        if (parent != null && !Files.exists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new StorageException("Failed to create parent directory: " + parent, e);
            }
        }

        Path tempFile = parent != null
                ? parent.resolve(filePath.getFileName().toString() + ".tmp")
                : Path.of(filePath.getFileName().toString() + ".tmp");

        String jsonString = JsonMapper.toPrettyJson(data);

        try {
            Files.writeString(tempFile, jsonString, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            try {
                Files.move(tempFile, filePath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException atomicEx) {
                Files.move(tempFile, filePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException ignored) {
                // suppress secondary exception during cleanup
            }
            throw new StorageException("Failed to save data atomically to " + filePath, e);
        }
    }
}
