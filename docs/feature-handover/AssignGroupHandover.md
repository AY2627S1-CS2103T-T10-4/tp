---
layout: page
title: Assign Group Handover
---

# Feature 9: Assign configured groups

Current filter integration (22 October 2026): [Filter Students Handover](FilterStudentsHandover.md)
documents Features 18/19. Assignment mutations refresh active group-filter membership; save rollback must
refresh it again after restoring assignments. This is covered by `FilterStudentsUiTest`, including a failed
assignment that must not leave the target incorrectly visible. The checkpoint details below remain historical.

Owner: **Gabriel Chan (HIIMHEY)** · [Issue #58](https://github.com/AY2627S1-CS2103T-T10-4/tp/issues/58) · v1.3.
Packages **A** (catalogue/display), **R** (load-failure recovery) and **B** (assignment/persistence/profile) are implemented.
The current 15 October integration reconciles checkpoint `6327f8eaf6bdc5649a587d5c8821ec7c160af50b` with team master
`afc7ff0be83896dd6e21a754360365c733bac3a2` on `58-assign-configured-groups`. It adds Delete Student (#54) to the
existing Edit Student (#55), Guardian (#51), View Student (#57), Search/List (#62) and Packages A/R/B implementation.
Gabriel's personal review and acceptance of this integration remain pending.

## Scope and catalogue

`ConfiguredGroups.getCatalogue()` supplies one shared, immutable, validated `GroupCatalogue` in this order:

| Code | Label | Kind |
|---|---|---|
| ENG | English | SUBJECT |
| MAT | Mathematics | SUBJECT |
| SCI | Science | SUBJECT |
| CHI | Chinese | SUBJECT |
| S2-MAT-A | Secondary 2 Mathematics A | CLASS |

This is Gabriel's explicitly chosen reduced MVP catalogue. The original specification describes a broader catalogue;
separate teammate approval of this reduction is not claimed. Definitions are application configuration, independent
of student JSON and resets. Existing `GroupCode` normalization and catalogue validation are reused.
Guardian contacts from #51 are retained independently of students. Guardian linking, unassign-group and group CRUD
remain outside this integration.

## Command contract

- `list-groups`: case-insensitive, no arguments; reports `Listed 5 configured group(s).` and opens Groups.
  Extra input reports `Invalid command format. Use: list-groups`. It never saves student data.
- `assign-group sid/STUDENT_ID g/GROUP_CODE`: command/prefix names ignore case; either prefix order is accepted.
  Both prefixes must occur exactly once. Missing/empty values, unknown/repeated prefixes and unexpected text fail.
  IDs require uppercase `S-` followed by at least four digits; group codes use canonical case normalization.
- Lookup uses the authoritative register, independent of visible list positions. **Do not construct StudentId
  merely to validate or look up input:** its constructor advances the generator. Reuse the existing student's ID.
- Validate the student, configured group and duplicate pair before mutation, in that order. Multiple distinct groups,
  including MAT and S2-MAT-A together, are allowed without an academic-level matching restriction.

| Outcome | Exact feedback (substitute actual canonical codes, labels and IDs) |
|---|---|
| Success | `Assigned group S2-MAT-A (Secondary 2 Mathematics A) to student S-0007.` |
| Duplicate | `Student S-0007 is already assigned to S2-MAT-A.` |
| Unknown student | `Student S-0007 does not exist.` |
| Unknown group | `Group CODE is not configured. Run list-groups to see available groups.` |
| Invalid format | `Invalid command format. Use: assign-group sid/STUDENT_ID g/GROUP_CODE` |
| Save failure | `Could not save changes. The group was not assigned.` |

## Ownership and execution path

Java paths below are relative to `src/main/java/seedu/address/`.

| Component / API | Responsibility and integration contract |
|---|---|
| `model/group/GroupAssignment` | Immutable StudentId/GroupCode pair with value equality/hash. |
| `model/AddressBook.addGroupAssignment` | Owns a LinkedHashSet; validates existing student, configured group and pair uniqueness. `ReadOnlyAddressBook.getGroupAssignments()` exposes an unmodifiable live view. |
| `AddressBook.resetData` / copy / equals / hashCode | Include students, guardians and assignments. Validate the entire replacement before publishing; self-reset works. Mutable collections are copied, immutable values may be shared. Membership equality ignores insertion order; JSON retains insertion order. |
| `AddressBook.updateStudent` / `setStudents` / `deleteStudent` | Edit validates duplicates against every student before replacement; the stable ID preserves assignments. Bulk replacement retains assignments for retained IDs. Removing a student removes only their assignments, preserving other students and the catalogue. |
| `Model.findStudent(String)` / `addGroupAssignment` / `getAssignedGroups(String)` | Pure full-register lookup; validated mutation; immutable catalogue-ordered group projection. Labels come from configuration. Unknown-ID projections are empty. `getStudentList()` is the filtered view; `getAddressBook().getStudentList()` remains complete. |
| `logic/parser/AddressBookParser` → Assign/List parser → command | Validates syntax and domain preconditions; argument splitting retains multiline input. Parsers do not access persistence. |
| `logic/LogicManager.execute` | Applies the read-only/recovery policy, snapshots the complete aggregate, executes and saves changed persisted state. IOException restores the snapshot and raises the command-specific error. After success and any required save, `revealRequestedStudent()` clears a filter hiding the selected target; then the result reaches the UI. |
| `logic/commands/CommandResult` | Carries stable-ID selection, typed workspace requests and conditional selection clearing. All metadata participates in equality/hash. Existing constructors default to `UNCHANGED` and no clearing; Delete Student carries the deleted ID for comparison with the pre-command selection. |

Keep invariants in the aggregate, persistence in Logic/Storage and rendering in UI components. Command-level
validation provides feedback; aggregate validation also protects loading/reset callers. Linear lookup and complete
snapshots suit the specified 30–200 records. ModelManager equality/hash include the register and preferences.

## Persistence and recovery

`JsonSerializableAddressBook` stores students with optional top-level guardian and assignment arrays:

```json
{
  "students": [{"id": "S-0007", "name": "Mei Lin", "academicLevel": "Sec 2"}],
  "guardians": [{"id": "G-0003", "name": "Anita Lim", "phone": "+65 91234567"}],
  "groupAssignments": [{"studentId": "S-0007", "groupCode": "MAT"}]
}
```

- Missing `guardians` or `groupAssignments` means an empty collection; explicit JSON null is a load failure.
  Existing two-/three-list Java compatibility constructors still treat null arguments as absent collections.
  Assignments persist only IDs/canonical codes; guardian records retain their ID, name and canonical phone.
- `JsonAdaptedGroupAssignment` resolves IDs from students already loaded. Students, guardians and assignments are validated
  in a temporary aggregate before publication; assignment-only changes participate in snapshots/equality and saving.
- Explicit null/malformed collections or entries, missing fields, duplicate IDs/identities/guardian phones or canonical pairs,
  dangling student references, unknown group codes and unsupported fields fail loading.
- `StoredStringDeserializer` rejects booleans/numbers/containers for Student, Guardian and assignment text fields rather than
  coercing them into valid text. `JsonUtil` rejects empty input, null roots, duplicate JSON fields and trailing content.
- Missing student arrays retain existing compatibility. Legacy `persons` and root `_comment` are intentionally ignored;
  legacy Person records are not migrated or retained on the next save. Recognize future schema fields before loading them.

### Recovery and write protection

The previous failure path treated unreadable data as a writable empty register. Package R adds an explicit,
latched storage-session failure state:

| Startup result | Behavior |
|---|---|
| Confirmed missing file | Load existing sample students; permit normal saving. Inaccessible/uncertain paths and dangling links are not treated as missing. |
| Valid file, including an empty register | Load the complete state; permit normal saving. |
| Read, parse or domain-validation failure | Preserve original bytes; start an empty recovery model with mutations and register writes disabled. |

`StorageManager.readAddressBook()` retains the first DataLoadingException. Its `getDataRecoveryReason()` is exposed
through Storage and Logic; successful rereading does **not** unlock the session.

1. `Command.isReadOnly()` defaults to false. Logic rejects mutations before execution during recovery.
   Help, exit, list-groups, view-student, find-student and list-students explicitly opt into read-only execution
   and skip saving in either session state. View Student on the empty recovery model reports the normal unknown ID.
2. `StorageManager.saveAddressBook()` independently rejects recovery writes before reaching the file writer.

Future commands must retain the safe default unless they leave persisted state unchanged; new Storage
implementations must honor the same contract. UI metadata never determines write permission.
Specific IO/JSON/domain failures become DataLoadingException; unrelated programming failures are not suppressed.

`MainApp.initModelManager()` supplies the recovery model. MainWindow initially displays
`DataLoadingException.MESSAGE_RECOVERY`:

> Could not load local data. Your original data file was not changed; restore or replace it before saving.

The footer remains `Saving disabled` and retains the original reason in a tooltip after query feedback changes.
Close the application, preserve the original, restore/replace it with valid data and restart. There is no in-session
unlock or repair editor. Window preferences retain their independent lifecycle.

## UI integration

- MainWindow mounts one GroupListPanel in `groupsWorkspacePlaceholder`; it renders code, label, kind and count.
  Tab switches reuse panels/listeners and preserve Student selection.
- `MainWindow.refreshStudentDetails()` supplies the selected Student and `Logic.getAssignedGroups(id)` to
  `StudentDetailsPanel.setStudent(Student, List<Group>)`. The single-argument overload renders no assignments.
- Successful assignment requests Students plus the target ID. Explicit refresh after success handles unchanged
  Student identity; selection listeners handle different students. Do not rely on same-object reselection.
- View/Assign/Add/Edit Student can target records hidden by search: Logic clears the filter only after success,
  then MainWindow selects the requested ID. Visible targets retain their filter. View/Edit/Search/List keep the current tab.
  Academic-level edits replace immutable Students without changing assignments; the same explicit refresh handles replacement.
- On failure, restore the previous stable-ID selection (including no selection), render the rolled-back model
  and retain the workspace. No ghost assignments should remain.
- Delete Student uses authoritative lookup and the existing aggregate cleanup. MainWindow applies its conditional
  clearing request through the same selection/refresh helper. Deleting another student, including a hidden one,
  preserves the visible selection, search filter and workspace. Deleting the selected student clears the profile.
- The profile shows canonical codes/labels or `No groups assigned.`. One `profileScrollPane` keeps all fields
  reachable with an 80-character name at the actual 820×560 minimum. Startup dimensions are at least 900×600.
  Preserve Raynor's profile styling and this narrow refresh/navigation integration surface.

## Teammate integration

The current merge target includes #51, #54, #55, #57 and #62. Other teammate work is excluded from this pinned integration;
recheck its current state before any later merge.

| PR | Integration contract |
|---|---|
| #51 Add Guardian — included | Complete snapshots/JSON retain all three collections. Strict JSON setters coexist with Java compatibility constructors and guardian-specific save errors. |
| #57 View Student and #62 Search/List — included | Explicit read-only classification, authoritative ID lookup and post-success filter clearing; manual View Student tab navigation remains. |
| #54 Delete Student — included | Non-allocating full-register lookup, removal of only the target's assignments, guardian preservation, full rollback and conditional profile clearing. See [Delete Student Handover](DeleteStudentHandover.md). |
| #55 Edit Student — included | Full-register textual lookup, aggregate duplicate validation, stable-ID assignment preservation, complete rollback and post-save filter reveal. See [Edit Student Handover](EditStudentHandover.md). |
| #63 Link Guardian — pending | Requires its own relationship/schema reconciliation. Do not silently accept or discard unsupported link fields. |

Keep complete-register rollback and each command's save-error wording in subsequent integrations. Existing tests
prove Edit and Delete Student integration with persisted assignments and guardian records. Retired Person adapters remain
baseline compatibility and must not be reconnected.

## Verification and maintainer checks

Recorded on **15 October 2026** for the combined working tree, Windows, Java **25.0.1**, Gradle **9.6.1**.
The pinned Delete Student merge remains uncommitted. The previous `6327f8ea` checkpoint passed 423 tests;
earlier audit results and complete logs remain in prior commits/local records.

| Check | Recorded result |
|---|---|
| Focused integration suite | **60 tests passed.** New regressions exposed hidden-target deletion and whitespace handling; the initial run and a corrected UI test setup error are retained in local records. |
| Complete combined suite | **440 tests, 93 suites; zero failures, errors or skips.** Original tests from both parents remain. Main/test Checkstyle, coverage and shadowJar passed with tasks rerun. The incoming test's duplicate import and line-wrap violation were corrected without changing its assertions. |
| Scoped Javadoc | Seven integration production files passed with `-Xdoclint:all,-missing`; API completeness was reviewed separately. The final metadata Javadoc clarification was rechecked with Checkstyle and JAR packaging. |
| Repository-wide Javadoc | NOT RUN for this merge. The 14 October audit found the unchanged unresolved `Command` link at `logic/commands/exceptions/CommandException.java:4` and capped missing-comment/tag warnings. |
| Built-JAR/GUI smoke | Six JVM scenarios passed through the JAR's Main entry point: create/edit, reload, delete, deleted-state reload, recovery and long-name layout. Deleting a hidden student preserved another profile/assignment; deleting the selected student cleared the profile. Restart retained the one surviving student, two guardians and one assignment. Queries preserved bytes; recovery blocked deletion. Automated JavaFX command-box events and screenshot inspection were used. |
| Human keyboard/mouse acceptance; other operating systems | **NOT RUN.** |
| Scope/whitespace | `git diff --check` passed; real data/preferences and existing ignored fixtures were preserved. |

Verified JAR SHA-256: `4894378D9EACC6F35B808A9968E8FFAE6EB82623D6F2C8E728178164F02551AA`.
Earlier failures and exact local commands are preserved separately. Regression tests retain the fixes for JSON
scalar coercion, ModelManager equality/hash, long-name overflow, ambiguous UI lookup and same-student refresh.

Key test entry points under `src/test/java/seedu/address/`:

- Catalogue/query: `model/group/*Test`, `ListGroupsCommandTest`, `ListGroupsCommandParserTest`,
  `LogicManagerTest`, `GroupListPanelTest`.
- Assignment/domain: `AddressBookAssignmentTest`, `ModelManagerAssignmentTest`,
  `AssignGroupCommandTest`, `AssignGroupCommandParserTest`, `AssignGroupIntegrationTest`.
- Storage/recovery: `GroupAssignmentStorageTest`, `StoredStringDeserializerTest`,
  `StorageManagerRecoveryTest`, `LogicManagerRecoveryTest`, `MainAppRecoveryTest`, `MainAppAssignmentTest`.
- UI: `AssignGroupUiTest`, `MainWindowTest`, `StudentDetailsPanelTest`.
- Combined integration: `CombinedRegisterTest`, `CombinedRegisterStorageTest`, `MergedCommandsIntegrationTest`,
  plus the extended startup/UI tests. These cover all three collections, filtered commands, complete rollback,
  strict guardian JSON and independent query execution during recovery. Edit regressions add hidden lookup/duplicates,
  stable-ID reuse, preserved assignments/guardians, restart and failed-save UI restoration. Delete regressions add
  hidden-target removal, assignment cleanup, conditional selection clearing and recovery/rollback preservation.

For the next checkout, use the existing repository tasks:

```powershell
.\gradlew.bat check coverage shadowJar --rerun-tasks
.\gradlew.bat javadoc
git diff --check
```

The recorded full run additionally used `--offline --no-daemon`, `integrationJavadoc` and a local init script for an
isolated test working directory and scoped Javadoc task; that helper is not shared build tooling. Logs, scripts and
screenshots for this merge are retained under ignored
`build/reports/feature9-delete-integration-b6d8268e52b649bda0d9dab8133f3871/`. Previous integration records remain under
`build/reports/feature9-edit-integration-ff934617b8ea43bf86149aa1bbfd5077/` and
`build/reports/feature9-integration-5e27982d3a414af4b77fdb3ab1ea9974/`.
Preserve existing fixtures: use a
disposable checkout or isolate the test working directory, copying test data while excluding its existing sandbox.
For manual acceptance, launch the built JAR by absolute path from fresh synthetic-data/preferences directories.
Check multiple assignments, editing/deleting hidden students, duplicate/invalid input, same/different Student selection,
tabs, minimum-size scrolling,
save/restart, then malformed-data recovery with byte/hash comparisons after rejected mutations, queries and exit.
The read-denied automated case injects AccessDeniedException; a directory at the file path covers a real read failure.

## Limitations and next steps

- **Normal saves are not atomic.** Direct Files.write can partially damage an existing file after IOException.
  Memory rollback and pre-write failure tests do not guarantee disk preservation. A separately approved fix should
  serialize first, write a unique sibling temporary file and atomically replace the destination, failing safely if
  unsupported. Test write/move failures, unchanged destination bytes, first save and recovery's writer guard.
- Changing/removing configured codes or extending stored data needs an explicit compatibility decision; unknown
  references/fields currently enter recovery. Do not weaken validation to make an integration file load.
- Existing StudentId numeric-overflow limits, the old Help-window guide URL, help/exit argument handling and
  repository-wide Javadoc cleanup remain separate follow-ups.
- Review the source and tests personally, reconcile teammate changes, and rerun verification before acceptance.
  Structured learning remains deferred; key paths are catalogue → parser/command → aggregate → save/rollback →
  profile refresh.
