# TODO List Desktop App

A simple, modern desktop TODO list application built in Java and JavaFX, designed around object-oriented principles (encapsulation, abstraction, inheritance, polymorphism), robust architecture, and Git version control.

See [implementation.md](implementation.md) for the complete implementation plan, milestone tracker, and architectural specifications.

## Prerequisites

- **Java Development Kit (JDK):** Version 17 or higher (tested with JDK 17, 21, and 25).
- **Apache Maven:** Version 3.9.0 or higher.
- **Git:** Version 2.30 or higher.

## Building and Running

### Build and Test

To compile the project, run all automated tests, and produce test coverage reports:

```bash
mvn verify
```

To run only the unit test suite:

```bash
mvn test
```

### Running the Application

To launch the JavaFX desktop application from the terminal:

```bash
mvn javafx:run
```

To pass program arguments during development (e.g. data path or seeding):

```bash
mvn javafx:run -Djavafx.args="--data=my-tasks.json"
```

### Developer Sample Seed

For manual testing and feature verification, use `--seed` to pre-populate sample data:

```bash
mvn javafx:run -Djavafx.args="--seed --data=/tmp/todo-dev.json"
```

When `--seed` is passed, approximately 12 sample tasks (covering all task types, priority ranks, categories, pinned cards, and completion states) are populated **only if the target data file is empty**.

## Opening in Visual Studio Code

1. Open VS Code.
2. Select **File ▸ Open Folder...** and choose the project root directory.
3. Install the recommended **Extension Pack for Java** (provided by Microsoft / Red Hat).
4. The Java Projects view will automatically detect and load `pom.xml`.
5. Run the application from the **integrated terminal** using `mvn javafx:run` (rather than the editor's direct Run button, ensuring JavaFX module-path setup from the Maven plugin).
6. Run unit tests directly from the **Testing** panel or via `mvn test`.
7. For viewing architectural diagrams in Markdown files under `docs/`, install a Markdown Mermaid preview extension (e.g., *Markdown Preview Mermaid Support*).

## Contributing & Git Workflow

- **Branching Model:** Linear single-branch workflow directly on `main`. No long-lived feature branches, no pull requests, and no merge commits.
- **Commit Conventions:** Follow [Conventional Commits](https://www.conventionalcommits.org/):
  - `feat(scope): ...` for new features
  - `fix(scope): ...` for bug fixes
  - `test(scope): ...` for test additions and updates
  - `docs(scope): ...` for documentation
  - `refactor(scope): ...` for code refactoring
  - `build: ...` or `chore: ...` for build configuration and dependencies
- **Collaboration:** If collaborating, pull updates using rebase (`git pull --rebase`) before pushing to preserve linear commit history.
