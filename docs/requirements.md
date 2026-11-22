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


---

## 6. Functional Requirements

The following requirements represent the canonical functional specifications for the system. Priority is defined according to the MoSCoW framework: **M**ust have, **S**hould have, **C**ould have.

| ID | Requirement | Pri |
|---|---|---|
| FR-01 | **Create** a task: required title (1–100 chars), optional description (≤2000 chars), priority, category, task type | M |
| FR-02 | **Read**: see all tasks in a list; open a task to view all its details | M |
| FR-03 | **Update** any editable field of an existing task | M |
| FR-04 | **Delete** a task, with a confirmation prompt | M |
| FR-05 | Mark a task complete / incomplete with one click | M |
| FR-06 | **Prioritize**: High / Medium / Low; change priority quickly from the list; visible colored indicator | M |
| FR-07 | **Sort** by priority, due date, creation date, or title (ascending/descending); pinned tasks always first | M |
| FR-08 | **Categories**: create, rename, delete, recolor; a non-deletable default category ("General"); deleting a category moves its tasks to General | M |
| FR-09 | **Filter** by category, status (all/active/completed) and priority | M |
| FR-10 | **Search** tasks by text in title/description (case-insensitive, live) | S |
| FR-11 | **Checklist tasks**: a task containing items that can be checked off; shows progress (e.g. 2/5) | S |
| FR-12 | **Deadline tasks**: due date; overdue and due-soon highlighting | S |
| FR-13 | **Persistence**: all data is saved automatically and restored on next launch | M |
| FR-14 | **Pin** a task to keep it at the top | S |
| FR-15 | **Undo** the last delete (until app closes or another delete happens) | C |
| FR-16 | Keyboard shortcuts (new task, delete, search focus, toggle complete) | C |
| FR-17 | **Validation**: clear user-facing messages for invalid input (blank title, over-length, duplicate category name, missing due date) | M |

### 6.1 Detailed Functional Specifications

#### FR-01: Task Creation
- The user can select the task type: Simple, Checklist, or Deadline.
- The title is mandatory, trimmed of leading/trailing whitespace, and must be between 1 and 100 characters.
- The description is optional, up to 2000 characters (stored as an empty string if null).
- Priority defaults to `MEDIUM` if unspecified.
- Category defaults to the default category (`General`) if unspecified.
- An immutable UUID is assigned upon creation, along with initial timestamps for `createdAt` and `updatedAt`.

#### FR-02: Task Reading & Inspection
- All tasks are rendered in a scrollable list view.
- Each task displays its title, priority stripe/badge, category chip, pin status, and polymorphic summary.
- Double-clicking or selecting a task opens the full task inspection and editing view.

#### FR-03: Task Updating
- All editable attributes (title, description, priority, category, deadline date, checklist items) may be modified.
- The polymorphic `TaskType` cannot be altered after creation in v1.0.
- Any modification automatically refreshes `updatedAt`.

#### FR-04: Task Deletion
- Initiating deletion opens a modal confirmation dialog displaying the task title.
- Confirming removes the task from the repository and places it into the single-slot undo buffer.
- Canceling the dialog aborts the deletion without altering any state.

#### FR-05: Task Completion
- A checkbox on the task card allows immediate toggling between completed and active.
- For Simple and Deadline tasks, completion marks the task complete and applies strikethrough styling.
- For Checklist tasks, toggling task completion marks all sub-items complete; checking the final incomplete sub-item auto-completes the task; unchecking any sub-item marks the task incomplete.

#### FR-06: Task Prioritization
- Supported priorities: `HIGH` (Rank 1, Red), `MEDIUM` (Rank 2, Amber), `LOW` (Rank 3, Green).
- Priority can be updated via the editor dialog or quickly via the right-click context menu.
- Each task card features a prominent colored indicator corresponding to its priority.

#### FR-07: Sorting
- Supports sorting by: Priority, Due Date (tasks without due dates positioned last regardless of order), Creation Date, or Title (case-insensitive alphabetical).
- Ascending and descending directions are supported for all criteria.
- **Pinned invariant:** Pinned tasks are always sorted to the top before unpinned tasks, with secondary sorting adhering to the chosen criterion and `createdAt` as the tiebreaker.

