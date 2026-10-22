---
layout: page
title: UI README
---

# Mentora UI README

This document maps the JavaFX interface to its implementation classes and FXML files. The main workspace displays Mentora student records with ID, name, academic level, optional student phone and configured-group assignments.

## Screen structure

The main window is arranged from top to bottom as follows:

1. **Brand header, navigation, and menus** — the `src/main/resources/images/mentora_logo.png` brand asset and Mentora identity on the left; Students and Groups navigation tabs sit beside the existing File and Help actions.
2. **Command area** — the primary keyboard-first input. A command can be submitted with Enter or the Run button.
3. **Result strip** — success and error feedback from the most recently executed command.
4. **Student workspace** — a student list on the left and the selected student's details on the right.
5. **Status footer** — local data-file status and keyboard hints.

The window defaults to `1000 × 650` and restores larger saved dimensions. Previously saved portrait-sized preferences are raised to at least `900 × 600`, keeping the primary workspace in a landscape layout. Users can resize down to the FXML minimum of `820 × 560`; the profile scrolls when its content exceeds the available height.

## Component map

| Visible component | Java class | FXML | Responsibility |
|---|---|---|---|
| Application window and navigation tabs | `MainWindow` | `MainWindow.fxml` | Composes the screen, keeps File/Help behavior, switches the Students and Groups workspaces, binds the student count, and connects student selection to the details panel. |
| Command input and Run button | `CommandBox` | `CommandBox.fxml` | Accepts commands, executes them on Enter or button press, clears successful input, and marks invalid input. |
| Latest command result | `ResultDisplay` | `ResultDisplay.fxml` | Reserves one line of content when empty, measures wrapped or multi-line feedback, and supplies the preferred content height used by the complete result row. |
| Student list | `StudentListPanel` | `StudentListPanel.fxml` | Owns the student list and stable-ID selection behavior. Each row uses the card component below. |
| Student list row | `StudentCard` | `StudentListCard.fxml` | Arranges the existing student ID and name as a padded summary on the left and academic level in an aligned right column. |
| Configured groups | `GroupListPanel` | `GroupListPanel.fxml` | Displays the fixed catalogue's code, label, kind, order, and actual count in the Groups workspace. |
| Selected student details | `StudentDetailsPanel` | `StudentDetailsPanel.fxml` | Shows ID, name, academic level, student phone (or `Phone: none`) and assignments, or an empty state; the profile scrolls as one unit when space is limited. |
| Persistence footer | `StatusBarFooter` | `StatusBarFooter.fxml` | Shows the active JSON path and keyboard hints; during recovery it shows Saving disabled and retains the load-failure reason in a tooltip. |
| Help window | `HelpWindow` | `HelpWindow.fxml` | Preserves the existing F1 help action and link-copy behavior. |
| Application/UI bootstrap | `UiManager` | — | Creates `MainWindow`, supplies application dependencies, and owns fatal-error dialogs. |
| Shared FXML loader | `UiPart<T>` | — | Loads each FXML resource and installs its Java class as controller. |

All paths above are relative to `src/main/java/seedu/address/ui/` for Java classes and `src/main/resources/view/` for FXML files.

## Selection and data flow

`MainWindow` creates the student list and details panel and passes the selected student to the details view. The first student is selected at startup. After `add-student`, the command result carries the generated ID and the list selects that student after the observable collection refreshes.

`MainWindow.refreshStudentDetails()` supplies the selected Student, `Logic#getAssignedGroups(String)` and the
linked Guardian to `StudentDetailsPanel.setStudent(Student, List<Group>, Guardian)`. The group projection resolves
current catalogue labels in catalogue order. An empty list displays `No groups assigned.`, including after removal
of the final assignment; this remains a selected student profile, not the empty-selection state.
Selection changes and successful command results both trigger this renderer: assigning or removing a group
retains the same Student object, so reselection alone is not a reliable notification.

Successful `assign-group` and `unassign-group` results request `Workspace.STUDENTS` plus the target ID.
MainWindow applies these requests after persistence succeeds, including when Groups or Guardians was active.
Before execution, MainWindow remembers the current selected ID. A failed command restores that selection (including
an empty selection) and rerenders the final model after any save rollback. The filter and workspace do not change
on failure, and the previous assignments and guardian details remain displayed.
Panels/listeners are constructed once. StudentListPanel's `clearSelection()` supports this restoration.

The Student list uses the filtered view from `Logic#getStudentList()`. Stable-ID lookup and persistence use the
complete AddressBook, which stores students, guardians, guardian links and assignments. After a successful result requests a
hidden student, `LogicManager.revealRequestedStudent()` clears the filter before MainWindow applies selection.
This runs after any required save, preserving the previous filter on failure. Visible targets retain the filter.
View/Search/List keep the active workspace; View Student still requires selecting the Students tab when Groups is open.

Edit Student uses the same post-success reveal/selection path and keeps the active workspace. Its immutable
Student replacement retains the ID and assignments; MainWindow explicitly renders the selected replacement,
including successive edits to the same ID. `restoreStudentSelection()` is the single selection/refresh helper
for successful results and failed commands. It clears an absent selection instead of selecting a different student.

