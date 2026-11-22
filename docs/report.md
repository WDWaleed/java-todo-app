# Software Construction — Open-Ended Lab (OEL)
# Final Project Report: TODO List Desktop Application

---

## Metadata & Title Page

- **Project Title:** Modern Offline-First TODO List Desktop Application
- **Course Title:** Software Construction (CS-311 / 5th Semester)
- **Academic Term:** Fall 2026
- **Release Version:** v1.0.0
- **Release Tag:** `v1.0.0` (Git commit `6c41303`)
- **Date of Submission:** November 22, 2026
- **Author / Student Name:** [TO BE FILLED BY AUTHOR]
- **Roll Number / Student ID:** [TO BE FILLED BY AUTHOR]
- **Laboratory Instructor:** [TO BE FILLED BY AUTHOR]
- **Repository Path:** `d:\University\5th Semester\Software Construction\Lab\OEL`

---

## Screenshots Checklist (For Manual Author Insertion)

Below is the definitive inventory of screenshots required for the physical submission. When capturing screenshots, save them to the `docs/img/` directory matching these exact filenames:

| Item | Target Filename | What the Screenshot Must Show | Status / Action Needed |
|:---:|---|---|:---:|
| 1 | `docs/img/01-main-window.png` | Main application dashboard displaying top toolbar, left category sidebar with counts, task card list with sample tasks, and status bar. | `[SCREENSHOT NEEDED: Capture main window with sample tasks loaded]` |
| 2 | `docs/img/02-task-simple.png` | "New Task" dialog with `Simple` type selected, title filled, description entered, High priority, and category assigned. | `[SCREENSHOT NEEDED: Capture Simple Task creation dialog]` |
| 3 | `docs/img/03-task-checklist.png` | Task Editor with `Checklist` type selected, showing dynamic sub-item inputs, progress bar, and several checked/unchecked items. | `[SCREENSHOT NEEDED: Capture Checklist Task creation dialog]` |
| 4 | `docs/img/04-task-deadline.png` | Task Editor with `Deadline` type selected, demonstrating the JavaFX DatePicker calendar dropdown. | `[SCREENSHOT NEEDED: Capture Deadline Task dialog with DatePicker open]` |
| 5 | `docs/img/05-card-highlights.png` | Task list cards displaying visual indicators: red High priority stripe, amber Medium stripe, green Low stripe, gold Pin badge, red Overdue badge, and amber Due-Soon badge. | `[SCREENSHOT NEEDED: Capture task cards showing priority stripes, pin badge & overdue tags]` |
| 6 | `docs/img/06-category-dialog.png` | Category Management dialog showing name input field and color selection palette. | `[SCREENSHOT NEEDED: Capture Category Creation modal dialog]` |
| 7 | `docs/img/07-search-filter.png` | Main window showing an active search query in the toolbar search box, category filter applied, and filtered card results. | `[SCREENSHOT NEEDED: Capture search/filter in action with matching cards]` |
| 8 | `docs/img/08-undo-toast.png` | Bottom toast notification banner displaying `"Task '...' deleted. [Undo]"` and the menu item `Edit ▸ Undo Delete`. | `[SCREENSHOT NEEDED: Capture delete confirmation and undo toast notification banner]` |
| 9 | `docs/img/09-about-dialog.png` | "About TODO App" dialog showing version `1.0.0`, toolkit details, and authorship information (`Help ▸ About`). | `[SCREENSHOT NEEDED: Capture About dialog]` |
| 10 | `docs/img/10-shortcuts-dialog.png` | Keyboard Shortcuts reference dialog (`Help ▸ Keyboard Shortcuts`). | `[SCREENSHOT NEEDED: Capture Keyboard Shortcuts help dialog]` |

---

## Table of Contents

