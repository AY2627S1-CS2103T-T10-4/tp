---
layout: page
title: UI README
---

# Mentora UI README

This document maps the JavaFX interface to its implementation classes and FXML files. The main workspace displays Mentora student records. The Add Student increment stores ID, name, and academic level; guardian and group profile sections are not yet implemented.

## Screen structure

The main window is arranged from top to bottom as follows:

1. **Brand header, navigation, and menus** — the `src/main/resources/images/mentora_logo.png` brand asset and Mentora identity on the left; Students and Groups navigation tabs sit beside the existing File and Help actions.
2. **Command area** — the primary keyboard-first input. A command can be submitted with Enter or the Run button.
3. **Result strip** — success and error feedback from the most recently executed command.
4. **Student workspace** — a student list on the left and the selected student's details on the right.
5. **Status footer** — local data-file status and keyboard hints.

The window defaults to `1000 × 650` and restores larger saved dimensions. Previously saved portrait-sized preferences are raised to at least `900 × 600`, keeping the primary workspace in a landscape layout.

## Component map

| Visible component | Java class | FXML | Responsibility |
|---|---|---|---|
| Application window and navigation tabs | `MainWindow` | `MainWindow.fxml` | Composes the screen, keeps File/Help behavior, switches the Students and Groups workspaces, binds the student count, and connects student selection to the details panel. |
| Command input and Run button | `CommandBox` | `CommandBox.fxml` | Accepts commands, executes them on Enter or button press, clears successful input, and marks invalid input. |
| Latest command result | `ResultDisplay` | `ResultDisplay.fxml` | Reserves one line of content when empty, measures wrapped or multi-line feedback, and supplies the preferred content height used by the complete result row. |
| Student list | `StudentListPanel` | `StudentListPanel.fxml` | Owns the student list and stable-ID selection behavior. Each row uses the card component below. |
| Student list row | `StudentCard` | `StudentListCard.fxml` | Arranges the existing student ID and name as a padded summary on the left and academic level in an aligned right column. |
| Selected student details | `StudentDetailsPanel` | `StudentDetailsPanel.fxml` | Shows the selected student's ID, name, and academic level, or an empty state. |
| Persistence footer | `StatusBarFooter` | `StatusBarFooter.fxml` | Shows that data is stored locally, the active JSON path, and keyboard hints. |
| Help window | `HelpWindow` | `HelpWindow.fxml` | Preserves the existing F1 help action and link-copy behavior. |
| Application/UI bootstrap | `UiManager` | — | Creates `MainWindow`, supplies application dependencies, and owns fatal-error dialogs. |
| Shared FXML loader | `UiPart<T>` | — | Loads each FXML resource and installs its Java class as controller. |

All paths above are relative to `src/main/java/seedu/address/ui/` for Java classes and `src/main/resources/view/` for FXML files.

## Selection and data flow

`MainWindow` creates the student list and details panel and passes the selected student to the details view. The first student is selected at startup. After `add-student`, the command result carries the generated ID and the list selects that student after the observable collection refreshes.

The student list uses `Logic#getStudentList()`. The active model and persistence layer store students only. Retired Person UI classes and compatibility APIs remain in source for existing test migration, but they are not attached to the window or exposed through the command parser. Student commands select rows by stable student ID; the Person index selection from PR #45 is no longer used.

For the command-driven selection API and Feature 13 integration notes, see
[Open Student Profile Handover](feature-handover/OpenStudentProfileHandover.md).

For the student-only model migration and PR #45 selection compatibility, see
[Person to Student Migration Handover](feature-handover/PersonToStudentMigrationHandover.md).

For student academic-level updates and stable-ID profile refresh, see
[Edit Student Handover](feature-handover/EditStudentHandover.md).

`ResultDisplay` measures the rendered feedback whenever its text, font, or available width changes. `MainWindow` binds the enclosing result row to that preferred content height plus the row's vertical padding, so the label, background, border, and workspace position resize together.

## Student workspace layout

The student workspace follows the established Mentora contact-screen composition while retaining student terminology and fields. List entries are full-width cards with consistent padding, dividers, selection highlighting, and aligned summary columns. The profile pane uses the same inset content area, title hierarchy, separator, and grouped field spacing. No labels, commands, or student data have been changed for this presentation update.

## Header navigation and Groups handover

`MainWindow.fxml` defines two header `ToggleButton` controls: `studentsTab` and `groupsTab`. Students is selected by default. `MainWindow` handles their actions through `handleStudentsTab()` and `handleGroupsTab()`, ensuring exactly one navigation button remains selected.

The growing area below the result strip is `workspace-container`. It currently contains:

- `studentWorkspace`, which holds the complete existing student split-pane and is visible by default.
- `groupsWorkspacePlaceholder`, an intentionally empty `StackPane` that is shown when Groups is selected.

The empty Groups placeholder remains managed while active so it occupies the workspace and keeps the status footer anchored at the bottom. The command box, latest result, menus, and status footer are shared shell components and stay visible when either tab is selected.

The Groups implementer should attach the future Groups root node to `groupsWorkspacePlaceholder`, ideally through a dedicated UI component such as `GroupListPanel` or `GroupWorkspace`. Keep Groups-specific controllers and data binding outside `MainWindow`; `MainWindow` should only construct the component and switch workspace visibility. Do not place temporary labels or student content in the Groups placeholder.

## Styling

`DarkTheme.css` is retained as the shared stylesheet filename for compatibility, but now contains the light Mentora theme. It defines the white and soft-green palette, typography, list selection, detail layout, command controls, status bar, menus, and dialogs. `Extensions.css` contains small state-specific rules such as command errors and empty list cells. `HelpWindow.css` gives the existing help window the same light visual language. The Mentora image resource is used in both the brand header and the application window icon.

The design uses only JavaFX controls and CSS. No web view, second runtime, or backend is introduced.
