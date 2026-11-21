package com.todoapp.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.todoapp.model.Category;
import com.todoapp.model.ChecklistItem;
import com.todoapp.model.ChecklistTask;
import com.todoapp.model.DeadlineTask;
import com.todoapp.model.Priority;
import com.todoapp.model.SimpleTask;
import com.todoapp.model.Task;
import com.todoapp.model.TaskType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Hand-written mapper converting between domain entities and Gson JSON representations.
 * Handles polymorphic (de)serialization of tasks via the {@code type} discriminator
 * without requiring runtime reflection or complex third-party type adapters.
 */
public final class JsonMapper {

    private static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();

    private JsonMapper() {
        // utility class
    }

    /**
     * Serializes an {@link AppData} snapshot into a structured {@link JsonObject}.
     *
     * @param data non-null application data snapshot
     * @return JSON object representation
     */
    public static JsonObject toJson(AppData data) {
        Objects.requireNonNull(data, "AppData must not be null");
        JsonObject root = new JsonObject();
        root.addProperty("schemaVersion", data.schemaVersion());

        JsonArray catArray = new JsonArray();
        for (Category category : data.categories()) {
            catArray.add(categoryToJson(category));
        }
        root.add("categories", catArray);

        JsonArray taskArray = new JsonArray();
        for (Task task : data.tasks()) {
            taskArray.add(taskToJson(task));
        }
        root.add("tasks", taskArray);

        return root;
    }

    /**
     * Serializes an {@link AppData} snapshot into a formatted pretty-printed JSON string.
     *
     * @param data non-null application data snapshot
     * @return pretty-printed JSON string
     */
    public static String toPrettyJson(AppData data) {
        return PRETTY_GSON.toJson(toJson(data));
    }

    /**
     * Deserializes an {@link AppData} snapshot from a {@link JsonObject}.
     * Invalid or unknown task entries are skipped and reported in the provided warnings list.
     *
     * @param root     non-null root JSON object
     * @param warnings optional list to collect non-fatal loading warnings
     * @return deserialized AppData instance
     */
    public static AppData fromJson(JsonObject root, List<String> warnings) {
        Objects.requireNonNull(root, "Root JSON object must not be null");
        List<String> warnList = warnings != null ? warnings : new ArrayList<>();

        int schemaVersion = root.has("schemaVersion") && !root.get("schemaVersion").isJsonNull()
                ? root.get("schemaVersion").getAsInt()
                : AppData.CURRENT_SCHEMA_VERSION;

        List<Category> categories = new ArrayList<>();
        if (root.has("categories") && root.get("categories").isJsonArray()) {
            JsonArray catArray = root.getAsJsonArray("categories");
            for (JsonElement elem : catArray) {
                if (elem.isJsonObject()) {
                    try {
                        categories.add(categoryFromJson(elem.getAsJsonObject()));
                    } catch (Exception e) {
                        warnList.add("Skipped invalid category entry: " + e.getMessage());
                    }
                } else {
                    warnList.add("Skipped non-object category entry");
                }
            }
        }

        // Ensure default category is always present
        boolean hasDefault = categories.stream().anyMatch(c -> Category.DEFAULT_ID.equals(c.getId()));
        if (!hasDefault) {
            categories.add(0, Category.createDefault());
        }

        List<Task> tasks = new ArrayList<>();
        if (root.has("tasks") && root.get("tasks").isJsonArray()) {
            JsonArray taskArray = root.getAsJsonArray("tasks");
            for (JsonElement elem : taskArray) {
                if (elem.isJsonObject()) {
                    try {
                        tasks.add(taskFromJson(elem.getAsJsonObject()));
                    } catch (Exception e) {
                        warnList.add("Skipped invalid task entry: " + e.getMessage());
                    }
                } else {
                    warnList.add("Skipped non-object task entry");
                }
            }
        }

        return new AppData(schemaVersion, categories, tasks);
    }

    /**
     * Deserializes an {@link AppData} snapshot from a {@link JsonObject} without warning collection.
     *
     * @param root non-null root JSON object
     * @return deserialized AppData instance
     */
    public static AppData fromJson(JsonObject root) {
        return fromJson(root, new ArrayList<>());
    }

    /**
     * Deserializes an {@link AppData} snapshot from a raw JSON string.
     *
     * @param rawJson  non-null JSON string
     * @param warnings optional list to collect warnings
     * @return deserialized AppData instance
     */
    public static AppData fromRawJson(String rawJson, List<String> warnings) {
        Objects.requireNonNull(rawJson, "Raw JSON string must not be null");
        JsonElement element = JsonParser.parseString(rawJson);
        if (!element.isJsonObject()) {
            throw new IllegalArgumentException("Root JSON must be an object");
        }
        return fromJson(element.getAsJsonObject(), warnings);
    }

    /**
     * Serializes a {@link Category} into a {@link JsonObject}.
     *
     * @param category non-null category entity
     * @return JSON object representation
     */
    public static JsonObject categoryToJson(Category category) {
        Objects.requireNonNull(category, "Category must not be null");
        JsonObject obj = new JsonObject();
        obj.addProperty("id", category.getId().toString());
        obj.addProperty("name", category.getName());
        obj.addProperty("colorHex", category.getColorHex());
        return obj;
    }

    /**
     * Deserializes a {@link Category} from a {@link JsonObject}.
     *
     * @param json non-null JSON object
     * @return restored Category entity
     */
    public static Category categoryFromJson(JsonObject json) {
        Objects.requireNonNull(json, "Category JSON object must not be null");
        UUID id = UUID.fromString(json.get("id").getAsString());
        String name = json.get("name").getAsString();
        String colorHex = json.get("colorHex").getAsString();
        return new Category(id, name, colorHex);
    }

