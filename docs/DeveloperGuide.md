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
`build/libs/mentora.jar`; launch it with `java -jar mentora.jar` from the directory in which
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

*Figure 6. AddressBook stores records, guardian links and group assignments. Links and assignments refer to records by ID or code; the diagram omits other fields and operations.*

`AddressBook` contains students, guardians, a `Map<StudentId, GuardianId>` of guardian links, a set
of `GroupAssignment` values and the current `GroupCatalogue`. `ModelManager.getGroupCatalogue()`
delegates to this register; ModelManager has no separate catalogue field. `ConfiguredGroups` supplies
the five defaults (`ENG`, `MAT`, `SCI`, `CHI`, `S2-MAT-A`), not a permanent restriction to those groups.

`Student`, `Guardian`, `Group`, `GroupAssignment` and `GroupCatalogue` are immutable domain classes
(not Java `record` declarations). Snapshots copy mutable collections and share immutable values, so
those values are not exclusively owned by one snapshot. A student has no guardian field: links and
assignments refer to records using IDs and group codes.
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

### Filtering and selection

Student and guardian lists are ordered by normalized name, then numeric ID. A name search replaces
that entity's current filter. `filter-students` accepts exactly one Subject group or one academic
level, replaces the current student filter and displays its description. It rejects Class groups
and combined filters. It tests explicit Subject assignments, not the subject implied by a class
code. `list-students` and `list-guardians` reset their respective filters; filters do not alter saved
records and are not persisted.

The full register remains available even when the displayed list is empty. When a successful result
requests a student hidden by the filter, `LogicManager.revealRequestedStudent` clears the student
filter after any required save. Assignment changes re-evaluate the current predicate because the
student object itself may be unchanged. MainWindow then refreshes the profile explicitly.

Workspace changes are requests in `CommandResult`, not implicit effects of every command:

| Successful command | Workspace behaviour |
| --- | --- |
| `link-guardian`, `unlink-guardian`, `assign-group`, `unassign-group`, `filter-students`, `list-students`, `find-student` | Switch to Students. |
| `list-guardians`, `find-guardian` | Switch to Guardians. |
| `list-groups`, `add-group`, `delete-group` | Switch to Groups. |
| `add-student`, `edit-student`, `view-student` | Request student selection, but keep the current workspace. |
| `delete-student`, `add-guardian`, `edit-guardian` | Keep the current workspace. |
| `help`, `exit` | Open help or close the application, respectively. |

Consequently, a student command can change the underlying Students view while another workspace
remains visible. This is a current navigation limitation, not a guarantee that all commands reveal
their result automatically. Deleting the selected student clears its selection only after successful
saving; deleting another student preserves the current selection.

### Saving, rollback and recovery

![Activity diagram of saving a changed register and restoring memory on a save failure](images/PersistenceActivityDiagram.png)

*Figure 10. Persistence after a mutating command executes successfully. Parsing, the recovery guard, snapshot creation and domain rejection precede this flow. Read-only commands bypass it, and disk rollback is not provided.*

The command boundary is:

1. Parse the input. Parse failures stop before command execution.
2. Execute read-only commands without saving. For a mutating command, first reject recovery mode.
3. Copy the complete register before executing the mutation. Commands reject invalid domain requests
   before changing the model.
4. Compare the resulting register with the snapshot. An unchanged result, such as setting the same
   phone number, does not cause a write.
5. Save a changed register once. If saving throws `IOException`, restore the in-memory snapshot and
   throw a command-specific `CommandException` retaining the original cause.
6. Only after success, reveal a requested hidden student and return the result for the UI to apply.

**Current disk-write limitation:** `FileUtil.writeToFile` writes directly to the destination.
The in-memory rollback does not undo a partial disk write and does not provide crash-safe or atomic
storage. A save error is not a guarantee that the original file is intact. Atomic replacement with
a temporary file is a required follow-up to meet NFR07 fully; it is not implemented here.

On startup there are three distinct outcomes:

| File state | Current behaviour |
| --- | --- |
| Confirmed missing path | Load two sample students and five default groups. The register file is first written after a command changes register data. |
| Valid JSON register | Load and validate all data, then permit commands and saving. |
| Unreadable or invalid register | Preserve the input file, load an empty recovery register with default groups, display the recovery error and disable register mutations and saves for the session. |

