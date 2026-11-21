# Software Requirements Specification (SRS)
## TODO List Desktop Application

> **Project:** Simple TODO list desktop application (Google Keep / Microsoft Notes style) in Java 17 and JavaFX 21.  
> **Course / Context:** Software Construction — Lab Open-Ended Lab (OEL)  
> **Assignment Task Mapping:** Task 1 (Define requirements and features) · **CLO:** CLO 2 (Analyze and specify software requirements).

---

## 1. Purpose & Scope

### 1.1 Purpose
The purpose of this document is to specify the functional and non-functional requirements for the TODO List Desktop Application. The application is a single-user personal task manager inspired by lightweight, fluid desktop note-taking applications (such as Google Keep and Microsoft OneNote/Sticky Notes). It emphasizes clear object-oriented design, responsive desktop interaction via JavaFX, robust input validation, and automatic local data persistence.

### 1.2 In-Scope Capabilities
- **Task Lifecycle (CRUD):** Creating, viewing, editing, completing, and deleting tasks.
- **Polymorphic Task Types:** Support for Simple tasks (basic notes), Checklist tasks (sub-items with completion tracking), and Deadline tasks (due-date tracking with overdue and due-soon badges).
- **Task Prioritization:** Clear High, Medium, and Low priorities with visual indicator stripes and quick-action updates.
- **Categorization:** Color-coded categories with a guaranteed default "General" category and safe cascade handling upon deletion.
- **Organization & Retrieval:** Multi-criteria filtering (category, status, priority), multi-attribute sorting (priority, due date, creation date, title), and live keyword search.
- **Pinned Tasks:** Anchoring critical tasks at the top of the list regardless of the active sorting criterion.
- **Safety & Usability:** Delete confirmation dialogues, single-slot undo buffer, validation error feedback, and standard keyboard shortcuts.
- **Storage:** Local atomic JSON persistence with background debounced autosave.

### 1.3 Out-of-Scope (v1.0 Release)
The following capabilities are deliberately excluded from version 1.0:
- Cloud synchronization and multi-device accounts.
- Multi-user authentication, access control, and collaboration.
- Operating-system level background push notifications and audible alarms.
- Recurring or repeating task schedules (daily, weekly, custom intervals).
- Rich-text formatting (bold, italic, markdown inside text bodies) or image attachments.
- Mobile, web, or browser-based companion clients.
- Drag-and-drop manual card reordering.

---

## 2. Stakeholders & User Personas

| Stakeholder / Persona | Role & Background | Core Needs & Frustrations |
|---|---|---|
| **Sarah Chen** (Undergraduate Student) | Third-year computer science student balancing coursework, lab assignments, and group projects. | Needs fast, distraction-free capture of assignment deadlines and sub-task checklists; frustrated by heavy cloud productivity tools that require accounts or lag when offline. |
| **Marcus Vance** (Consultant / Software Engineer) | Freelance technical consultant managing multiple client engagements, deliverables, and personal goals. | Needs quick prioritization (High/Medium/Low), categorization per client project, instant search, and keyboard shortcuts to manage daily todos without interrupting development workflows. |

---

## 3. Glossary

- **Task:** The fundamental domain entity representing an actionable item with an immutable UUID, title, description, category reference, priority, completion flag, pin flag, and timestamps.
- **Simple Task:** A standard task subclass containing basic textual title and detailed notes.
- **Checklist Task:** A specialized task subclass containing an ordered collection of checklist sub-items, dynamically calculating completion percentage and summary ratios.
- **ChecklistItem:** An individual actionable sub-item within a checklist task with its own completion state.
- **Deadline Task:** A specialized task subclass associated with a specific calendar due date, evaluating whether it is overdue or due soon.
- **Category:** An organizational tag defined by a unique name and color code (`#RRGGBB`).
- **General Category:** The fixed, permanent default category (`Category.DEFAULT_ID`) that cannot be deleted or renamed.
- **Priority:** An urgency level (`HIGH`, `MEDIUM`, `LOW`) with assigned numeric ranks and distinct visual colors.
- **Pinned Task:** A task flagged with `pinned = true`, forcing it to display at the top of any queried or sorted list.
- **Autosave:** A debounced background process that automatically flushes dirty application state to persistent storage without user intervention.
- **Undo Buffer:** An ephemeral single-item restore slot enabling instant recovery of the most recently deleted task.

