package com.todoapp.ui;

import com.todoapp.model.Priority;

/**
 * Visual styling constants defining color palettes, dimensions, fonts, and spacing.
 * Ensures consistent design across all JavaFX views without hardcoded inline style strings.
 */
public final class Theme {

    private Theme() {
        // Prevent instantiation
    }

    // --- Priority Colors ---
    /** Color code for High priority tasks. */
    public static final String COLOR_PRIORITY_HIGH = "#E53935";

    /** Color code for Medium priority tasks. */
    public static final String COLOR_PRIORITY_MEDIUM = "#FB8C00";

    /** Color code for Low priority tasks. */
    public static final String COLOR_PRIORITY_LOW = "#43A047";

    // --- Core UI Colors ---
    /** Primary brand blue color. */
    public static final String COLOR_PRIMARY = "#1976D2";

    /** Light background tone for application canvas. */
    public static final String COLOR_BACKGROUND = "#F5F5F7";

    /** Surface card white. */
    public static final String COLOR_SURFACE = "#FFFFFF";

    /** Dark primary text color. */
    public static final String COLOR_TEXT_PRIMARY = "#212121";

    /** Secondary muted text color. */
    public static final String COLOR_TEXT_SECONDARY = "#757575";

    /** Border outline gray color. */
    public static final String COLOR_BORDER = "#E0E0E0";

    /** Error indicator red color. */
    public static final String COLOR_ERROR = "#D32F2F";

    /** Selected category or card background highlight. */
    public static final String COLOR_SELECTION_BG = "#E3F2FD";

    // --- Layout Spacing & Dimensions ---
    /** Extra small gap (4px). */
    public static final double SPACING_XS = 4.0;

    /** Standard small gap (8px). */
    public static final double SPACING_SMALL = 8.0;

    /** Medium layout gap (12px). */
    public static final double SPACING_MEDIUM = 12.0;

    /** Large section gap (16px). */
    public static final double SPACING_LARGE = 16.0;

    /** Standard pane padding (12px). */
    public static final double PADDING_STANDARD = 12.0;

    /** Task card corner radius in pixels. */
    public static final double CORNER_RADIUS = 6.0;

    // --- Typography ---
    /** System font family stack. */
    public static final String FONT_FAMILY = "Segoe UI, -apple-system, BlinkMacSystemFont, Roboto, sans-serif";

    /** Small caption font size. */
    public static final int FONT_SIZE_SMALL = 11;

    /** Body font size. */
    public static final int FONT_SIZE_BASE = 13;

    /** Subtitle font size. */
    public static final int FONT_SIZE_MEDIUM = 15;

    /** Heading/title font size. */
    public static final int FONT_SIZE_TITLE = 18;

    /**
     * Resolves the corresponding color hex string for a given priority.
     *
     * @param priority task priority
     * @return hex color code
     */
    public static String getPriorityColor(Priority priority) {
        if (priority == null) {
            return COLOR_PRIORITY_LOW;
        }
        return switch (priority) {
            case HIGH -> COLOR_PRIORITY_HIGH;
            case MEDIUM -> COLOR_PRIORITY_MEDIUM;
            case LOW -> COLOR_PRIORITY_LOW;
        };
    }
}
