---
layout: page
title: Edit Student Handover
---

# Edit Student Handover

## Implemented

The `edit-student sid/STUDENT_ID al/ACADEMIC_LEVEL` command updates only the academic level of a student. It retains the student's ID and name, rejects an unknown ID and rejects a level change that duplicates another student's normalized name and level. The parser validates the ID as text, so parsing and executing an unsuccessful lookup do not advance the generated-ID counter.

On success, the command reports `Updated student S-0007: academic level is now Sec 3.` and requests selection by stable student ID. Replacing the immutable student in the observable model list refreshes the list row and selected profile. If saving fails, `LogicManager` restores its pre-command snapshot and reports `Could not save changes. Student S-0007 was not updated.`

## Implementation map

| File | Responsibility |
|---|---|
| `logic/parser/AddressBookParser.java` | Registers `edit-student`. |
| `logic/parser/EditStudentCommandParser.java` | Validates required and repeated prefixes, ID format, and academic level. |
| `logic/commands/EditStudentCommand.java` | Resolves the student from its textual ID, checks duplicate identity, updates the model using the matched record's ID, and returns feedback with stable-ID selection. |
| `model/AddressBook.java` | Looks up by ID and replaces the matching record while preserving unique name/level identity. |
| `model/Model.java`, `model/ModelManager.java` | Expose student lookup and update APIs. |
| `logic/LogicManager.java` | Rolls back an edit when persistence throws an `IOException`. |
| `ui/MainWindow.java`, `ui/StudentListPanel.java` | Select the edited student after success and restore the previous selection after command errors. |
| `docs/UserGuide.md` | Documents command syntax and behavior. |

Academic levels are already serialized by `JsonAdaptedStudent`, so this change does not alter the JSON schema.

## Tests and verification

- `logic/commands/EditStudentCommandTest.java`: successful update, unknown and large IDs, duplicate identity, and unchanged data on rejection.
- `logic/parser/EditStudentCommandParserTest.java`: valid input, large-ID generator stability, missing/repeated prefixes, unknown prefixes, and preamble rejection.
- `ui/MainWindowTest.java`: deleting a different student preserves Amy's selection, while deleting Amy clears it.
- `logic/LogicManagerTest.java`: persistence failure restores the old academic level and reports the student ID.
- The complete `./gradlew test checkstyleMain checkstyleTest` command passed after implementation.

## Integration notes

- Duplicate matching is based on normalized student name and academic level. A command must exclude the edited student's own ID from collision checks; otherwise every edit appears to conflict with itself.
- `Student` is immutable. Use `Model.updateStudent` to replace its observable-list entry rather than mutating a field.
- If adding future student updates, extend `LogicManager`'s snapshot rollback and provide the feature-specific save error when a failed save must identify the affected record.
- Guardian links and group assignments are not changed by this command.
