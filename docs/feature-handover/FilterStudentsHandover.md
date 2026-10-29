---
layout: page
title: Filter Students Handover
---

# Features 18 and 19: Filter students

This handover covers the subject-group and academic-level variants of `filter-students` in the
uncommitted working tree, verified on **22 October 2026**. Both variants share one command and parser.

## Command contract

| Variant | Format | Examples |
| --- | --- | --- |
| Subject group | `filter-students g/GROUP_CODE` | `filter-students g/MAT`; `filter-students g/SCI` |
| Academic level | `filter-students al/ACADEMIC_LEVEL` | `filter-students al/Sec 2`; `filter-students al/IP 4` |

- Accept exactly one `g/` or one `al/`. Combining variants or repeating a parameter is invalid.
- Command and prefix casing are ignored. Group codes use `GroupCode` canonicalization; levels use
  `AcademicLevel`, which accepts Sec 1–5 and IP 1–6 with case and whitespace normalization.
- Group filtering requires a configured **Subject** group. A Class group such as `S2-MAT-A` is rejected;
  academic-level filtering compares the student's level, independently of Class-group membership.
- Each valid command replaces the previous student filter and searches the complete source list.
  Results retain normalized-name order, with permanent student ID as the tie-breaker. A student assigned
  to several groups appears only once.
- Both variants are read-only: they update the displayed view without saving student data or changing JSON.

| Outcome | Exact feedback (substitute the actual count, canonical code or level) |
| --- | --- |
| Group matches | `Found 12 student(s) assigned to MAT.` |
| Empty group result | `No students are assigned to MAT.` |
| Level matches | `Found 3 student(s) at Sec 2.` |
| Empty level result | `No students are at Sec 2.` |
| Unknown group | `Group CODE is not configured. Run list-groups to see available groups.` |
| Class group | `Group S2-MAT-A is a Class group; use the academic-level filter.` |
| Invalid level value | `Academic level must be Sec 1-5 or IP 1-6.` |
| Invalid group syntax | `Invalid command format. Use: filter-students g/GROUP_CODE` |
| Invalid level syntax | `Invalid command format. Use: filter-students al/ACADEMIC_LEVEL` |

Missing/empty values, repeated/additional prefixes and unexpected preamble are format errors.
A slash remaining inside a parsed value is malformed syntax: `g//MAT`, `g/MATg/SCI` and
`al/Sec 2 /extra` report usage rather than a group-lookup or level-validation error.
For mixed variants, usage follows the first recognized `g/` or `al/`; if neither exists, it defaults to group usage.

## Implementation and UI integration

Java paths below are relative to `src/main/java/seedu/address/`.

| Component | Responsibility |
| --- | --- |
| `logic/parser/AddressBookParser` | Routes `filter-students` to `FilterStudentsCommandParser`. |
| `logic/parser/FilterStudentsCommandParser` | Validates syntax and constructs the group or level command variant. |
| `logic/commands/FilterStudentsCommand` | Checks group existence/kind before changing the view; applies the predicate and summary; returns count/empty feedback and `Workspace.STUDENTS`. |
| `model/ModelManager` | Owns the existing `FilteredList<Student>` and filter summary; refreshes membership after assignment changes and assignment rollback. |
| `logic/LogicManager` | Exposes the summary and skips persistence for this read-only command. |
| `ui/MainWindow` / `MainWindow.fxml` | Switches workspace on success and displays `studentFilterSummaryLabel` above the list. |

The group predicate reads current assignment membership, rather than capturing a snapshot. The level
predicate compares `AcademicLevel` values. Authoritative lookup and storage continue using the complete register.

Successful filtering opens Students and displays `Filtered by group: MAT — Mathematics` or
`Filtered by academic level: Sec 2`. An empty filter still displays its summary. Selecting a row opens
the existing student profile by permanent ID. A previous selection is retained only if it remains visible;
otherwise the profile shows the existing empty-selection state.

`list-students` restores the full list, hides the summary and opens the Students workspace.
A subsequent name search also replaces the predicate, clears this summary and opens Students,
including when no students match. Invalid query/list arguments preserve the current workspace.
`LogicManager.revealRequestedStudent()` clears the predicate and summary when a successful command requests
a student hidden by the filter, after any required save succeeds.

Rejected filter commands leave the tab, list and summary unchanged. MainWindow refreshes the summary after
successful command execution; the label is unmanaged and invisible when no group/level filter is active.

## Review fixes and rollback contract

Two defects found during review and GUI/CLI testing were fixed in this working tree:

1. **Stale filtered rows after a failed assignment save.** Assignment mutation refreshed the list, but
   `AddressBook.resetData()` restored assignments without emitting a student-list event when the student
   records were unchanged. `ModelManager.setAddressBook()` now detects assignment differences before reset
   and refreshes the active filter after a successful reset. Unrelated guardian-only rollbacks retain the
   existing student predicate. Keep this behavior when changing snapshot/rollback code.
2. **Wrong errors for malformed values.** The filter parser now rejects remaining slash delimiters before
   domain validation. Unknown codes without malformed syntax still use the shared group-not-configured error;
   invalid level values such as `Sec 6` still use the shared academic-level error.

Assignment-only refresh is needed after both successful mutations and rollback. A failed assignment or
unassignment must restore the filtered rows as well as the underlying relationships. The UI must retain its
previous selected student, filter summary and workspace. See the
[assignment](AssignGroupHandover.md) and [unassignment](UnassignGroupHandover.md) handovers for persistence ownership.

## Tests and verification

Test paths are relative to `src/test/java/seedu/address/`.

| Test | Coverage |
| --- | --- |
| `logic/commands/FilterStudentsCommandTest` | Both variants, empty results, shared errors, summary, assignment refresh and read-only classification. |
| `logic/parser/FilterStudentsCommandParserTest` | Routing, case/whitespace, missing/repeated/mixed/additional/malformed parameters and shared invalid-level feedback. |
| `logic/commands/ListStudentsCommandTest` | Restores the full list and clears the active summary. |
| `ui/FilterStudentsUiTest` | Real command processing, workspace/summary/results, malformed-input state preservation, and failed assignment/unassignment rollback with selection restoration. |

Run from the repository root with Java 25:

```powershell
.\gradlew.bat test --tests '*FilterStudents*' --tests '*ListStudentsCommandTest'
.\gradlew.bat check shadowJar
git diff --check
```

Post-fix verification on Windows: **593 tests passed**, both Checkstyle tasks passed, and the JAR built.
A separate CLI harness invoking the built JAR's parser and LogicManager passed **24/24 checks**, including
unchanged data bytes and both formerly incorrect error messages. This harness is not a standalone product CLI.

Before the fixes, native GUI interaction verified matching/empty results, tab switching, profile selection,
summary reset, Run/Enter submission and rejection state preservation. It also reproduced the malformed-input
errors and the failed-save stale row using a read-only disposable data file. Post-fix UI verification used the
automated JavaFX regression tests above; the native manual scenario was not rerun after the fixes.

Local exploratory evidence is under ignored `build/filter-smoke/` and `build/reports/filter-review/`.
Some reports describe the pre-fix failures; these are historical evidence, not the current status.
These directories are not shared test infrastructure and may be removed by `gradlew clean`.
