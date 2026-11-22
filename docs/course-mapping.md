# Course Outcomes & Assignment Mapping

> **Course:** Software Construction (5th Semester) — Open-Ended Lab (OEL)  
> **Project:** Modern Desktop TODO List Application (Java 17 / JavaFX 21 / Maven / JUnit 5)  
> **Course Learning Outcomes (CLOs):**
> - **CLO 2:** Design and implement software solutions using object-oriented principles, design patterns, and clean architecture.
> - **CLO 4:** Apply testing, quality assurance, and version control tools in modern software construction.

---

## 1. Assignment Tasks Matrix (Tasks 1–5)

| Assignment Task | Focus Area | Deliverables & Artifacts | Relevant Code & Docs | Git Commit & Tag Evidence | CLO Alignment |
|---|---|---|---|---|---|
| **Task 1: Requirements Analysis** | Problem formulation, user personas, functional (FR-01..17) and non-functional (NFR-01..07) requirements, use cases, acceptance criteria, traceability | `docs/requirements.md` | Functional & NFR specifications, 14 scenarios, Use Cases UC-01..08, Bi-directional Traceability Matrix | `de46c5a`, `3c058d1`, `456426c`, `b3117f3`, `1fea83d` (M01) | CLO 2, CLO 4 |
| **Task 2: Architecture & System Design** | Layered software architecture, package decoupling, UML class diagrams, sequence diagrams, data structure analysis, design patterns | `docs/design.md` | MVC architecture, class diagrams, 4 sequence diagrams, design decisions log | `e5b158d`, `0819969`, `70053cd`, `1ca5fae` (M02, M13) | CLO 2 |
| **Task 3: Object-Oriented Implementation** | Core domain hierarchy, repositories, business services, GUI components, atomic JSON persistence, debounced autosave | `src/main/java/com/todoapp/` | `model/`, `repository/`, `service/`, `storage/`, `controller/`, `ui/`, `App.java`, `Launcher.java` | Commits `5d0077b` through `357e8c7`<br>Tags: `v0.1.0`, `v0.2.0`, `v0.3.0`, `v1.0.0` | CLO 2 |
| **Task 4: Testing & Quality Assurance** | Headless unit testing, edge case & stress testing, performance evaluation, manual GUI test script, JaCoCo coverage (≥80%) | `src/test/java/`, `docs/test-plan.md`, `target/site/jacoco/` | 14 test suites, 147 test cases, 12 manual test scenarios (TS-01..12), JaCoCo threshold rule | `be123c9`, `8d09e20`, `e9503e6`, `40a68c9`, `b7d85ea`, `7aab15c` (M12) | CLO 4 |
| **Task 5: Version Control & Project Management** | Single-branch linear Git workflow, Conventional Commits specification, semantic version tags, milestone planning & execution | Repository Git history, `implementation.md` | Linear log, zero merge commits, milestone progress tracking table | `8182104` to release<br>Tags: `v0.1.0`, `v0.2.0`, `v0.3.0`, `v1.0.0` | CLO 4 |

---

## 2. Course Learning Outcomes (CLO) Evidence

### CLO 2: Design and Implement Object-Oriented Solutions
- **Layered Architecture:** Clear unidirectional separation between `ui`, `controller`, `service`, `repository`, `storage`, and `model`. Non-UI core packages have zero imports from `javafx.*`, enabling 100% headless testing.
- **Polymorphism & Abstraction:** Concrete task variants (`SimpleTask`, `ChecklistTask`, `DeadlineTask`) extend abstract `Task` and provide specialized dynamic implementations for `getType()`, `getSummary()`, `getProgress()`, `getDueDate()`, `isOverdue()`, and `copy()`.
- **Encapsulation & Defensive Immutability:** Fields are private and immutable where possible; domain entities returned by `TaskService` are deep-cloned via `Task.copy()` to prevent direct state corruption by UI components.
- **Design Patterns Applied:**
  - *Factory Pattern:* `TaskFactory.java` centralized instantiation and draft application.
  - *Observer Pattern:* `TaskChangeListener.java` for domain events and `ViewListener.java` for UI updates.
  - *Model-View-Controller (MVC):* Decouples JavaFX presentation (`MainView`) from controller orchestration (`MainController`) and business logic (`TaskService`).
  - *Data Mapper:* `JsonMapper.java` abstracts Gson JSON serialization and deserialization with type discriminator handling.
  - *Strategy / Functional Comparators:* Dynamic comparator composition in `TaskService.query` and `SortOption`.