Recovery mode still permits list, find, filter, view, help and exit commands (subject to their normal
validation and whether a target exists). The footer displays `Saving disabled`. To recover, close
Mentora, preserve the problematic file, restore a valid backup or deliberately replace it, then
restart. Editing the file while Mentora remains open does not clear the latched recovery state.
Preference errors are handled separately: defaults may be used and rewritten, so the register's
preserve-on-load-failure guarantee does not apply to `preferences.json`.

**Design choice:** Snapshot the complete register for save-error rollback. This is simple and keeps
relationships and the catalogue together, at a cost proportional to register size. Per-command
inverse operations would reduce copying but would require every mutation to implement a correct
inverse. These snapshots are temporary; they do not implement user-visible undo/redo.

### Known limitations

These are implementation gaps or release constraints, not changes to the feature specification:

* **Persistence:** partial disk writes remain possible after an I/O failure or interruption (NFR07).
  Backups and restoration are manual. The ordinary `Saved locally` footer text is static, not a
  per-command write acknowledgement; startup does not display the specification's loaded-record count.
* **Navigation:** several student commands preserve the current workspace even when they select a
  student. Guardian detail selection has no dedicated typed `view-guardian` command. Use the current
  workspace table above when testing keyboard workflows.
* **Feedback differences:** empty-value feedback can be a field-validation message instead of a
  format message. Deletion reports `Student S-9999 does not exist.` for an unknown ID and confirms
  guardian retention when the deleted student had a guardian link.
* **Long sessions:** assignment updates refresh filters by wrapping the previous predicate. Many
  repeated updates can build a deep call chain; resetting/reapplying the filter removes the chain.
  This needs a bounded refresh implementation rather than an assumption of unlimited session length.
* **Record limits:** one guardian per student; no guardian deletion, name editing, student-phone
  clearing, enrolment-status field, archiving, class filtering, combined filtering, contact-list export,
  spreadsheet import or user-visible undo/redo. Two students with the same normalized name and level
  cannot be distinguished by the current duplicate rule.
* **Compatibility:** legacy `persons` data is ignored rather than migrated. ID generation is based on
  current-process state and loaded IDs; it is not a permanent historical ID registry. Extreme numeric
  suffixes are not a supported way to extend the generator.
* **Release verification:** the build targets Java 25 and bundles JavaFX 17 platform libraries for
  Windows, macOS and Linux. Platform/CPU compatibility, timing, display scaling and keyboard acceptance
  must be checked on the intended release package; the targets below are not claims of universal testing.

### Extending the implementation

For a new command, add its command and parser, a route in `AddressBookParser`, built-in help and
tests. Mark it read-only only if it does not mutate register data. Keep reference and uniqueness
checks in the domain layer. Return UI requests through `CommandResult`; add command-specific
save-failure feedback in LogicManager when needed. For a new stored field or relationship, update
copy/reset/equality, JSON conversion, compatibility checks and round-trip tests together.

Focused handover notes remain useful for implementation history:
[student creation](feature-handover/AddStudentHandover.md),
[student editing](feature-handover/EditStudentHandover.md),
[student deletion](feature-handover/DeleteStudentHandover.md),
[guardian creation](feature-handover/AddGuardianHandover.md),
[guardian phone updates](UpdateGuardianPhoneHandover.md),
[linking](feature-handover/LinkGuardianHandover.md),
[unlinking](feature-handover/UnlinkGuardianHandover.md),
[assignment](feature-handover/AssignGroupHandover.md),
[unassignment](feature-handover/UnassignGroupHandover.md),
[group creation](feature-handover/AddGroupHandover.md),
[group deletion](feature-handover/DeleteGroupHandover.md), and
[filtering](feature-handover/FilterStudentsHandover.md).
Their release-specific plans should be read alongside the current code and feature specification.

## Documentation, logging, testing and release checks

See the [documentation guide](Documentation.md), [logging guide](Logging.md),
[testing guide](Testing.md) and [DevOps guide](DevOps.md).

The test suite covers value validation, parser rejection, domain invariants, Logic/Storage
integration and JavaFX rendering/selection. Useful regression entry points include:

