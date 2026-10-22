---
layout: page
title: Unassign Group Handover
---

# Feature 10: Remove a group assignment

Current filter integration (22 October 2026): [Filter Students Handover](FilterStudentsHandover.md)
documents Features 18/19. Removing an assignment refreshes group-filter membership. If saving fails,
`ModelManager.setAddressBook()` restores membership and refreshes the filtered rows, even when no Student
object changes. `FilterStudentsUiTest` covers restoration of rows, selection, summary and workspace.
The checkpoint details below describe the original Feature 10 implementation.

Owner: **Gabriel Chan (HIIMHEY)** · [Issue #72](https://github.com/AY2627S1-CS2103T-T10-4/tp/issues/72) · [PR #73](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/73) · v1.4 Alpha.

This handover describes source checkpoint `2a725351`, which includes unlink-guardian from [PR #70](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/70). See the [User Guide](../UserGuide.md) and [feature specification](../FeatureSpecification.md) for the user-facing contract.

## Scope and command contract

Format: `unassign-group sid/STUDENT_ID g/GROUP_CODE`

Examples: `unassign-group sid/S-0007 g/S2-MAT-A`; `unassign-group g/mat sid/S-0007`.

The command removes exactly one existing configured Subject or Class assignment, including a student's final assignment. `MAT` and `S2-MAT-A` are distinct assignments. Student records and stable IDs, guardians, guardian links, unrelated assignments (including another student's assignment to the same group), and the configured catalogue are preserved. F10 adds no bulk removal, group CRUD, academic-level restriction or JSON migration.

* Command and prefix casing are ignored; either prefix order is accepted. Leading/trailing whitespace and repeated spaces, tabs and newlines follow the existing assignment parser conventions.
* Exactly one nonempty `sid/` and `g/` is required. Missing, empty, repeated or unknown prefixes, unexpected preamble and extra text are format errors.
* Student ID syntax is `S-[0-9]{4,}`: uppercase `S-` and at least four ASCII digits, with no maximum suffix length. Parse and resolve the text without numeric conversion or constructing a `StudentId`, whose constructor can reserve IDs and overflow on huge values.
* `GroupCode` provides canonical identity. After syntax validation, the normal lookup order is student, configured group, then membership. Lookup uses `Model.findStudent(String)` against the complete register, independently of filtering or selection.

| Outcome | Exact feedback (substitute actual IDs and canonical group codes) |
| --- | --- |
| Success | `Removed group S2-MAT-A from student S-0007.` |
| Missing membership | `Student S-0007 is not assigned to S2-MAT-A.` |
| Unknown student | `Student S-0007 does not exist.` |
| Unknown group | `Group CODE is not configured. Run list-groups to see available groups.` |
| Invalid syntax | `Invalid command format. Use: unassign-group sid/STUDENT_ID g/GROUP_CODE` |
| Save failure | `Could not save changes. The group assignment was not removed.` |

## Execution and persistence

Java paths below are relative to `src/main/java/seedu/address/`.

1. `logic/LogicManager.execute` asks `logic/parser/AddressBookParser` to route to `UnassignGroupCommandParser`. The parser validates syntax and creates `logic/commands/UnassignGroupCommand` with ID text and a canonical `GroupCode`.
1. The command inherits mutating classification from `Command`. Logic checks recovery state and takes a complete `AddressBook` snapshot before executing it. Malformed input fails parsing before the recovery guard; valid input in recovery is blocked before student/group lookup or mutation.
1. The command resolves the existing student and configured group, builds `GroupAssignment` with the resolved student's existing ID, and calls `Model.removeGroupAssignment(GroupAssignment)`. `ModelManager` delegates to `AddressBook`, which validates student, group and membership before removing the exact pair. Expected domain `IllegalArgumentException`s become `CommandException`s with the original message and cause.
1. The command returns success feedback and a `CommandResult` requesting the target ID and `Workspace.STUDENTS` to Logic. Logic detects the aggregate change and saves exactly once through `StorageManager` and `JsonAddressBookStorage`. The command itself performs no saving or UI updates. Syntax/domain rejection performs no save.
1. After saving succeeds, Logic reveals a hidden requested student and returns the result to the UI. A save `IOException`, including `AccessDeniedException`, restores the complete in-memory snapshot and produces the F10 save-failure message with the original cause; no success result reaches the UI.

`AddressBook` copy/reset/equality/hash include students, guardians, guardian links and group assignments. Mutable collections are copied and exposed through read-only views; immutable records and relationship values may be shared. Both removal operations remain narrow: F10 changes only `groupAssignments`, while `unlinkGuardian(String)` changes only `guardianLinks` and returns the existing guardian ID.

The existing JSON arrays `students`, `guardians`, `guardianLinks` and `groupAssignments` persist the result without schema changes. Assignment entries contain `studentId` and canonical `groupCode`; a student with no remaining assignments still persists and reloads. `ConfiguredGroups` remains separate immutable application configuration.

Failed-load recovery is retained by `StorageManager`, which independently blocks writes. Valid F10 commands report `DataLoadingException.MESSAGE_RECOVERY`; existing read-only commands remain usable and do not clear the recovery reason. Repairing the file does not automatically unlock an existing recovering session.

## UI integration

`MainWindow` applies the result's Students workspace and stable-ID selection after Logic returns successfully. `restoreStudentSelection()` explicitly calls `refreshStudentDetails()`, which obtains current assigned groups through Logic/Model; assignment-only changes keep the same `Student` reference and may not fire a selection listener. `StudentDetailsPanel` renders current group codes/labels or `No groups assigned.` while retaining the selected student's identity, details and guardian display.

A filter hiding the target is cleared only after a successful save; a filter already containing the target is retained. Parse/domain errors and handled save failures preserve the previous selection (including no selection), filter and workspace, and render the restored model after save rollback. Keep this shared refresh path compatible with the [Unlink Guardian Handover](UnlinkGuardianHandover.md): successful commands also refresh guardian details and linked-student counts.

## Tests and regression commands

| Tests | Main coverage |
| --- | --- |
| `AddressBookAssignmentTest`, `ModelManagerAssignmentTest` | Exact-pair and final removal, validation before mutation, preserved records/relationships/catalogue, independent snapshots, equality/hash, read-only collections and filtered projections. |
| `UnassignGroupCommandParserTest` | Prefix order/casing/whitespace, malformed arguments, four/five-digit and huge IDs. A representable absent ID above the sampled generator counter detects accidental reservation without resetting global state. |
| `UnassignGroupCommandTest` | Subject/Class removal, exact feedback, authoritative hidden lookup, reused IDs, huge unknown IDs, preserved state, mutating classification and navigation metadata. |
| `UnassignGroupIntegrationTest` | Router dispatch, save counts, full JSON reload including zero assignments, both I/O failure types and retry, recovery/validation precedence, and reveal after saving. |
| `UnassignGroupUiTest` | Rendered same-object refresh, final empty state, workspace navigation, hidden/visible filters, selection preservation, save failures and compact RESULT-heading readability. |

Run from the repository root using Java 25 and the existing wrapper. Offline mode requires cached dependencies. Tests use disposable fixtures and temporary storage for failure injection.

```powershell
.\gradlew.bat --offline --no-daemon test --tests '*UnassignGroup*' --tests '*AssignmentTest'
.\gradlew.bat --offline --no-daemon test --tests '*UnlinkGuardian*' --tests '*RecoveryTest'
.\gradlew.bat --offline --no-daemon check coverage
git diff --check
```

## Limitations and later integration

* Normal disk writes are not atomic. Snapshot rollback restores memory, not bytes already partly written. Tests that fail before writing establish unchanged bytes only for that injected fault; they do not prove safety after a partial write. The save handler does not roll back arbitrary runtime exceptions from `command.execute()`, which runs outside its save `try` block.
* Extremely long feedback can grow beyond the window: an unknown ID containing 10,000 digits at compact size pushes the workspace/footer outside the viewport. This is shared with `assign-group`. The RESULT-heading minimum-width fix preserves the heading but does not bound feedback height. A bounded, scrollable result area is a separate follow-up; do not impose an ID-length limit to conceal it.
* Preserve both `unassign-group` and `unlink-guardian` routes, save-error mappings and documentation during later merges. Guardian search and guardian/student phone features are outside this source checkpoint. When integrating them, retain full-register lookup, complete snapshots/JSON and refresh after successful saving; inspect their actual branches before reconciling shared files.
