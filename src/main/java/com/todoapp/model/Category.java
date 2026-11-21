package com.todoapp.model;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Organizational category entity used to group and classify tasks.
 */
public class Category {

    /**
     * Canonical fixed UUID for the permanent default "General" category.
     */
    public static final UUID DEFAULT_ID = UUID.nameUUIDFromBytes("default-category".getBytes(StandardCharsets.UTF_8));

    /**
     * Name of the permanent default category.
     */
    public static final String DEFAULT_NAME = "General";

    /**
     * Default hex color code for the General category.
     */
    public static final String DEFAULT_COLOR = "#757575";

    /**
     * Maximum character length permitted for category names.
     */
    public static final int MAX_NAME_LENGTH = 30;

    /**
     * Validation pattern for hex color codes in #RRGGBB format.
     */
    public static final Pattern COLOR_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    private final UUID id;
    private String name;
    private String colorHex;

    /**
     * Constructs a Category with full attributes.
     *
     * @param id       immutable category identifier
     * @param name     category name (1–30 characters)
     * @param colorHex hex color string (e.g. #1976D2)
     * @throws IllegalArgumentException if validation rules are violated
     */
    public Category(UUID id, String name, String colorHex) {
        this.id = Objects.requireNonNull(id, "Category ID must not be null");
        setName(name);
        setColorHex(colorHex);
    }

    /**
     * Convenience constructor generating a random UUID.
     *
     * @param name     category name
     * @param colorHex hex color string
     */
    public Category(String name, String colorHex) {
        this(UUID.randomUUID(), name, colorHex);
    }

    /**
     * Creates and returns the default "General" category.
     *
     * @return the default Category instance
     */
    public static Category createDefault() {
        return new Category(DEFAULT_ID, DEFAULT_NAME, DEFAULT_COLOR);
    }

    /**
     * Returns the unique category ID.
     *
     * @return category UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Returns the category name.
     *
     * @return category name
     */
    public String getName() {
        return name;
    }

    /**
     * Updates the category name.
     *
     * @param name trimmed name (1–30 characters)
     * @throws IllegalArgumentException if name is blank or exceeds limit
     */
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name must not be empty");
        }
        String trimmed = name.trim();
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Category name must not exceed " + MAX_NAME_LENGTH + " characters");
        }
        this.name = trimmed;
    }

    /**
     * Returns the hex color string.
     *
     * @return hex color string
     */
    public String getColorHex() {
        return colorHex;
    }

    /**
     * Updates the hex color code.
     *
     * @param colorHex valid #RRGGBB hex color code
     * @throws IllegalArgumentException if color format is invalid
     */
    public void setColorHex(String colorHex) {
        if (colorHex == null || !COLOR_PATTERN.matcher(colorHex.trim()).matches()) {
            throw new IllegalArgumentException("Invalid color hex format: '" + colorHex + "'. Expected format: #RRGGBB");
        }
        this.colorHex = colorHex.trim().toUpperCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", colorHex='" + colorHex + '\'' +
                '}';
    }
}
