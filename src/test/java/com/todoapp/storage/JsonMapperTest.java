package com.todoapp.storage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.todoapp.model.Category;
import com.todoapp.model.DeadlineTask;
import com.todoapp.model.Priority;
import com.todoapp.model.SimpleTask;
import com.todoapp.model.Task;
import com.todoapp.model.TaskType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JsonMapper verifying polymorphic serialization,
 * deserialization, warning generation, and fault tolerance.
 */
class JsonMapperTest {

    @Test
    @DisplayName("Round-trip mapping of polymorphic task types and categories")
    void testRoundTripPolymorphicTasksAndCategories() {
        Category academics = new Category(UUID.randomUUID(), "Academics", "#1976D2");
        Category personal = new Category(UUID.randomUUID(), "Personal", "#388E3C");

        LocalDateTime created = LocalDateTime.of(2026, 11, 20, 10, 15, 30);
        LocalDateTime updated = LocalDateTime.of(2026, 11, 21, 8, 0, 0);

        SimpleTask simple = new SimpleTask(
                UUID.randomUUID(),
                "Simple Task Title",
                Priority.HIGH,
                academics.getId(),
                false,
                true,
                created,
                updated
        );

        DeadlineTask deadline = new DeadlineTask(
                UUID.randomUUID(),
                "Deadline Task Title",
                Priority.LOW,
                Category.DEFAULT_ID,
                true,
                false,
                created,
                updated,
                LocalDate.of(2026, 11, 25)
        );

        AppData originalData = new AppData(
                1,
                List.of(Category.createDefault(), academics, personal),
                List.of(simple, deadline)
        );

        String jsonString = JsonMapper.toPrettyJson(originalData);
        assertNotNull(jsonString);
        assertTrue(jsonString.contains("\"type\": \"SIMPLE\""));
        assertTrue(jsonString.contains("\"type\": \"DEADLINE\""));

        List<String> warnings = new ArrayList<>();
        AppData restoredData = JsonMapper.fromRawJson(jsonString, warnings);
        assertTrue(warnings.isEmpty(), "Expected no warnings for valid data");

        assertEquals(1, restoredData.schemaVersion());
        assertEquals(3, restoredData.categories().size());
        assertEquals(2, restoredData.tasks().size());

        // Verify categories
        assertEquals(Category.DEFAULT_ID, restoredData.categories().get(0).getId());
        assertEquals("Academics", restoredData.categories().get(1).getName());
        assertEquals("#388E3C", restoredData.categories().get(2).getColorHex());

        // Verify simple task
        Task restoredSimple = restoredData.tasks().get(0);
        assertInstanceOf(SimpleTask.class, restoredSimple);
        assertEquals(simple.getId(), restoredSimple.getId());
        assertEquals("Simple Task Title", restoredSimple.getTitle());
        assertEquals(Priority.HIGH, restoredSimple.getPriority());
        assertEquals(academics.getId(), restoredSimple.getCategoryId());
        assertFalse(restoredSimple.isCompleted());
        assertTrue(restoredSimple.isPinned());
        assertEquals(created, restoredSimple.getCreatedAt());
        assertEquals(updated, restoredSimple.getUpdatedAt());

        // Verify deadline task
        Task restoredDeadline = restoredData.tasks().get(1);
        assertInstanceOf(DeadlineTask.class, restoredDeadline);
        DeadlineTask castDeadline = (DeadlineTask) restoredDeadline;
        assertEquals(deadline.getId(), castDeadline.getId());
        assertEquals(LocalDate.of(2026, 11, 25), castDeadline.getDueDateValue());
        assertTrue(castDeadline.isCompleted());
    }

