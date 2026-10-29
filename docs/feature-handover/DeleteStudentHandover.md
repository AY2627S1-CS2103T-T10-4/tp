---
layout: page
title: Delete Student Handover
---

# Delete Student Handover

## Implemented

The `delete-student sid/STUDENT_ID` command resolves a student against the complete register, independent of Search, and removes that student and their group assignments. Other students, assignments and all guardian records are retained. The command result clears selection only when the deleted student was selected before execution; deleting another student preserves the open profile, filter and workspace.

The parser validates lookup IDs as text and the command reuses the matched record's StudentId. Neither path constructs a lookup ID or advances the ID generator. Command/prefix names ignore case; exactly one nonempty `sid/` value is required. Malformed, repeated or unknown prefixes and extra text are rejected.

Success reports `Deleted student S-0007: Mei Lin.` If a guardian was linked, it also reports `Guardian link removed; guardian G-0003 was retained.` Unknown IDs report `Student S-0007 does not exist.` If saving fails, `LogicManager` restores the register and reports `Could not save changes. Student S-0007 was not deleted.` The UI restores the previous selection and profile, including an empty selection. Failed-load recovery blocks deletion before execution and preserves the damaged file.

## Implementation map

| File | Responsibility |
|---|---|
| `logic/parser/AddressBookParser.java` | Registers `delete-student` while leaving the legacy Person `delete` command unavailable. |
| `logic/parser/DeleteStudentCommandParser.java` | Accepts one valid `sid/` value and rejects malformed or extra arguments. |
| `logic/commands/DeleteStudentCommand.java` | Calls authoritative `Model.findStudent(String)`, deletes by the matched ID, and requests conditional selection clearing. |
| `logic/commands/CommandResult.java` | Preserves workspace/selection APIs and adds the clear flag and conditional ID to value equality/hash and diagnostics. |
| `logic/LogicManager.java` | Retains recovery protection and complete snapshot rollback; supplies delete-specific save feedback. |
| `model/AddressBook.java`, `model/Model.java`, `model/ModelManager.java` | Existing deletion removes only the student's assignments and record. No JSON schema change is needed. |
| `ui/MainWindow.java`, `ui/StudentListPanel.java`, `ui/StudentDetailsPanel.java` | Apply clearing metadata against the pre-command selection through the existing restoration/refresh helper. No second selection pipeline is introduced. |
| `docs/UserGuide.md` | Documents the current command and its persistence behavior. |

## Tests and verification

- `logic/commands/DeleteStudentCommandTest.java`: successful deletion, feedback, remaining records, unknown IDs, and generator stability after an unknown large ID.
- `logic/parser/DeleteStudentCommandParserTest.java`: valid IDs, generator stability after parsing a large lookup ID, and malformed, repeated, unknown, or extra arguments.
- `logic/LogicManagerTest.java`: successful persistence and rollback after save failure.
- `ui/MainWindowTest.java`: conditional clearing metadata and visible recovery feedback; its original removal test uses a logic double.
- `MergedCommandsIntegrationTest`, `AssignGroupUiTest`, `MainAppAssignmentTest`: real combined model/logic/storage tests for hidden targets, assignment cleanup, guardian preservation, restart, recovery and failed-save UI restoration.

The 15 October 2026 integration of `6327f8ea` with `afc7ff0b` passed **60 focused tests** and **440 full-suite tests**
(93 suites; no failures, errors or skips), both Checkstyle tasks, coverage generation, shadowJar and scoped Javadoc.
The original test assertions were retained; only the duplicate import and a line-wrap violation were corrected
in `DeleteStudentCommandTest`. The initial failing regression run and all exact commands remain in local records.

Six disposable-data JAR scenarios passed through the real Main entry point using automated JavaFX command-box
events: create/edit, reload, deletion, deleted-state reload, recovery and long-name layout. Screenshots confirmed
preserved/cleared profiles and compact scrolling. Human keyboard/mouse acceptance and other operating systems
were not tested. See the [combined verification record](AssignGroupHandover.md#verification-and-maintainer-checks).

## Integration notes

- Keep deletion keyed by stable ID; row positions can change as lists are sorted or filtered.
- `LogicManager` snapshots students, guardians and assignments. Future mutations inherit complete rollback; add their operation-specific save feedback without replacing the common guard.
- When guardian relationships are added, deletion should remove only the student's relationship and retain shared guardian records and other students' links.
- Student profiles already exist as the selection-driven details pane. Clearing selection clears the displayed profile; no separate window is involved.
- Normal direct file writes remain non-atomic. In-memory rollback and pre-write failure tests do not guarantee intact disk contents after a partial write.

See [Delete Student Implementation Plan](DeleteStudentImplementationPlan.md) for the preserved historical plan and
[Assign Group Handover](AssignGroupHandover.md) for the complete integration contract and current verification results.