| Area | Test classes |
| --- | --- |
| Complete register and relationships | `AddressBookStudentTest`, `AddressBookGuardianTest`, `AddressBookGuardianLinkTest`, `AddressBookAssignmentTest` |
| Assignment commands and saved data | `AssignGroupIntegrationTest`, `UnassignGroupIntegrationTest`, `GroupCrudIntegrationTest` |
| Guardian linking and search | `LinkGuardianIntegrationTest`, `UnlinkGuardianIntegrationTest`, `FindGuardianIntegrationTest`, `ListGuardiansLogicIntegrationTest` |
| Invalid data and failed saves | `MainAppRecoveryTest`, `LogicManagerRecoveryTest`, `JsonAddressBookStorageTest`, `JsonSerializableAddressBookTest`, `StorageManagerTest` |
| Display and selection | `MainWindowTest`, `AssignGroupUiTest`, `UnassignGroupUiTest`, `UnlinkGuardianUiTest`, `FilterStudentsUiTest`, `EditGuardianPhoneUiTest`, `HelpCommandUiTest` |

Run `./gradlew check coverage shadowJar` (Windows PowerShell: `.\gradlew.bat check coverage shadowJar`)
before a release. UI tests require a graphical display; the Linux CI job uses Xvfb. Review the Gradle
test report and coverage report, then perform the manual scenarios below on the packaged JAR.
Passing unit tests does not establish the timing, operating-system or display-scaling NFRs.

### Maintaining the diagrams

Edit the `.puml` source, then regenerate the matching PNG; do not edit only the image. The coordinated diagram update uses
`mentora-style.puml` for the ten current design diagrams; regenerate each image when its source is updated. Older AB3 tutorial/proposal assets left in
`docs/diagrams` are not current Mentora design diagrams and are not embedded here.

From the repository root, with a local PlantUML JAR, render a changed diagram using:

```text
java -jar /path/to/plantuml.jar -charset UTF-8 -failfast2 -tpng -o ../images docs/diagrams/ModelClassDiagram.puml
```

Replace the JAR path with your installation path (quote it if it contains spaces). The output
directory is relative to the source diagram's directory. Use PlantUML 1.2025.10 for consistent output. Re-render every diagram that includes
`mentora-style.puml` when changing that shared style. Inspect the generated images
at the guide's normal reading width, check links and table rendering, and preview printing before
publishing. Prefer splitting an oversized diagram to shrinking its text.

## Appendix: Requirements

### Product scope

**Target user:** A coordinator at a small private tuition centre in Singapore who manages roughly
30–200 secondary-school student records and their guardian contacts, types comfortably, and works
alone with a local desktop data set. Records may describe enrolled or prospective students, but the
current model does not store that distinction as an enrolment-status field.

**Value proposition:** Keep student details, shared guardian contacts and tuition-group assignments
together, and retrieve or update them using short typed commands.

The current product manages contact and basic administrative data. It does not manage lessons,
attendance, grades, fees, payroll or accounting, send messages, or provide multi-user synchronization.
It is not intended to replace a full school-management system. Future stories below are possible
extensions, not claims that these features are available now.

### User stories

Each row completes “As a **role**, I want to **action**, so that I can **benefit**.” Priority is
independent of implementation status: **High** means required for the current contact-management
scope, **Medium** means desirable, and **Low** means a possible later extension. The Current table
records available capabilities; any partial coverage is stated explicitly. Existing story IDs are
retained for traceability; gaps in numbering occur because future stories are in a separate table.

#### Current capabilities

