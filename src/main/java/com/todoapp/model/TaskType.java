package com.todoapp.model;

/**
 * Enumeration of supported polymorphic task types.
 */
public enum TaskType {

    /**
     * Basic task containing a title.
     */
    SIMPLE("Simple"),

    /**
     * Task linked to a specific calendar deadline with overdue alerting.
     */
    DEADLINE("Deadline");

    private final String label;

    TaskType(String label) {
        this.label = label;
    }

    /**
     * Returns the human-readable label for this task type.
     *
     * @return the type label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Resolves a {@link TaskType} from its case-insensitive label or name.
     *
     * @param label label to resolve
     * @return matching TaskType constant
     * @throws IllegalArgumentException if label is null or unknown
     */
    public static TaskType fromLabel(String label) {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("Task type label must not be null or blank");
        }
        for (TaskType type : values()) {
            if (type.label.equalsIgnoreCase(label.trim()) || type.name().equalsIgnoreCase(label.trim())) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown task type label: " + label);
    }

    @Override
    public String toString() {
        return label;
    }
}
