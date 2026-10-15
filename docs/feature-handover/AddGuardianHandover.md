# Add guardian feature handover

## Implemented

- Added immutable guardian domain values under `model/guardian/`: `GuardianId`, `GuardianName`, `GuardianPhone`, and `Guardian`.
- `GuardianName` implements the shared page-one name rule. `GuardianPhone` accepts an eight-digit Singapore number beginning with 3, 6, 8, or 9, with an optional `+65` prefix and optional single space, and stores it as `+65 XXXXXXXX`.
- Added the keyboard-first `add-guardian n/NAME p/PHONE` command and parser. Prefix order and casing are flexible; missing, repeated, unknown, or stray parameters use the specified format error.
- Guardian phone numbers are unique after canonicalization. Duplicate attempts identify the existing guardian ID and recommend linking that record.
- Extended `AddressBook`, `ReadOnlyAddressBook`, `Model`, and `ModelManager` to retain guardian records independently from students. Guardians are sorted by normalized name and then stable ID for deterministic persistence and future list views.
- Extended JSON persistence with a `guardians` collection. Existing student-only files remain compatible because an absent guardian collection loads as empty. Invalid IDs, names, phones, duplicate IDs, and duplicate canonical phones are rejected on load.
- `LogicManager` restores the complete Student/Guardian/assignment register if saving fails and retains the specified guardian-specific save error. Queries bypass saving and remain available during recovery.
- Updated the current-build note in the user guide. No JavaFX/FXML changes were made, and an added guardian is not selected or displayed in the current student-only UI.

## Integration notes for guardian linking

- The combined serializer recognizes `guardians` alongside `students` and `groupAssignments`. An absent guardian array is empty; explicit JSON null, null entries, non-string fields and unsupported properties fail loading and latch recovery. Existing Java compatibility constructors still allow null collections to mean absent data.
- `GuardianId` is stable and generated independently as `G-0001`, `G-0002`, and so on. Loading a stored ID advances the generator beyond it.
- Use `Model#getGuardianList()` to resolve a `GuardianId` when implementing `link-guardian`; do not copy guardian name or phone into `Student`. Store only the relationship/guardian ID so later phone edits remain visible to every linked student.
- The MVP relationship is one optional guardian per student and many students per guardian. Adding a guardian deliberately creates no relationship.
- Add ID lookup methods to `AddressBook`/`Model` when link and edit commands arrive. `AddressBook#findGuardianByPhone` exists only for the add command's unique-phone check.
- Persist the relationship in a way that can validate dangling guardian IDs during load. Load guardians before validating student links, and reject a file when a referenced guardian does not exist.
- Keep the student list and profile as the UI source of truth. The link feature may refresh a profile, but should not introduce a separate guardian UI unless the product specification is revised.

## Deferred work

- `edit-guardian`, `link-guardian`, and `unlink-guardian` remain unimplemented.
- There is no guardian list/search view in this increment.
- Student and guardian name classes currently implement the same shared validation separately. A future cleanup may extract a neutral shared name value if it can preserve the existing public APIs and tests.
