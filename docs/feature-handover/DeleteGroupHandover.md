---
layout: page
title: Delete Group Handover
---

# Delete a group

Implementation handover (26 October 2026). This feature removes a catalogue entry, not an individual student's
assignment. `unassign-group` remains the command for removing one student/group relationship.

## Scope and command contract

Format: `delete-group g/GROUP_CODE`

Example: `delete-group g/S3-PHY-B`.

The command permanently deletes one existing Subject or Class group and opens the Groups workspace. It never deletes
students or guardians. To avoid implicit data loss, deletion is rejected while one or more students remain assigned;
the coordinator must use `unassign-group` for each relationship first.

* Command and prefix casing are ignored.
* Exactly one nonempty `g/` prefix is required, with no preamble, extra text, repeated prefix, or unknown prefix.
* Code syntax matches `add-group`: 2-20 ASCII letters, digits, or hyphens, beginning with a letter and ending with a
  letter or digit. Lookup is case-insensitive and feedback uses the canonical uppercase code.
* The assignment count is calculated from the complete register, independently of the current student filter.

| Outcome | Exact feedback |
| --- | --- |
| Success | `Deleted group S3-PHY-B: Secondary 3 Physics B (Class).` |
| Unknown group | `Group S3-PHY-B is not configured. Run list-groups to see available groups.` |
| Assigned group | `Group S3-PHY-B is assigned to 4 student(s). Remove those assignments before deleting it.` |
| Invalid syntax | `Invalid command format. Use: delete-group g/GROUP_CODE` |
| Save failure | `Could not save changes. Group S3-PHY-B was not deleted.` |

## Execution and model integration

Java paths below are relative to `src/main/java/seedu/address/`.

1. `logic/parser/AddressBookParser` dispatches to `DeleteGroupCommandParser`, which validates the single prefix and
   returns a `DeleteGroupCommand` containing a canonical `GroupCode`.
1. The command resolves the existing `Group` from the current catalogue before mutation so success feedback retains
   its label and kind. Unknown codes use the shared group-not-configured message.
1. `ModelManager.deleteGroup` delegates to `AddressBook.deleteGroup`. The aggregate counts exact canonical-code
   matches in `groupAssignments`; a nonzero count rejects deletion without changing either collection. Otherwise it
   replaces the catalogue with an ordered copy that omits exactly the requested group.
1. The command returns success feedback and `Workspace.GROUPS`. `MainWindow` refreshes the group list after Logic
   returns, so a deleted row is never removed from the UI before persistence succeeds.
1. `LogicManager` treats the command as mutating, blocks it during recovery, snapshots the complete register, and
   saves exactly once after a successful domain mutation. An `IOException` restores students, guardians, links,
   assignments, and the full catalogue, then uses the deleted code in the feature-specific save-failure message.

The domain guard is deliberately inside `AddressBook`, not only the command, so direct model callers cannot delete an
assigned group. `setGroups` also rejects replacement catalogues that would leave any assignment pointing to a missing
group.

## Persistence and compatibility

Deletion is persisted by writing the complete `groups` array in the existing register JSON. Restarting cannot restore
a successfully deleted group because `ConfiguredGroups` is used only when loading a legacy file with no `groups`
property. Once the property exists, its contents—including an empty catalogue—are authoritative.

Assignments remain separate `studentId`/`groupCode` records. The command never cascades assignment deletion, which
keeps stored relationship loss explicit and preserves validation ordering. Malformed files containing assignments to
missing groups enter the existing recovery mode instead of silently dropping records.

## UI and help integration

Successful deletion opens Groups, refreshes the list and count, and leaves no selection request for the removed row.
Parse, domain, recovery, and save failures retain the previously rendered catalogue. In-app help distinguishes
`delete-group` from `unassign-group` and explains the no-assigned-students precondition.

## Tests and regression commands

| Test | Main coverage |
| --- | --- |
| `DeleteGroupCommandParserTest` | Prefix casing, canonical codes, invalid-code constraints, and malformed input. |
| `DeleteGroupCommandTest` | Successful deletion, unknown groups, assignment guard, and preserved assignments. |
| `GroupCrudIntegrationTest` | Router dispatch, persisted deletion, workspace routing, and save-failure rollback. |
| `GroupCatalogueStorageTest` | Authoritative saved catalogue, default migration, and stored-data validation. |
| Existing assignment/filter suites | Dynamic catalogue lookup continues to support assignment and filtering. |

Run from the repository root with Java 25 and the existing Gradle wrapper:

```powershell
.\gradlew.bat --offline --no-daemon test --tests '*DeleteGroup*' --tests '*GroupCrudIntegrationTest'
.\gradlew.bat --offline --no-daemon test checkstyleMain checkstyleTest
git diff --check
```

## Limitations and follow-up work

* Deletion is intentionally not cascading and there is no bulk-unassign command. Removing a heavily used group may
  require multiple explicit `unassign-group` commands.
* The feedback reports the blocking assignment count but does not list the affected student IDs.
* There is no undo command. A successfully deleted group must be recreated explicitly.
* Normal file writes remain non-atomic; the existing rollback guarantee covers in-memory state after a reported save
  failure, not arbitrary partial bytes already written by the storage layer.
