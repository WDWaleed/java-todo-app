# TODO List Desktop App — User Guide

Welcome to the **TODO List Desktop App** user guide! This manual walks you through all features, workflows, and keyboard shortcuts to help you manage your daily tasks, checklists, deadlines, and categories efficiently.

---

## Table of Contents

1. [Getting Started](#1-getting-started)
2. [Interface Overview](#2-interface-overview)
3. [Creating Tasks](#3-creating-tasks)
   - [Simple Tasks](#31-simple-tasks)
   - [Checklist Tasks](#32-checklist-tasks)
   - [Deadline Tasks](#33-deadline-tasks)
4. [Managing & Editing Tasks](#4-managing--editing-tasks)
   - [Editing Tasks](#41-editing-tasks)
   - [Quick Priority Changes](#42-quick-priority-changes)
   - [Completing Tasks](#43-completing-tasks)
   - [Pinning Tasks to Top](#44-pinning-tasks-to-top)
5. [Deleting & Restoring Tasks (Undo)](#5-deleting--restoring-tasks-undo)
6. [Category Management](#6-category-management)
   - [Default Categories](#61-default-categories)
   - [Creating Custom Categories](#62-creating-custom-categories)
   - [Deleting Categories & Safe Reassignment](#63-deleting-categories--safe-reassignment)
7. [Searching, Filtering & Sorting](#7-searching-filtering--sorting)
   - [Live Search](#71-live-search)
   - [Status & Category Filters](#72-status--category-filters)
   - [Sorting Options & Pinned Invariant](#73-sorting-options--pinned-invariant)
8. [Data Storage & Backup](#8-data-storage--backup)
   - [File Location](#81-file-location)
   - [Autosave & Crash Resilience](#82-autosave--crash-resilience)
   - [How to Reset the Application](#83-how-to-reset-the-application)
9. [Keyboard Shortcuts Reference](#9-keyboard-shortcuts-reference)
10. [Troubleshooting & FAQs](#10-troubleshooting--faqs)

---

## 1. Getting Started

### Launching the Application
You can start the application from your terminal using Maven:

```bash
mvn javafx:run
```

Or, if you have packaged the standalone runnable JAR:

```bash
java -jar target/todo-app-1.0.0.jar
```

### Trying Sample Data (Demo Mode)
If you are opening the application for the first time and want to explore its features with pre-populated data, launch it with the `--seed` flag:

```bash
mvn javafx:run -Djavafx.args="--seed --data=todo-dev.json"
```

This generates approximately 12 realistic sample tasks spanning Simple, Checklist, and Deadline types across different categories and priorities.

---

## 2. Interface Overview

The application features a clean, card-based interface inspired by modern task tools:

```
┌──────────────────────────────────────────────────────────────────────────────┐
│ File   Edit   View   Help                                        (Menu Bar)  │
├──────────────────────────────────────────────────────────────────────────────┤
│ [+ New Task]   [🔍 Search tasks...]   [Filter: All/Active]  [Sort: Due Date] │
├───────────────────────┬──────────────────────────────────────────────────────┤
│ CATEGORIES            │ TASK LIST                                            │
│ • All Tasks (12)      │ ┌──────────────────────────────────────────────────┐ │
│ • General (3)         │ │ 📌 Finish lab report [High] [Work]             │ │
│ • Work (4)            │ │ Due tomorrow • 4/5 items (80%)                   │ │
│ • Personal (3)        │ └──────────────────────────────────────────────────┘ │
│ • Shopping (2)        │ ┌──────────────────────────────────────────────────┐ │
│                       │ │ Buy groceries [Medium] [Shopping]                │ │
│ [+ Add Category]      │ │ Milk, Bread, Apples                              │ │
│                       │ └──────────────────────────────────────────────────┘ │
├───────────────────────┴──────────────────────────────────────────────────────┤
│ Ready • 12 tasks (8 active, 4 completed) • Autosaved              (Status Bar)│
└──────────────────────────────────────────────────────────────────────────────┘
```

1. **Menu Bar:** Standard menus for file management, editing actions (including Undo), view filtering, and Help/About.
2. **Top Toolbar:** Quick-action button for "+ New Task", instant search bar, completion filter toggle, and sort selector.
3. **Category Sidebar:** Lists all categories with live task counts. Allows single-click filtering and category creation.
4. **Task Card List:** Displays your tasks as modern cards with priority color stripes, category badges, progress bars, and pin badges.
5. **Toast Banner:** A non-intrusive floating bar at the bottom for notifications and 1-click Undo.
6. **Status Bar:** Real-time statistics: total task count, active vs completed counts, and autosave status.

---

## 3. Creating Tasks

Press <kbd>Ctrl</kbd> + <kbd>N</kbd> or click the **+ New Task** button on the toolbar to open the **Task Editor Dialog**.

### 3.1 Simple Tasks
Use Simple Tasks for standard, straightforward to-do items:
1. Enter a **Title** (1 to 100 characters, required).
2. Enter an optional **Description** (detailed notes, links, or instructions).
3. Select **Task Type: Simple**.
4. Choose a **Priority**:
   - **High** (Red stripe)
   - **Medium** (Amber stripe)
   - **Low** (Green stripe)
5. Choose a **Category** from the dropdown.
6. Click **Save** (or press <kbd>Ctrl+Enter</kbd>).

### 3.2 Checklist Tasks
Use Checklist Tasks for complex activities that break down into multiple steps:
1. Select **Task Type: Checklist**.
2. A checklist section will appear below the description.
3. Type an item into the input field and click **+ Add** (or press <kbd>Enter</kbd>).
4. You can add as many sub-items as needed.
5. Sub-items have their own checkboxes. Check off sub-items as you complete them.
6. **Automatic Progress Tracking:** The task card displays a real-time progress ratio (e.g. `2/4 (50%)`) with an interactive visual progress bar.
7. **Two-Way Completion Logic:**
   - Checking the main task checkbox automatically checks all sub-items.
   - Checking off all individual sub-items automatically marks the parent task complete.

### 3.3 Deadline Tasks
Use Deadline Tasks for time-sensitive commitments:
1. Select **Task Type: Deadline**.
2. A **Due Date** picker will appear.
3. Select the target date from the calendar dropdown.
4. **Visual Urgency Highlights:**
   - **Overdue:** If the due date has already passed, the task card displays a prominent **red Overdue badge**.
   - **Due Soon:** If the task is due within the next 48 hours, it displays an **amber Due Soon badge**.
   - Upcoming deadlines display the formatted target date (e.g. `Due: Nov 28, 2026`).

---

## 4. Managing & Editing Tasks

### 4.1 Editing Tasks
To edit an existing task:
- Double-click the task card, **OR**
- Select the card and press <kbd>Enter</kbd> or <kbd>F2</kbd>, **OR**
- Right-click the card and choose **Edit Task**.

In the editor dialog, you can update the title, description, priority, category, deadline date, or sub-items. *(Note: The fundamental task type cannot be mutated after creation to preserve polymorphic integrity).*

### 4.2 Quick Priority Changes
You can change a task's priority instantly without opening the dialog:
1. Right-click the task card to open the context menu.
2. Hover over **Priority**.
3. Select **High**, **Medium**, or **Low**.
4. The color stripe updates immediately and the change is automatically saved.

### 4.3 Completing Tasks
- Click the **checkbox** on the left side of any task card, **OR**
- Select the task card and tap the <kbd>Space</kbd> bar.
- Completed tasks are displayed with struck-through titles and muted contrast.

### 4.4 Pinning Tasks to Top
Keep your most important tasks permanently visible:
- Click the **Pin icon** in the upper-right corner of any task card, **OR**
- Right-click the card and choose **Pin to Top** / **Unpin**.
- Pinned cards display a gold pin badge and **always stay at the top** of the task list, regardless of the active sorting option.

---

## 5. Deleting & Restoring Tasks (Undo)

### Deleting a Task
To delete a task:
1. Select the task and press the <kbd>Delete</kbd> key, **OR**
2. Click the **Trash icon** on the task card, **OR**
3. Right-click the card and select **Delete Task**.
4. A confirmation dialog appears asking to confirm the deletion. Confirming removes the task from the list.

### Restoring a Deleted Task (Undo)
If you deleted a task by mistake, you can restore it within seconds:
- **Toast Notification:** A temporary banner pops up at the bottom: `"Task '...' deleted. [Undo]"`. Click **Undo**.
- **Keyboard Shortcut:** Press <kbd>Ctrl</kbd> + <kbd>Z</kbd> immediately to restore the deleted task.
- **Menu Bar:** Click `Edit ▸ Undo Delete`.

The task is restored to its exact previous state, including its completion status, sub-items, category, and pin status.

---

## 6. Category Management

Categories allow you to organize tasks by context, project, or domain.

### 6.1 Default Categories
The application comes pre-configured with four standard categories:
- **General** (Default fallback category)
- **Work**
- **Personal**
- **Shopping**

### 6.2 Creating Custom Categories
1. In the left sidebar, click the **+ Add Category** button.
2. Enter a unique category name (e.g. `University`, `Finance`, `Fitness`).
3. Pick a custom color using the color selector.
4. Click **Create**.
5. The new category appears in the sidebar and becomes immediately available in the task editor dropdown.

### 6.3 Deleting Categories & Safe Reassignment
- To delete a custom category, hover over it in the sidebar and click the delete button.
- **Zero Orphan Guarantee:** When a category is deleted, all tasks currently belonging to that category are **automatically reassigned to the "General" category**. No tasks are ever lost or left without a valid category.
- Default categories cannot be deleted, ensuring a safe fallback is always available.

---

## 7. Searching, Filtering & Sorting

### 7.1 Live Search
- Click the search input on the top toolbar or press <kbd>Ctrl</kbd> + <kbd>F</kbd>.
- Type any keyword.
- The task list updates dynamically as you type, matching text across both task **titles** and **descriptions**.
- Press <kbd>Esc</kbd> to clear the search query.

### 7.2 Status & Category Filters
- **By Category:** Click any category in the left sidebar to isolate tasks belonging to that category. Click **All Tasks** to clear the category filter.
- **By Completion Status:** Check or uncheck **Show Completed Tasks** on the toolbar or via `View ▸ Show Completed Tasks`. When unchecked, completed tasks are hidden, keeping your view focused on active tasks.
- **Filter Combinations:** Filters work together! You can search for a keyword while filtering by category "Work" and viewing only active tasks.

### 7.3 Sorting Options & Pinned Invariant
Use the **Sort by** dropdown on the toolbar to reorder tasks:
- **Due Date:** Soonest deadlines first; tasks without deadlines follow.
- **Priority:** High priority first, then Medium, then Low.
- **Title (A–Z):** Alphabetical order by title.
- **Created Date:** Newest tasks first.

> **The Pinned Invariant:** Pinned tasks are always sorted among themselves at the very top of the list, followed by unpinned tasks sorted according to your selected criteria.

---

## 8. Data Storage & Backup

### 8.1 File Location
All tasks, categories, and application states are saved locally in a single JSON file:
- **Windows:** `C:\Users\<YourUsername>\.todoapp\data.json`
- **macOS / Linux:** `~/.todoapp/data.json`

### 8.2 Autosave & Crash Resilience
- **Autosave:** Any change you make (creating, editing, deleting, checking off items) triggers a debounced background save (after 300 ms of inactivity). You never need to click a "Save" button.
- **Atomic Writes:** Saves are written to a temporary file (`data.json.tmp`) first, and then swapped atomically into `data.json`. This guarantees that a sudden power loss or process kill will never leave your data file half-written or corrupted.
- **Corrupt File Isolation:** If the data file is tampered with or corrupted externally, the application renames the unreadable file to `data.json.corrupt-<timestamp>` for inspection and initializes a clean, working state.

### 8.3 How to Reset the Application
If you want to clear all data and start completely fresh:
1. Close the TODO app.
2. Navigate to your user home folder and open `.todoapp`.
3. Delete the `data.json` file.
4. Restart the app. It will re-seed default categories with an empty task repository.

---

## 9. Keyboard Shortcuts Reference

| Shortcut | Action | Where It Works |
|---|---|---|
| <kbd>Ctrl</kbd> + <kbd>N</kbd> | Create New Task | Anywhere in main window |
| <kbd>Ctrl</kbd> + <kbd>Z</kbd> | Undo Last Deleted Task | Anywhere in main window |
| <kbd>Ctrl</kbd> + <kbd>F</kbd> | Jump focus to Search field | Anywhere in main window |
| <kbd>Enter</kbd> / <kbd>F2</kbd> | Edit currently selected task | Task list |
| <kbd>Delete</kbd> | Delete currently selected task | Task list |
| <kbd>Space</kbd> | Toggle completion of selected task | Task list |
| <kbd>Esc</kbd> | Clear search filter / Close open dialog | Search field / Dialogs |

---

## 10. Troubleshooting & FAQs

### Q: Why does `java -jar target/todo-app-1.0.0.jar` fail on my machine?
**A:** The shaded JAR packages JavaFX native libraries for the operating system it was built on. If you built the JAR on Windows, it will run on Windows. If moving between different operating systems, either run `mvn javafx:run` (which downloads native binaries dynamically) or build the JAR on the target operating system with `mvn clean package -DskipTests`. Also verify you are using JDK 17 or higher.

### Q: Where are my tasks saved?
**A:** In your user home directory under `.todoapp/data.json`. You can back up this file anytime to preserve your tasks.

### Q: Can I run the application in dark mode or customize the font?
**A:** The application uses a custom JavaFX CSS stylesheet located in `src/main/resources/com/todoapp/ui/styles.css`. Visual styles conform to modern design guidelines with responsive card layouts and clear contrast.

### Q: Can I change a Simple Task into a Checklist Task later?
**A:** To preserve domain consistency and avoid accidental data loss, the task type cannot be altered once created. You can quickly create a new Checklist Task and delete the old Simple Task.