    /**
     * Serializes a polymorphic {@link Task} entity into a {@link JsonObject} with a type discriminator.
     *
     * @param task non-null task entity
     * @return JSON object representation
     */
    public static JsonObject taskToJson(Task task) {
        Objects.requireNonNull(task, "Task must not be null");
        JsonObject obj = new JsonObject();
        obj.addProperty("type", task.getType().name());
        obj.addProperty("id", task.getId().toString());
        obj.addProperty("title", task.getTitle());
        obj.addProperty("description", task.getDescription());
        obj.addProperty("priority", task.getPriority().name());
        obj.addProperty("categoryId", task.getCategoryId().toString());
        obj.addProperty("completed", task.isCompleted());
        obj.addProperty("pinned", task.isPinned());
        obj.addProperty("createdAt", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(task.getCreatedAt()));
        obj.addProperty("updatedAt", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(task.getUpdatedAt()));

        if (task instanceof DeadlineTask deadlineTask) {
            obj.addProperty("dueDate", DateTimeFormatter.ISO_LOCAL_DATE.format(deadlineTask.getDueDateValue()));
        } else if (task instanceof ChecklistTask checklistTask) {
            JsonArray itemsArray = new JsonArray();
            for (ChecklistItem item : checklistTask.getItems()) {
                itemsArray.add(checklistItemToJson(item));
            }
            obj.add("items", itemsArray);
        }

        return obj;
    }

    /**
     * Deserializes a polymorphic {@link Task} entity from a {@link JsonObject}.
     *
     * @param json non-null JSON object containing a valid {@code type} discriminator
     * @return restored Task entity
     * @throws IllegalArgumentException if type is missing, unknown, or required properties are invalid
     */
    public static Task taskFromJson(JsonObject json) {
        Objects.requireNonNull(json, "Task JSON object must not be null");
        if (!json.has("type") || json.get("type").isJsonNull()) {
            throw new IllegalArgumentException("Task JSON missing required 'type' discriminator");
        }

        String typeStr = json.get("type").getAsString();
        TaskType type;
        try {
            type = TaskType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown task type discriminator: " + typeStr, e);
        }

        UUID id = UUID.fromString(json.get("id").getAsString());
        String title = json.get("title").getAsString();
        String description = json.has("description") && !json.get("description").isJsonNull()
                ? json.get("description").getAsString() : "";
        Priority priority = json.has("priority") && !json.get("priority").isJsonNull()
                ? Priority.valueOf(json.get("priority").getAsString()) : Priority.MEDIUM;
        UUID categoryId = json.has("categoryId") && !json.get("categoryId").isJsonNull()
                ? UUID.fromString(json.get("categoryId").getAsString()) : Category.DEFAULT_ID;
        boolean completed = json.has("completed") && json.get("completed").getAsBoolean();
        boolean pinned = json.has("pinned") && json.get("pinned").getAsBoolean();
        LocalDateTime createdAt = json.has("createdAt")
                ? LocalDateTime.parse(json.get("createdAt").getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : LocalDateTime.now();
        LocalDateTime updatedAt = json.has("updatedAt")
                ? LocalDateTime.parse(json.get("updatedAt").getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : LocalDateTime.now();

        return switch (type) {
            case SIMPLE -> new SimpleTask(id, title, description, priority, categoryId,
                    completed, pinned, createdAt, updatedAt);
            case DEADLINE -> {
                if (!json.has("dueDate") || json.get("dueDate").isJsonNull()) {
                    throw new IllegalArgumentException("DeadlineTask missing required 'dueDate' property");
                }
                LocalDate dueDate = LocalDate.parse(json.get("dueDate").getAsString(),
                        DateTimeFormatter.ISO_LOCAL_DATE);
                yield new DeadlineTask(id, title, description, priority, categoryId,
                        completed, pinned, createdAt, updatedAt, dueDate);
            }
            case CHECKLIST -> {
                List<ChecklistItem> items = new ArrayList<>();
                if (json.has("items") && json.get("items").isJsonArray()) {
                    JsonArray arr = json.getAsJsonArray("items");
                    for (JsonElement elem : arr) {
                        if (elem.isJsonObject()) {
                            items.add(checklistItemFromJson(elem.getAsJsonObject()));
                        }
                    }
                }
                yield new ChecklistTask(id, title, description, priority, categoryId,
                        completed, pinned, createdAt, updatedAt, items);
            }
        };
    }

    /**
     * Serializes a {@link ChecklistItem} into a {@link JsonObject}.
     *
     * @param item non-null checklist item
     * @return JSON object representation
     */
    public static JsonObject checklistItemToJson(ChecklistItem item) {
        Objects.requireNonNull(item, "ChecklistItem must not be null");
        JsonObject obj = new JsonObject();
        obj.addProperty("id", item.getId().toString());
        obj.addProperty("text", item.getText());
        obj.addProperty("done", item.isDone());
        return obj;
    }

    /**
     * Deserializes a {@link ChecklistItem} from a {@link JsonObject}.
     *
     * @param json non-null JSON object
     * @return restored ChecklistItem
     */
    public static ChecklistItem checklistItemFromJson(JsonObject json) {
        Objects.requireNonNull(json, "ChecklistItem JSON object must not be null");
        UUID id = UUID.fromString(json.get("id").getAsString());
        String text = json.get("text").getAsString();
        boolean done = json.has("done") && json.get("done").getAsBoolean();
        return new ChecklistItem(id, text, done);
    }
}
