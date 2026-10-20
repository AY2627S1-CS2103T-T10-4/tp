# Unlink guardian (Feature 7)

`unlink-guardian sid/STUDENT_ID` removes only the student's guardian link.
Students, guardians, sibling links, and group assignments are retained. The
student can then be linked to another guardian to correct a mistaken relationship.

## Implementation

- `AddressBook.unlinkGuardian(String)` validates the student and relationship
  before mutation and returns the existing guardian ID. No ID is allocated.
- `UnlinkGuardianCommandParser` accepts exactly one `sid/` argument. Prefix and
  command casing are flexible; the ID must retain uppercase `S-`.
- `UnlinkGuardianCommand` reports the specification's feedback and requests the
  Students workspace and stable-ID selection. It remains mutating by default.
- `LogicManager` rejects mutations during recovery, saves before returning the
  result, and restores the complete snapshot on an I/O failure. Unlink failures
  report `Could not save changes. The guardian link was not removed.`
- The existing JSON link collection stores the removal without schema changes.
  As with other commands, rollback restores memory; the existing direct-write
  storage layer cannot guarantee recovery from a partially written disk file.

## UI integration

The existing MainWindow success path refreshes student and guardian details,
including same-row selection and relationship-only changes. The student shows
`Guardian: none`; the guardian's list count and linked-student rows refresh.
Hidden target students are revealed only after saving succeeds. Failed commands
retain the previous filter, selection, and workspace.

## Verification

Model tests cover sibling links, retained records/assignments, invalid requests,
snapshot restoration, and filtered students. Parser tests cover casing, whitespace,
and malformed or repeated arguments. Logic tests cover JSON reload, relinking,
both I/O failure paths, and recovery protection. JavaFX tests exercise the real
command to verify both profiles, guardian counts, and save-failure behaviour.

Run `./gradlew.bat check coverage` on Windows.
