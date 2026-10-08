# Add Student handover

## Implemented

- Added `StudentId`, `StudentName`, `AcademicLevel`, and immutable `Student` values in `model/student/`.
- Added a sorted student collection to `AddressBook`, exposed it through `Model` and `Logic`, and added `add-student` parsing and command execution.
- Persisted students alongside the legacy `persons` JSON property. Loaded IDs advance the process-wide ID generator, preventing reuse after restart.
- Added a student list and details view. A successful add returns its generated ID to `MainWindow`, which selects the matching list item.
- `LogicManager` snapshots the model before executing a command and restores it if persistence fails. Add Student reports the specified save failure message.

## Remaining scope

The app still retains the Address Book `Person` model, APIs, commands, serialization, and unused contact UI classes so existing features continue to compile. This increment does not implement guardians, assignments, search, profile relationships, or the full Person-to-Student migration. Existing persisted `persons` data is retained; the application displays only student records in the main workspace.

`compileJava` completed successfully. Tests and UI launch were not run. Continue by adding focused tests for student values, parser edge cases, duplicate rules, JSON reload/ID continuity, save rollback, and list selection, then migrate the remaining feature APIs and storage under coordinated follow-on work.