| ID / priority | As a… | I want to… | So that I can… |
| --- | --- | --- | --- |
| US01 / High | new coordinator | view built-in command help and examples | learn or recall command usage while offline. |
| US02 / High | coordinator | add a student with a name and academic level | create a basic record for that learner. |
| US03 / High | coordinator | update a student's academic level | keep the record accurate after progression or correction. |
| US04 / High | coordinator | delete a student while retaining shared guardians | remove an unwanted record without disrupting siblings' contacts. |
| US05 / High | coordinator | add a guardian with a name and phone number | retain a reusable administrative contact. |
| US06 / High | coordinator | update a guardian's phone once | use the updated contact for every linked student. |
| US07 / High | coordinator | link a student to an existing guardian | find the correct responsible adult from the student's profile. |
| US08 / High | coordinator | link siblings to one guardian record | avoid maintaining duplicate copies of the same contact. |
| US09 / High | coordinator | assign a student to configured subject and class groups | record the student's tuition arrangements. |
| US10 / High | coordinator | view group assignments in a student's profile | understand that student's current tuition arrangements. |
| US11 / High | coordinator | list all registered students | browse the complete register after searching or filtering. |
| US12 / High | coordinator | search students by a whole or partial name | answer enquiries without scanning every record. |
| US13 / High | coordinator | open a student profile by stable ID | inspect the level, student phone, assignments and linked guardian together. |
| US14 / High | coordinator | perform core record tasks using typed commands | reduce repeated switching between keyboard and mouse. |
| US15 / High | coordinator | receive success feedback or an explanation of rejection | determine whether a requested change took effect and correct invalid input. |
| US16 / High | coordinator | have changed records saved locally and loaded on restart | continue work without an account or a manual save step. |
| US17 / Medium | coordinator | search guardians by name | find an existing contact before creating another record. |
| US18 / Medium | coordinator | filter students by one subject or academic level | review a relevant cohort. |
| US19 / Medium | coordinator | edit a student by ID using a command | update a record without first locating its row in a list. |
| US25 / High | coordinator | have Singapore phone formats checked before saving | correct malformed contact numbers before relying on them. |
| US31 / High | coordinator | create groups and delete unused groups | adapt the catalogue while protecting existing assignments. |
| US32 / High | coordinator | unlink an incorrectly linked guardian without deleting records | link the correct guardian afterwards. |
| US33 / High | coordinator | remove an obsolete group assignment | keep a student's tuition arrangements current. |
| US34 / High | coordinator | list configured group codes, labels and kinds | choose an unambiguous group for assignment. |
| US35 / Medium | coordinator | list guardians and inspect their linked students | reuse existing family contacts accurately. |
| US36 / High | coordinator | add or update an optional student phone independently of the guardian phone | retain a direct student contact without changing the guardian's contact. |
| US37 / High | coordinator | have invalid loaded data preserved with saving disabled | restore the original data without an empty recovery register overwriting it. |
| US38 / High | coordinator | receive duplicate-record and invalid-relationship errors | avoid conflicting contacts or references to missing records. |

US14 is only partially satisfied across workspaces: typed commands cover record mutations and
queries, but navigation and guardian-detail selection have the limitations described above. US16
does not imply atomic disk writes; NFR07 remains partially unmet. The other stories describe
capabilities, not a claim that every edge case is defect-free.

#### Future capabilities — not implemented

| ID / priority | As a… | I want to… | So that I can… |
| --- | --- | --- | --- |
| US20 / Medium | coordinator | change a prospective student's status to active | record confirmed enrolment explicitly. |
| US21 / Medium | coordinator | list only students marked active | focus daily work on enrolled learners. |
| US22 / Medium | coordinator | archive withdrawn or graduated students | retain their history outside active-student results. |
| US23 / Medium | coordinator | retrieve guardians for a selected class | identify the audience for a class announcement. |
| US24 / Medium | coordinator | copy a class contact list with each shared guardian once | prepare an external message without duplicate recipients. |
| US26 / Low | coordinator | link multiple guardians to one student | retain more than one responsible contact. |
| US27 / Low | coordinator | record a student's school | retain additional administrative context. |
| US28 / Low | coordinator | filter students by class or enrolment status | retrieve cohorts beyond the existing subject and level filters. |
| US29 / Low | coordinator | review a report of incomplete or potentially duplicate records | find data-quality issues beyond the current strict duplicate checks. |
| US30 / Low | coordinator | import and export contact lists | move data between Mentora and existing spreadsheets. |

### Use cases

For all use cases, **System:** Mentora. **Actor:** tuition-centre coordinator.
**MSS** means main success scenario. Each extension is attached to an MSS step; “ends” terminates
the current attempt and “resumes at step N” returns to that MSS step. A coordinator may abandon an
attempt; previously completed commands remain saved. Multi-command workflows are not a single
transaction. These use cases describe current capabilities, not the future stories above.

The following shared extensions apply to every use case that modifies data. They supplement the
numbered, case-specific extensions without repeating the same failure paths in each case:

* **\*a.** A request has invalid syntax, missing fields or invalid field values.
  * **\*a1.** Mentora explains the error without changing the register.
  * The use case resumes at the request step that failed.