#### FR-08: Category Management
- Users can create custom categories with a name (1–30 characters, unique case-insensitively) and hex color code (`#RRGGBB`).
- Categories can be renamed and recolored.
- A permanent default category ("General") with fixed ID `Category.DEFAULT_ID` is always present and cannot be deleted or renamed.
- When a user deletes a category, all associated tasks are automatically reassigned to the "General" category.

#### FR-09: Multi-Dimensional Filtering
- The user can filter simultaneously by:
  - Selected Category (or "All Categories")
  - Completion Status (`ALL`, `ACTIVE`, `COMPLETED`)
  - Priority (`ANY`, `HIGH`, `MEDIUM`, `LOW`)
- Only tasks matching all active filter predicates are displayed in the list view.

#### FR-10: Live Keyword Search
- An interactive search field filters tasks dynamically as the user types (case-insensitive substring match).
- The search query checks both the task title and description.
- A clear button ("✕") resets the search query instantly.

#### FR-11: Checklist Tasks
- Tasks can contain an arbitrary number of sub-items (text 1–100 characters each).
- The task summary dynamically renders a progress ratio (e.g., "2/5 done") and a graphical progress bar.
- Individual sub-items can be checked or unchecked independently.

#### FR-12: Deadline Tasks
- Associates a calendar due date (`LocalDate`) with the task.
- Displays a formatted deadline summary (e.g., "Due Fri, 20 Nov").
- Visual highlighting marks tasks as overdue (due date prior to current date and incomplete) or due soon (within 2 days).

#### FR-13: Automatic Local Persistence
- The application automatically persists categories and tasks to a local JSON file (`~/.todoapp/data.json` or custom path).
- Changes are debounced (~300 ms) to prevent excessive disk I/O while guaranteeing data integrity.
- Data writes are atomic (using temporary files and atomic replace) to prevent corruption during unexpected shutdowns.

#### FR-14: Task Pinning
- Any task can be pinned or unpinned via the card pin icon or context menu.
- Pinned tasks display a visual pin icon, distinct card background styling, and stay grouped at the top of the list.

#### FR-15: Undo Last Deletion
- A single-slot undo buffer preserves the most recently deleted task.
- The user can restore the deleted task via the Edit menu, keyboard shortcut (`Ctrl+Z`), or a temporary status bar action.
- The restored task retains its original UUID, timestamps, fields, and checklist items.
- The buffer is cleared once restored or when replaced by a subsequent deletion.

#### FR-16: Keyboard Shortcuts
- `Ctrl+N`: Create new task.
- `Enter` / `F2`: Edit selected task.
- `Delete`: Delete selected task.
- `Ctrl+F`: Focus search input.
- `Space`: Toggle completion of selected task.
- `Ctrl+Z`: Undo last deleted task.

#### FR-17: Comprehensive Input Validation
- Blank titles or titles exceeding 100 characters produce immediate feedback.
- Descriptions exceeding 2000 characters are rejected.
- Duplicate category names (case-insensitive) are rejected.
- Deadline tasks without a selected due date are rejected.
- Validation failures keep input dialogs open without losing user-entered text.

---

## 7. Non-Functional Requirements

The non-functional requirements define the architectural qualities, operational constraints, and quality gates for the application.

| ID | Requirement |
|---|---|
| NFR-01 | **Usability:** core actions reachable in ≤2 clicks; no unexplained errors; empty states have helpful text |
| NFR-02 | **Performance:** UI refresh with 1,000 tasks < 200 ms; startup < 2 s |
| NFR-03 | **Reliability:** no data loss on crash during save (atomic write); corrupt data file never crashes the app |
| NFR-04 | **Portability:** runs on Windows, macOS, Linux with JDK 17+ (JavaFX is fetched by Maven; the shaded JAR is built per operating system, `mvn javafx:run` works everywhere) |
| NFR-05 | **Maintainability:** layered design (model / repository / service / controller / ui); Javadoc on all public types and methods |
| NFR-06 | **Testability:** model, repository, service, storage and controller layers have no JavaFX dependency; ≥80% line coverage there |
| NFR-07 | **Threading:** all scene-graph access on the JavaFX Application Thread; no blocking I/O on that thread during rendering; background work reports back via `Platform.runLater` |

### 7.1 Detailed Non-Functional Specifications

#### NFR-01: Usability & Ergonomics
- Common user workflows (creating a task, marking complete, filtering by category, changing priority) require at most two clicks or a single keyboard shortcut.
- When no tasks exist or no tasks match current search/filter criteria, clean empty state placeholders guide the user ("No tasks yet — click New Task" or "No tasks match your filters").
- User error messages are descriptive and actionable, avoiding raw exception names or technical jargon.

