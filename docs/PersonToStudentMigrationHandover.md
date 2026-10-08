---
layout: page
title: Person to Student Migration Handover
---

# Person to Student Migration Handover

## Completed in this increment

- The active `AddressBook`, `Model`, `ModelManager`, `Logic`, and `LogicManager` data flow now stores and exposes students only.
- The command parser accepts `add-student`, `help`, and `exit`. Person-only commands such as `add`, `edit`, `delete`, `clear`, `find`, and `list` now return the unknown-command error.
- `JsonSerializableAddressBook` writes only the `students` property and ignores the old `persons` property while loading legacy files. Person records cannot enter the active model.
- The main window renders the student list and student details panels. Add Student requests selection using its generated student ID. The old PR #45 row-index selection API is deprecated and ignored.
- New local data starts with sample student records. The command prompt and current-build section of the user guide reflect the available commands.

## Compatibility behavior and data migration

Person model/value classes, Person command implementations, parser helpers, and unused Person UI components remain in the source tree so existing source-level tests and callers do not disappear in one change. They are not registered in `AddressBookParser`; compatibility methods on the active model throw `UnsupportedOperationException` or return empty views. They must not be reconnected to the student UI.

Legacy JSON `persons` entries are ignored because they do not contain the academic level required to create a valid Student. The next successful save writes a student-only JSON document and removes those entries. Existing `students` entries are retained and their IDs continue to advance the ID generator when loaded.

## Continue here

1. Migrate or retire Person-focused tests, then remove the compatibility API and unused Person classes, commands, parsers, and FXML resources.
2. Add the remaining specified student, guardian, relationship, group, search, and profile features as separate focused increments.
3. Replace the generic `AddressBook` naming with student-register naming only if the team wants that broader rename; it is currently retained to avoid unrelated storage and bootstrap churn.

No tests were run. Java production compilation completed successfully with `compileJava`.