* **\*b.** A modifying request is attempted in recovery mode.
  * **\*b1.** Mentora rejects it and explains that local data must be restored and the application restarted.
  * The use case ends.
* **\*c.** Saving a changed register fails.
  * **\*c1.** Mentora restores the in-memory state immediately before that command and reports the save failure.
  * The use case ends. Earlier successful commands remain completed. Disk contents may need restoration
    because writes are not atomic; the use case does not promise a disk rollback.

#### UC01 — Register a student with a guardian and group

**Related stories:** US02, US05, US07–US10, US13, US16, US34.

**Preconditions:** Mentora is running. The intended group is configured.

**Success postcondition:** The student and guardian exist, are linked, and the student is assigned to the group.

**MSS**

1. Coordinator requests creation of a guardian using a name and phone number.
2. Mentora creates and saves the guardian, then returns its ID.
3. Coordinator requests creation of a student using a name, academic level and optional phone.
4. Mentora creates and saves the student, then returns its ID.
5. Coordinator requests linking that student to the guardian.
6. Mentora saves the relationship and confirms it.
7. Coordinator requests assignment of the configured group to the student.
8. Mentora saves the assignment and displays the updated student profile.

The use case ends.

**Extensions**

* **1a.** The coordinator already knows the guardian's existing ID.
  * **1a1.** The coordinator reuses that ID.
  * The use case resumes at step 3.
* **2a.** The phone already belongs to a guardian.
  * **2a1.** Mentora rejects the duplicate and identifies the existing guardian.
  * **2a2.** Coordinator chooses to reuse that ID or supply a corrected phone.
  * The use case resumes at step 3 if reusing the ID, or step 1 if correcting the phone.
* **4a.** The normalized student name and level already exist.
  * **4a1.** Mentora rejects the duplicate.
  * The use case resumes at step 3. Any guardian created at step 2 remains saved.
* **6a.** A supplied ID does not identify an existing student or guardian.
  * **6a1.** Mentora reports the missing record without creating a link.
  * The use case resumes at step 5.
* **6b.** The student already has a guardian.
  * **6b1.** Mentora reports either an identical existing link or the one-guardian limit.
  * The use case ends. To replace a guardian, use UC07.
* **8a.** The student is missing, the group is not configured, or the assignment already exists.
  * **8a1.** Mentora reports the reason and does not add an assignment. An unknown-group error directs the coordinator to list groups; it does not list them automatically.
  * The use case resumes at step 7.

#### UC02 — Find a student and update the shared guardian phone

**Related stories:** US06, US12, US13, US25.

**Preconditions:** The student and linked guardian exist; the Students workspace is visible.

**Success postcondition:** The guardian has the new phone, and linked student profiles resolve that phone.

**MSS**

1. Coordinator searches for a student by name.
2. Mentora displays matching students.
3. Coordinator requests the required student's profile by ID.
4. Mentora displays the profile, including the linked guardian.
5. Coordinator requests a phone update using the guardian's ID.
6. Mentora validates and saves the update, confirms it, and refreshes the displayed contact.

The use case ends.

**Extensions**

* **2a.** No student matches.
  * **2a1.** Mentora displays an empty result and explains that no name matched.
  * The use case resumes at step 1.
* **4a.** The supplied student ID does not exist.
  * **4a1.** Mentora reports the missing student.
  * The use case resumes at step 3.
* **6a.** The guardian ID does not exist or the new phone belongs to another guardian.
  * **6a1.** Mentora rejects the update and identifies the missing ID or conflicting guardian.
  * The use case resumes at step 5.
* **6b.** The canonical phone is unchanged.
  * **6b1.** Mentora confirms the requested value without writing an unchanged register.
  * The use case ends.

#### UC03 — Review a cohort

**Related stories:** US11, US13, US18.

**Preconditions:** Mentora is running.

**Success postcondition:** The coordinator can inspect students matching one chosen criterion; stored records are unchanged.

**MSS**

1. Coordinator requests students assigned to one Subject group or at one academic level.
2. Mentora displays the matching students and the filter description in Students.
3. Coordinator requests a matching student's profile by ID.
4. Mentora displays the profile.

The use case ends.

**Extensions**

* **2a.** No students match a valid criterion.
  * **2a1.** Mentora displays an empty result and the active filter.
  * The use case ends.
