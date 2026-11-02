---
layout: page
title: Edit Student Handover
---

# Edit Student Handover

## Implemented

The `edit-student sid/STUDENT_ID [al/ACADEMIC_LEVEL] [p/PHONE]` command updates a student's academic level, independent phone, or both. At least one of `al/` or `p/` is required. It retains the student's ID, name, assignments and guardian link, all guardian records and unrelated students. Fields omitted from the command retain their previous values. It rejects unknown IDs and duplicate normalized name/level identities against the complete register, independent of the filtered UI list. The parser validates the ID as text, so parsing and executing a lookup do not advance the generated-ID counter. Command/prefix casing and any prefix order are accepted; missing required, empty, repeated and unknown prefixes are rejected.

Successful edits report the supplied fields: `Updated student S-0007: academic level is now Sec 3.`, `Updated student S-0007 phone to +65 98765432.`, or `Updated student S-0007: academic level is now Sec 3 and phone is now +65 98765432.` The result requests selection by stable student ID. Logic reveals a hidden target after any required save; MainWindow selects it and explicitly refreshes the profile, including repeated edits to the same ID. The current workspace is retained. If saving fails, `LogicManager` restores the complete pre-command snapshot and reports `Could not save changes. Student S-0007 was not updated.` The previous filter, selection and workspace remain. An edit that leaves the register unchanged skips saving; failed-load recovery rejects edits before execution.

The student phone follows the shared Singapore number rule, need not be unique and does not change the guardian's phone. An existing phone cannot be cleared. See [Student Phone Handover](StudentPhoneHandover.md) for the later phone implementation and tests.

## Implementation map

| File | Responsibility |
|---|---|
| `logic/parser/AddressBookParser.java` | Registers `edit-student`. |
| `logic/parser/EditStudentCommandParser.java` | Requires a student ID and at least one edit field; validates prefixes, ID format, academic level and phone. |
| `logic/commands/EditStudentCommand.java` | Uses `Model.findStudent(String)`, reuses the matched record's ID, delegates replacement/duplicate validation to `Model.updateStudent`, and returns stable-ID selection or the specified rejection. |
| `model/AddressBook.java` | Looks up by ID and replaces the matching record while preserving unique name/level identity. |
| `model/Model.java`, `model/ModelManager.java` | Expose student lookup and update APIs. |
| `logic/LogicManager.java` | Applies recovery protection, saves changed aggregate state, rolls back every collection on `IOException`, and reveals hidden targets after success. |
| `ui/MainWindow.java`, `ui/StudentListPanel.java` | One selection/refresh path handles edited students and restores prior selection, including no selection, after errors. |
| `docs/UserGuide.md` | Documents command syntax and behavior. |

`JsonAdaptedStudent` persists academic level and the optional student phone. Older student records without a phone remain supported.

## Tests and verification

The checks and dated results below record the original academic-level increment. The later phone checks are documented in [Student Phone Handover](StudentPhoneHandover.md#tests-and-verification).

- `logic/commands/EditStudentCommandTest.java`: successful update, unknown and large IDs, duplicate identity, and unchanged data on rejection.
- `logic/parser/EditStudentCommandParserTest.java`: valid input, large-ID generator stability, missing/repeated prefixes, unknown prefixes, and preamble rejection.
- `ui/MainWindowTest.java`: recovery rejects edits with visible feedback. Its incoming list-removal test uses a logic double; it does not establish a Delete Student command implementation.
- `logic/LogicManagerTest.java`: persistence failure restores the old academic level and reports the student ID.
- Combined integration: `MergedCommandsIntegrationTest`, `CombinedRegisterTest`, `MainAppAssignmentTest` and
  `AssignGroupUiTest` cover hidden targets/duplicates, unchanged ID generation, all three collections, restart,
  recovery, unchanged-data save suppression, full rollback and profile/selection behavior.

On 15 October 2026, the pinned `fff49a93` + `dc80c701` integration passed 43 focused tests and all 423 tests
(91 suites, no failures/errors/skips), both Checkstyle tasks, coverage, shadowJar and scoped Javadoc on Java 25.0.1.
The full command was `check coverage shadowJar integrationJavadoc --offline --no-daemon --rerun-tasks` through
the Gradle wrapper with a local init script isolating fixtures and providing the scoped Javadoc task.
Built-JAR create/edit, restart, recovery and long-name profile scenarios passed with synthetic data; screenshots
were inspected. Human keyboard/mouse acceptance and other operating systems were not tested.
Exact commands and initial failing results remain in the ignored verification records linked from
[Assign Group Handover](AssignGroupHandover.md#verification-and-maintainer-checks).

## Integration notes

- `AddressBook.updateStudent` excludes the edited student's own ID from duplicate validation before replacing anything.
  Keep this authoritative check in the aggregate; `Model.getStudentList()` is filtered and is unsuitable for validation.
- `Student` is immutable. Use `Model.updateStudent` to replace its observable-list entry rather than mutating a field.
- Future mutations inherit complete-register snapshot rollback; add their feature-specific save feedback where needed.
- Normal direct file writes are still non-atomic; in-memory rollback cannot repair a partial disk write.
- Student deletion removes assignments and guardian links; see [Delete Student Handover](DeleteStudentHandover.md).
  Academic-level and phone edits preserve both relationships; see [Link Guardian Handover](LinkGuardianHandover.md).
