---
layout: page
title: Delete Student Handover
---

# Delete Student Handover

## Implemented

The `delete-student sid/STUDENT_ID` command removes a student by stable ID. It resolves the student before changing the model, rejects an unknown ID, and reports the deleted ID and name. The observable student list removes the row. The command result asks the UI to clear selection only when that student was selected before the command ran, so deleting another student preserves the open details.

The parser validates lookup IDs as text. `DeleteStudentCommand` does not construct a `StudentId` until it has found the matching record, so an unsuccessful lookup cannot advance the global ID generator.

If persistence fails, `LogicManager` restores the pre-command snapshot and reports `Could not save changes. Student S-0007 was not deleted.` The current student model has no guardian links or separate profile window, so neither is part of this increment.

## Implementation map

| File | Responsibility |
|---|---|
| `logic/parser/AddressBookParser.java` | Registers `delete-student` while leaving the legacy Person `delete` command unavailable. |
| `logic/parser/DeleteStudentCommandParser.java` | Accepts one valid `sid/` value and rejects malformed or extra arguments. |
| `logic/commands/DeleteStudentCommand.java` | Resolves the student by textual ID, deletes by the matched model ID, formats feedback, and requests conditional selection clearing. |
| `logic/commands/CommandResult.java` | Carries the deleted ID used to conditionally clear student selection. |
| `logic/LogicManager.java` | Restores the model snapshot and formats a delete-specific save failure. |
| `model/AddressBook.java`, `model/Model.java`, `model/ModelManager.java` | Provide deletion by `StudentId`. |
| `ui/MainWindow.java`, `ui/StudentListPanel.java`, `ui/StudentDetailsPanel.java` | Clear list selection and details only when the deleted student was selected before execution. |
| `docs/UserGuide.md` | Documents the current command and its persistence behavior. |

## Tests and verification

- `logic/commands/DeleteStudentCommandTest.java`: successful deletion, feedback, remaining records, unknown IDs, and generator stability after an unknown large ID.
- `logic/parser/DeleteStudentCommandParserTest.java`: valid IDs, generator stability after parsing a large lookup ID, and malformed, repeated, unknown, or extra arguments.
- `logic/LogicManagerTest.java`: successful persistence and rollback after save failure.
- `ui/MainWindowTest.java`: deletion clears the deleted student's details and preserves Amy's details when Mei is deleted.
- The implementation plan records a successful full `./gradlew test checkstyleMain checkstyleTest` run.

## Integration notes

- Keep deletion keyed by stable ID; row positions can change as lists are sorted or filtered.
- `LogicManager` snapshots the full student register before command execution. Include future mutating student commands in rollback handling and return an error that identifies the requested operation.
- When guardian relationships are added, deletion should remove only the student's relationship and retain shared guardian records and other students' links.
- Profile closing remains future work because the current details pane is selection-driven and there is no separate student profile window.

See [Delete Student Implementation Plan](DeleteStudentImplementationPlan.md) for the original scope and completion criteria.
