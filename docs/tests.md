# Test Suite Catalog

This document lists all **139 automated unit and integration tests** in the TODO List Desktop Application, describing what each test verifies and its expected outcome.

- **Total Tests:** 139
- **Passing:** 139 (100%)
- **Test Framework:** JUnit Jupiter 5.10.2
- **Execution Command:** `mvn test`

---

## Complete Test Catalog

| # | Test Class | Test Method | What It Tests | Expected Outcome |
|---|---|---|---|---|
| 1 | `AppConfigTest` | `testDefaultConfig` | Default configuration values | Default data path set, seed disabled |
| 2 | `AppConfigTest` | `testCliOverridesSystemProperties` | CLI arguments vs system properties | CLI flags take precedence over properties |
| 3 | `AppConfigTest` | `testParseStringArrayAndNulls` | Parsing null and string array arguments | Safe fallback to defaults without error |
| 4 | `AppConfigTest` | `testParseCliArguments` | Explicit `--data` and `--seed` CLI flags | Custom path and seed enabled |
| 5 | `AppConfigTest` | `testFallbackToSystemProperties` | Missing CLI arguments with system properties | System property values used |
| 6 | `AppSmokeTest` | `testAppName` | Application title constant | Returns `"TODO App"` |
| 7 | `DevSeedTest` | `testSeedIfEmptySuccess` | Seeding empty repository | Default categories and tasks populated |
| 8 | `DevSeedTest` | `testSeedIfEmptySkippedWhenPopulated` | Seeding non-empty repository | No changes made to existing data |
| 9 | `EdgeCaseAndIntegrationTest` | `testRapidRepeatedToggles` | 100 rapid completion toggles | State alternates reliably without corruption |
| 10 | `EdgeCaseAndIntegrationTest` | `testPerformanceWith1000Tasks` | Querying across 1,000 tasks | Execution completes under 200 ms |
| 11 | `EdgeCaseAndIntegrationTest` | `testToggleCompleteTwiceRestoresState` | Double toggling task completion | Restores original completion state |
| 12 | `EdgeCaseAndIntegrationTest` | `testUnwritableDataFileThrowsStorageException` | Saving to a write-protected file | Throws `StorageException` |
| 13 | `EdgeCaseAndIntegrationTest` | `testTitleAtMaxLength` | Title with exactly 100 characters | Passes validation and persists |
| 14 | `EdgeCaseAndIntegrationTest` | `testWhitespaceTitleRejected` | Title consisting of only spaces | Throws `IllegalArgumentException` |
| 15 | `EdgeCaseAndIntegrationTest` | `testDeleteWhileCategoryFiltered` | Deleting task under active category filter | Task removed and view refreshed |
| 16 | `EdgeCaseAndIntegrationTest` | `testFixedClockTimestamps` | Task creation with fixed `Clock` | Timestamps match fixed clock instant |
| 17 | `EdgeCaseAndIntegrationTest` | `testDuplicateCategoryNameRejected` | Case-insensitive duplicate category name | Throws `ValidationException` |
| 18 | `EdgeCaseAndIntegrationTest` | `testPinnedTaskAlwaysFirst` | Sorting tasks when pinned items exist | Pinned tasks precede unpinned tasks |
| 19 | `EdgeCaseAndIntegrationTest` | `testNullTitleRejected` | Task draft with null title | Throws `IllegalArgumentException` |
| 20 | `EdgeCaseAndIntegrationTest` | `testInvalidHexColorRejected` | Category with invalid hex color string | Throws `ValidationException` |
| 21 | `EdgeCaseAndIntegrationTest` | `testUndoBufferSingleSlot` | Deleting two tasks consecutively | Only last deleted task is undoable |
| 22 | `EdgeCaseAndIntegrationTest` | `testUndoDeleteEmptyBuffer` | Triggering undo with empty buffer | Safe no-op, returns false |
| 23 | `EdgeCaseAndIntegrationTest` | `testEditTaskWhoseCategoryWasDeleted` | Editing task with removed category | Reassigned to default category |
| 24 | `EdgeCaseAndIntegrationTest` | `testCanUndoLifecycle` | Undo availability across delete/undo cycle | `canUndo` true only after deletion |
| 25 | `EdgeCaseAndIntegrationTest` | `testCorruptFileIsQuarantined` | Loading corrupted JSON file | File renamed to `.corrupt-*`, returns defaults |
| 26 | `EdgeCaseAndIntegrationTest` | `testDeadlineFutureNotOverdue` | Deadline task with future due date | `isOverdue` returns false |
| 27 | `EdgeCaseAndIntegrationTest` | `testDeleteCategoryReassignsTasks` | Deleting a custom category | Tasks reassigned to General category |
| 28 | `EdgeCaseAndIntegrationTest` | `testTaskWithInvalidCategoryRejected` | Creating task with nonexistent category ID | Throws `ValidationException` |
| 29 | `EdgeCaseAndIntegrationTest` | `testDeadlineTodayNotOverdue` | Deadline task due today | `isOverdue` returns false |
| 30 | `EdgeCaseAndIntegrationTest` | `testCannotDeleteDefaultCategory` | Attempting to delete General category | Throws `ValidationException` |
| 31 | `EdgeCaseAndIntegrationTest` | `testSearchCaseInsensitive` | Case-insensitive title searching | Matches tasks regardless of letter case |
| 32 | `EdgeCaseAndIntegrationTest` | `testCannotRenameDefaultCategory` | Attempting to rename General category | Throws `ValidationException` |
| 33 | `EdgeCaseAndIntegrationTest` | `testLoadNonExistentFileReturnsEmpty` | Loading when JSON file is absent | Returns empty dataset with default category |
| 34 | `EdgeCaseAndIntegrationTest` | `testCompletedDeadlineNotOverdue` | Overdue deadline task that is completed | `isOverdue` returns false |
| 35 | `EdgeCaseAndIntegrationTest` | `testUnicodeEmojiTitle` | Task title containing Unicode and emojis | Persists and retrieves accurately |
| 36 | `EdgeCaseAndIntegrationTest` | `testTitleExceedsMaxLength` | Task title longer than 100 characters | Throws `IllegalArgumentException` |
| 37 | `EdgeCaseAndIntegrationTest` | `testCompletedStatusFilter` | Querying with completed filter | Only completed tasks returned |
| 38 | `EdgeCaseAndIntegrationTest` | `testSortByPriorityAscending` | Sorting tasks by priority ascending | Ordered LOW to HIGH |
| 39 | `EdgeCaseAndIntegrationTest` | `testActiveAndPriorityFilter` | Filtering by active status and priority | Returns only matching active tasks |
| 40 | `EdgeCaseAndIntegrationTest` | `testDeadlinePastIsOverdue` | Incomplete deadline task with past date | `isOverdue` returns true |
| 41 | `EdgeCaseAndIntegrationTest` | `testTogglePinned` | Toggling pinned state on task | Pinned flag alternates true/false |
| 42 | `EdgeCaseAndIntegrationTest` | `testJsonStorageRoundTrip` | Saving and loading full application data | Reloaded entities equal original entities |
| 43 | `MainControllerTest` | `testOnSearch` | Controller search query update | Search text updated, view refreshed |
| 44 | `MainControllerTest` | `testFilterCombinations` | Applying multiple query filters | View state reflects combined query |
| 45 | `MainControllerTest` | `testOnStatusFilter` | Status filter changes (ALL/ACTIVE/DONE) | Status updated in state, view refreshed |
| 46 | `MainControllerTest` | `testOnRecolorCategory` | Updating category color via controller | Color updated, view refreshed |
| 47 | `MainControllerTest` | `testOnSelectTask` | Selecting a task by ID | `selectedTaskId` set in view state |
| 48 | `MainControllerTest` | `testRemoveListener` | Deregistering a view listener | Removed listener stops receiving updates |
| 49 | `MainControllerTest` | `testOnDeleteCategorySuccess` | Deleting custom category via controller | Category removed, success message set |
| 50 | `MainControllerTest` | `testOnCreateCategorySuccess` | Creating valid category via controller | Category added, success message set |
| 51 | `MainControllerTest` | `testOnCreateCategoryValidationFailure` | Creating category with blank/invalid name | Error message set in view state |
| 52 | `MainControllerTest` | `testOnSetPriority` | Updating task priority via controller | Priority updated, view refreshed |
| 53 | `MainControllerTest` | `testOnDeleteCategoryFailures` | Deleting default or nonexistent category | Error message set in view state |
| 54 | `MainControllerTest` | `testViewStateHelpers` | `ViewState` immutable copy helpers | Returns new instance with modified fields |
| 55 | `MainControllerTest` | `testOnPriorityFilter` | Filtering tasks by priority level | Priority filter updated, view refreshed |
| 56 | `MainControllerTest` | `testDomainMutationsTriggerRefresh` | Domain mutations notifying listeners | Listeners invoked on each mutation |
| 57 | `MainControllerTest` | `testOnRenameCategory` | Renaming category via controller | Name updated, view refreshed |
| 58 | `MainControllerTest` | `testRefreshNotifiesListeners` | Manual `refresh()` invocation | Registered listeners receive current state |
| 59 | `MainControllerTest` | `testOnUpdateTaskSuccess` | Updating task fields via controller | Task updated, view refreshed |
| 60 | `MainControllerTest` | `testOnClearFilters` | Resetting all active filters | Default query restored, messages cleared |
| 61 | `MainControllerTest` | `testOnToggleComplete` | Toggling task completion in controller | Task state toggled, view refreshed |
| 62 | `MainControllerTest` | `testInitialState` | Initial controller state configuration | Default query, no errors, empty selection |
| 63 | `MainControllerTest` | `testMessageHandling` | Info and error message lifecycle | Messages set and cleared properly |
| 64 | `MainControllerTest` | `testOnSelectCategory` | Filtering by category ID | Category filter updated, view refreshed |
| 65 | `MainControllerTest` | `testOnCreateTaskValidationFailure` | Submitting invalid task draft | Error message set in view state |
| 66 | `MainControllerTest` | `testOnCreateTaskSuccess` | Submitting valid task draft | Task created, selected, view refreshed |
| 67 | `MainControllerTest` | `testOnUndoDelete` | Restoring deleted task via undo | Task restored, undo buffer cleared |
| 68 | `MainControllerTest` | `testOnSort` | Changing sort option and direction | Sort criteria updated, view refreshed |
| 69 | `MainControllerTest` | `testOnTogglePinned` | Toggling pinned status in controller | Pinned state toggled, view refreshed |
| 70 | `MainControllerTest` | `testOnDeleteTask` | Deleting task via controller | Task deleted, undo buffer populated |
| 71 | `MainControllerTest` | `testOnUpdateTaskFailures` | Updating nonexistent task or invalid draft | Error message set in view state |
| 72 | `TaskModelTest` | `testSimpleTask` | `SimpleTask` instantiation and properties | Type `SIMPLE`, no due date, progress 0.0 |
| 73 | `TaskModelTest` | `testTaskDraftAndFactory` | `TaskDraft` validation and `TaskFactory` | Factory constructs matching task instance |
| 74 | `TaskModelTest` | `testTaskTypeEnum` | `TaskType` enum values and labels | Contains `SIMPLE` and `DEADLINE` |
| 75 | `TaskModelTest` | `testDeadlineTask` | `DeadlineTask` creation, due date, summary | Type `DEADLINE`, formatted summary date |
| 76 | `TaskModelTest` | `testPriorityEnum` | `Priority` ranks and labels | HIGH (1), MEDIUM (2), LOW (3) ranks |
| 77 | `TaskModelTest` | `testCategory` | `Category` record properties and immutability | Fields preserved, default ID verified |
| 78 | `TaskModelTest` | `testPolymorphicTaskBehavior (SimpleTask)` | Polymorphic methods on simple tasks | Correct type, summary, and progress |
| 79 | `TaskModelTest` | `testPolymorphicTaskBehavior (DeadlineTask)` | Polymorphic methods on deadline tasks | Correct type, summary, and overdue check |
| 80 | `RepositoryTest` | `testCategoryRepositoryOperations` | CRUD on `InMemoryCategoryRepository` | Saves, finds, lists, and deletes categories |
| 81 | `RepositoryTest` | `testTaskRepositoryOperations` | CRUD on `InMemoryTaskRepository` | Saves, finds, lists, and deletes tasks |
| 82 | `CategoryServiceTest` | `testConstructorValidation` | Instantiating service with null repositories | Throws `NullPointerException` |
| 83 | `CategoryServiceTest` | `testRenameCategory` | Renaming category with valid/invalid names | Renames successfully, rejects duplicates |
| 84 | `CategoryServiceTest` | `testDeleteCategoryAndTaskReassignment` | Deleting category with assigned tasks | Reassigns tasks to General category |
| 85 | `CategoryServiceTest` | `testCreateCategoryValidations` | Creating category with blank/duplicate name | Throws `ValidationException` |
| 86 | `CategoryServiceTest` | `testGetAndCountHelpers` | Category retrieval and task counting | Returns accurate categories and counts |
| 87 | `CategoryServiceTest` | `testCreateCategorySuccess` | Creating new category with valid inputs | Persists category, notifies listener |
| 88 | `CategoryServiceTest` | `testRecolorCategory` | Updating category color hex | Color changed, listener notified |
| 89 | `CategoryServiceTest` | `testListenerExceptionIsolation` | Listener throwing exception on category change | Exception caught, operation succeeds |
| 90 | `TaskQueryEngineTest` | `testSortOptionProperties` | `SortOption` enum properties and comparator | Valid comparators for all sort options |
| 91 | `TaskQueryEngineTest` | `testTaskQueryDefaultsAndWithHelpers` | `TaskQuery` record defaults and copy methods | Correct default query, immutable copies |
| 92 | `TaskQueryEngineTest` | `testCountHelpers` | Task counting by completion status | Accurate active and completed counts |
| 93 | `TaskQueryEngineTest` | `testSortByCreated` | Sorting tasks by creation timestamp | Chronological order maintained |
| 94 | `TaskQueryEngineTest` | `testKeywordSearch` | Filtering tasks by title keyword | Matches title substring case-insensitively |
| 95 | `TaskQueryEngineTest` | `testSortByDueDate` | Sorting tasks by deadline date | Earliest deadline first, no-deadline last |
| 96 | `TaskQueryEngineTest` | `testPinnedFirstInvariant (PRIORITY)` | Pinned task precedence under priority sort | Pinned task appears first |
| 97 | `TaskQueryEngineTest` | `testPinnedFirstInvariant (DUE_DATE)` | Pinned task precedence under due date sort | Pinned task appears first |
| 98 | `TaskQueryEngineTest` | `testPinnedFirstInvariant (CREATED)` | Pinned task precedence under created sort | Pinned task appears first |
| 99 | `TaskQueryEngineTest` | `testPinnedFirstInvariant (TITLE)` | Pinned task precedence under title sort | Pinned task appears first |
| 100 | `TaskQueryEngineTest` | `testSortByTitle` | Sorting tasks alphabetically by title | Case-insensitive alphabetical order |
| 101 | `TaskQueryEngineTest` | `testSortByPriority` | Sorting tasks by priority rank | HIGH before MEDIUM before LOW |
| 102 | `TaskQueryEngineTest` | `testQueryPerformance1000Tasks` | Querying across 1,000 tasks | Completes within SLA (<100 ms) |
| 103 | `TaskQueryEngineTest` | `testFilteringCombinations` | Combining category, status, priority filters | Returns only items matching all filters |
| 104 | `TaskServiceTest` | `testDirectRestore` | Direct task restoration via `restore()` | Task reinserted with original ID |
| 105 | `TaskServiceTest` | `testCreateTaskSuccess` | Creating task with valid draft | Task persisted, `CREATED` event fired |
| 106 | `TaskServiceTest` | `testCreateCopyIsolation` | Mutating returned task copy | Repository internal state unmodified |
| 107 | `TaskServiceTest` | `testCreateValidationFailures` | Creating task with invalid category or draft | Throws `ValidationException` |
| 108 | `TaskServiceTest` | `testTaskStateToggles` | Toggling completion and pinned flags | Flags updated, `UPDATED` event fired |
| 109 | `TaskServiceTest` | `testListenerBehavior` | Registering and notifying `TaskChangeListener` | Listener receives change events |
| 110 | `TaskServiceTest` | `testUpdateTaskValidation` | Updating task with invalid inputs | Throws `ValidationException` |
| 111 | `TaskServiceTest` | `testDeleteAndUndoBuffer` | Deleting task from service | Task removed and returned for undo |
| 112 | `TaskServiceTest` | `testUpdateTaskSuccess` | Updating task title, priority, due date | Fields updated, listener notified |
| 113 | `TaskServiceTest` | `testGetAndGetAll` | Retrieving single task and all tasks | Returns defensive copies of entities |
| 114 | `TaskServiceTest` | `testDefaultConstructor` | Default constructor without explicit clock | Initialized with system clock |
| 115 | `AutosaveListenerTest` | `testConvenienceConstructorsAndClosedGuard` | Constructor validation and post-close guard | Initializes properly, guards against use after close |
| 116 | `AutosaveListenerTest` | `testStorageErrorInvokesCallback` | Handling storage write exceptions | Error callback invoked with failure message |
| 117 | `AutosaveListenerTest` | `testFlushExecutesSynchronously` | Invoking synchronous `flush()` | Pending changes saved immediately |
| 118 | `AutosaveListenerTest` | `testAutosaveDebouncesRapidChanges` | Rapid consecutive domain modifications | Merged into single debounced disk write |
| 119 | `AutosaveListenerTest` | `testCloseFlushesPendingSaves` | Closing `AutosaveListener` | Pending saves flushed, executor terminated |
| 120 | `DataSyncTest` | `testLoadIntoPopulatesRepositories` | Loading `AppData` snapshot into repositories | Repositories populated with snapshot entities |
| 121 | `DataSyncTest` | `testLoadIntoNullIsNoOp` | Passing null data or repositories to sync | Safe no-op without exceptions |
| 122 | `DataSyncTest` | `testSnapshotCapturesCurrentState` | Creating snapshot from repositories | Snapshot contains all tasks and categories |
| 123 | `JsonMapperTest` | `testTaskFromJsonMissingTypeThrows` | Parsing JSON task without `"type"` | Throws `IllegalArgumentException` |
| 124 | `JsonMapperTest` | `testDeadlineTaskMissingDueDateThrows` | Parsing deadline task without `"dueDate"` | Throws `IllegalArgumentException` |
| 125 | `JsonMapperTest` | `testIndividualCategoryMapping` | Serializing/deserializing single category | Fields round-trip accurately |
| 126 | `JsonMapperTest` | `testNonObjectRootThrows` | Parsing JSON with non-object root | Throws `IllegalArgumentException` |
| 127 | `JsonMapperTest` | `testUnknownTaskTypeIsSkipped` | Parsing JSON with unrecognized task type | Task skipped with warning, no crash |
| 128 | `JsonMapperTest` | `testAppDataNullSafety` | Parsing null or empty JSON strings | Returns empty dataset with default category |
| 129 | `JsonMapperTest` | `testSingleArgFromJson` | Single-argument `fromJson(String)` helper | Parses `AppData` successfully |
| 130 | `JsonMapperTest` | `testDefaultCategoryAddedIfMissing` | Deserializing data without default category | General category automatically added |
| 131 | `JsonMapperTest` | `testRoundTripPolymorphicTasksAndCategories` | Round-trip JSON mapping of mixed tasks | Deserialized data equals original data |
| 132 | `JsonStorageTest` | `testStorageExceptionAndGetFilePath` | `StorageException` constructors and path getter | Preserves file path and message |
| 133 | `JsonStorageTest` | `testSaveNullThrows` | Saving null `AppData` | Throws `NullPointerException` |
| 134 | `JsonStorageTest` | `testCorruptedFileIsQuarantined` | Loading malformed/corrupted file | Renames file with `.corrupt-` timestamp |
| 135 | `JsonStorageTest` | `testAtomicSaveLeavesNoTempFile` | Atomic file write operation | Temporary `.tmp` file deleted upon completion |
| 136 | `JsonStorageTest` | `testRoundTripBetweenInstances` | Save/load across separate storage instances | Data restored identically |
| 137 | `JsonStorageTest` | `testMissingFileReturnsDefaultData` | Loading nonexistent storage file | Returns default empty `AppData` |
| 138 | `JsonStorageTest` | `testUnknownTaskTypePopulatesWarning` | Loading file with unknown task type | Warning recorded, known tasks loaded |
| 139 | `JsonStorageTest` | `testEmptyFileReturnsDefaultData` | Loading empty (0-byte) file | Returns default empty `AppData` |