#### NFR-02: Performance & Scalability
- UI updates, sorting, and filtering on data sets of up to 1,000 tasks complete within 200 milliseconds.
- Cold application startup to fully interactive main view takes less than 2.0 seconds on standard desktop hardware.
- Memory consumption remains lightweight (nominal heap < 120 MB).

#### NFR-03: Reliability & Fault Tolerance
- File writes use a write-to-temporary-file and atomic move pattern (`ATOMIC_MOVE`, `REPLACE_EXISTING`) to prevent partial or corrupted file states during sudden power loss or process termination.
- If the data file is unreadable or syntactically corrupt, the app renames the corrupt file to `data.json.corrupt-<timestamp>`, initializes an empty state with default categories, alerts the user, and continues running safely without crashing.

#### NFR-04: Portability & Environment Independence
- Targets standard Java 17 LTS and JavaFX 21 LTS across Windows, macOS, and Linux.
- Builds and runs completely from the command line using Apache Maven without requiring a specific IDE.
- Shaded runnable JAR packaging supports self-contained execution without separate JavaFX SDK installations.

#### NFR-05: Maintainability & Code Quality
- Strict architectural layering: `ui` -> `controller` -> `service` -> `repository` -> `storage` -> `model`. Dependencies point downward only.
- Strict Javadoc documentation on all public classes, interfaces, methods, and constants; compiler and javadoc tool produce zero warnings on public APIs.
- Code conforms to standard Java conventions (UTF-8, LF, 4-space indentation, no magic strings/numbers).

#### NFR-06: Testability & Quality Assurance
- Domain model, repositories, services, storage layer, and controller classes have zero dependencies on `javafx.*` packages, enabling fast headless execution in standard JUnit test runners.
- Automated unit test suite enforces ≥ 80% line coverage across non-UI packages (`model`, `repository`, `service`, `storage`, `controller`) verified via JaCoCo.

#### NFR-07: Threading & Concurrency Safety
- All JavaFX Scene Graph mutations, dialog displays, and UI control updates execute strictly on the JavaFX Application Thread.
- Heavy file I/O operations (such as disk serialization) run off the UI thread via a single-threaded background autosave executor.
- Background error and completion notifications report back to the UI thread using `javafx.application.Platform.runLater`.


---

## 8. Detailed Use Cases

### UC-01: Create Task
- **Primary Actor:** Desktop User
- **Preconditions:** The application is running and the main view is displayed.
- **Main Flow:**
  1. The user clicks the "New Task" button on the toolbar or presses `Ctrl+N`.
  2. The system displays the Task Editor modal dialog with focus on the Title input field.
  3. The user enters a task title (1–100 characters).
  4. The user optionally enters a description (≤2000 characters).
  5. The user selects a task type (Simple, Checklist, or Deadline).
     - *If Checklist:* The user enters checklist items in the sub-item field and presses Enter or "Add".
     - *If Deadline:* The user chooses a due date via the DatePicker control.
  6. The user selects a Priority (High, Medium, or Low) and an existing Category.
  7. The user clicks "Save" (or presses Enter in supported fields).
  8. The system validates the input, constructs a `TaskDraft`, creates the corresponding polymorphic `Task` instance, saves it to the repository, schedules background autosave, refreshes the UI list view, and closes the dialog.
- **Alternative / Error Flows:**
  - *3a. Blank Title:* The user attempts to save with an empty or whitespace-only title.
    - *System Response:* Highlights the title field, displays an inline error message ("Title is required"), consumes the event, and keeps the dialog open with entered data preserved.
  - *3b. Over-length Title:* The title exceeds 100 characters.
    - *System Response:* Displays inline error message ("Title must not exceed 100 characters") and keeps the dialog open.
  - *5a. Missing Due Date for Deadline Task:* The user selects Deadline type but leaves the due date blank.
    - *System Response:* Displays inline error message ("Due date is required for deadline tasks") and keeps the dialog open.
  - *7a. Cancellation:* The user clicks "Cancel" or presses `Esc`.
    - *System Response:* Closes the dialog without creating a task or altering state.
- **Postconditions:** A new task is added to the repository, displayed in the task list, and written to persistent storage.

