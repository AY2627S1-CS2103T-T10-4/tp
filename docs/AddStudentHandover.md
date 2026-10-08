# Student-only model migration handover

## Implemented

- Added `StudentId`, `StudentName`, `AcademicLevel`, and immutable `Student` values in `model/student/`.
- `AddressBook`, `ReadOnlyAddressBook`, `Model`, and `ModelManager` now store and expose student records only.
- The active parser accepts `add-student`, `help`, and `exit`; legacy Person commands are no longer reachable from the command box.
- JSON persistence writes student records only and ignores a legacy `persons` property when reading old Address Book files.
- The main window displays a student list and details view. Command results select students by stable ID. PR #45's Person row-index selection is retired and ignored.
- `LogicManager` snapshots the student-only model before executing a command and restores it if persistence fails for `add-student`.
- New installs start with sample student records. The command box prompt and user guide match the supported command set.

## Remaining scope

Legacy Person value classes, command implementations, parser helpers, and unused UI components remain in the source tree for compatibility with existing source-level tests. They are not exposed by the parser, are not part of the student model or persistence schema, and their model mutation APIs are deprecated and unsupported. Legacy `persons` entries are not migrated into students because they lack academic levels; saving the student-only model removes those entries. Guardian records, assignments, student edit/delete, search, and profile commands remain future work.

Tests and UI launch were not run. Continue by removing the retired Person compatibility source and updating the old Person-focused test suites, then implement the remaining specified student and guardian features.
