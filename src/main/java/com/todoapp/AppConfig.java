package com.todoapp;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Immutable configuration parameters parsed from command-line arguments and system properties.
 * Free of JavaFX dependencies for isolated unit testability.
 *
 * @param dataPath destination file path for persistent JSON storage
 * @param seed     flag indicating whether development sample data should be seeded
 */
public record AppConfig(Path dataPath, boolean seed) {

    /**
     * System property key for overriding the storage data file path.
     */
    public static final String PROP_DATA = "todoapp.data";

    /**
     * System property key for enabling development sample data seeding.
     */
    public static final String PROP_SEED = "todoapp.seed";

    /**
     * Compact constructor guaranteeing non-null data path.
     */
    public AppConfig {
        Objects.requireNonNull(dataPath, "dataPath must not be null");
    }

    /**
     * Resolves the default storage data path located in user home directory: {@code ~/.todoapp/data.json}.
     *
     * @return default Path
     */
    public static Path defaultDataPath() {
        return Path.of(System.getProperty("user.home"), ".todoapp", "data.json");
    }

    /**
     * Parses application configuration from a list of raw command line arguments,
     * falling back to system properties and defaults.
     *
     * @param rawArgs raw CLI argument list (e.g. from {@code Application.getParameters().getRaw()})
     * @return resolved AppConfig instance
     */
    public static AppConfig parse(List<String> rawArgs) {
        Path resolvedPath = null;
        boolean resolvedSeed = false;

        if (rawArgs != null) {
            for (String arg : rawArgs) {
                if (arg == null) continue;
                if (arg.startsWith("--data=")) {
                    resolvedPath = Path.of(arg.substring("--data=".length()).trim());
                } else if ("--seed".equals(arg.trim())) {
                    resolvedSeed = true;
                }
            }
        }

        // Fallback to system properties if not specified via CLI arguments
        if (resolvedPath == null) {
            String propData = System.getProperty(PROP_DATA);
            if (propData != null && !propData.isBlank()) {
                resolvedPath = Path.of(propData.trim());
            } else {
                resolvedPath = defaultDataPath();
            }
        }

        if (!resolvedSeed) {
            String propSeed = System.getProperty(PROP_SEED);
            if (propSeed != null) {
                resolvedSeed = "true".equalsIgnoreCase(propSeed.trim()) || "".equals(propSeed.trim());
            }
        }

        return new AppConfig(resolvedPath, resolvedSeed);
    }

    /**
     * Convenience method parsing configuration from a string array.
     *
     * @param args string array of arguments
     * @return resolved AppConfig instance
     */
    public static AppConfig parse(String[] args) {
        return parse(args != null ? List.of(args) : List.of());
    }
}