### UC-02: View Task Details
- **Primary Actor:** Desktop User
- **Preconditions:** At least one task exists in the current view.
- **Main Flow:**
  1. The user browses the task list and locates the desired task card.
  2. The card displays summary information: title, priority stripe, category chip, pin badge, and polymorphic status summary (summary text, checklist ratio progress, or deadline string).
  3. The user double-clicks the task card or presses `Enter`/`F2`.
  4. The system opens the Task Editor dialog pre-populated with all saved details in inspection/edit mode.
- **Alternative / Error Flows:**
  - *1a. Deleted Task Concurrency:* The user attempts to open a task that was recently deleted in a background flow.
    - *System Response:* Notifies the user via status bar and refreshes the list view.
- **Postconditions:** The user has reviewed all task details; no state is mutated.

### UC-03: Edit Task
- **Primary Actor:** Desktop User
- **Preconditions:** The target task exists in the repository.
- **Main Flow:**
  1. The user selects a task and triggers edit mode (double-click, right-click context menu "Edit", or `F2`).
  2. The system presents the Task Editor dialog with current values populated. (The `TaskType` selector is disabled).
  3. The user modifies editable fields (title, description, priority, category, deadline date, or checklist items).
  4. The user clicks "Save".
  5. The system validates the modified fields, applies the changes in place via `TaskFactory.apply`, updates `updatedAt`, triggers change listeners, schedules autosave, and refreshes the view.
- **Alternative / Error Flows:**
  - *3a. Invalid Input:* Input fails validation checks (empty title, missing date, over-length).
    - *System Response:* Displays inline error message and keeps dialog open.
  - *4a. Cancel Edit:* User clicks "Cancel" or presses `Esc`.
    - *System Response:* Closes dialog; original task attributes remain unchanged.
- **Postconditions:** Task state is updated in memory and scheduled for persistent storage.

### UC-04: Delete Task & Undo
- **Primary Actor:** Desktop User
- **Preconditions:** A task is selected in the list view.
- **Main Flow:**
  1. The user initiates deletion (clicks "Delete" in context menu or presses the `Delete` key).
  2. The system displays a confirmation dialog: "Are you sure you want to delete '[Task Title]'? This action can be undone."
  3. The user confirms deletion.
  4. The system removes the task from the repository, stores a copy in the single-slot undo buffer, triggers change listeners, and refreshes the list.
  5. The status bar displays an information banner: "Task deleted — Undo" with an active Undo button.
- **Alternative / Error Flows:**
  - *3a. User Cancels:* The user clicks "Cancel" in the confirmation dialog.
    - *System Response:* Dialog closes; task remains in list.
  - *5a. User Triggers Undo:* Within the active session, the user clicks "Undo" or presses `Ctrl+Z`.
    - *System Response:* The system restores the task from the undo buffer into the repository with original UUID and attributes, clears the undo buffer, refreshes the view, and notifies via status bar ("Task restored").
- **Postconditions:** The task is removed from active storage (or subsequently restored if undo is triggered).

### UC-05: Prioritize Task
- **Primary Actor:** Desktop User
- **Preconditions:** Target task exists in the view.
- **Main Flow:**
  1. The user right-clicks a task card to open the context menu.
  2. The user navigates to the "Priority" sub-menu and selects "High", "Medium", or "Low".
  3. The system updates the task's priority attribute, sets `updatedAt` to now, notifies change listeners, updates the card's priority stripe immediately, and triggers autosave.
- **Alternative / Error Flows:**
  - *None:* Selecting an already active priority acts as a no-op.
- **Postconditions:** The task's priority reflects the new selection and displays the corresponding color badge.

### UC-06: Manage Categories
- **Primary Actor:** Desktop User
- **Preconditions:** The application is running.
- **Main Flow:**
  1. **Add Category:** User clicks "＋" in the Category sidebar. CategoryDialog appears. User enters a name (1–30 characters) and selects a color swatch. Clicking "Save" registers the category and updates the sidebar.
  2. **Rename / Recolor:** User selects a custom category and clicks "Edit Category". User adjusts name or color. Clicking "Save" updates all task cards displaying this category.
  3. **Delete Category:** User selects a custom category and clicks "Delete". The system displays confirmation: "Delete category '[Name]'? All tasks in this category will move to General." Upon confirmation, all associated tasks have their `categoryId` changed to `Category.DEFAULT_ID`, the category is deleted from the repository, and the view refreshes.
