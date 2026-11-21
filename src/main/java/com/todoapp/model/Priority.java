package com.todoapp.model;

/**
 * Task priority levels with associated numeric ranks and user-visible labels.
 * Lower rank numbers indicate higher urgency.
 */
public enum Priority {

    /**
     * High priority (Rank 1).
     */
    HIGH(1, "High"),

    /**
     * Medium priority (Rank 2). Default level for newly created tasks.
     */
    MEDIUM(2, "Medium"),

    /**
     * Low priority (Rank 3).
     */
    LOW(3, "Low");

    private final int rank;
    private final String label;

    Priority(int rank, String label) {
        this.rank = rank;
        this.label = label;
    }

    /**
     * Returns the numeric urgency rank (1 = highest).
     *
     * @return the numeric rank
     */
    public int getRank() {
        return rank;
    }

    /**
     * Returns the human-readable display label.
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Resolves a {@link Priority} instance from its case-insensitive label.
     *
     * @param label display label to parse
     * @return matching Priority constant
     * @throws IllegalArgumentException if label is null or unknown
     */
    public static Priority fromLabel(String label) {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("Priority label must not be null or blank");
        }
        for (Priority priority : values()) {
            if (priority.label.equalsIgnoreCase(label.trim()) || priority.name().equalsIgnoreCase(label.trim())) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Unknown priority label: " + label);
    }

    @Override
    public String toString() {
        return label;
    }
}