Delete Student adds conditional clearing metadata to CommandResult alongside the existing workspace and selected-ID
fields. MainWindow compares the deleted ID with the selection saved before execution, then uses the same
`restoreStudentSelection()` helper. Deleting the selected student clears the profile; deleting another student,
including one hidden by Search, retains the visible selection, filter and workspace. Save failures restore the
complete register and previous selection without applying the deletion result.

Retired Person UI classes and compatibility APIs remain in source for existing test migration, but they are not attached to the window or exposed through the command parser. Student commands select rows by stable student ID; the Person index selection from PR #45 is no longer used.

For the command-driven selection API and Feature 13 integration notes, see
[Open Student Profile Handover](feature-handover/OpenStudentProfileHandover.md).

For the student-only model migration and PR #45 selection compatibility, see
[Person to Student Migration Handover](feature-handover/PersonToStudentMigrationHandover.md).

For stable-ID student deletion, assignment cleanup and conditional selection clearing, see
[Delete Student Handover](feature-handover/DeleteStudentHandover.md).

For student academic-level updates and stable-ID profile refresh, see
[Edit Student Handover](feature-handover/EditStudentHandover.md).

`ResultDisplay` measures the rendered feedback whenever its text, font, or available width changes. `MainWindow` binds the enclosing result row to that preferred content height plus the row's vertical padding, so the label, background, border, and workspace position resize together. The `RESULT` heading retains its preferred width while long feedback, including unassignment errors, wraps beside it.

On a failed data load, `Logic#getDataRecoveryReason()` exposes the storage session's recovery state. `MainWindow`
shows the exact recovery explanation in the initial result strip and constructs a write-disabled footer. Read-only
commands may update the result strip, but the footer remains `Saving disabled` with the original failure reason.
Mutating commands are rejected before execution. See [Recovery and write protection](feature-handover/AssignGroupHandover.md#recovery-and-write-protection).

## Student workspace layout

Features 18/19 add `studentFilterSummaryLabel` above the student list. Successful `filter-students g/CODE`
or `filter-students al/LEVEL` requests the Students workspace and shows the active group/level summary.
`list-students` clears the predicate and hides/unmanages the summary, retaining its existing tab behavior.
Assignment-only rollback refreshes group-filter membership as well as restoring relationships; see
[Filter Students Handover](feature-handover/FilterStudentsHandover.md) for the API, regression tests and review fixes.

The student workspace follows the established Mentora contact-screen composition while retaining student terminology and fields. List entries are full-width cards with consistent padding, dividers, selection highlighting, and aligned summary columns. The profile pane keeps its inset content area, title hierarchy, separator and grouped field spacing, with the added Assignments section. One `profileScrollPane` contains all profile fields so long valid names cannot push assignments or the footer offscreen. There are no nested scrolling regions in the profile.

## Header navigation and Groups handover

`MainWindow.fxml` defines two header `ToggleButton` controls: `studentsTab` and `groupsTab`. Students is selected by default. `MainWindow` handles their actions through `handleStudentsTab()` and `handleGroupsTab()`, ensuring exactly one navigation button remains selected.

The growing area below the result strip is `workspace-container`. It currently contains:

- `studentWorkspace`, which holds the complete existing student split-pane and is visible by default.
- `groupsWorkspacePlaceholder`, which holds one `GroupListPanel` and is shown when Groups is selected.

The Groups workspace remains managed while active so it occupies the workspace and keeps the status footer anchored at the bottom. The command box, latest result, menus, and status footer are shared shell components and stay visible when either tab is selected.

`MainWindow.fillGroupPanel()` constructs the panel once using `Logic#getGroupCatalogue()`. The model owns access to the shared immutable configuration supplied by `ConfiguredGroups`; the panel renders its rows using the current theme. The catalogue is separate from persisted student data.

`list-groups` returns a `CommandResult.Workspace.GROUPS` navigation request. `assign-group` and `unassign-group` request `STUDENTS` and a stable student ID. `MainWindow` switches tabs after successful execution and any required save, without interpreting feedback text or replacing the Student panels. Earlier commands retain `UNCHANGED` navigation. Invalid list arguments leave the current workspace intact. Repeated commands and tab switches reuse the same panels.

See [Assign Group Handover](feature-handover/AssignGroupHandover.md) for catalogue, assignment, persistence and refresh contracts. The Student UI change is limited to the Assignments section and the selection/refresh hooks above.

## Styling

`DarkTheme.css` is retained as the shared stylesheet filename for compatibility, but now contains the light Mentora theme. It defines the white and soft-green palette, typography, list selection, detail layout, command controls, status bar, menus, and dialogs. `Extensions.css` contains small state-specific rules such as command errors and empty list cells. `HelpWindow.css` gives the existing help window the same light visual language. The Mentora image resource is used in both the brand header and the application window icon.

The design uses only JavaFX controls and CSS. No web view, second runtime, or backend is introduced.
