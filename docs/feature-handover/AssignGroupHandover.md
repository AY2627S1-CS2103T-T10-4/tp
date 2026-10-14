---
layout: page
title: Assign Group Handover
---

# Feature 9: Assign configured groups

Owner: **Gabriel Chan (HIIMHEY)** · [Issue #58](https://github.com/AY2627S1-CS2103T-T10-4/tp/issues/58) · v1.3.
Packages **A** (catalogue/display), **R** (load-failure recovery) and **B** (assignment/persistence/profile) are implemented.
This handover reflects the 14 October 2026 audit on `58-assign-configured-groups`, based on
`d24144961fe21adb3f58d18f70d7654fc0f20bde`. Gabriel's personal review and acceptance remain pending.

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
No unassign-group, group CRUD, guardian functionality or teammate edit/delete commands are implemented here.

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
| `AddressBook.resetData` / copy / equals / hashCode | Include assignments. Validate the entire replacement before publishing; self-reset works. Mutable collections are copied, immutable values may be shared. Membership equality ignores insertion order; JSON retains insertion order. |
| `AddressBook.setStudents` / `deleteStudent` | Replacement preserves assignments for retained IDs. Removing a student removes only their assignments, preserving other students and the catalogue. |
| `Model.findStudent(String)` / `addGroupAssignment` / `getAssignedGroups(String)` | Pure full-register lookup; validated mutation; immutable catalogue-ordered group projection. Labels come from configuration. Unknown-ID projections are empty. |
| `logic/parser/AddressBookParser` → Assign/List parser → command | Validates syntax and domain preconditions; argument splitting retains multiline input. Parsers do not access persistence. |
| `logic/LogicManager.execute` | Applies the read-only/recovery policy, snapshots the complete aggregate, executes and saves mutations. IOException restores the snapshot and raises the command-specific error. UI results are returned only after saving succeeds. |
| `logic/commands/CommandResult` | Carries stable-ID selection and typed `Workspace.GROUPS` / `STUDENTS`; existing constructors default to `UNCHANGED`. |

Keep invariants in the aggregate, persistence in Logic/Storage and rendering in UI components. Command-level
validation provides feedback; aggregate validation also protects loading/reset callers. Linear lookup and complete
snapshots suit the specified 30–200 records. ModelManager equality/hash include the register and preferences.

## Persistence and recovery

`JsonSerializableAddressBook` stores students plus an optional top-level assignment array:

```json
{
  "students": [{"id": "S-0007", "name": "Mei Lin", "academicLevel": "Sec 2"}],
  "groupAssignments": [{"studentId": "S-0007", "groupCode": "MAT"}]
}
```

- Older Student files without `groupAssignments` load with no assignments. Only IDs/canonical codes are persisted.
- `JsonAdaptedGroupAssignment` resolves IDs from students already loaded. Students and all assignments are validated
  in a temporary aggregate before publication; assignment-only changes participate in snapshots/equality and saving.
- Explicit null/malformed collections or entries, missing fields, duplicate IDs/identities or canonical pairs,
  dangling student references, unknown group codes and unsupported fields fail loading.
- `StoredStringDeserializer` rejects booleans/numbers/containers for Student and assignment text fields rather than
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
   Help, exit and list-groups explicitly opt into read-only execution and skip saving in either session state.
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
- On failure, restore the previous stable-ID selection (including no selection), render the rolled-back model
  and retain the workspace. No ghost assignments should remain.
- The profile shows canonical codes/labels or `No groups assigned.`. One `profileScrollPane` keeps all fields
  reachable with an 80-character name at the actual 820×560 minimum. Startup dimensions are at least 900×600.
  Preserve Raynor's profile styling and this narrow refresh/navigation integration surface.

## Teammate integration

At the **14 October audit**, the following PRs were open/unmerged. Their patches were inspected; combined execution
was not verified. Recheck their current state before integration.

| PR / inspected head | Required reconciliation |
|---|---|
| [#51 Add Guardian](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/51) / `d43b719a` | Preserve the union of students, guardians and assignments in copy/reset/equality/JSON. Reconcile its JSON creator with strict setters/validation; do not silently drop new fields. |
| [#54 Delete Student](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/54) / `909c3884` | Preserve assignment cleanup and combine clear-selection metadata with workspace/selection requests; refresh the shared profile renderer. |
| [#55 Edit Student](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/55) / `0be68fa4` | Retain memberships when reconstructing Students with the same ID. Reconcile `findStudent(StudentId)` / `updateStudent(Student)` with non-allocating String lookup. |
| [#57 View Student](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/57) / `fd533ddf` | Declare ViewStudentCommand read-only or recovery will reject it. Retain assignment-aware equality if skipping unchanged saves; reconcile navigation through CommandResult. |

Keep complete-register rollback when merging the command-specific helpers from #51/#54/#55, and preserve each
command's save-error wording. Shared parser, Model, serializer, LogicManager, CommandResult and MainWindow changes
need a combined regression pass. Existing tests prove aggregate replacement/deletion compatibility, not those
unmerged commands. Retired Person adapters remain baseline compatibility and must not be reconnected.

## Verification and maintainer checks

Recorded on **14 October 2026**, Windows, Java **25.0.1**, Gradle **9.6.1**; these are the final implementation-audit
results, not new execution during documentation consolidation.

| Check | Recorded result |
|---|---|
| Focused storage/model/UI suite | 62 tests passed. |
| Complete suite, including A/R/B and Add Student | **330 tests, 74 suites; zero failures, errors or skips.** Main/test Checkstyle, coverage and shadowJar passed with tasks rerun. |
| Scoped Javadoc | Passed for changed/new production files with `-Xdoclint:all,-missing`; API completeness was reviewed separately. |
| Repository-wide Javadoc | Failed on the pre-existing unresolved `Command` link at `logic/commands/exceptions/CommandException.java:4`; output also reached the 100 missing-comment/tag warning cap. |
| Built-JAR/GUI smoke | Separate create, reload, recovery and long-name JVMs passed using the JAR's actual Main entry point; six assignment pairs survived restart and invalid bytes stayed unchanged. Rendered images inspected at normal/compact sizes. |
| Human keyboard/mouse acceptance; other operating systems | **NOT RUN.** |
| Scope/whitespace | `git diff --check` passed; real data/preferences and existing ignored fixtures were preserved. |

Verified JAR SHA-256: `4866CB02643250DBD7BE99AFCD9642120E2700D0ABC178AC2DC7F1727E2A3293`.
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

For the next checkout, use the existing repository tasks:

```powershell
.\gradlew.bat check coverage shadowJar --rerun-tasks
.\gradlew.bat javadoc
git diff --check
```

The recorded full run additionally used `--offline --no-daemon` and a local init script for an isolated test working
directory and scoped Javadoc task; that helper is not shared build tooling. Preserve existing fixtures: use a
disposable checkout or isolate the test working directory, copying test data while excluding its existing sandbox.
For manual acceptance, launch the built JAR by absolute path from fresh synthetic-data/preferences directories.
Check multiple assignments, duplicate/invalid input, same/different Student selection, tabs, minimum-size scrolling,
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
  The [Developer Guide acknowledgement](../DeveloperGuide.md#acknowledgements) records Gabriel's ChatGPT
  planning/supervision and substantial Codex implementation/testing/review assistance. Structured learning remains
  deferred; key paths are catalogue → parser/command → aggregate → save/rollback → profile refresh.
