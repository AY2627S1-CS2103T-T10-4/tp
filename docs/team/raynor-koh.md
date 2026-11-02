---
layout: page
title: Raynor Koh's Project Portfolio Page
---

### Project: Mentora

Mentora is a Java desktop application that helps coordinators of small tuition centres manage student records, guardian contacts, and subject or class assignments. Users enter commands through a JavaFX interface, and records are stored locally.

This portfolio summarises my contributions as of **3 November 2026**.

* **GitHub**: [raynor-koh](https://github.com/raynor-koh)
* **Code contributed**: [Authored commits](https://github.com/AY2627S1-CS2103T-T10-4/tp/commits/master/?author=raynor-koh)

#### New features

* **Student registration and model foundation**: Added student value types, permanent generated IDs, normalised names, academic levels, and JSON persistence. Implemented `add-student` with validation, duplicate detection, failed-save rollback, and selection of newly added records. Migrated the active Person-based flow to student records during integration. ([#47](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/47))
* **Student deletion**: Implemented `delete-student sid/STUDENT_ID`, unknown-ID feedback, persistence rollback, and selection/profile updates. Later aligned linked-guardian deletion feedback with the specification and verified preservation of the guardian and a sibling's link. ([#54](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/54), [#125](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/125))
* **Student academic-level editing**: Implemented `edit-student` support for academic levels, duplicate-identity checks, profile refresh, and rollback on failed saves, allowing coordinators to maintain existing records safely. ([#55](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/55))
* **Guardian phone updates**: Added `edit-guardian gid/GUARDIAN_ID p/PHONE`, reusing Singapore phone validation and canonical formatting. Detected duplicate phone numbers and updated the shared guardian record so linked student profiles show the changed contact number. ([#76](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/76))
* **Optional student phones**: Extended student creation, editing, storage, and profiles with optional student phone numbers. Retained compatibility with older records and kept student and guardian phone values independent. ([#77](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/77))

#### Enhancements and testing

* Added student parser, command, storage, profile, and rollback tests, plus guardian-phone validation, persistence, duplicate handling, and linked-profile tests. Tightened editing validation for empty fields, duplicate phone prefixes, phone spacing, and combined-edit feedback. ([#47](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/47), [#55](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/55), [#76](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/76), [#77](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/77))
* Fixed missing-file startup to open an empty register instead of loading sample students. Added a regression covering empty startup and subsequent normal saving. ([#123](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/123))
* Fixed Unicode case-insensitive student identity comparison across duplicate detection, editing, loading, and name searches, with regressions for case variants. ([#124](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/124))

#### Documentation

* Updated the initial Mentora landing page and site branding. ([#26](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/26))
* Documented student creation, deletion, editing, and phone support, and added feature handovers for integration and maintenance. ([#47](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/47), [#54](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/54), [#55](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/55), [#77](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/77))
* Updated Model and Parser diagrams, added Register and persistence diagrams, and clarified the Developer Guide's save, in-memory rollback, and disk-write recovery behaviour. ([#85](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/85), [#145](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/145))

#### Team contributions

* Identified missing immediate guardian profile updates during review of linking, prompting completion of the feature's UI integration. ([Review on #63](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/63#pullrequestreview-5532609256))
* Requested release-note alignment and a feature handover for group unassignment. ([Release-note review on #73](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/73#discussion_r4330295994), [handover review on #73](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/73#discussion_r4330302336))
* Reviewed teammates' recovery, layout, documentation, and release CI work. ([#81](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/81), [#134](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/134), [#135](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/135), [#147](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/147))

#### Contribution timeline

Dates below follow Git author dates; linked PRs provide the implementation and discussion records.

| Period (2026) | Contribution |
| --- | --- |
| 1 October | Landing page, site branding, and team profile. |
| 8 October | Student model, registration, persistence, Person-to-Student migration, and tests. |
| 13–15 October | Student deletion, academic-level editing, rollback, and profile updates. |
| 21–22 October | Guardian phone updates, optional student phones, validation, and Model diagram. |
| 28–29 October | Empty-register startup, Unicode duplicate handling, deletion feedback, and Developer Guide diagrams. |
| 2 November | AI-use acknowledgement in the Developer Guide. |

**Credits**: Mentora builds on [SE-EDU AddressBook Level 3](https://github.com/se-edu/addressbook-level3). My AI-assisted design, specification discussions, implementation planning, coding, and testing are acknowledged in the [Developer Guide](../DeveloperGuide.md#acknowledgements).
