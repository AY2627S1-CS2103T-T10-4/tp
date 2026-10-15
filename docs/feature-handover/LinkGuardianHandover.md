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

## Profile display integration

`MainWindow` retrieves `logic.getLinkedGuardian(studentId)` when displaying a
profile. The details panel shows the current name and canonical phone, or
`Guardian: none`. Successful profile requests explicitly refresh the details,
even when the student was already selected. Linking explicitly requests the
Students workspace through `CommandResult.Workspace.STUDENTS`.

After a successful command and save, Logic reveals a requested student hidden by
search by clearing the filter, following the shared selection contract from master.
The UI selects that student and refreshes both guardian and group details.
Failed commands preserve the previous filter and profile. Saved guardian details
also appear on startup and selection.

JavaFX integration tests exercise real commands and storage for same-row refresh,
different students, hidden students, reopening saved data, and save failures.
The User Guide remains with its owner and should mark linking available.

## Integration with parallel work

The merge of `upstream/master` at `5eb1667c` integrates PR #59 and student editing
and deletion. Snapshots, equality, JSON storage, and rollback retain both guardian
links and group assignments. Deletion removes both relationships for the deleted
student while retaining shared guardians and other students' relationships.

`link-guardian` remains mutating and is blocked by the shared recovery gate.
Its command-specific save failure message is retained. Read-only profile and
group commands remain available during recovery.

## Validation

Model tests cover shared guardians, duplicate/replacement rejection, current
contact lookup, snapshot independence, rollback notifications, and deletion.
Storage tests cover round trips, old files, and malformed links. Command/parser
tests cover message contracts, argument validation, and ID-generator stability.
Logic integration tests cover saving, reopening, both I/O failure paths, and
preserving existing links when a later link fails to save.

Run `./gradlew check coverage` (or `./gradlew.bat check coverage` on Windows).