- **Alternative / Error Flows:**
  - *1a. Duplicate Name:* User enters a name that matches an existing category (case-insensitive).
    - *System Response:* Displays inline error ("A category named '[Name]' already exists") and keeps dialog open.
  - *2a. Modify Default Category:* User selects "General".
    - *System Response:* Delete and Rename buttons are disabled (color change only is permitted).
- **Postconditions:** Categories are updated; tasks maintain referential integrity without orphan states.

### UC-07: Filter, Sort & Search Tasks
- **Primary Actor:** Desktop User
- **Preconditions:** The application is running with tasks loaded.
- **Main Flow:**
  1. **Filter by Category:** User clicks a category in the sidebar (or "All"). The list updates to display only matching tasks.
  2. **Filter by Status:** User selects "All", "Active", or "Completed" from the status combo box.
  3. **Filter by Priority:** User selects "Any", "High", "Medium", or "Low" from the priority combo box.
  4. **Sort:** User selects sort criterion (Priority, Due Date, Creation Date, or Title) and toggles Ascending/Descending. The list immediately rearranges, keeping pinned tasks at the very top.
  5. **Search:** User types into the search box. Within ~200 ms debounce, the list filters in real time matching text in either title or description.
  6. **Clear Filters:** User clicks "Clear Filters" or search clear button ("✕"). All filter criteria reset to default ("All", "All Status", "Any Priority", empty search).
- **Alternative / Error Flows:**
  - *No Matches:* Filter/search combinations yield zero results.
    - *System Response:* Displays placeholder message: "No tasks match your filters".
- **Postconditions:** The view displays only tasks satisfying the composed predicates in the requested order.

---

## 9. Acceptance Criteria (Given / When / Then)

Acceptance criteria are specified for all **Must** requirements (`FR-01` through `FR-09`, `FR-13`, `FR-17`):

### Scenario 1: Create Simple Task (FR-01, FR-17)
- **Given:** The user has the Task Editor open.
- **When:** The user enters title "Review pull request #42", priority "High", category "General", and clicks "Save".
- **Then:** The task is created, appears in the task list with a red priority stripe, and is scheduled for persistence.

### Scenario 2: Reject Blank Title (FR-01, FR-17)
- **Given:** The user has the Task Editor open.
- **When:** The user leaves the title blank or types only whitespace and clicks "Save".
- **Then:** An error message "Title is required" is shown, the dialog remains open, and no task is created.

### Scenario 3: Reject Over-length Title (FR-01, FR-17)
- **Given:** The user has the Task Editor open.
- **When:** The user enters a title containing 101 characters and clicks "Save".
- **Then:** An error message "Title must not exceed 100 characters" is shown, the dialog remains open, and no task is created.

### Scenario 4: Read Task Details (FR-02)
- **Given:** A task titled "Prepare CS502 Presentation" exists in the repository.
- **When:** The user double-clicks the task in the list view.
- **Then:** The Task Editor dialog opens with all fields matching the task's stored title, description, priority, and category.

### Scenario 5: Update Existing Task (FR-03)
- **Given:** An existing task with title "Draft Report" and priority "Medium".
- **When:** The user changes the title to "Draft Final Project Report", priority to "High", and clicks "Save".
- **Then:** The task card immediately reflects the new title and red priority stripe, and `updatedAt` is refreshed.

### Scenario 6: Delete Task with Confirmation (FR-04)
- **Given:** An existing task selected in the task list.
- **When:** The user presses the `Delete` key and clicks "Confirm" in the confirmation prompt.
- **Then:** The task is removed from the task list and placed into the undo buffer.

### Scenario 7: Cancel Task Deletion (FR-04)
- **Given:** An existing task selected in the task list.
- **When:** The user presses the `Delete` key and clicks "Cancel" in the confirmation prompt.
- **Then:** The dialog closes and the task remains unaffected in the task list.

### Scenario 8: Toggle Task Completion (FR-05)
- **Given:** An active task in the task list.
- **When:** The user clicks the checkbox on the task card.
- **Then:** The task is marked completed, title displays strikethrough styling, and active task count decrements by 1.

### Scenario 9: Quick Priority Change (FR-06)
- **Given:** A task with "Low" priority (green stripe).
- **When:** The user right-clicks the card and selects Priority -> High.
- **Then:** The task priority updates to High, and the indicator stripe turns red immediately.

