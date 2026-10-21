# Student Phone Handover

## Implemented behavior

Students now have an optional independent phone number. Add one with
`add-student n/NAME al/ACADEMIC_LEVEL p/PHONE`; add or replace it later with
`edit-student sid/STUDENT_ID p/PHONE`. Existing academic-level edits remain supported,
and both `al/` and `p/` may be supplied together. Student and guardian phones may
coincide and changing one never changes the other. Student duplicate identity remains
normalized name plus academic level.

The phone uses `StudentPhone`, a student-specific wrapper around the existing
`GuardianPhone` Singapore validation and canonical `+65 XXXXXXXX` formatting. It is
optional on the immutable `Student` model. `AddStudentCommandParser` and
`EditStudentCommandParser` construct that value, while `EditStudentCommand` replaces
the student through the model's existing identity check. `LogicManager` already saves
the complete record and restores its snapshot on an `IOException`, so student-phone
edits inherit the existing rollback behavior and failure feedback.

`JsonAdaptedStudent` writes the optional `phone` property and treats a missing property
as no phone, preserving compatibility with older student records. The student profile
renders the canonical phone or `Phone: none` through `StudentDetailsPanel` and
`StudentDetailsPanel.fxml`.

## Feedback

- Add without a phone retains the prior `Added student ... (LEVEL).` message.
- Add with a phone appends `with phone +65 XXXXXXXX.`
- Phone edit reports `Updated student ID phone to +65 XXXXXXXX.`
- Invalid phones use the shared Singapore phone validation error.
- Malformed edits show the command format supporting academic-level and phone edits.
- Unknown IDs use `Student ID does not exist.`
- Persistence failure rolls back and says `Could not save changes. Student ID was not updated.`

The complete user-facing command contract, duplicate rules, and error messages are in
`docs/FeatureSpecification.md`; examples are also in `docs/UserGuide.md`.

## Tests and verification

Focused tests cover add and edit feedback, optional add parsing and phone normalization,
phone-only and combined student edits, invalid phone input, legacy records without the
phone property, student phone storage conversion, and profile rendering with and without
a phone. The full run compiled main and test sources and completed 510 tests
successfully. The JavaFX test helper allows 30 seconds for actions on its application
thread to avoid timing out on slower CI runners. Both `checkstyleMain` and
`checkstyleTest` passed.
