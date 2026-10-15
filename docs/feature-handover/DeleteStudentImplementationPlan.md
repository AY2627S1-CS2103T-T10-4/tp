# Delete Student Implementation Plan

> Historical plan from PR #54, preserved for its original scope and records. Its student-only model and command
> availability statements describe that earlier snapshot. See [Delete Student Handover](DeleteStudentHandover.md)
> for the current combined register, profile behavior, recovery contracts and verification.

## Scope

Implement the currently specified `delete-student sid/STUDENT_ID` command against
the active student-only application. The supported model at this stage contains
only student ID, name, and academic level. Guardian links and student profiles
are not yet part of that model, so this increment can delete a student record but
cannot report a guardian link or close a student profile.

The `FeatureSpecification.md` and `UserGuide.md` describe the eventual behavior
with guardians and profiles. Update those descriptions to match the implemented
increment, or defer the guardian/profile clauses until those models are integrated.

## Existing foundation

- `Model`, `ModelManager`, and `AddressBook` already expose `deleteStudent(StudentId)`.
- `StudentId` validates the permanent `S-0001` format and advances the ID generator
  when constructed from persisted IDs.
- The student list is backed by an observable list, so removing a row refreshes it.
- `CommandResult` and `MainWindow` already support selecting a student by stable ID.
- `LogicManager` already snapshots the model, but its persistence rollback and
  user-facing save error currently cover only `AddStudentCommand`.
- The active parser accepts only `add-student`, `help`, and `exit`. Legacy
  `DeleteCommand` is Person-based and must remain unregistered.

## Implementation steps

1. **Command and parser**
   - Add `DeleteStudentCommand` with command word `delete-student`.
   - Resolve the supplied stable ID against the student list before mutating it;
     return the specified student-not-found error if absent.
   - Delete by `StudentId`, not by visible list position.
   - Return feedback containing the deleted student's ID and name.
   - Add `DeleteStudentCommandParser` for exactly one `sid/` parameter. Reject
     missing, malformed, repeated, unknown, or extra parameters with the shared
     format message from the feature specification.
   - Register the parser in `AddressBookParser` without exposing legacy
     Person commands.

2. **Persistence rollback**
   - Extend `LogicManager` to restore the pre-command snapshot if saving a delete
     fails, and surface `Could not save changes. Student S-0007 was not deleted.`
     with the actual ID substituted.
   - Keep existing add rollback behavior intact. Prefer a small command-specific
     rollback/error hook over broad command refactoring.

3. **UI behavior**
   - Let the observable student list remove the deleted row naturally.
   - Verify JavaFX clears or updates selection and the details pane when its
     selected student disappears. Add explicit selection handling only if the
     current controls leave stale details or an invalid selection.
   - Profile closing is deferred until the student profile command/window exists.

4. **Tests**
   - Command: successful deletion, unknown ID, exact feedback, and preservation
     of other students.
   - Parser: valid ID and missing, invalid, repeated, unknown, and extra input.
   - Logic/persistence: successful save and failed-save rollback/error.
   - UI: deleted selected student disappears and details do not show stale data;
     deleting a non-selected student preserves a valid selection.

5. **Documentation**
   - Update the current-build command list in `UserGuide.md` and its delete section
     to state only behavior available in this increment.
   - Keep the complete guardian-aware success wording as future behavior in
     `FeatureSpecification.md`, clearly marking the guardian/profile clauses as
     dependent on those features, or revise the feature contract if this increment
     is intended to ship without them.
   - Link this plan from `docs/UI.md` only if handover plans are being indexed
     there; otherwise this file lives with the feature handovers.

## Out of scope / integration points

- Guardian records, student-guardian links, and retaining shared guardians.
  The active model has no guardian entity or relationship storage. When added,
  deletion must remove only the target student's link and retain the guardian
  and other students' links.
- Closing an open student profile. The current details pane is selection-driven;
  the profile feature handover describes an older Person-based foundation and
  does not provide a student profile implementation.
- Replacing generic `AddressBook` naming, removing legacy Person compatibility
  classes, or adding archive/undo behavior.

## Completion criteria

- The active command parser accepts `delete-student` and rejects the retired
  Person `delete` command.
- Valid IDs delete exactly one student; invalid or unknown IDs leave the model
  unchanged.
- Failed persistence restores the student and displays the specified error.
- The visible list and selected-student details remain consistent after deletion.
- User-facing current-build documentation matches the shipped behavior.

## Implementation status

Implemented in the current increment:

- The active parser accepts `delete-student sid/STUDENT_ID` and leaves legacy
  Person `delete` unavailable.
- The command deletes by stable ID, reports missing IDs, and returns the removed
  student's ID and name.
- Save failures restore the pre-command student register and report a
  delete-specific error.
- The selected details are cleared after deletion, and the current-build guide
  documents the implemented behavior and its guardian/profile limitations.
- Focused command, parser, logic rollback, parser registration, and UI tests were
  added. The full test suite and both Checkstyle tasks passed with
  `./gradlew test checkstyleMain checkstyleTest`.
