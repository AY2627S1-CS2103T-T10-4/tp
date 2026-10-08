---
layout: page
title: UI README
---

# Mentora UI README

This document maps the JavaFX interface to its implementation classes and FXML files. The visual design follows the light, landscape Mentora mockup, while the displayed data deliberately follows the application's current `Person` model: name, phone, email, address, and tags.

## Screen structure

The main window is arranged from top to bottom as follows:

1. **Brand header and menus** — the `src/main/resources/images/mentora_logo.png` brand asset and Mentora identity on the left, with the existing File and Help actions on the right.
2. **Command area** — the primary keyboard-first input. A command can be submitted with Enter or the Run button.
3. **Result strip** — success and error feedback from the most recently executed command.
4. **Contact workspace** — a compact contact list on the left and the selected contact's full details on the right.
5. **Status footer** — local data-file status and keyboard hints.

The window defaults to `1000 × 650` and restores larger saved dimensions. Previously saved portrait-sized preferences are raised to at least `900 × 600`, keeping the primary workspace in a landscape layout.

## Component map

| Visible component | Java class | FXML | Responsibility |
|---|---|---|---|
| Application window | `MainWindow` | `MainWindow.fxml` | Composes the screen, keeps File/Help behavior, binds the visible contact count, and connects contact selection to the details panel. |
| Command input and Run button | `CommandBox` | `CommandBox.fxml` | Accepts commands, executes them on Enter or button press, clears successful input, and marks invalid input. |
| Latest command result | `ResultDisplay` | `ResultDisplay.fxml` | Reserves one line of content when empty, measures wrapped or multi-line feedback, and supplies the preferred content height used by the complete result row. |
| Contact list | `PersonListPanel` | `PersonListPanel.fxml` | Owns the `ListView<Person>`, creates list cells, and exposes the selected person. |
| Contact list row | `PersonCard` | `PersonListCard.fxml` | Shows the list position, name, tags, and phone number for one person. |
| Selected contact details | `PersonDetailsPanel` | `PersonDetailsPanel.fxml` | Shows all current `Person` fields: name, tags, phone, email, and address. It also supplies the no-selection state. |
| Persistence footer | `StatusBarFooter` | `StatusBarFooter.fxml` | Shows that data is stored locally, the active JSON path, and keyboard hints. |
| Help window | `HelpWindow` | `HelpWindow.fxml` | Preserves the existing F1 help action and link-copy behavior. |
| Application/UI bootstrap | `UiManager` | — | Creates `MainWindow`, supplies application dependencies, and owns fatal-error dialogs. |
| Shared FXML loader | `UiPart<T>` | — | Loads each FXML resource and installs its Java class as controller. |

All paths above are relative to `src/main/java/seedu/address/ui/` for Java classes and `src/main/resources/view/` for FXML files.

## Selection and data flow

`MainWindow` creates both `PersonListPanel` and `PersonDetailsPanel`. It listens to the list's selected-person property and passes the selected `Person` to the details panel. The first visible contact is selected at startup, so contact information is displayed immediately. If the filtered list is empty or selection is cleared, the right side shows an empty state.

The list itself continues to use `Logic#getFilteredPersonList()`. Commands and storage behavior are unchanged by the visual redesign.

For the command-driven selection API and Feature 13 integration notes, see
[Open Student Profile Handover](OpenStudentProfileHandover.md).

`ResultDisplay` measures the rendered feedback whenever its text, font, or available width changes. `MainWindow` binds the enclosing result row to that preferred content height plus the row's vertical padding, so the label, background, border, and workspace position resize together.

## Styling

`DarkTheme.css` is retained as the shared stylesheet filename for compatibility, but now contains the light Mentora theme. It defines the white and soft-green palette, typography, list selection, detail layout, command controls, status bar, menus, and dialogs. `Extensions.css` contains small state-specific rules such as command errors and empty list cells. `HelpWindow.css` gives the existing help window the same light visual language. The Mentora image resource is used in both the brand header and the application window icon.

The design uses only JavaFX controls and CSS. No web view, second runtime, or backend is introduced.
