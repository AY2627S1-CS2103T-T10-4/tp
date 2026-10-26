---
layout: page
title: Add Group Handover
---

# Create a group

Implementation handover (26 October 2026). This increment supersedes the earlier fixed-catalogue limitation without
changing the existing `list-groups`, `assign-group`, `unassign-group`, or `filter-students` command words.

## Scope and command contract

Format: `add-group g/GROUP_CODE l/GROUP_LABEL k/GROUP_KIND`

Examples: `add-group g/PHY l/Physics k/Subject`; `add-group k/Class g/S3-PHY-B l/Secondary 3 Physics B`.

The command creates one reusable Subject or Class group and opens the Groups workspace. It does not assign any
student. Prefix and command casing are ignored, prefixes may appear in any order, and each prefix is required exactly
once with a nonempty value.

* A new code has 2-20 ASCII letters, digits, or hyphens, starts with a letter, ends with a letter or digit, and is
  stored uppercase. Its canonical value is the group's identity.
* A label is trimmed, internal whitespace is collapsed, and its normalized length is 1-80 characters.
* Kind is exactly `Subject` or `Class`, case-insensitively, and is displayed in that canonical spelling.
* Codes are unique case-insensitively. Labels may repeat because the code disambiguates groups.

| Outcome | Exact feedback |
| --- | --- |
| Success | `Created group PHY (Physics, Subject).` |
| Duplicate code | `Group PHY is already configured.` |
| Invalid code | `Group code must be 2-20 letters, digits, or hyphens, start with a letter, and end with a letter or digit.` |
| Invalid label | `Group label must be 1-80 characters.` |
| Invalid kind | `Group kind must be Subject or Class.` |
| Invalid syntax | `Invalid command format. Use: add-group g/GROUP_CODE l/GROUP_LABEL k/GROUP_KIND`<br>`GROUP_KIND must be Subject or Class.`<br>`Example: add-group g/PHY l/Physics k/Subject` |
| Save failure | `Could not save changes. No group was created.` |

## Execution and model integration

Java paths below are relative to `src/main/java/seedu/address/`.

1. `logic/parser/AddressBookParser` dispatches to `AddGroupCommandParser`. The parser rejects unknown, missing, empty,
   or repeated prefixes and constructs an immutable `Group` from `GroupCode`, its normalized label, and `GroupKind`.
1. `logic/commands/AddGroupCommand` delegates to `Model.addGroup(Group)`. `ModelManager` delegates to `AddressBook`,
   whose `addGroup` method performs the authoritative duplicate-code check and appends the group in catalogue order.
1. The command returns `CommandResult.forGroup`, requesting the Groups workspace and the new canonical code. The UI
   refreshes `GroupListPanel` from Logic and selects that row only after execution and saving succeed.
1. The command inherits mutating classification from `Command`. `LogicManager` blocks it during recovery, snapshots
   the complete `AddressBook`, saves once, and restores the snapshot with the feature-specific message if saving fails.

`GroupCode.isValidNewCode` and `Group.isValidNewLabel` expose creation-specific validation without narrowing the
legacy value constructors used by assignment lookup. `GroupKind.toDisplayString` supplies one canonical rendering to
commands, storage, and the Groups list.

## Persistence and migration

The group catalogue now belongs to `AddressBook` and participates in copy, reset, equality, and hashing. This is what
makes catalogue-only changes visible to `LogicManager`'s existing change detection and rollback boundary.

`JsonSerializableAddressBook` writes a `groups` array through `JsonAdaptedGroup`; each entry stores `code`, `label`,
and `kind`. Loading validates missing fields, code/label/kind constraints, duplicate canonical codes, and assignment
references before publishing a register. Files created by older builds have no `groups` property; they load the five
entries from `ConfiguredGroups`. An explicit empty array remains a valid empty catalogue.

The saved catalogue is loaded before group assignments so custom group codes can be resolved during assignment
validation. Existing data recovery behavior remains unchanged: malformed catalogues preserve the original file and
disable mutations for the session.

## UI and help integration

`GroupListPanel.refresh(GroupCatalogue)` replaces its read-only displayed snapshot and count. After a successful
`add-group`, `MainWindow` also applies the result's requested group selection. In-app help includes syntax, examples,
validation, duplicate behavior, and save rollback. `list-groups` now describes saved rather than fixed groups.

## Tests and regression commands

| Test | Main coverage |
| --- | --- |
| `AddGroupCommandParserTest` | Prefix order/casing, normalization, field constraints, and malformed input. |
| `AddGroupCommandTest` | Success metadata, duplicate rejection, and unchanged catalogue after failure. |
| `GroupCrudIntegrationTest` | Router dispatch, JSON reload, workspace/selection metadata, and save-failure rollback. |
| `GroupCatalogueStorageTest` | Custom catalogue round trip, legacy default migration, and invalid stored groups. |
| `GroupListPanelTest` | Catalogue refresh, count update, and canonical-code selection. |

Run from the repository root with Java 25 and the existing Gradle wrapper:

```powershell
.\gradlew.bat --offline --no-daemon test --tests '*AddGroup*' --tests '*GroupCatalogueStorageTest'
.\gradlew.bat --offline --no-daemon test checkstyleMain checkstyleTest
git diff --check
```

## Limitations and follow-up work

* Group codes are immutable. Renaming a code requires removing its assignments, deleting the group, and creating a
  replacement.
* There is no group-edit command in this increment.
* Catalogue order is insertion order. The feature does not provide manual sorting or reordering.
* Normal disk writes retain the application's existing non-atomic write limitation; in-memory rollback cannot repair
  bytes written before a late I/O failure.
