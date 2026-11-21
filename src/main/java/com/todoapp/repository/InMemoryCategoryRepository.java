package com.todoapp.repository;

import com.todoapp.model.Category;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * In-memory implementation of {@link CategoryRepository} backed by a {@link LinkedHashMap}.
 * Ensures the default "General" category is always present after instantiation and clearing.
 */
public class InMemoryCategoryRepository implements CategoryRepository {

    private final Map<UUID, Category> categories = new LinkedHashMap<>();

    /**
     * Constructs an in-memory category repository populated with the default category.
     */
    public InMemoryCategoryRepository() {
        initDefault();
    }

    private void initDefault() {
        Category defaultCategory = Category.createDefault();
        categories.put(defaultCategory.getId(), defaultCategory);
    }

    @Override
    public List<Category> findAll() {
        return new ArrayList<>(categories.values());
    }

    @Override
    public Optional<Category> findById(UUID id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(categories.get(id));
    }

    @Override
    public Optional<Category> findByNameIgnoreCase(String name) {
        if (name == null || name.isBlank()) return Optional.empty();
        String trimmed = name.trim();
        return categories.values().stream()
                .filter(c -> c.getName().equalsIgnoreCase(trimmed))
                .findFirst();
    }

    @Override
    public Category save(Category category) {
        Objects.requireNonNull(category, "Category to save must not be null");
        categories.put(category.getId(), category);
        return category;
    }

    @Override
    public Optional<Category> deleteById(UUID id) {
        if (id == null || Category.DEFAULT_ID.equals(id)) {
            return Optional.empty();
        }
        return Optional.ofNullable(categories.remove(id));
    }

    @Override
    public void clear() {
        categories.clear();
        initDefault();
    }

    @Override
    public int count() {
        return categories.size();
    }
}