### CLO 4: Apply Testing, Quality Assurance, and Version Control
- **Automated Testing Suite:** 14 test classes containing 147 tests covering edge cases, null checks, boundary conditions, query sorting invariants, persistence round-trips, and corruption recovery.
- **Strict Code Coverage Enforcement:** JaCoCo plugin configured in `pom.xml` enforcing a minimum threshold of ≥ 80% line coverage on non-UI packages (`model`, `repository`, `service`, `storage`, `controller`). Actual project line coverage exceeds 95%.
- **Static Quality & Linting:** Strict compiler flags (`<compilerArgs><arg>-Xlint:all</arg></compilerArgs>`) enabled; zero warnings, zero dead code, and clean Javadoc across all public APIs.
- **Disciplined Git Version Control:**
  - 100% linear Git history directly on `main` without feature branches or merge commits.
  - Commit messages strictly adhere to the [Conventional Commits v1.0.0](https://www.conventionalcommits.org/) format (`feat:`, `fix:`, `test:`, `docs:`, `build:`, `refactor:`, `style:`, `chore:`).
  - Semantic release tags (`v0.1.0`, `v0.2.0`, `v0.3.0`, `v1.0.0`) mark key deliverable milestones.

---

## 3. Object-Oriented Programming (OOP) Concepts & Real File Mapping

| OOP Concept | Implementing Classes & File Paths | Key Methods / Declarations | Concrete Implementation Details |
|---|---|---|---|
| **Encapsulation** | `src/main/java/com/todoapp/model/Task.java`<br>`src/main/java/com/todoapp/service/TaskService.java`<br>`src/main/java/com/todoapp/model/TaskDraft.java` | `Task.copy()`<br>`TaskDraft.validate()`<br>`TaskService.get(UUID)` | All domain entity fields (`id`, `title`, `priority`, etc.) are private. Validation constraints (1–100 char title, non-null fields) are strictly enforced in `TaskDraft.validate()`. `TaskService` returns deep copies via `copy()`, preventing callers from mutating internal repository state directly. |
| **Inheritance** | `src/main/java/com/todoapp/model/Task.java`<br>`src/main/java/com/todoapp/model/SimpleTask.java`<br>`src/main/java/com/todoapp/model/ChecklistTask.java`<br>`src/main/java/com/todoapp/model/DeadlineTask.java` | `public abstract class Task`<br>`public class SimpleTask extends Task`<br>`public class ChecklistTask extends Task`<br>`public class DeadlineTask extends Task` | `Task` provides common entity fields (`id`, `title`, `description`, `priority`, `categoryId`, `completed`, `pinned`, `createdAt`, `updatedAt`) and concrete methods (`touch()`, `toggleCompleted()`). Specific task classes inherit this base state while extending specialized behaviors. |
| **Polymorphism** | `src/main/java/com/todoapp/model/Task.java`<br>`src/main/java/com/todoapp/ui/TaskCell.java`<br>`src/main/java/com/todoapp/storage/JsonMapper.java` | `Task.getType()`<br>`Task.getSummary()`<br>`Task.getProgress()`<br>`Task.isOverdue(LocalDate)`<br>`TaskCell.updateItem(Task, boolean)` | Abstract methods in `Task` are overridden polymorphically. `TaskCell` renders appropriate sub-views (checklist progress bars, deadline due-soon badges) using `instanceof` and polymorphic dispatch without changing caller contracts. `JsonMapper` serializes/deserializes based on polymorphic `"type"` tags. |
| **Abstraction** | `src/main/java/com/todoapp/repository/TaskRepository.java`<br>`src/main/java/com/todoapp/repository/CategoryRepository.java`<br>`src/main/java/com/todoapp/storage/Storage.java`<br>`src/main/java/com/todoapp/service/TaskChangeListener.java` | `TaskRepository.save()`<br>`TaskRepository.findById()`<br>`Storage.load()`<br>`Storage.save()` | Service and controller layers interact solely with abstract interfaces (`TaskRepository`, `Storage`), completely decoupling domain logic from physical storage mechanisms (in-memory maps, JSON files, future SQL databases). |
| **Interface Segregation** | `src/main/java/com/todoapp/service/TaskChangeListener.java`<br>`src/main/java/com/todoapp/controller/ViewListener.java` | `onTasksChanged(ChangeType, UUID)`<br>`onViewChanged(List<Task>, ...)` | Small, focused listener interfaces allow observers (such as `AutosaveListener` and `MainView`) to subscribe only to relevant event streams without coupling to unrelated operations. |

---

## 4. Milestone Execution & Git History Excerpt

Below is an excerpt from the repository's linear Git log illustrating milestone progression and Conventional Commit adherence:

```text
* 1ca5fae docs: update design document to match final implementation
* 1ea0bf9 docs: add user guide
* 717e053 docs: complete README with build, run and usage instructions
* 357e8c7 build: add shade plugin and release version 1.0.0
* 7aab15c docs: mark M12 complete
* b7d85ea docs(test-plan): add test strategy and manual GUI test script
* 40a68c9 refactor: remove unused code and enable lint cleanup
* e9503e6 fix(service): propagate clock timestamp during task creation
* 8d09e20 test: add edge case and integration tests
* be123c9 build: enforce coverage threshold with jacoco
* 2383053 docs: mark M11 complete
* f4ececd fix(ui): import KeyCode in ToolbarPane
* e92ef75 feat(ui): remember window size and position
* 4e6f13c style(ui): polish visuals, tooltips and due-soon highlighting
* a9edcd1 feat(ui): add keyboard shortcuts
* fa99a74 feat(ui): add menu bar and about dialog
* 794f99c feat(ui): add undo for deleted tasks
* 47cd21f docs: mark M10 complete (tag: v0.3.0)
* f88cc31 test(controller): cover category and filter state handling
* 2b9f1a6 feat(ui): add pin toggle and pinned styling
* 012a0a4 feat(ui): complete toolbar with search, sort and filters
* 46f6009 feat(controller): add category handlers
* c504c26 feat(ui): add category dialog and management controls
* 180e25e chore: ignore todo runtime json files
* bad88ff fix(ui): resolve layout priority collision and get return type in TaskCell
* 9dfdc0c docs: mark M09 complete (tag: v0.2.0)
* 0e25152 test(controller): cover CRUD handlers and error paths
* 3564dd2 feat(ui): add delete confirmation, quick priority change and context menu
* 3d2b76b feat(ui): support editing tasks and checklist items
* 5d34a4a feat(ui): add task editor dialog for creating tasks
* ba8b6a0 feat(controller): add create, update, delete and priority handlers
* 754ff5f feat(ui): add polymorphic task card cell
* dc942c3 docs: mark M08 complete
* 06b3b03 feat: add optional sample data seeding for development
* 03dfb4c feat: wire application bootstrap with persistence and autosave
* 929c4b4 feat(ui): add main view with toolbar, category pane, task list and status bar
* 54cea55 feat(ui): add theme constants and stylesheet
* aec8566 test(controller): cover controller query handling
* 24b46d0 feat(controller): add MainController with view state and listeners
* 911faa4 docs: mark M07 complete (tag: v0.1.0)
* 72972ca test(storage): cover round-trips, corruption and autosave
* fbefe56 feat(storage): add repository sync helper and debounced autosave listener
* 536f9a1 feat(storage): add JsonStorage with atomic save and corrupt file handling
* 511341e feat(storage): add AppData and JSON mapper with type discriminator
* 31d7af5 docs: mark M06 complete
* 0a06a0d test(service): cover query engine including ordering invariants
* 09a1c8a feat(service): add task count helpers for status bar
* 446f4ce feat(service): implement filtering, searching and sorting in TaskService
* f3e8370 feat(service): add TaskQuery, StatusFilter and SortOption
* 3842790 docs: mark M05 complete
* 6fe6f5a test(service): cover TaskService behavior and error cases
* c1cc7b7 feat(service): notify listeners on task changes
* 499ea00 feat(service): add delete with undo buffer and complete/priority/pin operations
* 642fc16 feat(service): add TaskService create, read and update
* 17e3627 docs: mark M04 complete
* d15ce0d test: cover repositories and CategoryService
* 730d9d6 feat(service): add CategoryService with validation rules
* 985e11f feat(service): add exceptions and change listener interfaces
* d8ac7a1 feat(repository): add repository interfaces and in-memory implementations
* 2c9b1c3 docs: mark M03 complete
* 33bf43f test(model): cover validation, polymorphism and copy semantics
* c92b600 feat(model): add TaskDraft and TaskFactory
* 1d036b1 feat(model): add DeadlineTask and ChecklistTask
* 4ca5a81 feat(model): add abstract Task and SimpleTask
* 5d0077b feat(model): add Priority, TaskType, ChecklistItem and Category
* 9aab8a3 docs: mark M02 complete
* 70053cd docs(design): add storage schema, wireframes and design decisions
* 0819969 docs(design): add sequence diagrams and checklist state rules
* e5b158d docs(design): add architecture overview and class diagram
* 1fea83d docs: mark M01 complete
* b3117f3 docs(requirements): add traceability matrix
* 456426c docs(requirements): add use cases and acceptance criteria
* 3c058d1 docs(requirements): add functional and non-functional requirements
* de46c5a docs(requirements): add scope, personas and user stories
* 99a9cc2 docs: mark M00 complete
* d4fcf08 docs: add README skeleton and package documentation
* fcd5a6d feat: add JavaFX application entry point with empty window
* ada2eb3 build: add maven project with javafx, gson and junit
* 8182104 chore: initialize repository with gitignore and editorconfig
```