* **2b.** The group is unknown, is a Class group, or the criterion is malformed or combines both filter types.
  * **2b1.** Mentora explains the error and retains the previous filter.
  * The use case resumes at step 1.
* **3a.** The coordinator requests an existing student outside the filter.
  * **3a1.** Mentora clears the filter and displays that student's profile.
  * The use case ends.
* **4a.** The student ID is missing or invalid.
  * **4a1.** Mentora reports the error and retains the previous selection.
  * The use case resumes at step 3.

#### UC04 — Find an existing guardian

**Related stories:** US17, US35.

**Preconditions:** Mentora is running.

**Success postcondition:** Matching guardian records and their IDs are available for reuse; no register data changes.

**MSS**

1. Coordinator requests guardians matching a whole or partial name.
2. Mentora opens Guardians and shows matching records with IDs, phones and linked-student counts.

The use case ends.

**Extensions**

* **1a.** The coordinator wants the complete guardian list.
  * **1a1.** Coordinator requests all guardians.
  * **1a2.** Mentora clears the guardian-name filter and displays all guardians.
  * The use case ends.
* **2a.** No guardians match, or the register has no guardians when listing all.
  * **2a1.** Mentora displays the corresponding empty-result message.
  * The use case ends.
* **2b.** The query is missing or invalid.
  * **2b1.** Mentora explains the error without replacing the existing results.
  * The use case resumes at step 1.

#### UC05 — Update a student's details

**Related stories:** US03, US19, US36.

**Preconditions:** The student exists.

**Success postcondition:** The supplied level and/or phone are updated; the student's ID, guardian link and assignments remain intact.

**MSS**

1. Coordinator requests a new academic level and/or student phone using the student's ID.
2. Mentora validates and saves the replacement student record and confirms the change.

The use case ends.

**Extensions**

* **2a.** The ID does not exist or the proposed name-and-level combination duplicates another student.
  * **2a1.** Mentora rejects the update and explains the conflict.
  * The use case resumes at step 1.
* **2b.** All supplied values already match the stored values.
  * **2b1.** Mentora confirms them without saving an unchanged register.
  * The use case ends.

#### UC06 — Delete a student

**Related stories:** US04.

**Preconditions:** The coordinator knows the ID of the student to remove.

**Success postcondition:** The student, its guardian link and its assignments are removed; guardian records and other students' data are retained.

**MSS**

1. Coordinator requests deletion of the student by ID.
2. Mentora deletes the student and its relationships, saves the result and confirms deletion.

The use case ends. There is no confirmation prompt or undo command.

**Extensions**

* **2a.** The ID does not identify an existing student.
  * **2a1.** Mentora reports the missing student without deleting anything.
  * The use case resumes at step 1.

#### UC07 — Correct a guardian relationship

**Related stories:** US07, US08, US32.

**Preconditions:** The student and intended replacement guardian exist.

**Success postcondition:** The student is linked to the intended guardian; guardian records and sibling relationships are retained.

**MSS**

1. Coordinator requests removal of the student's existing guardian link.
2. Mentora removes and saves only that relationship and confirms retention of the guardian record.
3. Coordinator requests linking the student to the intended guardian.
4. Mentora validates and saves the new relationship and displays the updated profile.

The use case ends.

**Extensions**

* **2a.** The student exists but has no guardian.
  * **2a1.** Mentora reports that there is no link to remove.
  * The use case resumes at step 3.
* **2b.** The student does not exist.
  * **2b1.** Mentora reports the missing student.
  * The use case ends.
* **4a.** An endpoint is missing or the student already has a guardian.
  * **4a1.** Mentora explains the rejection without overwriting a relationship.
  * The use case ends. A successful unlink at step 2 is not undone if this later command fails.

#### UC08 — Change a student's group assignments

**Related stories:** US09, US10, US33, US34.

**Preconditions:** The student exists. Any group to be added is configured.

**Success postcondition:** The requested assignment changes are saved; unrelated data is unchanged.

**MSS**

1. Coordinator requests removal of one obsolete assignment from the student.
2. Mentora removes and saves that assignment and confirms the change.
3. Coordinator requests assignment of the replacement group.
4. Mentora validates and saves the assignment and displays the updated profile.

The use case ends.

**Extensions**

* **1a.** The coordinator only wants to add an assignment.
  * The use case resumes at step 3.
