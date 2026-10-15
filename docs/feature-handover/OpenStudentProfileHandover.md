---
layout: page
title: Open Student Profile Handover
---

# Open Student Profile Handover

## Current integration contract

The active model and UI now use students with permanent IDs, names, and academic
levels. To request selection, return
`new CommandResult(feedback, false, false, studentId)`, where `studentId` is the
existing student's ID as a `String`.

```text
CommandResult.getSelectedStudentId()
    -> MainWindow forwards the ID to StudentListPanel.selectStudentId(id)
    -> MainWindow refreshes StudentDetailsPanel with current guardian details
```

The command must validate the ID. MainWindow resolves the profile from the complete
register, even when a search hides the student. In that case the filter is retained
and list selection is cleared. The old `Index` constructor
and `getSelectedIndex()` are deprecated and no longer request selection.

`view-student sid/STUDENT_ID` now uses this contract through `ViewStudentCommandParser`
and `ViewStudentCommand`. The parser rejects missing, malformed, repeated, and extra
arguments. The command verifies that the ID exists before requesting selection;
lookup does not construct a `StudentId`, because its constructor advances the ID generator.
Its current existence check uses the displayed list, so `view-student` still
rejects students hidden by a search. The full-register UI lookup supports
`link-guardian` requests for hidden students without changing that command's scope.

The UI displays ID, name, academic level, and the current guardian name and phone
(or `Guardian: none`). Successful profile requests open the Students tab and
explicitly refresh details even if the student is already selected.
`LogicManager` skips saving when student data is unchanged, so viewing a profile
does not require writable storage.

Parser and command tests cover validation and read-only lookup. `LogicManagerTest`
checks command execution and persistence behavior. `LinkGuardianProfileTest`
covers guardian display and refresh through real commands. User Guide updates
remain with their owner.

Assignment details remain pending. See the
[migration handover](PersonToStudentMigrationHandover.md) for the shared model changes.

Everything below records the original PR #45 implementation and its validation
at that time. Its Person/index APIs and model prerequisites are historical, not
instructions for new integration.

## Historical scope of the v1.2 increment (PR #45)

Feature 13 will open a read-only student profile using `view-student sid/STUDENT_ID`.
This increment implements the command-to-UI selection connection using the existing
`Person` model. No production command requests selection yet, so there is no new
user-entered command to demonstrate in this increment. The connection is exercised
through automated tests.

The student model, permanent IDs, academic levels, assignments, and guardian records
are not implemented by this change. Keeping this increment independent of those
models avoids duplicating teammates' work. See [UI README](../UI.md) for the overall
component map.

## High-level structure

```text
Command returns CommandResult(feedback, selectedIndex)
    -> Logic returns the result to MainWindow
    -> MainWindow reads getSelectedIndex()
    -> PersonListPanel.selectPerson(index) selects and scrolls to the row
    -> Existing selectedPersonProperty listener notifies PersonDetailsPanel
    -> PersonDetailsPanel displays the selected Person
```

The command layer communicates a selection request without depending on UI classes.
The UI reuses the existing details panel and selection listener; no new window,
FXML layout, or storage schema is introduced.

## Files and responsibilities

Java paths below are relative to `src/main/java/seedu/address/`.

| File | Change or role |
| --- | --- |
| `logic/commands/CommandResult.java` | Adds a constructor accepting an `Index` and an optional selection accessor. Equality, hashing, and string output include the selection request. Existing constructors request no selection. |
| `ui/MainWindow.java` | Forwards successful command-result selection requests to the list panel. |
| `ui/PersonListPanel.java` | Adds `selectPerson(Index)` to validate, select, and scroll to a visible row. |
| `ui/PersonDetailsPanel.java` | Unchanged. Its existing `setPerson(Person)` method displays name, phone, email, address, and tags. |
| `docs/UI.md` | Links to this handover. |

`Index` implements equality without overriding `hashCode`, so `CommandResult`
hashes the numeric index value to keep equal results' hash codes consistent.

## Selection flow

A command can return `new CommandResult(feedback, selectedIndex)` to request that
`MainWindow` select and scroll to a row through `PersonListPanel#selectPerson(Index)`.
The existing selection listener then updates `PersonDetailsPanel`. Results without a
selection request preserve the current selection. Selection does not request keyboard
focus, so command entry can continue.

## Index contract

The index is an internal position in the **filtered list after the command executes**.
The command must validate it before returning the result. The panel rejects null or
out-of-range indices without changing selection. List positions are not permanent
student IDs.

Selection must run on the JavaFX application thread, as it does through the existing
command-box callback. A null index throws `NullPointerException`; an index outside
the visible list throws `IllegalArgumentException`. These are API precondition
failures, not user-facing command errors. A future command must validate user input
and raise the appropriate parse or command exception before returning its result.

An absent selection request does not clear selection or restore selection changed by
the command itself; it simply causes no additional UI selection action.

## Integration with the student model

This foundation does not implement the `view-student` command yet.
Once the shared student model is available, `view-student sid/STUDENT_ID` must resolve
the permanent student ID against all students, ensure the student is visible, and
return the corresponding visible index. Academic level, assignments, and guardian
details still require the shared domain model and profile integration.

Suggested next steps:

1. Agree on the shared student type, permanent ID representation, and lookup API with
   the add-student owner before extending the parser or model.
2. Add a parser and command for `view-student sid/STUDENT_ID`, including missing,
   malformed, repeated, and unknown parameters and unknown student IDs.
3. Resolve IDs against all students, not just the filtered list. Decide how opening
   a hidden student changes the visible list, then calculate its visible index.
4. Return feedback such as `Opened profile for S-0007.` with that index.
5. Extend the profile to show the ID, name, academic level, assignments, and guardian.
   Resolve guardian information from the current guardian record by ID when displayed;
   do not store a stale copy of the guardian's phone in the profile.
6. Add command/parser integration tests and tests for hidden students, unknown IDs,
   and guardian updates. Update this handover when those pieces are implemented.

The existing `LogicManager` saves the address book after every successful command,
including read-only commands. This increment does not change that behavior. Coordinate
with the persistence owner when integrating the read-only profile command.

## Tests and validation

Test paths are relative to `src/test/java/seedu/address/`.

| Test file | Coverage added |
| --- | --- |
| `logic/commands/CommandResultTest.java` | Selection payload, legacy constructors, null arguments, equality, and consistent hash codes for separately created equal indices. |
| `ui/PersonListPanelTest.java` | Selection and scroll request, repeated selection, filtered-list indices, invalid/null indices, and an empty list. |
| `ui/MainWindowTest.java` | A stub Logic result drives list selection and details updates; later results without selection requests and parse failures preserve that selection. |

The MainWindow test uses a stub command string, not an implemented profile command.
JavaFX tests initialize CSS where needed so list skins and ScrollPane content are
available for assertions.

Normal validation command from the repository root:

```powershell
.\gradlew.bat test checkstyleMain checkstyleTest
```

During this increment, direct compilation and JUnit execution using cached project
dependencies passed all 257 tests. Direct Checkstyle 14.1.0 execution passed; it was
also rerun after the later naming and Javadoc cleanup. Gradle could not start in the
assistant environment because of a local socket connection failure. These direct
runs do not confirm a successful Gradle build; run the normal checks locally and in
CI before merging. Temporary validation scripts under ignored `_temp/` are local
workarounds, not project build tooling.
