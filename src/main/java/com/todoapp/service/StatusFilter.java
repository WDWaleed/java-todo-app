package com.todoapp.service;

/**
 * Filter options for task completion status.
 */
public enum StatusFilter {

    /**
     * Include all tasks regardless of completion state.
     */
    ALL("All Tasks"),

    /**
     * Include only active (uncompleted) tasks.
     */
    ACTIVE("Active"),

    /**
     * Include only completed tasks.
     */
    COMPLETED("Completed");

    private final String label;

    StatusFilter(String label) {
        this.label = label;
    }

    /**
     * Returns user-visible display label.
     *
     * @return filter label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Resolves a StatusFilter from label or name.
     *
     * @param label label to parse
     * @return matching StatusFilter constant
     */
    public static StatusFilter fromLabel(String label) {
        if (label == null || label.isBlank()) {
            return ALL;
        }
        for (StatusFilter filter : values()) {
            if (filter.label.equalsIgnoreCase(label.trim()) || filter.name().equalsIgnoreCase(label.trim())) {
                return filter;
            }
        }
        throw new IllegalArgumentException("Unknown status filter: " + label);
    }

    @Override
    public String toString() {
        return label;
    }
}