* **2a.** The student or configured group is missing, or that pair is not assigned.
  * **2a1.** Mentora reports the reason without removing anything.
  * The use case resumes at step 1.
* **2b.** No replacement is needed, including when the final assignment was removed.
  * The use case ends after confirming the removal.
* **4a.** The student or group is missing, or the replacement pair already exists.
  * **4a1.** Mentora rejects the addition and explains the reason.
  * The use case resumes at step 3. Any removal at step 2 remains completed.

#### UC09 — Create a group

**Related stories:** US31, US34.

**Preconditions:** Mentora is running.

**Success postcondition:** The new group is configured and available for assignment.

**MSS**

1. Coordinator requests creation of a group with a code, label and kind.
2. Mentora validates and saves the new group, then displays it in Groups.

The use case ends.

**Extensions**

* **2a.** The canonical code already exists.
  * **2a1.** Mentora rejects the duplicate without changing the catalogue.
  * The use case resumes at step 1.

#### UC10 — Resume work from local storage

**Related stories:** US16, US37.

**Preconditions:** The coordinator has access to the application and its working directory.

**Success postcondition:** A valid register is available for work, loaded from storage or initialized with samples on first use.

**MSS**

1. Coordinator launches Mentora.
2. Mentora loads preferences and validates the saved register.
3. Mentora displays the loaded students and accepts commands.

The use case ends.

**Extensions**

* **2a.** The register file is missing.
  * **2a1.** Mentora uses sample students and the default groups, without writing the register yet.
  * The use case resumes at step 3.
* **2b.** The register cannot be read or validated.
  * **2b1.** Mentora preserves that file, reports recovery mode and provides an empty register with default groups.
  * **2b2.** Mentora rejects modifying commands for the session while allowing read-only commands.
  * The use case ends. The coordinator can restore data while the application is closed and restart.
* **2c.** Preferences cannot be read or validated.
  * **2c1.** Mentora logs the problem, uses default preferences and attempts to save them.
  * The use case continues with register loading at step 2; this alone does not enable register recovery mode.

#### UC11 — Consult command help

**Related stories:** US01, US15.

**Preconditions:** Mentora is running.

**Success postcondition:** The coordinator can read help without a network connection.

**MSS**

1. Coordinator requests help for a supported command.
2. Mentora displays its syntax, examples, validation rules and common errors.

The use case ends.

**Extensions**

* **1a.** No command name is supplied.
  * **1a1.** Mentora displays the command overview.
  * The use case ends.
* **2a.** The command is unknown, or more than one command word is supplied.
  * **2a1.** Mentora reports the unsupported topic or required format.
  * The use case resumes at step 1.

#### UC12 — Delete an unused group

**Related stories:** US31, US34.

**Preconditions:** Mentora is running and the coordinator knows the intended group code.

**Success postcondition:** The group is removed; records and other groups are unchanged.

**MSS**

1. Coordinator requests deletion of a group by code.
2. Mentora verifies that the group exists and has no assigned students, deletes and saves it, and confirms deletion.

The use case ends. There is no confirmation prompt or undo command.

**Extensions**

* **2a.** The group does not exist.
  * **2a1.** Mentora reports the unknown group and suggests listing configured groups.
  * The use case resumes at step 1.
* **2b.** Students are assigned to the group.
  * **2b1.** Mentora rejects deletion and reports the assignment count.
  * The use case ends. The coordinator can remove assignments using UC08 before trying again.

#### UC13 — Browse configured groups

**Related stories:** US34.

**Preconditions:** Mentora is running.

**Success postcondition:** The current catalogue is visible; stored data is unchanged.

**MSS**

1. Coordinator requests the configured groups.
2. Mentora displays each group's code, label and kind in Groups and reports the count.

The use case ends. An empty catalogue is a valid result with a count of zero.

**Extensions**

* **2a.** The request contains unexpected arguments.
  * **2a1.** Mentora reports the required format and leaves the existing workspace unchanged.
  * The use case resumes at step 1.

### Non-functional requirements

These are acceptance targets and constraints. **Pending verification** means the stated measurement
has not been established by this documentation review. **Partial** means an identified implementation
gap remains; it is not an exemption. The single-user/local-data scope is a product constraint rather
than a claim that concurrent writers are safely coordinated.

