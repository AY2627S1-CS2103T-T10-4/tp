---
layout: page
title: Developer Guide
page_class: developer-guide
---

This guide describes the current Mentora source implementation: a Java desktop contact manager for a
single tuition-centre coordinator. The Java packages retain the inherited `seedu.address` name, and
`AddressBook` is the name of Mentora's register class. Neither name means that legacy AddressBook
commands are still supported.

Read the [User Guide](UserGuide.md) for command usage and the
[MVP feature specification](FeatureSpecification.md) for the product contract. Where implementation
falls short of that contract, this guide records the gap in [Known limitations](#known-limitations);
documenting a limitation does not remove the requirement. Future stories are labelled separately.

* Table of Contents
  {:toc}

## Acknowledgements

* Mentora adapts the JavaFX application structure, command architecture, tests and documentation of
  [SE-EDU AddressBook Level 3](https://github.com/se-edu/addressbook-level3).
* Runtime libraries include [OpenJFX](https://openjfx.io/) for the desktop interface and
  [Jackson](https://github.com/FasterXML/jackson) for JSON conversion. Development tools include
  [JUnit](https://junit.org/junit5/) for tests and [Gradle](https://gradle.org/) for builds.
  The exact dependency versions are declared in `build.gradle`.
* Diagrams are rendered with [PlantUML](https://plantuml.com/). Their notation follows the
  [course UML reference](https://se-education.org/se-book/uml/); the requirements format follows the
  [course requirements reference](https://se-education.org/se-book/specifyingRequirements/).

## Setting up and getting started

Follow [Setting up and getting started](SettingUp.md). Use **JDK 25** for the current source build.
From the repository root, run `./gradlew check shadowJar` on macOS/Linux or
`.\gradlew.bat check shadowJar` in Windows PowerShell. The output is
`build/libs/addressbook.jar`; launch it with `java -jar addressbook.jar` from the directory in which
you want Mentora to store its data. The first build needs access to dependency repositories.
Normal application use and built-in command help work offline.

## Design

### Reading the diagrams

Every diagram below has a PlantUML source in `docs/diagrams` and a rendered PNG in `docs/images`.
The class diagrams are deliberately partial: their captions state the omissions. A solid arrow
denotes a navigable association; a dashed arrow denotes a dependency; a hollow triangular arrowhead
denotes generalization (solid line) or interface realization (dashed line). A filled diamond denotes
composition. Multiplicities describe the shown association ends; an omitted multiplicity is
unspecified. Colours are decorative and do not encode extra semantics.

Sequence diagrams show synchronous calls with solid arrows, returns with dashed arrows, and
activation bars for the shown executions. Creation messages meet the newly created object's head.
No destruction marker is used merely because a Java local variable goes out of scope. See
[Maintaining the diagrams](#maintaining-the-diagrams) for rendering instructions.

### Architecture

![UML component diagram showing UI, Logic, Model and Storage dependencies](images/ArchitectureDiagram.png)

*Figure 1. Main component dependencies. Arrows indicate use, not the direction of every data transfer. MainApp wiring and shared Commons utilities are omitted.*

| Component | Responsibility | Public interface / implementation |
| --- | --- | --- |
| UI | Accept commands; display records, feedback and help. | `Ui` / `UiManager` |
| Logic | Parse and execute commands; coordinate saving and report failures. | `Logic` / `LogicManager` |
| Model | Maintain the in-memory register, relationships, filtered views and preferences. | `Model` / `ModelManager` |
| Storage | Load and save the register and preferences as local JSON files. | `Storage` / `StorageManager` |

The UI calls Logic and displays Model value types obtained through Logic. It does not hold a
`ModelManager` or write files. Logic depends on the Model and Storage interfaces. Storage depends on
Model types to convert between JSON and domain data; Model does not depend on Storage. The
`seedu.address.commons` package supplies shared utilities, logging, exceptions and GUI settings.

`Main` launches `MainApp`. During initialization, `MainApp` loads preferences, constructs Storage,
loads the register into Model, then creates Logic and UI with their dependencies. A missing register
uses sample data; an invalid or unreadable register uses recovery mode. On normal shutdown,
`MainApp.stop()` saves preferences. Register changes are saved during command execution, not deferred
until shutdown. MainApp wiring, Commons and JavaFX infrastructure are omitted from Figure 1 to keep
the architectural view at component level.

### UI component

API: [`Ui.java`](https://github.com/AY2627S1-CS2103T-T10-4/tp/blob/master/src/main/java/seedu/address/ui/Ui.java).

![Partial UI class diagram showing the manager, main window, Logic interface and student panels](images/UiClassDiagram.png)

*Figure 2. UI coordination and representative student panels. `UiPart` uses `T = Stage` for `MainWindow` and `T = Region` for the panels. The nullable associations account for initialization before `fillInnerParts()` installs the panels; other UI parts are omitted.*

`UiManager` creates `MainWindow`. `MainWindow` holds the shared `Logic` reference and coordinates
three workspaces: Students, Guardians and Groups. `UiPart<T>` loads JavaFX FXML; `T` is `Stage` for
windows and `Region` for the panels shown. The remaining UI parts follow the same inheritance
pattern and are listed here rather than repeating crossing inheritance lines in the diagram.

| UI part | Purpose |
| --- | --- |
| `CommandBox` | Submits text through a callback to `MainWindow.executeCommand`. |
| `ResultDisplay` | Displays success or error feedback, including multiline messages. |
| `StudentListPanel`, `StudentCard`, `StudentDetailsPanel` | Show the filtered student list, rows and the selected student's profile. |
| `GuardianListPanel`, `GuardianCard`, `GuardianDetailsPanel` | Show guardians, linked-student counts and linked-student details. |
| `GroupListPanel` | Shows the current catalogue and group selection. |
| `HelpWindow` | Shows built-in command help from `help/commands.properties`. |
| `StatusBarFooter` | Shows the data path and the session's recovery status. |

FXML layouts are in `src/main/resources/view`. For example, `MainWindow.fxml` supplies the containers
that `MainWindow.fillInnerParts()` populates. Student and guardian lists are observable; relation-only
changes also need an explicit profile refresh because they need not replace a `Student` object.

`CommandResult` carries feedback and optional UI requests. After Logic returns successfully,
`MainWindow` applies selection and workspace requests and refreshes details. On a `ParseException`
or `CommandException`, it displays the error and restores the previous student and guardian
selection where those records remain visible. See [Filtering and selection](#filtering-and-selection)
for the current navigation behaviour.

### Logic component

API: [`Logic.java`](https://github.com/AY2627S1-CS2103T-T10-4/tp/blob/master/src/main/java/seedu/address/logic/Logic.java).

![Partial Logic class diagram showing retained dependencies and command execution](images/LogicClassDiagram.png)

*Figure 3. LogicManager retains Model, Storage and one AddressBookParser; concrete commands and parsers are shown separately. CommandResult is a returned value and does not execute UI actions.*

`LogicManager.execute(String)` asks `AddressBookParser` to route the command, executes the returned
`Command` against Model, and saves changed register data through Storage. Commands do not call
Storage or manipulate JavaFX controls. Read-only commands may change a filter or request a profile;
they do not change the persisted register. `Command.isReadOnly()` defaults to `false`.

![Parser class diagram using assign-group as a concrete command family](images/ParserClasses.png)

*Figure 4. One command family: AddressBookParser creates a command-specific parser but does not implement Parser itself. Parser is shown with its generic type parameter; other parsers, prefixes and value types are omitted.*

`Parser<T>` is an interface with `T extends Command`. For example,
`AssignGroupCommandParser` implements `Parser<AssignGroupCommand>`. Its argument validation uses
`ArgumentTokenizer`, `ArgumentMultimap` and validated domain values before constructing the command.
Other command families follow this dispatch pattern, but their validation rules are specific to
their command. The old Person parsers are not routes in the current `AddressBookParser`.

### Model component

API: [`Model.java`](https://github.com/AY2627S1-CS2103T-T10-4/tp/blob/master/src/main/java/seedu/address/model/Model.java).

![Model class diagram showing ModelManager composition and read-only interfaces](images/ModelClassDiagram.png)

*Figure 5. ModelManager constructs its own AddressBook and UserPrefs from the supplied initial data. Filtered views and the register's contents are shown separately or omitted.*

`ModelManager` also holds filtered student and guardian lists backed by the register's observable
lists. These views are for browsing, while full-register lookups are authoritative for commands.
Read-only interfaces restrict the API exposed to callers; they do not imply a detached snapshot.

![Register class diagram showing records, identifier-based relationships and group catalogue](images/RegisterClassDiagram.png)

*Figure 6. AddressBook stores the current catalogue and relationships. Record fields and operations are omitted. Immutable values may be shared by snapshots, so composition is not implied; Student has no Guardian field because links and assignments use IDs and codes.*

`AddressBook` contains students, guardians, a `Map<StudentId, GuardianId>` of guardian links, a set
of `GroupAssignment` values and the current `GroupCatalogue`. `ModelManager.getGroupCatalogue()`
delegates to this register; ModelManager has no separate catalogue field. `ConfiguredGroups` supplies
the five defaults (`ENG`, `MAT`, `SCI`, `CHI`, `S2-MAT-A`), not a permanent restriction to those groups.

`Student`, `Guardian`, `Group`, `GroupAssignment` and `GroupCatalogue` are immutable domain classes
(not Java `record` declarations). Snapshots copy mutable collections and share immutable values, so
the associations in Figure 6 deliberately do not assert exclusive composition of those values.
Edits replace a student or guardian while retaining its ID. `AddressBook.resetData` validates a
replacement register before publishing its contents. See [Domain invariants](#domain-invariants)
for the rules enforced by the aggregate.

### Storage component

API: [`Storage.java`](https://github.com/AY2627S1-CS2103T-T10-4/tp/blob/master/src/main/java/seedu/address/storage/Storage.java).

![Storage class diagram including all five persisted record collections](images/StorageClassDiagram.png)

*Figure 7. StorageManager delegates to separate register and preference stores; the JSON wrapper composes all five adapter collections. Model types, Jackson and deserialization utilities are omitted. An absent groups property selects defaults, while an empty array means no groups.*

`JsonAddressBookStorage` uses `JsonUtil` and Jackson to read or write `JsonSerializableAddressBook`.
The wrapper converts between JSON adapters and the complete register. Relationship adapters store
IDs and codes, not copies of student or guardian records. `StoredStringDeserializer` rejects
non-string JSON values in text fields rather than allowing automatic number-to-string conversion.

| JSON collection | Adapter | Contents |
| --- | --- | --- |
| `students` | `JsonAdaptedStudent` | ID, name, academic level and optional phone. |
| `guardians` | `JsonAdaptedGuardian` | ID, name and phone. |
| `groups` | `JsonAdaptedGroup` | Code, label and kind (`Subject` or `Class`). |
| `guardianLinks` | `JsonAdaptedGuardianLink` | Student ID and guardian ID. |
| `groupAssignments` | `JsonAdaptedGroupAssignment` | Student ID and group code. |

Loading reconstructs the catalogue, students and guardians before validating assignments and links.
A conversion error prevents the partially reconstructed register from reaching Model. Missing
`students`, `guardians`, `guardianLinks` and `groupAssignments` properties default to empty
collections for compatibility. Missing `groups` uses the five defaults; an explicit empty array
means an empty catalogue. Explicitly null collections, null entries, duplicate fields, trailing JSON
content, invalid values, duplicate records and dangling references are rejected. A missing or null
student `phone` is allowed.

Unsupported fields are rejected by the register and record adapters, except the legacy root
`persons` and `_comment` fields, which are ignored. **Legacy Person data is not migrated into
Students.** An old file containing only `persons` therefore loads no students and can lose that
ignored content on the next save. Preserve a copy before attempting a migration.

The register path is `data/addressbook.json`; preferences are in `preferences.json`, both relative
to the process working directory. `MainApp` fixes these paths in the current implementation.
`StorageManager` remembers the first register-load failure and refuses later register writes for
that storage session. Preference storage is independent and can still run in recovery mode.

## Implementation

### Command routing and validation

Commands and prefixes are case-insensitive; stable ID values use uppercase `S-` or `G-` and at least
four digits. Prefix order is flexible, but a prefix cannot be repeated. Each parser rejects missing
required fields, unsupported prefixes and invalid syntax. Domain value classes validate names,
levels, phones and group values. Exact feedback comes from the relevant command/parser constants;
format errors and invalid-value errors are not necessarily the same message.

![Sequence diagram showing assign-group parsing and command creation](images/CommandParsingSequenceDiagram.png)

*Figure 8. Parsing `assign-group sid/S-0001 g/MAT`. The incoming call is from LogicManager; `text` is the complete input and `arguments` excludes the command word. `sid` remains validated ID text, `code` is a canonical GroupCode, and `new(...)` denotes construction. Execution and persistence occur afterwards.*

Existing-record commands retain the ID as text and resolve it in the full register. Constructing a
`StudentId` or `GuardianId` advances its generator, so lookup must not instantiate an ID just to
validate user input. New-record creation generates IDs; loading existing records advances the
generators past the numeric IDs read. IDs remain stable for a record, but are not list positions,
need not be consecutive and are not guaranteed never to be reused after deletion and restart.
The generator counters are process state and are not rolled back with a register snapshot.

### Domain invariants

| Area | Invariant and consequence |
| --- | --- |
| Student identity | IDs are unique. Normalized name plus academic level is also unique, both on addition and editing. Phone is not part of identity. |
| Guardian identity | IDs and canonical phone numbers are unique. Guardian names may repeat. A student phone may equal any other student or guardian phone. |
| Names and search | Names retain display casing. Whitespace normalization, Unicode NFKC normalization and case folding support matching. Searches use a contiguous substring of the normalized name, not separate OR keywords. |
| Academic level | `Sec 1`–`Sec 5` or `IP 1`–`IP 6`, stored canonically. Changing level does not automatically change group assignments. |
| Phone | Eight digits starting with `3`, `6`, `8` or `9`, optionally prefixed by `+65` with zero or one following space; stored as `+65 XXXXXXXX`. |
| Guardian links | A student has zero or one guardian; a guardian may have zero or many students. Both endpoints must exist. Linking twice is rejected; replacing a guardian requires unlinking first. |
| Groups | Codes are unique case-insensitively; newly configured codes have 2–20 ASCII letters, digits or hyphens, start with a letter and end with a letter or digit. Labels contain 1–80 characters after whitespace normalization. Kind is Subject or Class. |
| Assignments | Each pair of student ID and configured group code is unique and both references must exist. A student can have zero or many assignments. Class and Subject assignments are independent; class membership does not imply subject membership. |
| Student deletion | Removes that student's record, guardian link and assignments. Keeps guardians, sibling links, other assignments and configured groups. |
| Group deletion | Rejects groups with any assigned students; an unused group can be deleted. Removing an assignment never deletes its group or student. |

`AddressBook` enforces cross-record rules even when callers bypass the command layer. Model changes
that could violate an invariant are validated before mutation. `GroupCatalogue` is immutable;
adding/deleting groups replaces it and preserves catalogue order. Saving, copying, restoring and
equality checks include the catalogue, records, guardian links and assignments.

### Guardian relationships and contact updates

![Sequence diagram of ModelManager delegating a guardian link to AddressBook](images/GuardianLinkSequenceDiagram.png)

*Figure 9. Successful guardian linking inside Model. The command has already validated ID syntax; AddressBook checks references and the one-guardian rule. Returns abbreviate nonempty Optional values, map operations are omitted, and Logic saves afterwards.*

`LinkGuardianCommand` resolves both records and calls `Model.linkGuardian`. `ModelManager` delegates
to `AddressBook`, which rejects an existing identical link or a different guardian for that student.
`UnlinkGuardianCommand` removes only the link and returns the removed guardian ID for feedback.
Both commands request the student's profile in the Students workspace after saving succeeds.

`EditGuardianCommand` checks for another guardian with the proposed canonical phone, then replaces
the guardian with a new immutable value having the same ID and name. Student profiles resolve the
guardian through the current register, so siblings share the updated contact. MainWindow refreshes
the selected student profile, guardian details and linked-student counts after a successful command.

**Design choice:** Store one guardian record and link by ID. Copying a guardian's phone into each
student would simplify an individual display lookup but permit inconsistent sibling contacts.
The shared record requires reference validation but makes an update apply consistently.

### Group assignments and catalogue changes

`AssignGroupCommand` and `UnassignGroupCommand` resolve the student in the full register and the
group in the current catalogue. They construct `GroupAssignment` from the existing student's ID
and the canonical group's code, then call `Model.addGroupAssignment` or
`Model.removeGroupAssignment`. AddressBook validates reference existence and pair membership before
changing the set. Removing the final assignment is valid; the profile then shows `No groups assigned.`

`AddGroupCommand` adds a validated group; `DeleteGroupCommand` removes only an unused group.
These commands request the Groups workspace. New Subject groups are immediately eligible for
subject filtering. Existing catalogue data is authoritative on reload, including custom groups or
an empty catalogue.

**Design choice:** Reject deletion of an assigned group instead of cascading assignment removal.
This requires the coordinator to remove assignments explicitly but prevents an apparently small
catalogue change from discarding many students' tuition arrangements.

