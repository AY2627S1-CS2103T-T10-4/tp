---
layout: page
title: Valentino Nathan's Project Portfolio Page
---

### Project: Mentora

Mentora is a Java desktop application that helps coordinators of small tuition centres manage student records, guardian contacts, and subject or class assignments. Users enter commands through a JavaFX interface, and records are stored locally.

This portfolio summarises my contributions as of **3 November 2026**.

* **GitHub**: [valentinonathan](https://github.com/valentinonathan)
* **Code contributed**: [Authored commits](https://github.com/AY2627S1-CS2103T-T10-4/tp/commits/master/?author=valentinonathan)

#### New features

* **Mentora interface and navigation**: Replaced the inherited portrait layout with a responsive landscape interface, a light theme, a selected-contact details panel, and a Run button. Subsequently restyled the student list and profile and added navigation tabs, giving coordinators a shared workspace for their commands and records. ([#41](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/41), [#49](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/49))
* **Guardian registration**: Implemented `add-guardian`, including guardian value types, Singapore phone canonicalisation, duplicate detection, JSON persistence, and rollback after a failed save. This established the shared contact records used by later guardian-linking features. ([#51](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/51))
* **Guardian listing workspace**: Added the Guardians tab and `list-guardians`, displaying guardian contact details, linked-student counts, and a detail pane of linked students. Integrated filter reset and navigation while preserving the current workspace for invalid commands. ([#67](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/67))
* **Group creation and deletion**: Implemented `add-group` and `delete-group` across parsing, commands, the model, storage, help, and the Groups workspace. Persisted a mutable catalogue, retained default groups for older data files, prevented deletion of assigned groups, and restored in-memory catalogue state after failed saves. ([#95](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/95))

#### Enhancements and testing

* Added JavaFX tests for the redesigned interface and tab navigation, plus command, parser, model, storage, and integration coverage for guardian and group features. Enabled Java assertions explicitly for Gradle test runs. ([#41](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/41), [#49](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/49), [#67](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/67), [#83](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/83), [#95](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/95))
* Fixed Unicode name-length counting, trailing guardian input, extra arguments to `exit`, command-specific help rules, group selection scrolling, and long group-label layouts, with regression coverage. ([#122](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/122))
* Aligned student and guardian list columns, constrained long guardian names, improved filtering syntax feedback, and corrected Unicode group-label length validation. ([#163](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/163))

#### Documentation

* Added the initial MVP feature specification and updated the README to describe Mentora. ([#6](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/6), [#10](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/10))
* Updated the User Guide, Developer Guide, and README for the implemented workflows; documented group creation and deletion, validation, persistence, and recovery restrictions. ([#65](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/65), [#97](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/97))
* Updated the UI class diagram and contributed Developer Guide updates before release. ([#83](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/83), [#141](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/141))

#### Team contributions

* Reviewed student filtering, including validation, read-only execution, failed-save rollback, and UI integration. ([Review on #90](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/90#pullrequestreview-5616178598))
* Reviewed teammates' work on student creation, guardian selection recovery, and offline help. ([#47](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/47), [#81](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/81), [#93](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/93))

#### Contribution timeline

Dates below follow Git author dates; linked PRs provide the implementation and discussion records.

| Period (2026) | Contribution |
| --- | --- |
| 30 September–1 October | Initial feature specification, README, and team profile. |
| 6–12 October | Interface redesign, student layout, navigation tabs, and JavaFX tests. |
| 13–19 October | Guardian registration, persistence, and listing workspace. |
| 21–26 October | UI diagram, test assertions, and group creation/deletion. |
| 27 October–3 November | Alpha regression fixes, documentation alignment, and responsive list improvements. |

**Credits**: Mentora builds on [SE-EDU AddressBook Level 3](https://github.com/se-edu/addressbook-level3). My AI-assisted development and manually designed icons are acknowledged in the [Developer Guide](../DeveloperGuide.md#acknowledgements).
