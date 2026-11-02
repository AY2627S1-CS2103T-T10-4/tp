---
layout: page
title: Matthew Tan's Project Portfolio Page
---

### Project: Mentora

Mentora is a Java desktop application that helps coordinators of small tuition centres manage student records, guardian contacts, and subject or class assignments. Users enter commands through a JavaFX interface, and records are stored locally.

This portfolio summarises my contributions as of **3 November 2026**.

* **GitHub**: [matthewtansh](https://github.com/matthewtansh)
* **Code contributed**: [Authored commits](https://github.com/AY2627S1-CS2103T-T10-4/tp/commits/master/?author=matthewtansh)

#### New features

* **Student name search**: Implemented `find-student n/QUERY` with normalised, case-insensitive substring and multiword phrase matching. Added the filtered observable student list, input validation, match/no-match feedback, and tests while keeping persisted records and permanent student IDs unchanged. ([#62](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/62))
* **Student list reset**: Added `list-students` to restore the complete list after a search, allowing coordinators to return to their full register without restarting the application. ([#62](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/62))
* **Subject and academic-level filtering**: Implemented both `filter-students g/GROUP_CODE` and `filter-students al/ACADEMIC_LEVEL`. Validated subject versus class groups, displayed an active-filter summary, and retained stable student IDs and deterministic result ordering. Refreshed membership after assignment changes and failed-save rollback, while keeping filtering read-only. ([#90](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/90))

#### Enhancements and testing

* Added command, parser, predicate, and model tests for searching and list reset. Added filtering and JavaFX regressions for invalid inputs, workspace/summary updates, and assignment/unassignment rollback. ([#62](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/62), [#90](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/90))
* Fixed `list-students` and `find-student` so successful execution opens the Students tab, including empty results. Invalid commands preserve the previous tab and list. Added command and JavaFX regression tests. ([#136](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/136))
* Corrected invalid student searches to return the documented query-specific validation message. Added regressions for invalid characters, ID-shaped queries, length boundaries, and preservation of previous results and stored data. ([#138](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/138))

#### Documentation

* Contributed Developer Guide user stories, glossary, non-functional requirements, and use cases. ([#35](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/35))
* Added and updated the student filtering handover and student search/list integration notes. ([#90](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/90), [#136](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/136))
* Reconciled the feature specification and User Guide with the actual student list: rows show ID, name, and academic level, while guardian details appear in the selected student's profile. ([#137](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/137))

#### Team contributions

* Reviewed and approved release documentation and Java CI changes. ([Review on #147](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/147#pullrequestreview-5700119886))
* Reviewed teammates' profile and website updates and AI-use documentation. ([#24](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/24), [#26](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/26), [#156](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/156), [#160](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/160))

#### Contribution timeline

Dates below follow Git author dates; linked PRs provide the implementation and discussion records.

| Period (2026) | Contribution |
| --- | --- |
| 1 October | Team photo/profile and Developer Guide requirements material. |
| 15 October | Student name search, filtered-list support, list reset, and regression tests. |
| 22 October | Subject-group and academic-level filters, UI integration, and handover. |
| 29 October | Student tab navigation, query-specific errors, and student-list documentation fixes. |
| 2 November | AI-use acknowledgement in the Developer Guide. |

**Credits**: Mentora builds on [SE-EDU AddressBook Level 3](https://github.com/se-edu/addressbook-level3). My use of Codex for implementation, testing, debugging, documentation, and reviews is acknowledged in the [Developer Guide](../DeveloperGuide.md#acknowledgements).
