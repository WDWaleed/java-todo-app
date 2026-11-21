package com.todoapp.service;

import com.todoapp.model.Priority;

import java.util.UUID;

/**
 * Immutable query parameter record defining multi-criteria filtering,
 * keyword searching, and sorting rules.
 *
 * @param categoryId optional category filter (null matches all categories)
 * @param status     completion status filter (ALL, ACTIVE, COMPLETED)
 * @param priority   optional priority filter (null matches any priority)
 * @param searchText keyword search query (matched case-insensitively in title and description)
 * @param sort       sorting strategy
 * @param ascending  sort direction flag (true = ascending, false = descending)
 */
public record TaskQuery(
        UUID categoryId,
        StatusFilter status,
        Priority priority,
        String searchText,
        SortOption sort,
        boolean ascending
) {

    /**
     * Compact constructor normalizing null inputs to sensible defaults.
     */
    public TaskQuery {
        status = status != null ? status : StatusFilter.ALL;
        sort = sort != null ? sort : SortOption.CREATED;
        searchText = searchText != null ? searchText : "";
    }

    /**
     * Creates a default TaskQuery matching all tasks, sorted by creation date descending.
     *
     * @return default query configuration
     */
    public static TaskQuery defaultQuery() {
        return new TaskQuery(null, StatusFilter.ALL, null, "", SortOption.CREATED, false);
    }

    /**
     * Returns a copy with the category filter updated.
     *
     * @param categoryId category UUID, or null for all categories
     * @return updated TaskQuery
     */
    public TaskQuery withCategory(UUID categoryId) {
        return new TaskQuery(categoryId, this.status, this.priority, this.searchText, this.sort, this.ascending);
    }

    /**
     * Returns a copy with the status filter updated.
     *
     * @param status status filter (ALL, ACTIVE, COMPLETED)
     * @return updated TaskQuery
     */
    public TaskQuery withStatus(StatusFilter status) {
        return new TaskQuery(this.categoryId, status, this.priority, this.searchText, this.sort, this.ascending);
    }

    /**
     * Returns a copy with the priority filter updated.
     *
     * @param priority priority level, or null for any priority
     * @return updated TaskQuery
     */
    public TaskQuery withPriority(Priority priority) {
        return new TaskQuery(this.categoryId, this.status, priority, this.searchText, this.sort, this.ascending);
    }

    /**
     * Returns a copy with the search keyword updated.
     *
     * @param searchText search query string
     * @return updated TaskQuery
     */
    public TaskQuery withSearch(String searchText) {
        return new TaskQuery(this.categoryId, this.status, this.priority, searchText, this.sort, this.ascending);
    }

    /**
     * Returns a copy with the sorting strategy and direction updated.
     *
     * @param sort      sort strategy
     * @param ascending direction
     * @return updated TaskQuery
     */
    public TaskQuery withSort(SortOption sort, boolean ascending) {
        return new TaskQuery(this.categoryId, this.status, this.priority, this.searchText, sort, ascending);
    }

    /**
     * Returns a copy with the sort option updated while preserving direction.
     *
     * @param sort sort strategy
     * @return updated TaskQuery
     */
    public TaskQuery withSortOption(SortOption sort) {
        return new TaskQuery(this.categoryId, this.status, this.priority, this.searchText, sort, this.ascending);
    }

    /**
     * Returns a copy with the sort direction updated while preserving strategy.
     *
     * @param ascending direction flag
     * @return updated TaskQuery
     */
    public TaskQuery withAscending(boolean ascending) {
        return new TaskQuery(this.categoryId, this.status, this.priority, this.searchText, this.sort, ascending);
    }
}
