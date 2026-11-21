package com.todoapp.service;

import com.todoapp.model.Task;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;

/**
 * Sorting strategies for ordering tasks in views and queries.
 */
public enum SortOption {

    /**
     * Sort by priority rank (HIGH -> MEDIUM -> LOW).
     */
    PRIORITY("Priority"),

    /**
     * Sort by calendar due date. Tasks without due dates always appear last regardless of direction.
     */
    DUE_DATE("Due Date"),

    /**
     * Sort by creation timestamp (oldest first if ascending, newest first if descending).
     */
    CREATED("Date Created"),

    /**
     * Sort by task title (case-insensitive alphabetical order).
     */
    TITLE("Title");

    private final String label;

    SortOption(String label) {
        this.label = label;
    }

    /**
     * Returns human-readable label.
     *
     * @return option label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Constructs a {@link Comparator} for this sort option respecting ascending/descending order.
     * Note: Tasks without a due date are always placed at the end when sorting by DUE_DATE.
     *
     * @param ascending true for ascending, false for descending
     * @return Comparator for Task instances
     */
    public Comparator<Task> getComparator(boolean ascending) {
        return switch (this) {
            case PRIORITY -> {
                Comparator<Task> comp = Comparator.comparingInt(t -> t.getPriority().getRank());
                yield ascending ? comp : comp.reversed();
            }
            case DUE_DATE -> (t1, t2) -> {
                Optional<LocalDate> d1 = t1.getDueDate();
                Optional<LocalDate> d2 = t2.getDueDate();
                if (d1.isEmpty() && d2.isEmpty()) return 0;
                if (d1.isEmpty()) return 1;  // null dates always last
                if (d2.isEmpty()) return -1; // null dates always last
                int cmp = d1.get().compareTo(d2.get());
                return ascending ? cmp : -cmp;
            };
            case CREATED -> {
                Comparator<Task> comp = Comparator.comparing(Task::getCreatedAt);
                yield ascending ? comp : comp.reversed();
            }
            case TITLE -> {
                Comparator<Task> comp = Comparator.comparing(
                        t -> t.getTitle().toLowerCase(Locale.ROOT)
                );
                yield ascending ? comp : comp.reversed();
            }
        };
    }

    /**
     * Returns the default ascending comparator.
     *
     * @return ascending comparator
     */
    public Comparator<Task> getComparator() {
        return getComparator(true);
    }

    /**
     * Resolves SortOption from string label or constant name.
     *
     * @param label display label to parse
     * @return matching SortOption
     */
    public static SortOption fromLabel(String label) {
        if (label == null || label.isBlank()) {
            return CREATED;
        }
        for (SortOption opt : values()) {
            if (opt.label.equalsIgnoreCase(label.trim()) || opt.name().equalsIgnoreCase(label.trim())) {
                return opt;
            }
        }
        throw new IllegalArgumentException("Unknown sort option: " + label);
    }

    @Override
    public String toString() {
        return label;
    }
}