1. **NFR01 — Platform compatibility.** The packaged application must launch and complete the core
   manual scenarios on Windows, macOS and Linux with Java 25 and compatible JavaFX native libraries.
   Record the OS version and CPU architecture for each release check; do not infer ARM support from
   an x86-64 build. **Status:** cross-platform release verification pending; the build currently
   specifies JavaFX `win`, `mac` and `linux` classifiers.
2. **NFR02 — Portable distribution.** With the required Java runtime installed, the user must be
   able to run the release JAR from a writable directory without installing Mentora or a separate
   database. Verify a fresh-directory launch using `java -jar addressbook.jar`. **Status:** packaging
   supported by `shadowJar`; test each final release artifact.
3. **NFR03 — Offline operation.** After obtaining the JAR and Java runtime, all current record
   commands, reload and built-in help must work with network access disabled and without an account
   or remote service. Optional external documentation links and build-time downloads are excluded.
   **Status:** locally implemented; offline packaged acceptance check required.
4. **NFR04 — Inspectable storage.** The complete register and preferences must be stored as local
   UTF-8 JSON readable by a text editor. A valid save/reload must preserve every domain value and
   relationship, including custom groups and optional student phones. **Status:** implemented with
   JSON conversion and round-trip tests; manual file changes require closing the application first.
5. **NFR05 — Capacity and command latency.** For 200 students, up to 200 guardians, 50 groups, at most
   one guardian link and ten group assignments per student, a valid command must display its result
   within one second on a reference computer with a 2 GHz four-core CPU, 8 GB RAM, an SSD and Java 25.
   Measure from command submission to displayed result, including any save; exclude time spent typing
   and OS start-up. Run 30 trials per command family after warm-up and record the maximum, including
   worst-size lists and searches. **Status:** benchmark pending; this is a target, not a measured claim.
6. **NFR06 — Startup latency.** With the NFR05 data set on the same reference computer, the application
   must reach a visible, command-ready window within three seconds of process launch. Measure ten
   launches and record the maximum and cache conditions; do not count Java installation or first-time
   dependency downloads. **Status:** benchmark pending.
7. **NFR07 — Data integrity.** A failed state-changing command must leave the pre-command in-memory
   register intact, and saving must either replace the entire persistent register or leave its previous
   contents intact. An invalid or unreadable register must not be overwritten by recovery-mode saves.
   Verify using injected write failures, interrupted-write tests and invalid-file startup tests.
   **Status: partial.** In-memory rollback on `IOException` and load-failure protection exist, but
   direct disk writes do not meet atomicity or interruption safety. The contract is retained.
8. **NFR08 — Keyboard accessibility.** A user must be able to create, retrieve, update, link/unlink,
   assign/unassign and delete supported records, obtain help and exit without mouse input, starting
   from any workspace. Verify the complete command workflow, focus movement and result visibility.
   **Status: partial.** Commands exist, but cross-workspace result visibility and guardian-detail
   selection need further work and keyboard acceptance testing.
9. **NFR09 — Display compatibility.** At 1920×1080 and above with 100% or 125% scaling, text and controls
   must remain readable; at 1280×720 with 150% scaling every function and complete result must remain
   reachable without controls falling off-screen. Test long names, multiline errors, many assignments
   and help content at these settings. **Status:** verification pending with a known risk: MainWindow
   initially requests at least 900×600 logical units, while 1280×720 at 150% offers less logical space.
10. **NFR10 — Distribution size.** Each distributed JAR or ZIP must be no larger than 100 MB
    (100,000,000 bytes). Measure the final artifact, including bundled native libraries and assets.
    **Status:** check the release artifact; source-directory size is not the metric.
11. **NFR11 — Dependency distribution.** Bundled third-party libraries must be available without
    additional user payment, carry redistribution-compatible open-source licences with the required
    notices, and require no separate end-user installation beyond the declared Java prerequisite.
    Check the final dependency list and notices for each release. **Status:** audit required; a blanket
    “all libraries are permissively licensed” claim is inappropriate for the JavaFX dependency.
12. **NFR12 — Maintainability.** The application must remain a Java implementation with distinct UI,
    Logic, Model and Storage responsibilities. Domain invariants must be testable without starting
    JavaFX UI controls, and command parsers must be testable without writing user files. Verify through
    dependency review and the domain/parser test suites. **Status:** reflected in the current structure.