### Scenario 10: Sort with Pinned Invariant (FR-07, FR-14)
- **Given:** Three tasks exist: Task A (unpinned, title "Alpha"), Task B (pinned, title "Zeta"), Task C (unpinned, title "Beta").
- **When:** The user sorts by Title in ascending order.
- **Then:** Task B (pinned) appears first, followed by Task A, then Task C.

### Scenario 11: Delete Category Reassigns Tasks (FR-08)
- **Given:** A custom category "Work" contains 3 tasks, and the default "General" category exists.
- **When:** The user deletes the "Work" category and confirms the action.
- **Then:** The "Work" category is removed, and all 3 tasks are now assigned to the "General" category.

### Scenario 12: Reject Duplicate Category Name (FR-08, FR-17)
- **Given:** A category named "Urgent" exists.
- **When:** The user attempts to create a new category named "urgent" (different case).
- **Then:** A validation error "A category named 'urgent' already exists" is displayed, and the category is not created.

### Scenario 13: Multi-Criteria Filter Combination (FR-09)
- **Given:** 5 tasks across categories "General" and "Study", with mixed priorities and completion states.
- **When:** The user selects category "Study", status "Active", and priority "High".
- **Then:** Only tasks that are both in "Study", active, AND High priority are displayed.

### Scenario 14: Data Persistence Round-Trip (FR-13, NFR-03)
- **Given:** The application has created tasks and custom categories and been closed.
- **When:** The application is restarted with the same data file path.
- **Then:** All tasks, categories, completion states, and checklist items are restored exactly as saved.

---

## 10. Assumptions & Constraints

- **Single User & Local Execution:** The application is designed as a standalone personal desktop utility with single-process, single-user access to a local JSON data file.
- **Platform Independence:** The software must run on standard 64-bit desktop operating systems (Windows 10/11, macOS 12+, Ubuntu Linux 20.04+) with JDK 17+ installed.
- **JavaFX 21 LTS Runtime:** JavaFX controls and graphics are resolved via Maven dependencies, requiring no separate platform SDK installation.
- **File System Permissions:** The application assumes read and write permissions to the user directory (`~/.todoapp/` or user-specified path).
- **Display Environment:** The GUI requires an active graphical window manager/display environment to launch (headless execution is supported for domain/service tests and graceful exit on headless startup).


---

## 11. Requirements Traceability Matrix

The traceability matrix establishes bi-directional alignment between requirements, development milestones, implementing classes, and automated verification suites. The Milestone column is populated below; implementing classes and test suites will be reconciled upon release in milestone M13.