---

## 4. Use-Case Overview

```mermaid
flowchart LR
    User([Desktop User])

    subgraph Core Task Management
        UC01(UC-01: Create Task)
        UC02(UC-02: View Task Details)
        UC03(UC-03: Edit Task)
        UC04(UC-04: Delete Task & Undo)
        UC05(UC-05: Prioritize Task)
    end

    subgraph Organization & Search
        UC06(UC-06: Manage Categories)
        UC07(UC-07: Filter, Sort & Search)
        UC08(UC-08: Pin / Unpin Task)
    end

    User --> UC01
    User --> UC02
    User --> UC03
    User --> UC04
    User --> UC05
    User --> UC06
    User --> UC07
    User --> UC08
```

---

## 5. User Stories

Each user story directly maps to a functional requirement (`FR-01` through `FR-17`):

- **US-01 (FR-01):** As a student, I want to create tasks with a title, optional description, priority, category, and task type, so that I can systematically record my coursework and obligations.
- **US-02 (FR-02):** As a user, I want to view all tasks in an organized list and open any task to inspect its details, so that I maintain clear visibility over my commitments.
- **US-03 (FR-03):** As a professional, I want to update editable fields of existing tasks, so that I can keep task details aligned with changing project specifications.
- **US-04 (FR-04):** As a user, I want a confirmation prompt before deleting a task, so that I avoid losing notes due to inadvertent clicks or keypresses.
- **US-05 (FR-05):** As a user, I want to toggle task completion status with a single click, so that marking work complete is immediate and satisfying.
- **US-06 (FR-06):** As a consultant, I want to designate tasks as High, Medium, or Low priority with distinct visual colors and fast list updates, so that critical tasks immediately stand out.
- **US-07 (FR-07):** As a student, I want to sort tasks by priority, due date, creation date, or title, while keeping pinned tasks anchored on top, so that I can arrange my workload logically.
- **US-08 (FR-08):** As a user, I want to create, rename, recolor, and delete categories, with tasks of deleted categories automatically moving to "General", so that organizing tasks never risks orphan data.
- **US-09 (FR-09):** As a professional, I want to filter tasks by category, status (All, Active, Completed), and priority, so that I can eliminate noise and focus on what needs action right now.
- **US-10 (FR-10):** As a user, I want real-time live keyword search across titles and descriptions, so that I can retrieve relevant tasks in milliseconds.
- **US-11 (FR-11):** As a student, I want checklist tasks where I can add, toggle, and delete sub-items while observing overall progress ratios, so that I can execute multi-step assignments methodically.
- **US-12 (FR-12):** As a professional, I want deadline tasks with due dates and visual overdue/due-soon alerts, so that I deliver client milestones on schedule.
- **US-13 (FR-13):** As a user, I want my tasks, categories, and settings automatically saved and restored on startup, so that I never lose work when closing or restarting the app.
- **US-14 (FR-14):** As a user, I want to pin vital tasks to the top of the list, so that key reminders remain front and center regardless of active sorting.
- **US-15 (FR-15):** As a user, I want an undo mechanism after deleting a task, so that I can immediately reverse an accidental deletion without data loss.
- **US-16 (FR-16):** As a power user, I want keyboard shortcuts for new task, delete, search focus, undo, and completion toggle, so that I can operate the app efficiently without reaching for the mouse.
- **US-17 (FR-17):** As a user, I want clear, inline validation messages whenever input violates constraints, so that I understand exactly what to adjust before saving.