    @Test
    @DisplayName("Unknown task type is skipped and reported in warnings")
    void testUnknownTaskTypeIsSkipped() {
        String json = """
                {
                  "schemaVersion": 1,
                  "categories": [],
                  "tasks": [
                    {
                      "type": "UNKNOWN_FUTURE_TYPE",
                      "id": "11111111-1111-1111-1111-111111111111",
                      "title": "Future task"
                    },
                    {
                      "type": "SIMPLE",
                      "id": "22222222-2222-2222-2222-222222222222",
                      "title": "Valid task",
                      "priority": "LOW",
                      "categoryId": "00000000-0000-0000-0000-000000000001",
                      "completed": false,
                      "pinned": false,
                      "createdAt": "2026-11-20T10:00:00",
                      "updatedAt": "2026-11-20T10:00:00"
                    }
                  ]
                }
                """;

        List<String> warnings = new ArrayList<>();
        AppData data = JsonMapper.fromRawJson(json, warnings);

        assertEquals(1, data.tasks().size());
        assertEquals("Valid task", data.tasks().get(0).getTitle());
        assertFalse(warnings.isEmpty());
        assertTrue(warnings.stream().anyMatch(w -> w.contains("UNKNOWN_FUTURE_TYPE")));
    }

    @Test
    @DisplayName("Default category is added automatically if absent from categories array")
    void testDefaultCategoryAddedIfMissing() {
        String json = """
                {
                  "schemaVersion": 1,
                  "categories": [
                    {
                      "id": "6c84fb90-12c4-11e1-840d-7b25c5ee775a",
                      "name": "Custom Category",
                      "colorHex": "#E53935"
                    }
                  ],
                  "tasks": []
                }
                """;

        AppData data = JsonMapper.fromRawJson(json, new ArrayList<>());
        assertEquals(2, data.categories().size());
        assertEquals(Category.DEFAULT_ID, data.categories().get(0).getId());
        assertEquals("General", data.categories().get(0).getName());
        assertEquals("Custom Category", data.categories().get(1).getName());
    }

    @Test
    @DisplayName("Invalid task json without type throws IllegalArgumentException")
    void testTaskFromJsonMissingTypeThrows() {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", UUID.randomUUID().toString());
        obj.addProperty("title", "No Type");

        assertThrows(IllegalArgumentException.class, () -> JsonMapper.taskFromJson(obj));
    }

    @Test
    @DisplayName("Deadline task missing dueDate throws IllegalArgumentException")
    void testDeadlineTaskMissingDueDateThrows() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", TaskType.DEADLINE.name());
        obj.addProperty("id", UUID.randomUUID().toString());
        obj.addProperty("title", "Missing Due Date");

        assertThrows(IllegalArgumentException.class, () -> JsonMapper.taskFromJson(obj));
    }

    @Test
    @DisplayName("Non-object root JSON throws IllegalArgumentException")
    void testNonObjectRootThrows() {
        assertThrows(IllegalArgumentException.class, () -> JsonMapper.fromRawJson("[\"not\", \"an\", \"object\"]", null));
    }

    @Test
    @DisplayName("Single-argument fromJson delegates and restores valid AppData")
    void testSingleArgFromJson() {
        AppData appData = AppData.empty();
        JsonObject json = JsonParser.parseString(JsonMapper.toJson(appData)).getAsJsonObject();
        AppData restored = JsonMapper.fromJson(json);
        assertNotNull(restored);
        assertEquals(1, restored.categories().size());
    }

    @Test
    @DisplayName("Category individual mapping round-trip")
    void testIndividualCategoryMapping() {
        Category cat = new Category(UUID.randomUUID(), "Design", "#FB8C00");
        JsonObject catJson = JsonMapper.categoryToJson(cat);
        Category restoredCat = JsonMapper.categoryFromJson(catJson);
        assertEquals(cat.getId(), restoredCat.getId());
        assertEquals("Design", restoredCat.getName());
        assertEquals("#FB8C00", restoredCat.getColorHex());
    }

    @Test
    @DisplayName("AppData constructor null handling")
    void testAppDataNullSafety() {
        AppData nullCollections = new AppData(null, null);
        assertNotNull(nullCollections.categories());
        assertNotNull(nullCollections.tasks());
        assertTrue(nullCollections.categories().isEmpty());
        assertTrue(nullCollections.tasks().isEmpty());
    }
}
