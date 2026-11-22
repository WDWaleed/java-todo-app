# TODO List Desktop App

[![Java 17+](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://openjdk.org/)
[![JavaFX 21](https://img.shields.io/badge/JavaFX-21.0.2-orange.svg)](https://openjfx.io/)
[![Maven](https://img.shields.io/badge/Maven-3.9%2B-red.svg)](https://maven.apache.org/)
[![Version](https://img.shields.io/badge/Release-v1.0.0-green.svg)](pom.xml)
[![Tests](https://img.shields.io/badge/Tests-147%20passing-brightgreen.svg)](src/test/java)
[![Coverage](https://img.shields.io/badge/Coverage-%3E95%25%20line-success.svg)](target/site/jacoco)

A modern, offline-first desktop TODO list application built in Java 17 and JavaFX 21. Inspired by Google Keep and Microsoft To-Do, it features a card-based visual design, polymorphic task types, multi-criteria filtering, thread-safe debounced autosave with atomic file persistence, and full keyboard navigation.

Developed for the **Software Construction (Lab Open-Ended Lab)** course, demonstrating object-oriented principles (encapsulation, abstraction, inheritance, polymorphism), decoupled layered architecture, thorough automated testing, and a disciplined Git version control workflow.

---

## Table of Contents

- [Features](#features)
- [Screenshots & UI Walkthrough](#screenshots--ui-walkthrough)
- [Prerequisites](#prerequisites)
- [Building and Running](#building-and-running)
  - [Compile, Test and Verify](#compile-test-and-verify)
  - [Run with Maven JavaFX Plugin](#run-with-maven-javafx-plugin)
  - [Run Shaded Executable JAR](#run-shaded-executable-jar)
  - [Developer Sample Data Seed](#developer-sample-data-seed)
- [Data Storage & Reset](#data-storage--reset)
- [Keyboard Shortcuts](#keyboard-shortcuts)
- [Architecture & Design](#architecture--design)
- [Project Structure](#project-structure)
- [IDE Setup](#ide-setup)
- [Git Workflow](#git-workflow)
- [Documentation Index](#documentation-index)
- [License & Authors](#license--authors)

---

## Features

### 1. Polymorphic Task System
- **Simple Tasks:** Clean title, description, priority, category, and completion toggle.
- **Checklist Tasks:** Dynamic sub-items with individual completion checkboxes and live progress indicator (e.g. `2/5 completed (40%)`). Completing the parent marks all sub-items done; completing all sub-items marks the parent done.
- **Deadline Tasks:** Due-date integration using JavaFX `DatePicker`. Features dynamic status badges:
  - **Overdue** (red badge) if the deadline has passed.
  - **Due Soon** (amber badge) if due within 48 hours.
  - Formatted countdown / date string.

### 2. Organization & Category Management
- Preloaded with default categories: `General`, `Work`, `Personal`, `Shopping`.
- Create custom categories with custom hex color identifiers and unique case-insensitive names.
- Category sidebar displays real-time task count badges.
- **Safe deletion:** Deleting a category automatically and safely reassigns all member tasks to `General`, preventing orphaned tasks.

### 3. Prioritization & Pinning
- **Three Priority Levels:** High (red indicator stripe), Medium (amber stripe), Low (green stripe).
- Quick priority modification via right-click context menu directly on task cards.
- **Pin to Top:** Pin critical tasks so they remain anchored at the very top of the list, regardless of active sort or filter options.

### 4. Search, Filtering & Sorting
- **Instant Search:** Debounced real-time text query matching across task titles and descriptions.
- **Multi-Criteria Filtering:** Combine Category filter, Status filter (All vs Active), and Priority filter simultaneously.
- **Flexible Sorting:** Sort by Due Date (soonest first), Priority (High to Low), Title (A to Z), or Created Date (newest first). Pinned invariant is always preserved.

### 5. Data Safety & Usability
- **Atomic File Persistence:** State is serialized to JSON (`~/.todoapp/data.json`) via a temp-file write and atomic replace, preventing file corruption on crashes or power loss.
- **Corrupt File Recovery:** Unreadable data files are automatically isolated to a timestamped backup (`data.json.corrupt-<timestamp>`) while initializing a clean state.
- **Thread-Safe Debounced Autosave:** Non-blocking background persistence (300 ms debounce) on a daemon executor thread ensures zero UI stutter.
- **Undo Delete:** Accidentally deleted a task? Restore it instantly using the temporary Undo toast notification banner, the menu bar (`Edit ▸ Undo Delete`), or `Ctrl+Z`.
- **Window Geometry Persistence:** Remembers window width, height, position (X, Y), and maximized state across application restarts via `java.util.prefs.Preferences`.

---

## Screenshots & UI Walkthrough

| Screen / Feature | Description | File Reference |
|---|---|---|
| **Main Window** | Full dashboard view: category sidebar, top toolbar, card list view, and status bar | `docs/img/main-window.png` |
| **Simple Task Editor** | Modal dialog for creating and editing standard tasks | `docs/img/task-editor-simple.png` |
| **Checklist Task Editor** | Task editor with dynamic sub-item addition, removal, and checkbox list | `docs/img/task-editor-checklist.png` |
| **Deadline Task Editor** | Task editor with date picker and deadline configuration | `docs/img/task-editor-deadline.png` |
| **Category Management** | Category creation dialog with color picker and name validation | `docs/img/category-dialog.png` |
| **Search & Filter View** | Live search query active with filtered category chip and count | `docs/img/search-filter.png` |

> *Note for manual graders / evaluators:* If the pre-rendered image files are not present in your checkout, refer to [docs/test-plan.md](docs/test-plan.md) for step-by-step instructions on launching the application with `--seed` to observe all screens directly.

---

## Prerequisites

- **Java Development Kit (JDK):** Version 17 or higher (tested on OpenJDK 17, 21, and 25).
- **Apache Maven:** Version 3.9.0 or higher.
- **Git:** Version 2.30 or higher.
- **Display Environment:** A graphical desktop environment (Windows 10/11, macOS 12+, or Linux with X11/Wayland).

---

## Building and Running

### Compile, Test and Verify

Run the full Maven lifecycle verification (compilation, automated unit tests, JaCoCo code coverage verification at ≥80% line coverage):

```bash
mvn verify
```

To run only the automated test suite without packaging:

```bash
mvn test
```

### Run with Maven JavaFX Plugin

The recommended and portable way to run the application during development:

```bash
mvn javafx:run
```

To specify a custom data storage location:

```bash
mvn javafx:run -Djavafx.args="--data=my-custom-tasks.json"
```

### Run Shaded Executable JAR

A standalone fat JAR containing all dependencies (Gson, JavaFX controls) is produced by `maven-shade-plugin` during packaging:

```bash
# 1. Build the shaded JAR
mvn clean package -DskipTests

# 2. Run the JAR directly with Java
java -jar target/todo-app-1.0.0.jar
```

> **Platform Note on Shaded JARs:** The shaded JAR bundles native JavaFX binaries corresponding to the operating system on which `mvn package` was executed. To distribute for Windows, build on Windows; to distribute for macOS/Linux, build on macOS/Linux. Alternatively, running via `mvn javafx:run` automatically resolves native binaries for any host OS.

### Developer Sample Data Seed

To launch with a rich sample dataset (~12 preconfigured tasks spanning all task types, categories, priorities, deadlines, and completion states):

```bash
mvn javafx:run -Djavafx.args="--seed --data=todo-dev.json"
```

*Note: The `--seed` flag will only populate sample tasks if the specified data file is newly created or empty.*

---

## Data Storage & Reset

### Storage Location
- By default, the application persists all data to:
  - **Windows:** `C:\Users\<username>\.todoapp\data.json`
  - **Linux / macOS:** `~/.todoapp/data.json`
- You can override this location using the `--data=<path>` argument or by setting the system property `-Dtodoapp.data=<path>`.

### Resetting to Fresh State
To reset the application data completely:
1. Close the application.
2. Delete the file: `~/.todoapp/data.json`.
3. Restart the application. It will automatically re-create default categories (`General`, `Work`, `Personal`, `Shopping`) with an empty task repository.

---

## Keyboard Shortcuts

The application provides first-class keyboard navigation:

| Shortcut | Action | Scope / Context |
|---|---|---|
| <kbd>Ctrl</kbd> + <kbd>N</kbd> | Create New Task | Application-wide |
| <kbd>Ctrl</kbd> + <kbd>Z</kbd> | Undo Last Deleted Task | Application-wide (when undo is active) |
| <kbd>Ctrl</kbd> + <kbd>F</kbd> | Focus Search Field | Application-wide |
| <kbd>Enter</kbd> / <kbd>F2</kbd> | Edit Selected Task | Task List |
| <kbd>Delete</kbd> | Delete Selected Task (with confirmation) | Task List |
| <kbd>Space</kbd> | Toggle Completion of Selected Task | Task List |
| <kbd>Esc</kbd> | Clear Search / Dismiss Dialogs | Search Bar / Dialogs |

---

## Architecture & Design

The application follows a strictly layered, unidirectional architecture with a decoupled presentation tier:

```
┌─────────────────────────────────────────────────────────────┐
│  com.todoapp.ui                                             │  ← JavaFX controls, custom TaskCell, CSS
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│  com.todoapp.controller                                     │  ← MainController, ViewState, ViewListener
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│  com.todoapp.service                                        │  ← TaskService, CategoryService, TaskQuery
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│  com.todoapp.repository                                     │  ← In-memory repositories & queries
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│  com.todoapp.storage                                        │  ← Atomic JsonStorage, AutosaveListener
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│  com.todoapp.model                                          │  ← Task hierarchy, Category, Priority, TaskDraft
└─────────────────────────────────────────────────────────────┘
```

### Core Invariants
1. **Headless Domain Logic:** Packages `model`, `repository`, `service`, `storage`, and `controller` contain **zero** `javafx.*` imports, enabling ultra-fast headless JUnit tests (147 tests execute in < 2 seconds).
2. **Defensive Immutability:** Services return deep copies (`Task.copy()`) to prevent external callers from bypassing business validation rules.
3. **Thread Safety:** Heavy disk writes and debouncing occur on a dedicated background daemon thread, while UI updates strictly marshal back via `Platform.runLater`.

For the complete architectural design, class diagrams, and design pattern descriptions, see [docs/design.md](docs/design.md).

---

## Project Structure

```
todo-app/
├── pom.xml                                # Maven build definition with JaCoCo & Shade plugins
├── README.md                              # Main project documentation and quickstart
├── implementation.md                      # Milestone roadmap and progress tracker
├── .gitignore                             # Git ignore rules for build, IDE, and temp files
├── docs/
│   ├── requirements.md                    # Requirements specification (FR-01..17, NFR-01..07)
│   ├── design.md                          # Comprehensive software architecture & class diagrams
│   ├── test-plan.md                       # Test strategy, automated inventory & manual GUI script
│   ├── user-guide.md                      # End-user task-oriented operating manual
│   └── course-mapping.md                  # Academic mapping: tasks 1–5, CLO 2/4 & OOP concepts
└── src/
    ├── main/
    │   ├── java/com/todoapp/
    │   │   ├── Launcher.java              # Standard Java entry point (bypasses JavaFX modular check)
    │   │   ├── App.java                   # JavaFX Application bootstrap and wiring
    │   │   ├── AppConfig.java             # CLI argument & system properties parser
    │   │   ├── DevSeed.java               # Developer sample data generator
    │   │   ├── model/                     # Task hierarchy (Simple, Checklist, Deadline), Category, Draft
    │   │   ├── repository/                # TaskRepository, CategoryRepository interfaces & in-memory impl
    │   │   ├── service/                   # TaskService, CategoryService, TaskQuery, SortOption, Filters
    │   │   ├── storage/                   # JsonStorage, JsonMapper, AutosaveListener, DataSync
    │   │   ├── controller/                # MainController, ViewState, ViewListener
    │   │   └── ui/                        # MainView, ToolbarPane, CategoryPane, TaskListPane, TaskCell...
    │   └── resources/com/todoapp/ui/
    │       └── styles.css                 # Application visual styling and theme definition
    └── test/
        └── java/com/todoapp/              # 14 automated test suites (147 test cases)
```

---

## IDE Setup

### Visual Studio Code
1. Open VS Code and open the repository folder via **File ▸ Open Folder...**
2. Install the **Extension Pack for Java** (Microsoft).
3. The Java language server will automatically detect `pom.xml`.
4. Run tests directly from the **Testing** tab.
5. To run the application, open the integrated terminal and execute `mvn javafx:run`.

### IntelliJ IDEA
1. Select **File ▸ Open...** and choose the `pom.xml` file.
2. Select **Open as Project**.
3. IntelliJ will automatically resolve Maven dependencies and configure the project SDK (ensure JDK 17+ is selected in Project Structure).
4. Run tests by right-clicking `src/test/java` and selecting **Run 'All Tests'**.
5. Run the application via the Maven tool window (`Plugins ▸ javafx ▸ javafx:run`).

---

## Git Workflow

The project follows a disciplined version control protocol:
- **Branching Model:** Strictly linear development on `main`. No long-lived feature branches, pull requests, or merge commits.
- **Commit Format:** Strict adherence to [Conventional Commits v1.0.0](https://www.conventionalcommits.org/):
  - `feat(scope): ...` for new user-facing functionality
  - `fix(scope): ...` for bug fixes
  - `test(scope): ...` for automated testing additions or enhancements
  - `docs(scope): ...` for documentation updates
  - `refactor(scope): ...` for internal restructuring without behavior change
  - `build: ...` or `chore: ...` for build, dependencies, and packaging
- **Release Tags:** Annotated semantic version tags:
  - `v0.1.0` : Core Domain & Persistence (M07)
  - `v0.2.0` : Task CRUD in GUI (M09)
  - `v0.3.0` : Categories & Filtering in GUI (M10)
  - `v1.0.0` : Official Release (M13)

---

## Documentation Index

- **[docs/requirements.md](docs/requirements.md):** Functional (FR-01..17) and non-functional (NFR-01..07) requirements, use cases, and traceability matrix.
- **[docs/design.md](docs/design.md):** Software architecture, class diagram, design patterns, and package design.
- **[docs/user-guide.md](docs/user-guide.md):** User manual with step-by-step instructions for all features.
- **[docs/test-plan.md](docs/test-plan.md):** Testing methodology, automated test inventory, and manual GUI testing script.
- **[docs/course-mapping.md](docs/course-mapping.md):** Academic course learning outcomes (CLO 2 & CLO 4) and assignment tasks mapping.
- **[implementation.md](implementation.md):** Milestone roadmap and execution log.

---

## License & Authors

- **Course:** Software Construction (5th Semester) — Open-Ended Lab (OEL)
- **Toolkit:** Java 17+, JavaFX 21, Maven 3.9+, JUnit 5, Google Gson
- **License:** Educational / Academic Use
