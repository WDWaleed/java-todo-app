package com.todoapp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Basic smoke test verifying application configuration without starting the JavaFX toolkit.
 */
class AppSmokeTest {

    @Test
    @DisplayName("Verify application name constant")
    void testAppName() {
        assertEquals("TODO App", App.APP_NAME);
    }
}
