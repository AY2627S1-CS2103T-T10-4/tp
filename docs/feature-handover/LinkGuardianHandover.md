# Link guardian handover (Feature 6)

## Implemented scope

`link-guardian sid/STUDENT_ID gid/GUARDIAN_ID` links an existing student to an
existing guardian. Prefix order and casing are flexible; IDs retain uppercase
`S-` and `G-`. Missing, repeated, malformed, or extra arguments report the command
usage. Lookup never constructs an ID or advances either generator.

Each student has at most one guardian, while siblings may share a guardian.
Repeating a link or attempting to replace it produces the specification's error
message. Unlinking is a separate feature.

## Model and persistence contract

- `ReadOnlyAddressBook#getGuardianLinks()` exposes an unmodifiable mapping from
  `StudentId` to `GuardianId`. Relationships never contain copied contact details.
- `Model#linkGuardian(String, String)` validates both references and rejects an
  existing relationship before mutating the register.
- `Logic#getLinkedGuardian(String)` resolves the current guardian record each time;
  an unknown or unlinked student returns an empty optional.
- JSON stores an optional `guardianLinks` array containing `studentId` and
  `guardianId` fields. Files without that array remain compatible. Null entries,
  missing IDs, dangling references, and multiple links for a student are rejected.
- Load students and guardians before links. Include links in snapshot copying,
  equality, and hashing, so link-only changes are detected and saved.
- Deleting a student removes only their link, preserving guardians and siblings'
  links. Replacing student records retains links for surviving IDs. Replacing
  guardians cannot remove a referenced guardian.
- Failed saves restore the pre-command model and report
  `Could not save changes. The guardian link was not created.` Unchanged student
  lists are not reset, preserving their selection.

The existing storage layer writes directly to disk. In-memory rollback does not
guarantee recovery of a file partially written before an I/O failure; atomic file
replacement and failed-load recovery protection remain separate storage work.

## UI and documentation handoff

UI, FXML, and User Guide files are intentionally unchanged, as agreed with their
owners. The command result requests selection using the student's existing ID.
The existing interface still displays only the fields it already supports.

The UI owner should retrieve `logic.getLinkedGuardian(studentId)` when displaying
or refreshing a profile. Show the current name and canonical phone, or
`Guardian: none`. Refresh after successful linking even when the student was
already selected; selecting the same row may not trigger a selection listener.
The specification's immediate guardian display requires this UI integration.
The User Guide owner should mark linking available once the feature is integrated.

## Integration with parallel work

This branch starts from `a3611c19`, after PRs #51 and #57. It does not depend on
unmerged PR #59. Both branches touch model copying, equality, JSON storage,
parsing, and save rollback. When merging, retain students, guardians, guardian
links, and group assignments in all of those paths. Preserve the null-guardian
validation from #51 and the read-only behavior of `view-student` from #57.

If #59's recovery gate is adopted, `link-guardian` must remain mutating and be
blocked during recovery. Preserve its command-specific save failure message.
Reuse the same `sid/` prefix rather than introducing duplicate constants.

## Validation

Model tests cover shared guardians, duplicate/replacement rejection, current
contact lookup, snapshot independence, rollback notifications, and deletion.
Storage tests cover round trips, old files, and malformed links. Command/parser
tests cover message contracts, argument validation, and ID-generator stability.
Logic integration tests cover saving, reopening, both I/O failure paths, and
preserving existing links when a later link fails to save.

Run `./gradlew check coverage` (or `./gradlew.bat check coverage` on Windows).
