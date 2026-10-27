---
layout: page
title: Mentora v1.4 Alpha Release Notes
---

# Mentora v1.4 Alpha Release Notes

Mentora is a local Java desktop application for tuition-centre coordinators to manage students, guardian
contacts and configured subject/class assignments through typed commands. This alpha increment builds on
[v1.3](https://github.com/AY2627S1-CS2103T-T10-4/tp/releases/tag/v1.3); features may still need polishing.

## Changes since v1.3

- **Guardian browsing and search:** `list-guardians` opens the Guardians workspace with contacts and
  linked-student details. `find-guardian n/QUERY` finds names by case-insensitive substring; listing resets search.
- **Shared guardian phone updates:** `edit-guardian gid/GUARDIAN_ID p/PHONE` updates the contact shown in all
  linked student profiles while rejecting duplicate guardian phone numbers.
- **Unlinking guardians:** `unlink-guardian sid/STUDENT_ID` removes the relationship while keeping both records
  and sibling links. The selected profile and guardian linked-student counts refresh after saving succeeds.
- **Independent student phones:** `add-student` accepts optional `p/PHONE`; `edit-student` can add or replace the
  phone, alone or with an academic-level edit. Profiles show the student's own phone separately from the guardian's.
- **Removing assignments:** `unassign-group sid/STUDENT_ID g/GROUP_CODE` removes one assignment, including the
  final assignment, without deleting the student or affecting other assignments.
- **Student filters:** `filter-students g/MAT` filters by a configured Subject group and
  `filter-students al/Sec 2` filters by academic level. Students shows the active filter; `list-students` resets it.
- **Offline command help:** `help` lists every supported command. `help COMMAND` shows syntax, examples,
  validation rules and common errors in the same window, with a link to Mentora's User Guide.
- **Reliability and documentation:** Added validation and UI regression tests, including filtered-list and
  selection restoration after failed saves. Updated the UI, Logic, Model and Storage UML class diagrams.

## Running Mentora

1. Install Java 25.
2. Download the release JAR into a folder where Mentora can save its data.
3. Open a terminal in that folder and run `java -jar addressbook.jar`, substituting the downloaded filename
   if the release asset has been renamed.
4. Enter `help` to browse commands, or read the [User Guide](UserGuide.md).

Back up `data/addressbook.json` before upgrading. Student records without a phone remain supported.
An invalid data file is preserved and puts Mentora into recovery mode with saving disabled for that session.

## Supported commands

`add-student`, `edit-student`, `delete-student`, `list-students`, `find-student`, `filter-students`,
`view-student`, `add-guardian`, `list-guardians`, `find-guardian`, `edit-guardian`, `link-guardian`,
`unlink-guardian`, `list-groups`, `assign-group`, `unassign-group`, `help`, and `exit`.

## Known limitations

- One guardian per student; siblings may share a guardian. Guardian names cannot be edited and guardian records
  cannot be deleted through commands.
- The fixed catalogue contains `ENG`, `MAT`, `SCI`, `CHI` and `S2-MAT-A`. Group creation is unavailable.
  Subject filtering rejects Class groups; academic-level filtering is independent of assignments.
- Normal saves are not atomic. A failed save restores the in-memory register but may leave a partially written
  disk file. Keep backups.
- User Guide illustrations remain mockups.
- Repeated assignment changes without replacing an active filter can accumulate predicate wrappers; an extreme
  long-session stress test can cause a stack overflow. Reapply the filter or run `list-students` to reset it.
- Mentora is a single-user application. Attendance, grades, fees, accounting, messaging, imports and exports
  remain outside this release.

## Release verification

Run `gradlew.bat check coverage shadowJar` (Windows) or `./gradlew check coverage shadowJar` before publishing.
The generated JAR is `build/libs/addressbook.jar`. Verify the final merged release commit in CI and launch
that JAR before attaching it to the GitHub release.
