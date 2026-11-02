---
layout: page
title: Gabriel Chan's Project Portfolio Page
---

### Project: Mentora

Mentora is a Java desktop application that helps coordinators of small tuition centres manage student records, guardian contacts, and subject or class assignments. Users enter commands through a JavaFX interface, and records are stored locally.

This portfolio summarises my contributions as of **3 November 2026**.

* **GitHub**: [HIIMHEY](https://github.com/HIIMHEY)
* **Code contributed**: [Authored commits](https://github.com/AY2627S1-CS2103T-T10-4/tp/commits/master/?author=HIIMHEY)

#### New features

* **Group catalogue foundation**: Added `GroupCode`, `GroupKind`, `Group`, and `GroupCatalogue`, with validation, case-insensitive lookup, defensive copying, and tests. These types established the shared foundation for subject and class groups. ([#43](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/43))
* **Group discovery and assignment**: Implemented `list-groups`, the Groups workspace, and `assign-group sid/STUDENT_ID g/GROUP_CODE`. Persisted memberships using permanent student IDs and canonical group codes, prevented duplicate assignments, and displayed assignments in student profiles. ([#59](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/59))
* **Data-loading recovery protection**: Added safeguards that prevent mutating commands from overwriting the original data file after a malformed or unreadable file fails to load. This protects existing records while allowing users to inspect the recovery state. Normal saves remain non-atomic. ([#59](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/59))
* **Group unassignment**: Implemented `unassign-group` across the model, parser, command routing, persistence, and UI. Removed only the requested membership, preserved unrelated records and groups, refreshed student profiles, and restored in-memory state after failed saves. ([#73](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/73))

#### Enhancements and testing

* Restored guardian selection and profile details after failed saves while preserving filters and workspace state. Added rollback diagnostics that retain the original exception without including command arguments, and replaced raw-input UI failure logging with a sanitised diagnostic. ([#81](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/81))
* Kept academic levels visible beside long student names and made long group codes fully readable, with JavaFX regressions for layout, resizing, and visibility. ([#134](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/134), [#153](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/153))
* Strengthened assignment tests to assert canonical values and verify custom-catalogue persistence after unassignment. Rejected duplicate `add-group` prefixes preceded by tabs, with parser regressions. ([#149](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/149), [#154](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/154))
* Fixed an unresolved Javadoc link and changed the executable build output from the inherited filename to `mentora.jar`. ([#79](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/79), [#132](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/132))

#### Documentation

* Documented group catalogue, assignment, unassignment, and recovery contracts in feature handovers and the guides. Updated the Logic class diagram and its Developer Guide explanation. ([#43](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/43), [#59](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/59), [#73](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/73), [#87](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/87))
* Replaced legacy manual-testing instructions with repeatable student, guardian, and group workflows, boundary and invalid-input checks, persistence/recovery scenarios, and expected UI outcomes. ([#133](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/133))
* Updated the User Guide and product website with installation guidance, implemented workflows, backup/recovery advice, and screenshots captured from the running application using synthetic records. ([#135](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/135))

#### Team contributions

* Identified locale-dependent student ID formatting during review and requested `Locale.ROOT`; also raised integration concerns about hidden legacy Person records and student-profile selection. ([ID review on #47](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/47#discussion_r4220518614), [integration review on #47](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/47#discussion_r4220522370))
* Reviewed teammates' student deletion/editing, guardian features, and student search/navigation fixes. ([#54](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/54), [#55](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/55), [#70](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/70), [#136](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/136), [#138](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/138))

#### Contribution timeline

Dates below follow Git author dates; linked PRs provide the implementation and discussion records.

| Period (2026) | Contribution |
| --- | --- |
| 30 September–1 October | Team photo and About Us profile. |
| 8 October | Group catalogue types, validation, tests, and handover. |
| 14–21 October | Group listing/assignment, loading recovery, unassignment, and failed-save guardian selection repair. |
| 22–29 October | Logic diagram, JAR branding, manual testing, User Guide, product website, and long-name layout repair. |
| 31 October–2 November | Stronger assignment assertions, catalogue persistence regression, long group-code display, duplicate-prefix validation, and AI-use acknowledgement. |

**Credits**: Mentora builds on [SE-EDU AddressBook Level 3](https://github.com/se-edu/addressbook-level3). My extensive use of ChatGPT and Codex for code, tests, documentation, debugging, and reviews is acknowledged in the [Developer Guide](../DeveloperGuide.md#acknowledgements).
