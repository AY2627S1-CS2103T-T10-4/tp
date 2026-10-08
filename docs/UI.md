---
layout: page
title: UI README
---

# Mentora UI README

This document maps the JavaFX interface to its implementation classes and FXML files. The main workspace displays Mentora student records. The Add Student increment stores ID, name, and academic level; guardian and group profile sections are not yet implemented.

## Screen structure

The main window is arranged from top to bottom as follows:

1. **Brand header and menus** — the `src/main/resources/images/mentora_logo.png` brand asset and Mentora identity on the left, with the existing File and Help actions on the right.
2. **Command area** — the primary keyboard-first input. A command can be submitted with Enter or the Run button.
3. **Result strip** — success and error feedback from the most recently executed command.
4. **Student workspace** — a student list on the left and the selected student's details on the right.
5. **Status footer** — local data-file status and keyboard hints.

The window defaults to `1000 × 650` and restores larger saved dimensions. Previously saved portrait-sized preferences are raised to at least `900 × 600`, keeping the primary workspace in a landscape layout.

## Component map

| Visible component | Java class | FXML | Responsibility |
|---|---|---|---|
| Application window | `MainWindow` | `MainWindow.fxml` | Composes the screen, keeps File/Help behavior, binds the visible contact count, and connects contact selection to the details panel. |
| Command input and Run button | `CommandBox` | `CommandBox.fxml` | Accepts commands, executes them on Enter or button press, clears successful input, and marks invalid input. |
| Latest command result | `ResultDisplay` | `ResultDisplay.fxml` | Reserves one line of content when empty, measures wrapped or multi-line feedback, and supplies the preferred content height used by the complete result row. |
| Student list | `StudentListPanel` | `StudentListPanel.fxml` | Displays students with ID, name, and academic level; selection uses the stable student ID. |
| Selected student details | `StudentDetailsPanel` | `StudentDetailsPanel.fxml` | Shows the selected student's ID, name, and academic level, or an empty state. |
| Persistence footer | `StatusBarFooter` | `StatusBarFooter.fxml` | Shows that data is stored locally, the active JSON path, and keyboard hints. |
| Help window | `HelpWindow` | `HelpWindow.fxml` | Preserves the existing F1 help action and link-copy behavior. |
| Application/UI bootstrap | `UiManager` | — | Creates `MainWindow`, supplies application dependencies, and owns fatal-error dialogs. |
| Shared FXML loader | `UiPart<T>` | — | Loads each FXML resource and installs its Java class as controller. |

All paths above are relative to `src/main/java/seedu/address/ui/` for Java classes and `src/main/resources/view/` for FXML files.

## Selection and data flow

`MainWindow` creates the student list and details panel and passes the selected student to the details view. The first student is selected at startup. After `add-student`, the command result carries the generated ID and the list selects that student after the observable collection refreshes.

The student list uses `Logic#getStudentList()`. Legacy Address Book person APIs remain in the codebase for other unimplemented features, but are not shown in this workspace.

`ResultDisplay` measures the rendered feedback whenever its text, font, or available width changes. `MainWindow` binds the enclosing result row to that preferred content height plus the row's vertical padding, so the label, background, border, and workspace position resize together.

## Styling

`DarkTheme.css` is retained as the shared stylesheet filename for compatibility, but now contains the light Mentora theme. It defines the white and soft-green palette, typography, list selection, detail layout, command controls, status bar, menus, and dialogs. `Extensions.css` contains small state-specific rules such as command errors and empty list cells. `HelpWindow.css` gives the existing help window the same light visual language. The Mentora image resource is used in both the brand header and the application window icon.

The design uses only JavaFX controls and CSS. No web view, second runtime, or backend is introduced.