| Requirement | Milestone(s) | Class(es) [M13] | Test(s) [M13] |
|---|---|---|---|
| **FR-01** (Create task) | M03, M05, M08, M09 | `TaskDraft`, `TaskFactory`, `TaskService`, `InMemoryTaskRepository`, `MainController`, `TaskEditorDialog` | `TaskModelTest`, `TaskServiceTest`, `MainControllerTest`, `EdgeCaseAndIntegrationTest` |
| **FR-02** (Read task) | M03, M05, M08, M09 | `Task`, `TaskService`, `InMemoryTaskRepository`, `MainController`, `TaskListPane`, `TaskCell` | `TaskModelTest`, `TaskServiceTest`, `MainControllerTest`, `RepositoryTest` |
| **FR-03** (Update task) | M03, M05, M09 | `Task`, `TaskFactory`, `TaskService`, `MainController`, `TaskEditorDialog` | `TaskModelTest`, `TaskServiceTest`, `MainControllerTest` |
| **FR-04** (Delete task) | M05, M09 | `TaskService`, `InMemoryTaskRepository`, `MainController`, `TaskCell`, `AppMenuBar` | `TaskServiceTest`, `RepositoryTest`, `MainControllerTest` |
| **FR-05** (Mark complete / incomplete) | M03, M05, M09 | `Task`, `ChecklistTask`, `TaskService`, `MainController`, `TaskCell` | `TaskModelTest`, `TaskServiceTest`, `MainControllerTest` |
| **FR-06** (Prioritize) | M03, M05, M08, M09 | `Priority`, `Task`, `TaskService`, `MainController`, `TaskCell` | `TaskModelTest`, `TaskServiceTest`, `MainControllerTest` |
| **FR-07** (Sort) | M06, M08, M10 | `SortOption`, `TaskQuery`, `TaskService`, `MainController`, `ToolbarPane` | `TaskQueryEngineTest`, `TaskServiceTest`, `MainControllerTest` |
| **FR-08** (Categories) | M03, M04, M10 | `Category`, `CategoryRepository`, `CategoryService`, `MainController`, `CategoryPane`, `CategoryDialog` | `CategoryServiceTest`, `RepositoryTest`, `MainControllerTest` |
| **FR-09** (Filter) | M06, M08, M10 | `StatusFilter`, `TaskQuery`, `TaskService`, `MainController`, `ToolbarPane`, `CategoryPane` | `TaskQueryEngineTest`, `TaskServiceTest`, `MainControllerTest` |
| **FR-10** (Search) | M06, M08, M10 | `TaskQuery`, `TaskService`, `MainController`, `ToolbarPane` | `TaskQueryEngineTest`, `TaskServiceTest`, `MainControllerTest` |
| **FR-11** (Checklist tasks) | M03, M07, M09 | `ChecklistItem`, `ChecklistTask`, `JsonMapper`, `TaskEditorDialog`, `TaskCell` | `TaskModelTest`, `JsonMapperTest`, `EdgeCaseAndIntegrationTest` |
| **FR-12** (Deadline tasks) | M03, M07, M09, M11 | `DeadlineTask`, `JsonMapper`, `TaskEditorDialog`, `TaskCell` | `TaskModelTest`, `JsonMapperTest`, `EdgeCaseAndIntegrationTest` |
| **FR-13** (Persistence) | M07, M08 | `Storage`, `JsonStorage`, `JsonMapper`, `AppData`, `AutosaveListener`, `DataSync` | `JsonStorageTest`, `JsonMapperTest`, `AutosaveListenerTest`, `DataSyncTest`, `EdgeCaseAndIntegrationTest` |
| **FR-14** (Pin task) | M03, M05, M06, M10 | `Task`, `TaskService`, `MainController`, `TaskCell` | `TaskModelTest`, `TaskServiceTest`, `TaskQueryEngineTest`, `MainControllerTest` |
| **FR-15** (Undo delete) | M05, M11 | `TaskService`, `MainController`, `MainView`, `AppMenuBar` | `TaskServiceTest`, `MainControllerTest` |
| **FR-16** (Keyboard shortcuts) | M11 | `App`, `AppMenuBar`, `TaskListPane`, `ToolbarPane` | `AppSmokeTest`, `docs/test-plan.md` (TS-01..10) |
| **FR-17** (Validation) | M03, M04, M05, M09, M10 | `TaskDraft`, `TaskService`, `CategoryService`, `ValidationException`, `TaskEditorDialog`, `CategoryDialog` | `TaskModelTest`, `TaskServiceTest`, `CategoryServiceTest`, `EdgeCaseAndIntegrationTest` |
| **NFR-01** (Usability) | M08, M09, M10, M11 | `MainView`, `TaskCell`, `Theme`, `StatusBar`, `AppMenuBar` | `docs/test-plan.md` (Manual GUI Script TS-01..12) |
| **NFR-02** (Performance) | M06, M12 | `TaskService`, `TaskQuery`, `JsonStorage` | `EdgeCaseAndIntegrationTest` (`testHighVolumeQueryPerformance`) |
| **NFR-03** (Reliability) | M07, M12 | `JsonStorage`, `AutosaveListener`, `StorageException` | `JsonStorageTest`, `AutosaveListenerTest`, `EdgeCaseAndIntegrationTest` |
| **NFR-04** (Portability) | M00, M12, M13 | `Launcher`, `App`, `AppConfig`, `pom.xml` | `AppConfigTest`, `AppSmokeTest`, Runnable Shaded JAR build |
| **NFR-05** (Maintainability) | M00, M03–M13 | All classes in `com.todoapp.*` (`-Xlint:all` clean, zero public API warnings) | Maven Compiler Linting, Javadoc verification |
| **NFR-06** (Testability) | M03–M08, M12 | Domain, service, repository, storage, controller packages (zero JavaFX imports) | 147 JUnit 5 tests, JaCoCo line coverage > 95% (threshold ≥80%) |
| **NFR-07** (Threading) | M07, M08, M11, M12 | `AutosaveListener`, `MainController`, `MainView`, `Platform.runLater` | `AutosaveListenerTest`, Threading audit |
