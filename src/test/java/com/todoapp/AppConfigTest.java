package com.todoapp;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AppConfig verifying CLI arguments parsing and system property fallbacks.
 */
class AppConfigTest {

    @AfterEach
    void cleanUpSystemProperties() {
        System.clearProperty(AppConfig.PROP_DATA);
        System.clearProperty(AppConfig.PROP_SEED);
    }

    @Test
    @DisplayName("Default configuration when no CLI args or system properties are set")
    void testDefaultConfig() {
        AppConfig config = AppConfig.parse(List.of());

        assertEquals(AppConfig.defaultDataPath(), config.dataPath());
        assertFalse(config.seed());
    }

    @Test
    @DisplayName("Parse CLI arguments: --data and --seed")
    void testParseCliArguments() {
        List<String> args = List.of("--seed", "--data=/custom/data.json");
        AppConfig config = AppConfig.parse(args);

        assertEquals(Path.of("/custom/data.json"), config.dataPath());
        assertTrue(config.seed());
    }

    @Test
    @DisplayName("Fallback to system properties when CLI arguments are omitted")
    void testFallbackToSystemProperties() {
        System.setProperty(AppConfig.PROP_DATA, "/prop/data.json");
        System.setProperty(AppConfig.PROP_SEED, "true");

        AppConfig config = AppConfig.parse(List.of());

        assertEquals(Path.of("/prop/data.json"), config.dataPath());
        assertTrue(config.seed());
    }

    @Test
    @DisplayName("CLI arguments override system properties")
    void testCliOverridesSystemProperties() {
        System.setProperty(AppConfig.PROP_DATA, "/prop/data.json");
        System.setProperty(AppConfig.PROP_SEED, "false");

        AppConfig config = AppConfig.parse(List.of("--data=/cli/data.json", "--seed"));

        assertEquals(Path.of("/cli/data.json"), config.dataPath());
        assertTrue(config.seed());
    }

    @Test
    @DisplayName("Convenience parse with string array and null safety")
    void testParseStringArrayAndNulls() {
        AppConfig c1 = AppConfig.parse(new String[]{"--seed"});
        assertTrue(c1.seed());

        AppConfig c2 = AppConfig.parse((List<String>) null);
        assertFalse(c2.seed());
    }
}