1. [Abstract](#1-abstract)
2. [Introduction](#2-introduction)
   - 2.1 [Background & Motivation](#21-background--motivation)
   - 2.2 [Project Objectives & Scope](#22-project-objectives--scope)
   - 2.3 [Technology Stack & Environment](#23-technology-stack--environment)
   - 2.4 [Course Outcomes & Assignment Mapping](#24-course-outcomes--assignment-mapping)
3. [Requirements Analysis](#3-requirements-analysis)
   - 3.1 [User Personas & Scenarios](#31-user-personas--scenarios)
   - 3.2 [Functional Requirements (FR-01 to FR-17)](#32-functional-requirements-fr-01-to-fr-17)
   - 3.3 [Non-Functional Requirements (NFR-01 to NFR-07)](#33-non-functional-requirements-nfr-01-to-nfr-07)
   - 3.4 [Core Use Cases & Interaction Flows](#34-core-use-cases--interaction-flows)
   - 3.5 [Scope Boundaries & Assumptions](#35-scope-boundaries--assumptions)
4. [System Architecture & Detailed Design](#4-system-architecture--detailed-design)
   - 4.1 [Architectural Overview & Layer Decoupling](#41-architectural-overview--layer-decoupling)
   - 4.2 [Package Decomposition](#42-package-decomposition)
   - 4.3 [Comprehensive Class Design](#43-comprehensive-class-design)
   - 4.4 [Data Structures & Algorithmic Rationale](#44-data-structures--algorithmic-rationale)
   - 4.5 [Persistent Storage Schema & Atomic I/O](#45-persistent-storage-schema--atomic-io)
   - 4.6 [Dynamic Behavioral Workflows (Sequence Diagrams)](#46-dynamic-behavioral-workflows-sequence-diagrams)
   - 4.7 [User Interface Layout & Wireframes](#47-user-interface-layout--wireframes)
   - 4.8 [Object-Oriented Design Patterns Applied](#48-object-oriented-design-patterns-applied)
   - 4.9 [Design Decisions & Trade-Off Analysis](#49-design-decisions--trade-off-analysis)
5. [Implementation Narrative & Code Highlights](#5-implementation-narrative--code-highlights)
6. [Object-Oriented Programming (OOP) Concepts Demonstrated](#6-object-oriented-programming-oop-concepts-demonstrated)
7. [Graphical User Interface (GUI) Walkthrough](#7-graphical-user-interface-gui-walkthrough)
8. [Testing & Quality Assurance](#8-testing--quality-assurance)
9. [Version Control with Git](#9-version-control-with-git)
10. [Evaluation Against Requirements](#10-evaluation-against-requirements)
11. [Limitations, Future Work & Conclusion](#11-limitations-future-work--conclusion)
12. [References](#12-references)
13. [Appendices](#13-appendices)

---

## 1. Abstract

This report documents the end-to-end engineering, architectural design, implementation, and quality verification of the **TODO List Desktop Application (v1.0.0)**, developed for the Software Construction Lab Open-Ended Lab (OEL). The system is a standalone, offline-first personal productivity desktop utility written in modern Java 17 and JavaFX 21, following strict object-oriented design principles, layered separation of concerns, and an automated continuous test pipeline. The application features a card-based user experience inspired by Google Keep and Microsoft To-Do, supporting polymorphic task types (Simple tasks, Checklist tasks with sub-item progress tracking, and Deadline tasks with due-date alerts), custom categories with safe reassignment, multi-criteria live filtering and sorting with a pinned-to-top invariant, and atomic JSON persistence backed by thread-safe debounced autosave. The codebase contains 54 production Java source files (6,131 lines of code) and 14 automated JUnit test suites (2,928 lines of code). Automated verification enforces a strict JaCoCo threshold of ≥80% line coverage across all non-UI packages, achieving an actual coverage of **96.63%** across 147 test cases with zero defects. The development followed a disciplined, strictly linear Git workflow across 81 Conventional Commits, culminating in the tagged `v1.0.0` release.

---

## 2. Introduction

### 2.1 Background & Motivation
Task management applications are fundamental personal computing utilities. While web and cloud-based task management solutions are prevalent, they frequently present usability drawbacks for local power users: reliance on active network connectivity, sluggish load times, vendor lock-in, and aggressive subscription models. Desktop users require a lightweight, responsive, locally resilient tool that opens instantly, saves continuously without manual intervention, and respects user data privacy through transparent local storage formats. 

From an educational perspective, developing a comprehensive desktop task management system provides an exemplary vehicle for synthesizing key concepts in software construction: domain modeling, polymorphism, interface segregation, GUI event handling, asynchronous background concurrency, fault-tolerant file persistence, unit testing, and rigorous version control.

### 2.2 Project Objectives & Scope
The primary objectives of this project were:
1. **Architectural Decoupling:** Establish a strict layered architecture (UI $\rightarrow$ Controller $\rightarrow$ Service $\rightarrow$ Repository $\rightarrow$ Storage $\rightarrow$ Model) wherein the core domain, services, and persistence layers possess zero dependencies on GUI frameworks (`javafx.*`), enabling rapid headless unit testing.
2. **Polymorphic Domain Modeling:** Implement a robust task hierarchy supporting standard to-dos, multi-item checklists with calculated completion metrics, and calendar deadlines with automated overdue detection.
3. **Ergonomic & Resilient User Interface:** Construct an aesthetically pleasing, responsive JavaFX desktop interface featuring card layouts, priority indicators, category sidebars, keyboard shortcuts, inline input validation, and undo functionality.
4. **Data Integrity & Crash Resilience:** Deliver a thread-safe persistence engine utilizing atomic temporary-file writes, automated corruption quarantine, and non-blocking debounced autosave.
5. **Quality Assurance & Traceability:** Achieve $\ge 80\%$ automated line coverage, execute comprehensive edge-case testing, and maintain full bi-directional traceability from requirements to code and test cases.

### 2.3 Technology Stack & Environment
The software was engineered, built, and verified using the following industrial-standard toolchain:
- **Programming Language:** Java Development Kit (OpenJDK 17 LTS / OpenJDK 21 LTS / OpenJDK 25)
- **Graphical Toolkit:** JavaFX (OpenJFX) 21.0.2 (Controls, Graphics, Base)
- **Build & Dependency Management:** Apache Maven 3.9.9
- **Data Serialization:** Google Gson 2.10.1 (Type discriminator mapping)
- **Automated Testing:** JUnit Jupiter 5.10.2 (JUnit 5 Platform)
- **Code Coverage:** JaCoCo Maven Plugin 0.8.11 (Threshold enforcement at package/bundle levels)
- **Version Control:** Git 2.30+ (Single-branch linear workflow on `main`)
- **Documentation & Publishing:** Pandoc 3.8.3, Markdown, KaTeX, Mermaid Diagrams

### 2.4 Course Outcomes & Assignment Mapping
This project demonstrates the student's mastery of the course objectives specified in the Software Construction syllabus:
- **CLO 2 (Design & Implementation):** Evidenced by Chapters 4, 5, 6, and 7 through detailed UML modeling, design pattern adoption (MVC, Observer, Factory, Data Mapper), and clean object-oriented implementation.
- **CLO 4 (Testing & Quality Assurance):** Evidenced by Chapters 8, 9, and 10 through 147 headless unit tests, JaCoCo code coverage verification (96.63%), static linting (`-Xlint:all`), and a structured Git repository.

The mapping between assignment tasks and report chapters is summarized below:

| Assignment Task | Focus Area | Report Chapter |
|---|---|---|
| **Task 1** | Requirements Gathering, Analysis & Specification | [Chapter 3: Requirements Analysis](#3-requirements-analysis) |
| **Task 2** | Architecture, Detailed Design & System Modeling | [Chapter 4: System Architecture & Detailed Design](#4-system-architecture--detailed-design) |
| **Task 3** | Object-Oriented Implementation & GUI Development | [Chapter 5](#5-implementation-narrative--code-highlights), [Chapter 6](#6-object-oriented-programming-oop-concepts-demonstrated) & [Chapter 7](#7-graphical-user-interface-gui-walkthrough) |
| **Task 4** | Automated Testing, Edge Cases & Verification | [Chapter 8: Testing & Quality Assurance](#8-testing--quality-assurance) |
| **Task 5** | Version Control, Git Workflow & Lifecycle Tracking | [Chapter 9: Version Control with Git](#9-version-control-with-git) |

---

## 3. Requirements Analysis

### 3.1 User Personas & Scenarios
Requirements analysis identified two core primary personas:
1. **Alex (University Student):** Manages conflicting lab deadlines, homework assignments, and club responsibilities. Requires instant task creation, visual deadline highlighting (due soon / overdue), checklist breakdowns for multifaceted projects, and high reliability so notes are never lost.
2. **Priya (Working Professional):** Needs to organize tasks by context (Work, Personal, Finance), rapidly triage priorities via keyboard shortcuts, pin critical action items to the top of the queue, and filter out noise by hiding completed items.

### 3.2 Functional Requirements (FR-01 to FR-17)
The system satisfies 17 functional requirements categorized using the MoSCoW prioritization scheme:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                       MoSCoW REQUIREMENTS SUMMARY                           │
├─────────────────────────────────────────────────────────────────────────────┤
│ MUST HAVE (Core Essentials):                                                │
│ • FR-01: Create Task (Title, description, priority, category)               │
│ • FR-02: Read / View Task Details (Summary cards, inspector dialog)         │
│ • FR-03: Update Task (In-place modification of task fields)                 │
│ • FR-04: Delete Task (Explicit removal with confirmation dialog)            │
│ • FR-05: Mark Complete / Incomplete (One-click toggle, visual strikethrough)│
│ • FR-06: Prioritize Task (High, Medium, Low with color-coded stripes)       │
│ • FR-07: Sort Tasks (Due date, priority, title, created date; pin invariant)│
│ • FR-08: Categories (Add custom categories, color badges, safe reassignment)│
│ • FR-09: Filter Tasks (Category filter, status filter, priority filter)     │
│ • FR-10: Search Tasks (Instant debounced search across title & description) │
│ • FR-13: Persistence (Local JSON storage with auto-load and atomic save)    │
│ • FR-17: Validation (Title 1-100 chars, non-blank, date rules, unique names)│
├─────────────────────────────────────────────────────────────────────────────┤
│ SHOULD HAVE (Key Differentiators):                                          │
│ • FR-11: Checklist Tasks (Sub-items with individual completion checkboxes)  │
│ • FR-12: Deadline Tasks (Due date picker, overdue and due-soon badges)      │
│ • FR-14: Pin Tasks (Anchor critical items to top regardless of active sort) │
│ • FR-15: Undo Delete (Single-step restoration via toast notification banner)│
│ • FR-16: Keyboard Shortcuts (Ctrl+N, Ctrl+F, Ctrl+Z, Space, Delete, Enter)  │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 3.3 Non-Functional Requirements (NFR-01 to NFR-07)
1. **NFR-01: Usability & Ergonomics:** Modern, card-based interface styled via CSS. High visual hierarchy with color stripes for priority, clear contrast ratios, and full keyboard operability. Remembers window dimensions and position across restarts.
2. **NFR-02: Performance & Responsiveness:** UI remains 100% responsive under heavy workloads. Filtering, searching, and sorting 1,000 tasks executes in $< 100\text{ ms}$ (measured at **51 ms**).
3. **NFR-03: Reliability & Data Safety:** Zero data loss upon abrupt termination. All disk writes utilize atomic write-and-replace mechanics (`data.json.tmp` $\rightarrow$ `data.json`). Corrupt files are automatically quarantined to timestamped backups (`data.json.corrupt-<timestamp>`).
4. **NFR-04: Portability:** Cross-platform execution across 64-bit Windows, macOS, and Linux without platform-specific code. Shaded fat JAR and portable `mvn javafx:run` workflows supported.
5. **NFR-05: Maintainability & Code Cleanliness:** Strict Javadoc on all public APIs; zero warnings under `-Xlint:all`; decoupled package design with modular responsibilities.
6. **NFR-06: Testability:** Non-UI packages (`model`, `repository`, `service`, `storage`, `controller`) have zero JavaFX dependencies, enabling fast headless execution in standard JUnit test runners. Automated suite enforces $\ge 80\%$ line coverage.
7. **NFR-07: Threading & Concurrency Safety:** JavaFX Scene Graph mutations occur strictly on the JavaFX Application Thread. Disk I/O and debouncing run asynchronously off-thread via a single-threaded daemon executor. Background errors report back to the UI via `Platform.runLater`.

### 3.4 Core Use Cases & Interaction Flows
The system defines 8 detailed use cases specifying actor actions, system responses, and error flows:
- **UC-01 (Create Task):** User triggers dialog via `Ctrl+N` $\rightarrow$ enters fields $\rightarrow$ validation checks $\rightarrow$ entity constructed $\rightarrow$ repository updated $\rightarrow$ autosave scheduled $\rightarrow$ view refreshed.
- **UC-02 (View Task Details):** User double-clicks card $\rightarrow$ pre-filled modal inspector opened.
- **UC-03 (Edit Task):** User modifies attributes in place $\rightarrow$ `TaskFactory.apply` updates entity $\rightarrow$ view updates.
- **UC-04 (Delete Task with Undo):** User triggers delete $\rightarrow$ confirmation prompt $\rightarrow$ entity moved to single-slot undo buffer $\rightarrow$ toast banner displayed $\rightarrow$ user presses `Ctrl+Z` $\rightarrow$ entity restored.
- **UC-05 (Manage Categories):** User creates category with unique name and color $\rightarrow$ deletion reassigns child tasks to default `General` category.
- **UC-06 (Filter & Search):** User types query into search bar $\rightarrow$ debounced query engine filters matching items while respecting category and status constraints.
- **UC-07 (Sort with Pin Invariant):** Tasks sorted by selected criteria; pinned cards remain anchored at top.
- **UC-08 (Persistence & Recovery):** Autosave listener serializes state to disk; unreadable files are quarantined safely.

### 3.5 Scope Boundaries & Assumptions
- **In-Scope:** Single-user local desktop application, offline execution, polymorphic tasks, JSON persistence, debounced autosave, undo, search/sort, keyboard navigation.
- **Explicitly Out-of-Scope:** Multi-user concurrent networking, remote database synchronization, cloud authentication, recurring task engines, mobile native packaging.

---

## 4. System Architecture & Detailed Design

### 4.1 Architectural Overview & Layer Decoupling
The system adheres to a strict, layered architectural model where dependencies flow strictly downward. To maximize automated testability, the presentation layer (`com.todoapp.ui`) is completely decoupled from the underlying domain model, service logic, and storage mechanisms.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              PRESENTATION TIER                              │
│  com.todoapp.ui (MainView, TaskListPane, TaskCell, AppMenuBar, Dialogs)     │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ Calls actions / Listens to state
┌──────────────────────────────────────▼──────────────────────────────────────┐
│                              CONTROLLER TIER                                │
│  com.todoapp.controller (MainController, ViewState, ViewListener)           │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ Dispatches commands
┌──────────────────────────────────────▼──────────────────────────────────────┐
│                                SERVICE TIER                                 │
│  com.todoapp.service (TaskService, CategoryService, TaskQuery, SortOption)  │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ Queries & mutates entities
┌──────────────────────────────────────▼──────────────────────────────────────┐
│                              REPOSITORY TIER                                │
│  com.todoapp.repository (TaskRepository, CategoryRepository, In-Memory)     │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ Loaded from / Saved to
┌──────────────────────────────────────▼──────────────────────────────────────┐
│                               STORAGE TIER                                  │
│  com.todoapp.storage (Storage, JsonStorage, JsonMapper, AutosaveListener)   │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ Operates upon
┌──────────────────────────────────────▼──────────────────────────────────────┐
│                                DOMAIN MODEL                                 │
│  com.todoapp.model (Task hierarchy, Category, Priority, TaskDraft, Factory) │
└─────────────────────────────────────────────────────────────────────────────┘
```

#### Architectural Invariants
1. **Headless Domain Core:** Under no circumstances may any class inside `model`, `repository`, `service`, `storage`, or `controller` import any class from `javafx.*`. This rule ensures all business logic executes in standard, headless JUnit runners within milliseconds.
2. **Defensive Immutability Boundary:** To protect entity encapsulation, `TaskService` returns deep copies of `Task` objects to external callers via `Task.copy()`. External consumers cannot mutate stored repository references directly; updates must be submitted via validated `TaskDraft` records.
3. **Thread Confinement:** All JavaFX Scene Graph mutations, dialog displays, and CSS updates occur exclusively on the JavaFX Application Thread. Disk I/O executes asynchronously on a background daemon executor thread, marshaling completion/error callbacks back via `Platform.runLater`.

### 4.2 Package Decomposition
The application is structured into 6 focused packages under `com.todoapp`:

```
com.todoapp/
├── App.java                     # JavaFX bootstrap, dependency injection & lifecycle
├── AppConfig.java               # Immutable CLI & system properties record parser
├── DevSeed.java                 # Sample data generator for development testing
├── Launcher.java                # Non-Application launcher for shaded JAR compatibility
│
├── model/                       # Domain entities, records, and factories
│   ├── Task.java                # Abstract root entity with core attributes
│   ├── SimpleTask.java          # Standard to-do task implementation
│   ├── ChecklistTask.java       # Multi-item task with progress ratio calculation
│   ├── ChecklistItem.java       # Individual checklist sub-item entity
│   ├── DeadlineTask.java        # Calendar deadline task with overdue logic
│   ├── Category.java            # Categorization entity with hex color codes
│   ├── Priority.java            # Enumeration: HIGH (Red), MEDIUM (Amber), LOW (Green)
│   ├── TaskType.java            # Enumeration: SIMPLE, CHECKLIST, DEADLINE
│   ├── TaskDraft.java           # Immutable value record for creation & update inputs
│   └── TaskFactory.java         # Centralized instantiation and draft application factory
│
├── repository/                  # Storage-agnostic repository interfaces & implementations
│   ├── TaskRepository.java      # Task CRUD & query abstraction
│   ├── CategoryRepository.java  # Category CRUD & name lookup abstraction
│   ├── InMemoryTaskRepository.java     # LinkedHashMap-backed task repository
│   └── InMemoryCategoryRepository.java # LinkedHashMap-backed category repository
│
├── service/                     # Business logic, query engine, and domain events
│   ├── TaskService.java         # Task lifecycle, sorting, priority, pin, undo buffer
│   ├── CategoryService.java     # Category rules, validation, deletion reassignment
│   ├── TaskQuery.java           # Immutable multi-criteria query record
│   ├── SortOption.java          # Sorting strategy: DUE_DATE, PRIORITY, TITLE, CREATED
│   ├── StatusFilter.java        # Status filtering strategy: ALL, ACTIVE, COMPLETED
│   ├── TaskChangeListener.java  # Observer callback for repository mutations
│   ├── ChangeType.java          # Event types: CREATED, UPDATED, DELETED, RESTORED
│   ├── ValidationException.java  # Domain validation failure exception
│   └── NotFoundException.java   # Entity lookup failure exception
│
├── storage/                     # Persistence abstraction, atomic file I/O & autosave
│   ├── Storage.java             # Persistence contract: load() and save()
│   ├── JsonStorage.java         # Atomic file I/O with corruption quarantine
│   ├── JsonMapper.java          # Gson polymorphic mapping with type discriminator
│   ├── AppData.java             # Immutable root container record (categories, tasks)
│   ├── DataSync.java            # Repository-to-AppData snapshot & population utility
│   ├── AutosaveListener.java    # Debounced background executor for persistent autosave
│   └── StorageException.java    # File read/write failure runtime exception
│
├── controller/                  # Presentation orchestration & UI state management
│   ├── MainController.java      # Dispatches UI gestures to services, coordinates state
│   ├── ViewState.java           # Immutable snapshot of UI state (query, selection, messages)
│   └── ViewListener.java        # Observer contract for notifying views of state changes
│
└── ui/                          # JavaFX controls, custom cells, dialogs & styling
    ├── MainView.java            # Root BorderPane view composing all UI panels
    ├── AppMenuBar.java          # Top menu bar (File, Edit, View, Help) with shortcuts
    ├── ToolbarPane.java         # Search bar, "+ New Task" button, sort & status filters
    ├── CategoryPane.java        # Left sidebar listing categories with count badges
    ├── TaskListPane.java        # Center ListView hosting custom task cells
    ├── TaskCell.java            # Polymorphic ListCell rendering cards with stripes & badges
    ├── TaskEditorDialog.java    # Modal dialog for creating and editing tasks
    ├── CategoryDialog.java      # Modal dialog for creating custom categories
    ├── StatusBar.java           # Bottom bar showing task counts and autosave status
    └── Theme.java               # Design tokens, palette constants, and stylesheet bindings
```

### 4.3 Comprehensive Class Design
The system's structural design is captured in the UML class diagram below:

```mermaid
classDiagram
    direction TB

    %% Model Layer
    class Task {
        <<abstract>>
        -UUID id
        -String title
        -String description
        -Priority priority
        -UUID categoryId
        -boolean completed
        -boolean pinned
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        +getId() UUID
        +getTitle() String
        +setTitle(String)
        +getDescription() String
        +setDescription(String)
        +getPriority() Priority
        +setPriority(Priority)
        +getCategoryId() UUID
        +setCategoryId(UUID)
        +isCompleted() boolean
        +setCompleted(boolean)
        +isPinned() boolean
        +setPinned(boolean)
        +getCreatedAt() LocalDateTime
        +getUpdatedAt() LocalDateTime
        +touch()
        +getType()* TaskType
        +getSummary()* String
        +getProgress()* double
        +getDueDate()* Optional~LocalDate~
        +isOverdue(LocalDate)* boolean
        +copy()* Task
    }

    class SimpleTask {
        +getType() TaskType
        +getSummary() String
        +getProgress() double
        +getDueDate() Optional~LocalDate~
        +isOverdue(LocalDate) boolean
        +copy() SimpleTask
    }

    class ChecklistTask {
        -List~ChecklistItem~ items
        +getItems() List~ChecklistItem~
        +addItem(String) ChecklistItem
        +removeItem(UUID) boolean
        +toggleItem(UUID) boolean
        +getType() TaskType
        +getSummary() String
        +getProgress() double
        +getDueDate() Optional~LocalDate~
        +isOverdue(LocalDate) boolean
        +copy() ChecklistTask
    }

    class ChecklistItem {
        -UUID id
        -String text
        -boolean completed
        +getId() UUID
        +getText() String
        +setText(String)
        +isCompleted() boolean
        +setCompleted(boolean)
        +copy() ChecklistItem
    }

    class DeadlineTask {
        -LocalDate dueDate
        +getDueDate() Optional~LocalDate~
        +setDueDate(LocalDate)
        +isOverdue(LocalDate) boolean
        +getType() TaskType
        +getSummary() String
        +getProgress() double
        +copy() DeadlineTask
    }

    class Category {
        -UUID id
        -String name
        -String colorHex
        +getId() UUID
        +getName() String
        +setName(String)
        +getColorHex() String
        +setColorHex(String)
        +copy() Category
    }

    class Priority {
        <<enumeration>>
        HIGH
        MEDIUM
        LOW
    }

    class TaskType {
        <<enumeration>>
        SIMPLE
        CHECKLIST
        DEADLINE
    }

    class TaskDraft {
        <<record>>
        +TaskType type
        +String title
        +String description
        +Priority priority
        +UUID categoryId
        +LocalDate dueDate
        +List~String~ checklistItems
        +validate()
    }

    class TaskFactory {
        +static create(TaskDraft) Task
        +static create(TaskDraft, LocalDateTime) Task
        +static apply(Task, TaskDraft)
        +static apply(Task, TaskDraft, LocalDateTime)
    }

    Task <|-- SimpleTask
    Task <|-- ChecklistTask
    Task <|-- DeadlineTask
    ChecklistTask "1" *-- "0..*" ChecklistItem
    Task --> Priority
    Task --> TaskType
    Task ..> Category : references categoryId
    TaskFactory ..> Task : creates/modifies
    TaskFactory ..> TaskDraft : consumes

    %% Repository Layer
    class TaskRepository {
        <<interface>>
        +findAll() List~Task~
        +findById(UUID) Optional~Task~
        +save(Task) Task
        +deleteById(UUID) Optional~Task~
        +clear()
        +count() int
    }

    class InMemoryTaskRepository {
        -Map~UUID, Task~ tasks
        +findAll() List~Task~
        +findById(UUID) Optional~Task~
        +save(Task) Task
        +deleteById(UUID) Optional~Task~
        +clear()
        +count() int
    }

    class CategoryRepository {
        <<interface>>
        +findAll() List~Category~
        +findById(UUID) Optional~Category~
        +findByNameIgnoreCase(String) Optional~Category~
        +save(Category) Category
        +deleteById(UUID) Optional~Category~
        +clear()
        +count() int
    }

    class InMemoryCategoryRepository {
        -Map~UUID, Category~ categories
        +findAll() List~Category~
        +findById(UUID) Optional~Category~
        +findByNameIgnoreCase(String) Optional~Category~
        +save(Category) Category
        +deleteById(UUID) Optional~Category~
        +clear()
        +count() int
    }

    TaskRepository <|.. InMemoryTaskRepository
    CategoryRepository <|.. InMemoryCategoryRepository

    %% Service Layer
    class TaskService {
        -TaskRepository taskRepo
        -CategoryRepository categoryRepo
        -Clock clock
        -List~TaskChangeListener~ listeners
        -Task undoBuffer
        +create(TaskDraft) Task
        +update(UUID, TaskDraft) Task
        +delete(UUID) Task
        +undoDelete() Optional~Task~
        +get(UUID) Task
        +getAll() List~Task~
        +toggleComplete(UUID) Task
        +setPriority(UUID, Priority) Task
        +togglePinned(UUID) Task
        +query(TaskQuery) List~Task~
        +countByPriority(TaskQuery) Map~Priority, Integer~
        +countActive() int
        +countCompleted() int
        +addListener(TaskChangeListener)
    }

    class CategoryService {
        -CategoryRepository categoryRepo
        -TaskRepository taskRepo
        -Clock clock
        -List~TaskChangeListener~ listeners
        +create(String, String) Category
        +rename(UUID, String) Category
        +recolor(UUID, String) Category
        +delete(UUID) Category
        +getAll() List~Category~
        +get(UUID) Category
        +countTasks(UUID) int
        +addListener(TaskChangeListener)
    }

    class TaskChangeListener {
        <<interface>>
        +onTasksChanged(ChangeType, UUID)
    }

    class TaskQuery {
        <<record>>
        +UUID categoryId
        +StatusFilter status
        +Priority priority
        +String searchText
        +SortOption sort
        +boolean ascending
        +withCategory(UUID) TaskQuery
        +withStatus(StatusFilter) TaskQuery
        +withPriority(Priority) TaskQuery
        +withSearch(String) TaskQuery
        +withSort(SortOption, boolean) TaskQuery
    }

    TaskService --> TaskRepository
    TaskService --> CategoryRepository
    TaskService ..> TaskChangeListener : notifies
    CategoryService --> CategoryRepository
    CategoryService --> TaskRepository
    CategoryService ..> TaskChangeListener : notifies
    TaskService ..> TaskQuery : filters with

    %% Storage Layer
    class Storage {
        <<interface>>
        +load() AppData
        +save(AppData)
    }

    class JsonStorage {
        -Path filePath
        -Clock clock
        -String lastLoadWarning
        +load() AppData
        +save(AppData)
        +getLastLoadWarning() Optional~String~
    }

    class AppData {
        <<record>>
        +int schemaVersion
        +List~Category~ categories
        +List~Task~ tasks
    }

    class JsonMapper {
        +static toJson(AppData) JsonObject
        +static fromJson(JsonObject) AppData
        +static taskToJson(Task) JsonObject
        +static taskFromJson(JsonObject) Task
    }

    class DataSync {
        +static populateRepositories(AppData, TaskRepository, CategoryRepository)
        +static snapshot(TaskRepository, CategoryRepository) AppData
    }

    class AutosaveListener {
        -Storage storage
        -TaskRepository taskRepo
        -CategoryRepository categoryRepo
        -ScheduledExecutorService executor
        -Consumer~String~ errorCallback
        +onTasksChanged(ChangeType, UUID)
        +flush()
        +close()
    }

    Storage <|.. JsonStorage
    AutosaveListener ..|> TaskChangeListener
    JsonStorage ..> JsonMapper : uses
    JsonStorage ..> AppData : reads/writes
    DataSync ..> AppData : converts

    %% Controller Layer
    class MainController {
        -TaskService taskService
        -CategoryService categoryService
        -ViewState currentState
        -List~ViewListener~ viewListeners
        +onCreateTask(TaskDraft)
        +onUpdateTask(UUID, TaskDraft)
        +onDeleteTask(UUID)
        +onUndoDelete()
        +onToggleComplete(UUID)
        +onSetPriority(UUID, Priority)
        +onTogglePinned(UUID)
        +onSelectCategory(UUID)
        +onSearch(String)
        +onSort(SortOption, boolean)
        +onStatusFilter(StatusFilter)
        +onPriorityFilter(Priority)
        +onCreateCategory(String, String)
        +onRenameCategory(UUID, String)
        +onRecolorCategory(UUID, String)
        +onDeleteCategory(UUID)
        +refresh()
        +addListener(ViewListener)
    }

    class ViewState {
        <<record>>
        +TaskQuery query
        +UUID selectedTaskId
        +String infoMessage
        +String errorMessage
    }

    class ViewListener {
        <<interface>>
        +onViewChanged(List~Task~, List~Category~, ViewState)
    }

    MainController --> TaskService
    MainController --> CategoryService
    MainController ..> ViewListener : notifies
    MainController *-- ViewState

    %% UI Layer
    class MainView {
        -MainController controller
        -AppMenuBar menuBar
        -ToolbarPane toolbar
        -CategoryPane categoryPane
        -TaskListPane taskListPane
        -StatusBar statusBar
        +onViewChanged(List~Task~, List~Category~, ViewState)
    }

    class TaskCell {
        +updateItem(Task, boolean)
    }

    class TaskEditorDialog {
        +showAndWait() Optional~TaskDraft~
    }

    class AppMenuBar {
        -MenuItem undoMenuItem
        -CheckMenuItem showCompletedItem
        +updateState(ViewState, boolean)
        +static showAboutDialog()
        +static showShortcutsDialog()
    }

    class CategoryDialog {
        +showAndWait() Optional~CategoryDraft~
    }

    MainView ..|> ViewListener
    MainView --> MainController
    MainView *-- TaskCell
    MainView *-- AppMenuBar
    MainView ..> TaskEditorDialog : presents
    MainView ..> CategoryDialog : presents
```

### 4.4 Data Structures & Algorithmic Rationale
Every data structure in the application was selected to optimize performance, memory efficiency, and algorithmic invariants:

| Data Structure | Layer & Location | Concrete Rationale & Complexity Analysis |
|---|---|---|
| `LinkedHashMap<UUID, Task>` | `InMemoryTaskRepository` | Provides $\mathcal{O}(1)$ average-time complexity for lookups, insertions, and deletions by `UUID` primary key. Unlike standard `HashMap`, it preserves deterministic insertion order, ensuring reproducible test output and predictable JSON serialization. |
| `LinkedHashMap<UUID, Category>` | `InMemoryCategoryRepository` | Ensures $\mathcal{O}(1)$ key-based access while guaranteeing the default category (`General`) remains permanently first in iteration order. |
| `ArrayList<ChecklistItem>` | `ChecklistTask` | Optimal for small, ordered sequences of checklist sub-items. Contiguous memory allocation minimizes memory overhead, and index-based access is $\mathcal{O}(1)$. Typical checklist sizes rarely exceed 50 items. |
| `EnumMap<Priority, Integer>` | `TaskService.countByPriority` | Uses an internal compact array indexed directly by the `Priority` enum ordinal. Guarantees zero hashing collisions, zero resize rehashes, and negligible memory footprint during status-bar aggregation. |
| Composite `Comparator<Task>` Chain | `TaskService.query` | Functional comparator composition: `Comparator.comparing(Task::isPinned).reversed().thenComparing(...)`. Executes in $\mathcal{O}(N \log N)$ time, permanently anchoring pinned tasks at the top across all sort selections and providing deterministic tie-breaking via `createdAt`. |

### 4.5 Persistent Storage Schema & Atomic I/O
The application stores all tasks, categories, and metadata in a single, human-readable JSON document located at `~/.todoapp/data.json`.

#### Polymorphic JSON Schema Example
```json
{
  "schemaVersion": 1,
  "categories": [
    {
      "id": "00000000-0000-0000-0000-000000000001",
      "name": "General",
      "colorHex": "#1976D2"
    },
    {
      "id": "e4b10b83-a212-4c26-88d4-5394ef3f7521",
      "name": "Academics",
      "colorHex": "#388E3C"
    }
  ],
  "tasks": [
    {
      "type": "SIMPLE",
      "id": "b3e04771-419b-4a81-a67b-11c5040081d2",
      "title": "Review Software Construction lecture slides",
      "description": "Focus on Layered Architecture and MVC patterns.",
      "priority": "HIGH",
      "categoryId": "e4b10b83-a212-4c26-88d4-5394ef3f7521",
      "completed": false,
      "pinned": true,
      "createdAt": "2026-11-22T09:00:00",
      "updatedAt": "2026-11-22T09:15:00"
    },
    {
      "type": "CHECKLIST",
      "id": "c8a4128f-7c15-46a2-9721-e09214b76c89",
      "title": "Complete Open-Ended Lab Deliverables",
      "description": "Milestones M00 through M14.",
      "priority": "HIGH",
      "categoryId": "e4b10b83-a212-4c26-88d4-5394ef3f7521",
      "completed": false,
      "pinned": false,
      "createdAt": "2026-11-22T09:30:00",
      "updatedAt": "2026-11-22T10:00:00",
      "items": [
        {
          "id": "11111111-1111-1111-1111-111111111111",
          "text": "M11: UI Extras & Polish",
          "completed": true
        },
        {
          "id": "22222222-2222-2222-2222-222222222222",
          "text": "M12: Testing & Hardening",
          "completed": true
        },
        {
          "id": "33333333-3333-3333-3333-333333333333",
          "text": "M13: Packaging & Release",
          "completed": true
        },
        {
          "id": "44444444-4444-4444-4444-444444444444",
          "text": "M14: Final Project Report",
          "completed": false
        }
      ]
    },
    {
      "type": "DEADLINE",
      "id": "f5e92711-236b-4e12-859a-112345678901",
      "title": "Submit Final OEL Report & Code",
      "description": "Upload zipped artifact and commit log.",
      "priority": "HIGH",
      "categoryId": "e4b10b83-a212-4c26-88d4-5394ef3f7521",
      "completed": false,
      "pinned": true,
      "createdAt": "2026-11-22T10:00:00",
      "updatedAt": "2026-11-22T10:00:00",
      "dueDate": "2026-11-23"
    }
  ]
}
```

#### Atomic Persistence Mechanics
To guarantee NFR-03 (Reliability & Crash Resilience), `JsonStorage.java` performs file writes using a two-stage atomic replace:
1. The serialized JSON string is written to a temporary sibling file: `~/.todoapp/data.json.tmp`.
2. The file is flushed and closed to ensure all byte buffers are physically committed to storage.
3. `Files.move(tempPath, targetPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)` executes the atomic filesystem inode replacement.
4. If a crash, power outage, or OS kill occurs at any point during writing, the original `data.json` remains completely intact and uncorrupted.
5. If the target file contains malformed or unreadable JSON during startup, the application immediately isolates the corrupted file by renaming it to `data.json.corrupt-<timestamp>` and initializes a clean state with default categories, preventing catastrophic crashes.

### 4.6 Dynamic Behavioral Workflows (Sequence Diagrams)

#### 4.6.1 Task Creation Workflow
```mermaid
sequenceDiagram
    autonumber
    actor User
    participant View as MainView
    participant Dialog as TaskEditorDialog
    participant Ctrl as MainController
    participant Svc as TaskService
    participant Factory as TaskFactory
    participant Repo as TaskRepository
    participant Auto as AutosaveListener

    User->>View: Click "New Task" (or Ctrl+N)
    View->>Dialog: Open dialog(mode=CREATE)
    User->>Dialog: Fill title, priority, category & click "Save"
    Dialog->>Dialog: draft = new TaskDraft(...)
    Dialog->>Ctrl: onCreateTask(draft)
    Ctrl->>Svc: create(draft)
    Svc->>Svc: draft.validate()
    Svc->>Factory: TaskFactory.create(draft, now)
    Factory-->>Svc: new Task instance
    Svc->>Repo: save(task)
    Repo-->>Svc: savedTask
    Svc->>Auto: onTasksChanged(CREATED, taskId)
    Auto-->>Auto: Schedule debounced save (300ms)
    Svc-->>Ctrl: deep copy of savedTask
    Ctrl->>Ctrl: refresh()
    Ctrl->>Svc: query(currentQuery)
    Svc-->>Ctrl: refreshedTaskList
    Ctrl->>View: onViewChanged(tasks, categories, viewState)
    View->>View: Update ListView & Status Bar
    View->>Dialog: close()
```

#### 4.6.2 Task Deletion & Undo Workflow
```mermaid
sequenceDiagram
    autonumber
    actor User
    participant View as MainView
    participant Alert as ConfirmDialog
    participant Ctrl as MainController
    participant Svc as TaskService
    participant Repo as TaskRepository
    participant Auto as AutosaveListener

    User->>View: Press Delete (or Trash button)
    View->>Alert: Show confirmation dialog
    User->>Alert: Click "OK"
    Alert-->>View: Confirmed
    View->>Ctrl: onDeleteTask(taskId)
    Ctrl->>Svc: delete(taskId)
    Svc->>Repo: deleteById(taskId)
    Repo-->>Svc: deletedTask
    Svc->>Svc: undoBuffer = deletedTask.copy()
    Svc->>Auto: onTasksChanged(DELETED, taskId)
    Auto-->>Auto: Schedule debounced save (300ms)
    Svc-->>Ctrl: deletedTask
    Ctrl->>Ctrl: refresh()
    Ctrl->>View: onViewChanged(tasks, categories, viewState)
    View->>View: Display Toast Banner "Task deleted. [Undo]"

    opt User clicks Undo (or Ctrl+Z)
        User->>View: Click "Undo" button (or press Ctrl+Z)
        View->>Ctrl: onUndoDelete()
        Ctrl->>Svc: undoDelete()
        Svc->>Repo: save(undoBuffer)
        Svc->>Svc: undoBuffer = null
        Svc->>Auto: onTasksChanged(RESTORED, taskId)
        Svc-->>Ctrl: restoredTask
        Ctrl->>Ctrl: refresh()
        Ctrl->>View: onViewChanged(tasks, categories, viewState)
        View->>View: Task restored to ListView
    end
```

### 4.7 User Interface Layout & Wireframes
The user interface layout is organized around a primary root `BorderPane`:

```
+----------------------------------------------------------------------------------------------------+
|  File   Edit   View   Help                                                             (AppMenuBar)|
+----------------------------------------------------------------------------------------------------+
| [+ New Task]   [🔍 Search tasks...]     [Filter: All/Active v]   [Sort: Due Date v]  (ToolbarPane) |
+-----------------------------------+----------------------------------------------------------------+
| CATEGORIES         (CategoryPane) | TASKS                                           (TaskListPane) |
| • All Tasks                  (12) | +------------------------------------------------------------+ |
| • General                     (3) | | [ ] 📌 Finish Lab Report                      [Work] [High]| |
| • Work                        (4) | |     Complete all milestones and export PDF.                | |
| • Personal                    (3) | |     Due: Tomorrow • 4/5 items (80%) [====    ]             | |
| • Shopping                    (2) | +------------------------------------------------------------+ |
|                                   | +------------------------------------------------------------+ |
|                                   | | [X] Buy groceries                         [Shopping] [Med] | |
|                                   | |     Milk, Bread, Bananas                                   | |
| [+ Add Category]                  | +------------------------------------------------------------+ |
+-----------------------------------+----------------------------------------------------------------+
| Ready • 12 tasks (8 active, 4 completed) • Autosaved                             (StatusBar)       |
+----------------------------------------------------------------------------------------------------+
```

### 4.8 Object-Oriented Design Patterns Applied
The software architecture incorporates five established design patterns:
1. **Model-View-Controller (MVC):** `MainView` acts as View, capturing user input and presenting state; `MainController` acts as Controller, coordinating actions and updating `ViewState`; `TaskService` and domain entities act as Model.
2. **Factory Method:** `TaskFactory.java` encapsulates the creation and modification of polymorphic `Task` subtypes (`SimpleTask`, `ChecklistTask`, `DeadlineTask`), abstracting instantiation logic from callers.
3. **Observer Pattern:** Implemented via two distinct event channels: `TaskChangeListener` enables repositories and services to publish mutation events to `AutosaveListener`; `ViewListener` allows `MainController` to broadcast updated `ViewState` snapshots to `MainView`.
4. **Data Mapper Pattern:** `JsonMapper.java` decouples domain entities from physical storage formatting, using explicit `"type"` discriminator properties to map bidirectional transformations between entities and Gson `JsonObject` trees.
5. **Strategy / Functional Comparators:** `SortOption.java` and `TaskQuery.java` encapsulate dynamic sorting algorithms (`Comparator<Task>`), allowing runtime selection of sort strategies without modifying the querying engine.

### 4.9 Design Decisions & Trade-Off Analysis

| Architectural Decision | Chosen Alternative | Alternatives Considered | Concrete Rationale & Trade-Off Analysis |
|---|---|---|---|
| **GUI Framework** | **JavaFX 21 (LTS)** | Java Swing, SWT, JavaFX 17 | JavaFX 21 provides a modern scene-graph architecture, high-DPI font rendering, CSS styling, observable property bindings, and high-level controls (`ListView`, `DatePicker`, `Dialog`). Retrieved via Maven Central without requiring local SDK installations. |
| **GUI Construction Style** | **Programmatic Java Code** | FXML + `FXMLLoader` | Building views programmatically in Java keeps the compilation fast and lightweight, eliminates runtime reflection overhead, provides complete compile-time type safety, and produces clean Git diffs during version control. |
| **Persistence Format** | **Atomic JSON (Gson)** | SQLite / H2, Native Java Serialization | JSON offers human readability, instant debugging, zero platform-specific C-library dependencies, and effortless multi-platform portability. Atomic write-and-replace prevents file corruption. |
| **Entity Immutability** | **Defensive Copying via `Task.copy()`** | Shared mutable references, Read-only proxy wrappers | Service methods return deep copies of stored domain entities. This strictly prevents UI components from bypassing domain validation by directly mutating in-memory repository references. |
| **Git Workflow** | **Single Linear Branch on `main`** | GitFlow, Feature Branches with Pull Requests | Tailored for solo developer efficiency. Eliminates merge commits, avoids branch synchronization overhead, and guarantees a completely linear, bisectable, and auditable Git history. |
| **Launcher Separation** | **Distinct `Launcher` Entry Class** | Direct launch from `App extends Application` | JavaFX requires that executable shaded fat JARs do not have a main class extending `Application`, or the JVM throws `Missing JavaFX runtime components`. The separate `Launcher.java` resolves this cleanly. |
| **Autosave Strategy** | **Debounced Background Executor (300 ms)** | Immediate synchronous save, Periodic fixed timer | Immediate synchronous saves would block the UI thread during rapid keystrokes; fixed periodic timers risk losing up to the period duration of edits on crash. A 300 ms debounce triggers persistence immediately after typing stops while maintaining zero UI lag. |


---

## 5. Implementation Narrative & Code Highlights

### 5.1 Milestone-by-Milestone Narrative
The software construction was executed in an iterative, test-driven sequence across 14 distinct milestones:

- **M00 — Project Scaffolding:** Established the Maven build foundation, configured Java 17 and JavaFX 21 dependencies, created `.gitignore`, initialized the single-branch Git repository, and created the `Launcher` decoupling class.
- **M01 — Requirements Analysis:** Formulated comprehensive functional (FR-01..17) and non-functional (NFR-01..07) requirements, 14 detailed user scenarios, 8 use cases, and the initial traceability matrix.
- **M02 — Architecture & Detailed Design:** Designed the layered MVC architecture, documented the UML class model, formalized state transition rules for checklist tasks, and specified the JSON storage schema.
- **M03 — Domain Model:** Implemented the core polymorphic entity hierarchy (`Task`, `SimpleTask`, `ChecklistTask`, `DeadlineTask`, `Category`, `Priority`, `TaskDraft`, and `TaskFactory`), with 100% unit test coverage for copy semantics and validation rules.
- **M04 — Repositories & Category Service:** Built repository abstractions backed by deterministic in-memory `LinkedHashMap` stores; implemented `CategoryService` with case-insensitive name uniqueness and zero-orphan deletion reassignment.
- **M05 — Task Service Core:** Implemented complete task CRUD operations, single-slot undo buffer, defensive copying, and observer mutation notifications (`TaskChangeListener`).
- **M06 — Query Engine:** Implemented multi-criteria filtering (category, status, priority), real-time text search, and the composite `Comparator` chain enforcing the pinned-to-top invariant.
- **M07 — Persistence Layer (Tag `v0.1.0`):** Engineered atomic file writing (`JsonStorage`), Gson polymorphic serialization (`JsonMapper`), and the background debounced autosave listener (`AutosaveListener`).
- **M08 — JavaFX GUI Foundation:** Constructed the pure Java presentation shell: `MainView`, `ToolbarPane`, `CategoryPane`, `TaskListPane`, `StatusBar`, and `Theme`, orchestrated by `MainController`.
- **M09 — Task CRUD in the GUI (Tag `v0.2.0`):** Created the modal `TaskEditorDialog`, custom polymorphic `TaskCell` card views, deletion confirmation alerts, and context menus.
- **M10 — Categories & Filtering in GUI (Tag `v0.3.0`):** Added the `CategoryDialog`, dynamic category sidebar counts, pin toggle controls, and multi-criteria filter bindings.
- **M11 — UI Polish & Extras:** Delivered the non-blocking undo toast notification banner, top menu bar (`AppMenuBar`), application-wide keyboard shortcuts, due-soon highlights, and window geometry persistence across restarts via `java.util.prefs.Preferences`.
- **M12 — Testing & Hardening:** Established JaCoCo coverage threshold enforcement ($\ge 80\%$), created comprehensive edge-case and high-volume performance tests, resolved lint warnings, and authored `docs/test-plan.md`.
- **M13 — Packaging, Documentation & Release (Tag `v1.0.0`):** Configured `maven-shade-plugin` to package runnable fat JAR `target/todo-app-1.0.0.jar`, completed `README.md`, authored `docs/user-guide.md`, reconciled `docs/design.md`, completed the traceability matrix, and mapped academic course outcomes in `docs/course-mapping.md`.

---

### 5.2 Annotated Code Excerpts

#### 1. Polymorphic Task Entity Hierarchy
From `src/main/java/com/todoapp/model/Task.java`:
```java
public abstract class Task {
    private final UUID id;
    private String title;
    private String description;
    private Priority priority;
    private UUID categoryId;
    private boolean completed;
    private boolean pinned;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Polymorphic contracts implemented by concrete subclasses
    public abstract TaskType getType();
    public abstract String getSummary();
    public abstract double getProgress();
    public abstract Optional<LocalDate> getDueDate();
    public abstract boolean isOverdue(LocalDate referenceDate);
    public abstract Task copy();

    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }
}
```
*Annotation:* Abstract `Task` encapsulates shared identity and metadata while declaring polymorphic contracts (`getType()`, `getProgress()`, `isOverdue()`, `copy()`). Concrete subclasses (`SimpleTask`, `ChecklistTask`, `DeadlineTask`) provide tailored implementations without leaking subtype-specific fields to generic consumers.

#### 2. Centralized Instantiation via TaskFactory
From `src/main/java/com/todoapp/model/TaskFactory.java`:
```java
public final class TaskFactory {
    public static Task create(TaskDraft draft, LocalDateTime now) {
        draft.validate();
        return switch (draft.type()) {
            case SIMPLE -> new SimpleTask(
                    UUID.randomUUID(), draft.title(), draft.description(),
                    draft.priority(), draft.categoryId(), false, false, now, now
            );
            case CHECKLIST -> {
                ChecklistTask task = new ChecklistTask(
                        UUID.randomUUID(), draft.title(), draft.description(),
                        draft.priority(), draft.categoryId(), false, false, now, now
                );
                for (String itemText : draft.checklistItems()) {
                    task.addItem(itemText);
                }
                yield task;
            }
            case DEADLINE -> new DeadlineTask(
                    UUID.randomUUID(), draft.title(), draft.description(),
                    draft.priority(), draft.categoryId(), false, false, now, now,
                    draft.dueDate()
            );
        };
    }
}
```
*Annotation:* The Factory Pattern centralizes entity construction, guarantees validation invocation prior to instantiation, and isolates calling layers from concrete class constructors.

#### 3. Domain Event Notification via Observer Pattern
From `src/main/java/com/todoapp/service/TaskService.java`:
```java
    private void notifyListeners(ChangeType type, UUID entityId) {
        for (TaskChangeListener listener : listeners) {
            try {
                listener.onTasksChanged(type, entityId);
            } catch (Throwable ex) {
                LOGGER.log(Level.WARNING, "Error executing TaskChangeListener", ex);
            }
        }
    }
```
*Annotation:* Decouples business logic from persistence. Whenever a task is created, updated, or deleted, `TaskService` fires events to registered observers (such as `AutosaveListener`) with defensive exception isolation so one failing listener cannot crash core operations.

#### 4. Query Engine & Pinned Invariant Comparator Chain
From `src/main/java/com/todoapp/service/TaskService.java`:
```java
    public List<Task> query(TaskQuery query) {
        Comparator<Task> secondary = switch (query.sort()) {
            case DUE_DATE -> Comparator.comparing(
                    (Task t) -> t.getDueDate().orElse(LocalDate.MAX)
            ).thenComparing(Task::getCreatedAt, Comparator.reverseOrder());
            case PRIORITY -> Comparator.comparing(Task::getPriority)
                    .thenComparing(Task::getCreatedAt, Comparator.reverseOrder());
            case TITLE -> Comparator.comparing((Task t) -> t.getTitle().toLowerCase())
                    .thenComparing(Task::getCreatedAt, Comparator.reverseOrder());
            case CREATED_DATE -> Comparator.comparing(Task::getCreatedAt).reversed();
        };

        Comparator<Task> finalComparator = Comparator
                .comparing(Task::isPinned).reversed()
                .thenComparing(query.ascending() ? secondary : secondary.reversed());

        return taskRepo.findAll().stream()
                .filter(filterPredicate(query))
                .sorted(finalComparator)
                .map(Task::copy)
                .toList();
    }
```
*Annotation:* Combines functional predicates with a composite `Comparator` chain. Pinned tasks are sorted permanently ahead of unpinned cards (`comparing(Task::isPinned).reversed()`), with deterministic tie-breaking on creation timestamps.

#### 5. Polymorphic JSON Mapping with Type Discriminator
From `src/main/java/com/todoapp/storage/JsonMapper.java`:
```java
    public static JsonObject taskToJson(Task task) {
        JsonObject json = new JsonObject();
        json.addProperty("type", task.getType().name());
        json.addProperty("id", task.getId().toString());
        json.addProperty("title", task.getTitle());
        json.addProperty("description", task.getDescription());
        json.addProperty("priority", task.getPriority().name());
        json.addProperty("categoryId", task.getCategoryId().toString());
        json.addProperty("completed", task.isCompleted());
        json.addProperty("pinned", task.isPinned());
        json.addProperty("createdAt", task.getCreatedAt().toString());
        json.addProperty("updatedAt", task.getUpdatedAt().toString());

        if (task instanceof DeadlineTask dt && dt.getDueDate().isPresent()) {
            json.addProperty("dueDate", dt.getDueDate().get().toString());
        } else if (task instanceof ChecklistTask ct) {
            JsonArray arr = new JsonArray();
            for (ChecklistItem item : ct.getItems()) {
                JsonObject itemJson = new JsonObject();
                itemJson.addProperty("id", item.getId().toString());
                itemJson.addProperty("text", item.getText());
                itemJson.addProperty("completed", item.isCompleted());
                arr.add(itemJson);
            }
            json.add("items", arr);
        }
        return json;
    }
```
*Annotation:* Implements the Data Mapper pattern. An explicit `"type"` attribute acts as discriminator, allowing the deserializer to reconstruct the exact polymorphic subtype without proprietary Java serialization headers.

#### 6. Atomic File Writing & Quarantine Recovery
From `src/main/java/com/todoapp/storage/JsonStorage.java`:
```java
    @Override
    public synchronized void save(AppData data) throws StorageException {
        Path tempFile = Path.of(filePath.toString() + ".tmp");
        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            String json = JsonMapper.toJson(data).toString();
            Files.writeString(tempFile, json, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            Files.move(tempFile, filePath,
                    StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new StorageException("Failed to atomically write storage file: " + filePath, ex);
        }
    }
```
*Annotation:* Protects user data from catastrophic corruption. Writing to `.tmp` first followed by `StandardCopyOption.ATOMIC_MOVE` guarantees that an unhandled crash or power failure cannot truncate or leave `data.json` partially written.

#### 7. Polymorphic TaskCard Cell Rendering
From `src/main/java/com/todoapp/ui/TaskCell.java`:
```java
    @Override
    protected void updateItem(Task task, boolean empty) {
        super.updateItem(task, empty);
        if (empty || task == null) {
            setGraphic(null);
            setText(null);
            return;
        }

        titleLabel.setText(task.getTitle());
        titleLabel.getStyleClass().setAll(task.isCompleted() ? "task-title-completed" : "task-title");
        priorityStripe.setStyle("-fx-background-color: " + Theme.getPriorityColor(task.getPriority()) + ";");

        if (task instanceof ChecklistTask ct) {
            subInfoLabel.setText(ct.getSummary());
            progressBar.setProgress(ct.getProgress());
            progressBar.setVisible(true);
        } else if (task instanceof DeadlineTask dt && dt.getDueDate().isPresent()) {
            boolean overdue = dt.isOverdue(LocalDate.now());
            badgeLabel.setText(overdue ? "OVERDUE" : "Due " + dt.getDueDate().get());
            badgeLabel.getStyleClass().setAll(overdue ? "badge-overdue" : "badge-deadline");
            badgeLabel.setVisible(true);
        }
        setGraphic(cardContainer);
    }
```
*Annotation:* Polymorphically inspects `Task` subtypes during list rendering. Dynamically activates checklist progress bars, overdue alert badges, and completion strikethrough styles without altering list controller contracts.

#### 8. Fail-Fast Input Validation
From `src/main/java/com/todoapp/model/TaskDraft.java`:
```java
    public void validate() throws ValidationException {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("Task title is required.");
        }
        if (title.trim().length() > 100) {
            throw new ValidationException("Task title must not exceed 100 characters.");
        }
        if (type == null) {
            throw new ValidationException("Task type must not be null.");
        }
        if (priority == null) {
            throw new ValidationException("Priority must not be null.");
        }
        if (categoryId == null) {
            throw new ValidationException("Category must be selected.");
        }
        if (type == TaskType.DEADLINE && dueDate == null) {
            throw new ValidationException("Due date is required for deadline tasks.");
        }
    }
```
*Annotation:* Immutable record validation enforcing strict business integrity invariants. Rejects null values, empty strings, over-length inputs, and missing dates before entities ever reach repositories.

---

### 5.3 Difficulties Encountered & Engineering Solutions

1. **JavaFX Fat JAR Packaging & Main-Class Modular Conflict:**
   - *Problem:* JavaFX 21 prohibits the application main class from directly extending `javafx.application.Application` when packaged as a fat shaded JAR, throwing: `Error: JavaFX runtime components are missing, and are required to run this application`.
   - *Solution:* Introduced `com.todoapp.Launcher.java` as a standard Java entry point with a plain `public static void main(String[] args)` method that delegates to `Application.launch(App.class, args)`. This bypasses the JVM module-path check and enables standalone JAR execution (`java -jar target/todo-app-1.0.0.jar`).
2. **Maven Compiler Linting Conflicts with CLI Debugging Flags:**
   - *Problem:* Passing `-Xlint:all` directly to the `mvn` command line caused Apache Maven to interpret `-X` as debug mode, which stalled during remote artifact resolution.
   - *Solution:* Relocated compiler flags into `pom.xml` under `<configuration><compilerArgs><arg>-Xlint:all</arg><arg>-Xlint:-serial</arg><arg>-Xlint:-this-escape</arg></compilerArgs></configuration>`, ensuring reproducible and clean builds across all environments.
3. **Java 21 `this-escape` Warnings in GUI Constructors:**
   - *Problem:* Modern javac emits warnings when passing `this` to helper initialization methods within constructors before the object is fully initialized.
   - *Solution:* Suppressed specific warning `<arg>-Xlint:-this-escape</arg>` while maintaining full `-Xlint:all` strictness on all other language checks.
4. **Clock Timestamp Propagation in TaskService:**
   - *Problem:* During task creation in `TaskService`, `LocalDateTime.now()` was called directly instead of using the injected `Clock` dependency, causing time-travel unit tests with fixed clocks to fail assertion checks.
   - *Solution:* Refactored `TaskFactory.create(draft, now)` and `TaskFactory.apply(target, draft, now)` in commit `e9503e6` to receive `LocalDateTime.now(clock)`, guaranteeing 100% deterministic test execution.

---

## 6. Object-Oriented Programming (OOP) Concepts Demonstrated

The software design demonstrates thorough understanding and practical application of core object-oriented principles:

| OOP Concept | Target Class & File Path | Concrete Methods / Declarations | Technical Description & Educational Significance |
|---|---|---|---|
| **Encapsulation** | `src/main/java/com/todoapp/model/Task.java`<br>`src/main/java/com/todoapp/service/TaskService.java` | Private fields (`id`, `title`, `priority`)<br>`Task.copy()`<br>`TaskService.get(UUID)` | Domain entity fields are strictly private. Mutable state is protected behind controlled mutators and getters. To prevent UI controllers or external callers from corrupting stored state, `TaskService` returns deep copies via `copy()`. State modifications must submit a validated `TaskDraft`. |
| **Inheritance** | `src/main/java/com/todoapp/model/Task.java`<br>`src/main/java/com/todoapp/model/SimpleTask.java`<br>`src/main/java/com/todoapp/model/ChecklistTask.java`<br>`src/main/java/com/todoapp/model/DeadlineTask.java` | `public abstract class Task`<br>`class SimpleTask extends Task`<br>`class ChecklistTask extends Task`<br>`class DeadlineTask extends Task` | Base class `Task` centralizes common entity attributes (UUID, title, description, priority, category, completion status, timestamps) and lifecycle behavior (`touch()`). Specific task variants inherit this foundation while extending specialized data members (e.g. `items` in `ChecklistTask`, `dueDate` in `DeadlineTask`). |
| **Polymorphism** | `src/main/java/com/todoapp/model/Task.java`<br>`src/main/java/com/todoapp/ui/TaskCell.java`<br>`src/main/java/com/todoapp/storage/JsonMapper.java` | `abstract TaskType getType()`<br>`abstract String getSummary()`<br>`abstract double getProgress()`<br>`abstract boolean isOverdue(LocalDate)` | Dynamic method dispatch allows clients to treat all tasks uniformly via the `Task` abstraction. In `TaskCell`, progress bars and deadline badges are dynamically computed via polymorphic calls without modifying cell container contracts. `JsonMapper` handles serialization based on polymorphic types. |
| **Abstraction** | `src/main/java/com/todoapp/repository/TaskRepository.java`<br>`src/main/java/com/todoapp/repository/CategoryRepository.java`<br>`src/main/java/com/todoapp/storage/Storage.java` | `TaskRepository.save(Task)`<br>`TaskRepository.findById(UUID)`<br>`Storage.load()`<br>`Storage.save(AppData)` | High-level business services interact exclusively with abstract interfaces (`TaskRepository`, `Storage`), completely decoupling domain rules from storage mechanics (in-memory maps, JSON files, future SQL databases). |
| **Interface Segregation** | `src/main/java/com/todoapp/service/TaskChangeListener.java`<br>`src/main/java/com/todoapp/controller/ViewListener.java` | `onTasksChanged(ChangeType, UUID)`<br>`onViewChanged(List<Task>, ...)` | Interfaces are fine-grained and single-purpose. `AutosaveListener` implements `TaskChangeListener` to handle background persistence without being burdened with UI rendering responsibilities. |
| **Dependency Inversion** | `src/main/java/com/todoapp/service/TaskService.java`<br>`src/main/java/com/todoapp/controller/MainController.java` | Constructor injection: `new TaskService(TaskRepository, CategoryRepository, Clock)` | High-level modules do not instantiate low-level concrete implementations directly. Dependencies are injected via constructors as abstract interfaces, allowing test harnesses to supply mock repositories or fixed virtual clocks seamlessly. |

---

## 7. Graphical User Interface (GUI) Walkthrough

This chapter illustrates each graphical screen, user control, and interaction workflow.

> **Note for Evaluators & Graders:** Wherever screenshots are required, an explicit callout block is provided below indicating the exact view and file reference (`docs/img/*.png`) to be inserted. To inspect and test these screens live on your machine, launch the application using `mvn javafx:run -Djavafx.args="--seed --data=todo-dev.json"`.

---

### 7.1 Main Application Window & Dashboard
The primary window provides a three-pane layout: a top toolbar, a left category navigation sidebar, and a central scrollable task card view, anchored by a bottom status bar.

> **[SCREENSHOT NEEDED: docs/img/01-main-window.png]**  
> *Capture the entire main application window after launching with `--seed`. The screenshot must clearly display the top menu bar, the toolbar with search bar and sort dropdown, the category sidebar with count badges, multiple task cards with color stripes and pins, and the bottom status bar.*

![Main Window Dashboard](img/01-main-window.png)

- **Top Menu Bar:** Provides standard `File`, `Edit`, `View`, and `Help` cascading menus.
- **Top Toolbar:** Features a prominent **+ New Task** primary action button, live search text field, completion status filter toggle, and sort dropdown.
- **Category Sidebar:** Lists pre-configured categories (`All Tasks`, `General`, `Work`, `Personal`, `Shopping`) and user-created custom categories, displaying live item counts. Clicking a category filters the view immediately.
- **Status Bar:** Real-time summary showing total tasks, active count, completed count, and autosave status (`Autosaved`).

---

### 7.2 Creating a Simple Task
Clicking **+ New Task** or pressing `Ctrl+N` opens the modal `TaskEditorDialog`.

> **[SCREENSHOT NEEDED: docs/img/02-task-simple.png]**  
> *Capture the Task Editor modal dialog with "Simple" selected, title input filled, priority set to High, and category set to Work.*

![Simple Task Creation](img/02-task-simple.png)

- **Title Field:** Validates input in real time (1 to 100 characters; whitespace-only entries rejected).
- **Description Field:** Multi-line text area supporting extended notes.
- **Type Selector:** Radio buttons selecting `Simple`, `Checklist`, or `Deadline`.
- **Priority Dropdown:** Selects `High` (Red), `Medium` (Amber), or `Low` (Green).
- **Category Dropdown:** Populated dynamically from all active categories in the repository.

---

### 7.3 Creating & Managing Checklist Tasks
Selecting the `Checklist` radio button expands the dialog to reveal the sub-item management section.

> **[SCREENSHOT NEEDED: docs/img/03-task-checklist.png]**  
> *Capture the Task Editor with "Checklist" selected, displaying multiple sub-items added, several items checked off, and the dynamic progress bar visible.*

![Checklist Task Creation](img/03-task-checklist.png)

- **Sub-Item Input Field:** Type sub-task text and press `Enter` or click **+ Add**.
- **Interactive Checkboxes:** Sub-items can be checked or unchecked directly within the editor.
- **Removal Button:** Allows deleting selected sub-items prior to saving.
- **Two-Way Completion Logic:** Completing the parent task automatically completes all sub-items; checking all sub-items automatically marks the parent task completed.

---

### 7.4 Creating Deadline Tasks
Selecting the `Deadline` radio button displays the JavaFX `DatePicker` control.

> **[SCREENSHOT NEEDED: docs/img/04-task-deadline.png]**  
> *Capture the Task Editor with "Deadline" selected, showing the open JavaFX DatePicker calendar popup.*

![Deadline Task Creation](img/04-task-deadline.png)

- **Date Picker:** Enforces valid calendar date selection; blank dates trigger inline validation errors.
- **Dynamic Card Highlighting:**
  - **Overdue:** Displays a prominent red `OVERDUE` badge if the deadline has passed.
  - **Due Soon:** Displays an amber `Due in X days` badge if due within 48 hours.

---

### 7.5 Task Cards, Priority Stripes & Visual Indicators
The central `ListView` hosts custom `TaskCell` cards styled via CSS:

> **[SCREENSHOT NEEDED: docs/img/05-card-highlights.png]**  
> *Capture a close-up of multiple task cards in the list view showing the left vertical priority stripe (red/amber/green), gold pin badge in top-right, category chip, and checklist progress bar.*

![Task Card Visual Indicators](img/05-card-highlights.png)

- **Priority Stripe:** Vertical 5px stripe on the card's left edge indicating urgency (Red = High, Amber = Medium, Green = Low).
- **Pin Badge:** Clicking the pin icon toggles pin status; pinned cards display a gold badge and remain anchored at the top.
- **Category Chip:** Rounded badge displaying the category name and its assigned hex color.
- **Completion Checkbox:** Checking the box strikes through the title and reduces card opacity.

---

### 7.6 Category Management Dialog
Clicking **+ Add Category** at the bottom of the sidebar opens the `CategoryDialog`:

> **[SCREENSHOT NEEDED: docs/img/06-category-dialog.png]**  
> *Capture the Category Creation dialog showing the category name field and the color palette selection buttons.*

![Category Management Dialog](img/06-category-dialog.png)

- **Name Input:** Enforces case-insensitive uniqueness; duplicate names display inline red error messages.
- **Color Selector:** Grid of curated hex colors (Blue, Green, Red, Orange, Purple, Teal, Slate).
- **Safe Deletion:** Deleting a custom category automatically reassigns all member tasks to `General`, preventing orphaned tasks. Default categories cannot be deleted.

---

### 7.7 Live Search, Filtering & Sorting
Real-time filtering and sorting controls on the toolbar:

> **[SCREENSHOT NEEDED: docs/img/07-search-filter.png]**  
> *Capture the application with an active search query (e.g. "report") typed in the search box, showing filtered task cards and updated status bar count.*

![Live Search and Filtering](img/07-search-filter.png)

- **Instant Search Bar:** Matches queries across task titles and descriptions with a 200 ms debounce to prevent UI stutter. Pressing `Esc` clears the query.
- **Status Filter:** CheckMenuItem / Toggle to show or hide completed tasks.
- **Sorting Options:** Dropdown allowing sorting by Due Date, Priority, Title (A-Z), or Created Date, with the pinned invariant always preserved.

---

### 7.8 Task Deletion & Undo Toast Notification
Deleting a task prompts a confirmation dialog and activates single-step undo:

> **[SCREENSHOT NEEDED: docs/img/08-undo-toast.png]**  
> *Capture the bottom toast notification banner displaying "Task '...' deleted. [Undo]" alongside the menu bar Edit menu showing "Undo Delete (Ctrl+Z)".*

![Undo Toast Notification Banner](img/08-undo-toast.png)

- **Confirmation Alert:** Protects against accidental clicks on the trash icon or `Delete` key.
- **Toast Notification Banner:** Floats at the bottom for 5 seconds offering an **Undo** button.
- **Menu & Shortcut:** `Edit ▸ Undo Delete` and `Ctrl+Z` immediately restore the deleted task to its exact prior state.

---

### 7.9 About & Keyboard Shortcuts Dialogs
Help menu dialogs providing runtime information and keyboard guidance:

> **[SCREENSHOT NEEDED: docs/img/09-about-dialog.png]**  
> *Capture the "About TODO App" information dialog (`Help ▸ About TODO App`).*

![About Dialog](img/09-about-dialog.png)

> **[SCREENSHOT NEEDED: docs/img/10-shortcuts-dialog.png]**  
> *Capture the "Keyboard Shortcuts" help dialog (`Help ▸ Keyboard Shortcuts`).*

![Keyboard Shortcuts Dialog](img/10-shortcuts-dialog.png)

---

### 7.10 Complete Keyboard Shortcuts Reference

| Key Combination | Action Triggered | Context / Focus Scope |
|---|---|---|
| <kbd>Ctrl</kbd> + <kbd>N</kbd> | Open New Task Dialog | Application-wide |
| <kbd>Ctrl</kbd> + <kbd>Z</kbd> | Undo Last Deleted Task | Application-wide (when undo buffer active) |
| <kbd>Ctrl</kbd> + <kbd>F</kbd> | Focus Search Bar & Select All | Application-wide |
| <kbd>Enter</kbd> / <kbd>F2</kbd> | Open Task Editor for Selected Task | Task Card List |
| <kbd>Delete</kbd> | Delete Selected Task (Prompts confirmation) | Task Card List |
| <kbd>Space</kbd> | Toggle Completion of Selected Task | Task Card List |
| <kbd>Esc</kbd> | Clear Search Query / Dismiss Dialogs | Search Bar / Dialog Windows |
| <kbd>Ctrl</kbd> + <kbd>Enter</kbd> | Save Task & Close Dialog | Task Editor Dialog |
