---
layout: page
title: Christian Alexander's Project Portfolio Page
---

### Project: Mentora

Mentora is a Java desktop application that helps coordinators of small tuition centres manage student records, guardian contacts, and subject or class assignments. Users enter commands through a JavaFX interface, and records are stored locally.

This portfolio summarises my contributions as of **3 November 2026**.

* **GitHub**: [Chrsxndr](https://github.com/Chrsxndr)
* **Code contributed**: [Authored commits](https://github.com/AY2627S1-CS2103T-T10-4/tp/commits/master/?author=Chrsxndr)

#### New features

* **Command-driven student profiles**: Added the command-result selection mechanism and integrated `view-student sid/STUDENT_ID`, allowing coordinators to open a profile by permanent ID. Validated lookups and kept profile viewing read-only. ([#45](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/45), [#57](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/57))
* **Guardian linking**: Implemented `link-guardian`, persistent student–guardian relationships, and immediate profile updates showing the linked guardian's name and phone. Preserved relationships across other student operations and restored data after failed saves. ([#63](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/63))
* **Guardian unlinking**: Implemented `unlink-guardian` to correct a relationship without deleting either record. Updated student profiles, guardian counts, and linked-student details while preserving sibling links and group assignments. ([#70](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/70))
* **Guardian name search**: Implemented `find-guardian n/QUERY` with normalised, case-insensitive substring matching, Guardians workspace navigation, and query-specific empty-result feedback. Kept searches read-only and independent of student filters. ([#71](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/71))
* **Offline command help**: Added `help [COMMAND]` and bundled syntax, examples, validation rules, and common errors in the help window so coordinators can consult command guidance without internet access. ([#93](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/93))

#### Enhancements and testing

* Added parser, command, model, storage, recovery, and JavaFX integration tests for profile selection, guardian relationships, and searching. Covered failed-save rollback and preservation of unrelated records. ([#57](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/57), [#63](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/63), [#70](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/70), [#71](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/71))
* Corrected student profile headings, Unicode case matching in guardian search, and singular/plural feedback for student and guardian searches. ([#128](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/128), [#129](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/129), [#130](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/130))
* Strengthened Java CI with manual runs, JAR packaging on all three operating systems, sequential OS checks, and cancellation of outdated runs. ([#147](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/147))

#### Documentation

* Updated the User Guide and documented guardian unlinking, searching, and command help alongside their feature handovers. ([#36](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/36), [#70](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/70), [#71](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/71), [#93](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/93))
* Updated the Storage class diagram and Developer Guide explanations of filtering, selection, persistence, rollback, recovery, requirements, and guardian relationships, including the guardian-link sequence diagram. ([#91](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/91), [#143](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/143))
* Prepared v1.4 alpha documentation and release notes, aligned guides with merged release behaviour, and refreshed User Guide screenshots and Unicode guidance. ([#93](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/93), [#147](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/147), [#166](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/166))

#### Team contributions

* Identified a nested-predicate refresh problem in student filtering that could cause a `StackOverflowError` after repeated assignment changes, and requested documentation of both filter variants. ([Review on #90](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/90#discussion_r4345770403))
* Reviewed group assignment integration, requesting preservation of guardian persistence and read-only student viewing when resolving merge conflicts. ([Review on #59](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/59#pullrequestreview-5527951749))
* Reviewed teammates' student, guardian, and group regression changes. ([#47](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/47), [#51](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/51), [#149](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/149))

#### Contribution timeline

Dates below follow Git author dates; linked PRs provide the implementation and discussion records.

| Period (2026) | Contribution |
| --- | --- |
| 1–8 October | User Guide and team profile updates; command-driven profile selection. |
| 13–15 October | Student viewing, persistent guardian links, and immediate profile refresh. |
| 19–22 October | Guardian unlinking/searching, offline help, Storage diagram, and alpha release documentation. |
| 28–29 October | Unicode search and feedback fixes, Developer Guide diagrams, and release CI checks. |
| 3 November | AI-use acknowledgement and refreshed User Guide screenshots. |

**Credits**: Mentora builds on [SE-EDU AddressBook Level 3](https://github.com/se-edu/addressbook-level3). My use of ChatGPT and Codex for implementation, tests, documentation, reviews, and release preparation is acknowledged in the [Developer Guide](../DeveloperGuide.md#acknowledgements).
