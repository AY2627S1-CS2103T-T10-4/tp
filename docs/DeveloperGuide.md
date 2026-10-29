---
layout: page
title: Developer Guide
---
* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* _{List the sources of reused or adapted ideas, code, documentation, and third-party libraries here, with links to the originals.}_

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

<div markdown="span" class="alert alert-primary">

:bulb: **Tip:** The `.puml` files used to create diagrams are in `docs/diagrams`. Refer to the [_PlantUML Tutorial_ at se-edu/guides](https://se-education.org/guides/tutorials/plantUml.html) to learn how to create and edit diagrams.
</div>

### Architecture

#### Current Mentora MVP implementation

The inherited AB3 diagrams and Person-command examples below are historical reference material. The current
runtime stores Students, Guardians, student-guardian links, and group assignments; retired Person APIs remain only
for source compatibility.

- `ConfiguredGroups` supplies the five-entry default catalogue for new and legacy data files. `AddressBook` owns the
  current ordered `GroupCatalogue`, immutable `GroupAssignment` pairs, and student-to-guardian links. It validates
  references and uniqueness and includes the complete aggregate in copy/reset/equality/hash. Student replacement keeps
  relationships by stable ID; deleting a student removes only that student's relationships.
- `link-guardian` validates both stable IDs, enforces one guardian per student, permits a guardian to be shared by
  siblings, and refreshes the selected student's profile after saving.
- `assign-group` and `unassign-group` parse ID text without allocating IDs and validate before mutation. Their
  results request the Students workspace, which MainWindow applies only after Logic has saved successfully.
  `list-groups` is a read-only query and requests Groups.
- `add-group` creates a validated Subject or Class group. `delete-group` removes only an unassigned group; the domain
  rejects deletion while any student references its code. Both request Groups and use the normal save/rollback boundary.
- JSON has `students` plus optional `guardians`, `groups`, `guardianLinks`, and `groupAssignments` arrays. Stored text
  must be JSON strings; malformed or dangling relationships and records fail the complete load instead of publishing
  partial data. An absent legacy `groups` property loads the five defaults; a present array is authoritative.
- `StorageManager` retains a load-failure state. Logic rejects mutations before execution, and storage rejects
  writes independently. Read-only commands remain available. Save failure restores the in-memory aggregate;
  direct normal writes are not atomic and can still partially damage a file.
- MainWindow coordinates selection/navigation and explicitly refreshes assignment-only changes. The profile
  scrolls as a whole to keep long names and assignments reachable at compact sizes without changing its styling.
- `list-guardians` is a read-only query that clears the Guardian filter and requests the Guardians workspace.
  The three-tab MainWindow reuses one Guardian list/details pair; rows show ID, name, phone and linked-student
  count, while the profile resolves linked Students from the current aggregate.
- Model exposes a filtered Student view while the complete register remains authoritative for lookup and saving.
  View/Search/List are read-only; successful View/Assign/Unassign/Add Student results reveal a hidden target by clearing
  the filter after any required save. Failed operations preserve the prior filter, selection and workspace.

See [Assign Group Handover](feature-handover/AssignGroupHandover.md) for assignment behavior,
[Add Group Handover](feature-handover/AddGroupHandover.md) and
[Delete Group Handover](feature-handover/DeleteGroupHandover.md) for catalogue mutation, and
[Link Guardian Handover](feature-handover/LinkGuardianHandover.md) for the guardian-relationship and profile-display
contract.

For `unassign-group`, `LogicManager.execute` asks `AddressBookParser` to dispatch to
`UnassignGroupCommandParser`, which validates syntax, retains the student ID as text and creates
`UnassignGroupCommand`. Logic checks recovery state and snapshots the complete register before executing
the command. The command resolves the student from the full register without constructing another ID and
resolves the canonical group through the configured catalogue. It calls `Model.removeGroupAssignment`;
`ModelManager` delegates to `AddressBook.removeGroupAssignment`. The aggregate validates the student,
configured group and existing membership before removing exactly that pair, including a final assignment.
Student and guardian records, guardian links, other assignments and the catalogue are preserved.

After command execution, Logic saves the changed aggregate exactly once through the existing JSON schema.
Syntax and domain rejections do not save. A save `IOException` restores the complete in-memory snapshot and
becomes `Could not save changes. The group assignment was not removed.`, retaining the original cause.
Only successful saving permits Logic to reveal a hidden target and return the `CommandResult`.
MainWindow then applies its target ID and Students workspace request and explicitly refreshes the profile,
even when the Student object is unchanged. The profile displays `No groups assigned.` when the list is empty.
The command performs neither persistence nor UI updates. Normal saves remain non-atomic: in-memory rollback
does not guarantee that a file partially written before failure is unchanged.

F10 coverage is in `AddressBookAssignmentTest`, `ModelManagerAssignmentTest`, `UnassignGroupCommandTest`,
`UnassignGroupCommandParserTest`, `UnassignGroupIntegrationTest` and `UnassignGroupUiTest`. These exercise
validation and ID safety, relationship preservation, save counts, JSON reload, recovery and failed-save
rollback, post-save reveal, same-object profile refresh, navigation and rendered failure states.

See [Unassign Group Handover](feature-handover/UnassignGroupHandover.md) for the F10 command contract, implementation boundaries, regression commands and follow-up risks.

<img src="images/ArchitectureDiagram.png" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<img src="images/ArchitectureSequenceDiagram.png" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<img src="images/ComponentManagers.png" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/Ui.java)

![Structure of the UI Component](images/UiClassDiagram.png)

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103T-T10-4/tp/blob/master/src/main/java/seedu/address/logic/Logic.java)

The partial class diagram below uses `assign-group` and `unassign-group` as Mentora examples.
`Model` and `Storage` are external interfaces; their implementations and other command families are omitted.

<img src="images/LogicClassDiagram.png" width="740"/>

`LogicManager` implements `Logic` and retains the injected `Model` and `Storage` references. It constructs and
retains one `AddressBookParser`. That router does not implement `Parser<T>`: it creates a matching concrete
parser when routing input. `AssignGroupCommandParser` implements `Parser<AssignGroupCommand>`, and
`UnassignGroupCommandParser` implements `Parser<UnassignGroupCommand>`, where `T` is bounded by `Command`.

For these group commands, the execution path is:

1. `LogicManager.execute` passes the input to `AddressBookParser.parseCommand`. The router selects the concrete
   parser, which validates the arguments and creates the corresponding `Command` subclass. The group parsers
   retain the student ID as text and canonicalize the group code without allocating a `StudentId`.
1. Before executing a mutating command, `LogicManager` checks the storage recovery state and snapshots the
   complete in-memory register. `Command.isReadOnly()` defaults to `false`; both group commands inherit that
   classification. Read-only commands bypass saving, while recovery blocks mutations before execution.
1. The command executes against `Model`, resolving the student from the full register and the group from the
   configured catalogue. It calls `addGroupAssignment` or `removeGroupAssignment`; domain validation and
   mutation remain in the model. The command does not write to Storage or control the UI.
1. If execution changes the register, `LogicManager` saves it once through `Storage` before returning success.
   Parsing and domain rejections, read-only commands and unchanged data do not trigger a save. A save
   `IOException` restores the complete in-memory snapshot and becomes command-specific error feedback.
   Normal file writes are not atomic, so this rollback does not guarantee preservation of partially written disk data.
1. After execution and any required save succeed, Logic reveals a requested student hidden by the current filter
   and returns the `CommandResult`. The result carries feedback and optional student-ID selection and workspace
   requests. The UI applies these requests and refreshes the profile; `CommandResult` performs no UI actions.

`add-group` and `delete-group` use the same router, recovery guard, snapshot and save boundary. Their parsers validate
group-specific prefixes and construct canonical `Group`/`GroupCode` values without touching student IDs. The commands
call `Model.addGroup` or `Model.deleteGroup`; `AddressBook` enforces code uniqueness and the no-assigned-students deletion
rule. `CommandResult` can request a canonical group-code selection, which `MainWindow` applies only after saving succeeds.

**Historical AB3 illustrations:** The following unchanged diagrams explain inherited parsing and execution
patterns. Their `delete 1`, Person and Add/Delete command examples do not describe current Mentora command routes.

The sequence diagram illustrates the former `execute("delete 1")` flow:

![Interactions Inside the Logic Component for the `delete 1` Command](images/DeleteSequenceDiagram.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</div>

The inherited parser diagram also shows supporting utilities omitted from the current partial class diagram:

<img src="images/ParserClasses.png" width="600"/>

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-T10-4/tp/blob/master/src/main/java/seedu/address/model/Model.java)

<img src="images/ModelClassDiagram.png" width="850" />

`ModelManager` implements the `Model` API and owns the in-memory `AddressBook` and user preferences. `AddressBook` is the complete register: it owns Student and Guardian records, the ordered group catalogue, student-to-guardian links and group assignments. It implements `ReadOnlyAddressBook` for callers that need to inspect the register without mutating it.

Students and Guardians are immutable records composed from their stable IDs and validated value types. A student may link to at most one guardian, while a guardian may be shared by multiple students. `AddressBook` stores these links by `StudentId` and `GuardianId`, so contact details stay on the Guardian record and are resolved when a profile is displayed.

`GroupAssignment` pairs a `StudentId` with a configured `GroupCode`. `AddressBook` validates that the student exists,
the group is configured, and the pair is unique. It replaces the immutable `GroupCatalogue` value when adding or
deleting a group, preserving insertion order. `ConfiguredGroups` supplies only the default five groups. `setGroups`
rejects a replacement catalogue that would leave an assignment pointing to a missing group, and `deleteGroup` rejects
an assigned group rather than cascading relationship deletion.

`ModelManager` exposes filtered Student and Guardian views for the UI while keeping the full `AddressBook` available for authoritative lookup and saving. The diagram omits the retired Person classes, which remain only for source compatibility.


### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103T-T10-4/tp/blob/master/src/main/java/seedu/address/storage/Storage.java)

<img src="images/StorageClassDiagram.png" width="850" alt="Storage classes and the persisted Mentora record collections" />

`StorageManager` implements `Storage` and delegates register and user-preference file access to
`JsonAddressBookStorage` and `JsonUserPrefsStorage`, respectively. These collaborators are supplied through its
constructor. User preferences are stored separately from the student and guardian register.

`JsonAddressBookStorage` reads and writes JSON through `JsonUtil`. It creates a `JsonSerializableAddressBook`
from the complete `ReadOnlyAddressBook` when saving and calls `toModelType()` on the loaded wrapper when reading.
The wrapper holds five collections:

* `JsonAdaptedStudent` stores student IDs, names, academic levels and optional student phones.
* `JsonAdaptedGuardian` stores guardian IDs, names and phone numbers.
* `JsonAdaptedGroup` stores each group's canonical code, normalized label and Subject/Class kind.
* `JsonAdaptedGuardianLink` stores a student ID and guardian ID for each relationship.
* `JsonAdaptedGroupAssignment` stores a student ID and group code for each assignment.

The relationship adapters store IDs and codes rather than references to other adapters. Loading reconstructs the
catalogue before group assignments, so saved custom codes can be resolved, and reconstructs students and guardians
before validating assignment and guardian-link references. A conversion failure prevents a partially validated
register from being returned. For backward compatibility, an absent `groups` property uses `ConfiguredGroups`; an
explicit array, including an empty one, is saved and loaded as the authoritative catalogue.

When a register load fails, `StorageManager` retains the failure and blocks subsequent register saves for that
storage session. User-preference storage remains independent. Normal register writes are not atomic; the
in-memory rollback performed by Logic after a save failure does not guarantee restoration of disk contents.

The partial diagram omits model types, JSON utilities and deserialization helpers for readability. The retired
`JsonAdaptedPerson` and `JsonAdaptedTag` classes remain for compatibility but are not part of the current
persisted record collections.

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` — Saves the current address book state in its history.
* `VersionedAddressBook#undo()` — Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` — Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

![UndoRedoState0](images/UndoRedoState0.png)

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

![UndoRedoState1](images/UndoRedoState1.png)

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

![UndoRedoState2](images/UndoRedoState2.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.

</div>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

![UndoRedoState3](images/UndoRedoState3.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.

</div>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

![UndoSequenceDiagram](images/UndoSequenceDiagram-Logic.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.

</div>

Similarly, how an undo operation goes through the `Model` component is shown below:

![UndoSequenceDiagram](images/UndoSequenceDiagram-Model.png)

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<div markdown="span" class="alert alert-info">:information_source: **Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.

</div>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

![UndoRedoState4](images/UndoRedoState4.png)

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …​` command. This is the behavior that most modern desktop applications follow.

![UndoRedoState5](images/UndoRedoState5.png)

The following activity diagram summarizes what happens when a user executes a new command:

<img src="images/CommitActivityDiagram.png" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* coordinates a small private tuition centre in Singapore;
* manages approximately 30–200 active and prospective secondary-school students;
* frequently updates student and guardian contact details, enrolment statuses, academic levels, and class or subject assignments;
* needs to retrieve individual contacts and contact groups quickly for administrative work;
* can type quickly and prefers fast keyboard-driven workflows to mouse-heavy systems; and
* is the sole user of a local desktop application and does not require real-time collaboration.

**Value proposition**: Manage every student and guardian contact accurately, and find the right people in seconds using fast typed commands.

Mentora will:

* manage contact and basic administrative information for students and their guardians;
* represent student–guardian relationships without duplicating shared guardian records;
* help coordinators organize, find, update, and act on contacts by academic level, class, subject, or enrolment status;
* treat people and their contact information as its primary focus; and
* support keyboard-first, local desktop usage by one coordinator at a time.

Mentora will not:

* manage lesson content, attendance, grades, fees, payroll, or accounting;
* replace a full student-management or learning-management system;
* send WhatsApp, SMS, or email messages directly;
* support real-time collaboration between multiple staff members; or
* target large schools, universities, or multi-branch education businesses.


### User stories

Priorities: High (must have) - `* * *`, Medium (should have) - `* *`, Low (possible future direction) - `*`.

| ID | Priority | As a …​ | I want to …​ | So that I can …​ |
| -- | -------- | ------- | -------------- | ----------------- |
| US01 | `* * *` | new coordinator | view command help and clear examples | learn or recall how to use Mentora without leaving the application |
| US02 | `* * *` | coordinator | add a student with a name and academic level | create a basic student record |
| US03 | `* * *` | coordinator | update a student's academic level | keep the record accurate when the student advances or a mistake is found |
| US04 | `* * *` | coordinator | delete a student while preserving any shared guardian record | remove an incorrect student record without affecting siblings |
| US05 | `* * *` | coordinator | add a guardian with a name and phone number | retain the student's administrative contact information |
| US06 | `* * *` | coordinator | update a guardian's phone number once | show the new number in every linked student's profile |
| US07 | `* * *` | coordinator | link a student to a guardian | see the appropriate contact from the student's profile |
| US08 | `* * *` | coordinator | link siblings to the same guardian record | avoid entering and maintaining duplicate contact details |
| US09 | `* * *` | coordinator | assign a student to an existing subject or class group | record the student's tuition arrangements |
| US10 | `* * *` | coordinator | view a student's group assignments in the student's profile | understand the student's current tuition arrangements |
| US11 | `* * *` | coordinator | list all registered students | browse the centre's records |
| US12 | `* * *` | coordinator | search for a student by name | answer enquiries without scanning multiple spreadsheets |
| US13 | `* * *` | coordinator | open a student's profile | see the academic level, assignments, and linked guardian together |
| US14 | `* * *` | coordinator | perform core record-management tasks using typed commands | work quickly without switching repeatedly between keyboard and mouse |
| US15 | `* * *` | coordinator | receive clear feedback for successful and invalid commands | know whether the intended change was made and how to correct errors |
| US16 | `* * *` | coordinator | have records saved locally and reloaded when Mentora reopens | continue working without an account, network connection, or manual save step |
| US17 | `* *` | coordinator | search for guardians by name | retrieve an existing guardian record quickly |
| US18 | `* *` | coordinator | filter students by subject or academic level | review a relevant cohort without scanning all records |
| US19 | `* *` | coordinator | jump directly to a student's edit workflow using a command | update frequently changing information with minimal interruption |
| US20 | `* *` | coordinator | mark a prospective student as active | keep the enrolment status current when the student enrols |
| US21 | `* *` | coordinator | display only active students | keep daily work focused on currently enrolled students |
| US22 | `* *` | coordinator | archive withdrawn or graduated students | retain historical records without cluttering active lists |
| US23 | `* *` | coordinator | display guardian contacts for a selected class | prepare the correct audience for an announcement |
| US24 | `* *` | coordinator | obtain a copyable class contact list containing each shared guardian once | use the list in an external messaging application without sending duplicate messages |
| US25 | `* *` | coordinator | require guardian phone numbers to contain eight digits and explain invalid entries | correct contact details before they are saved |
| US26 | `*` | coordinator | link a student to more than one guardian | retain multiple valid contacts for the same student |
| US27 | `*` | coordinator | record a student's school | retain additional context useful for administration |
| US28 | `*` | coordinator | filter contacts by class or enrolment status | retrieve other useful contact groups quickly |
| US29 | `*` | coordinator | detect incomplete or duplicate records | correct inconsistent data before it causes administrative mistakes |
| US30 | `*` | coordinator | import and export contact lists | move existing spreadsheet data into or out of Mentora efficiently |
| US31 | `*` | coordinator | create and safely delete subject or class groups using commands | adapt Mentora when the centre's class structure changes |

### Use cases

(For all use cases below, the **System** is `Mentora` and the **Actor** is the tuition-centre coordinator, unless specified otherwise.)

**Use case UC01: Register a new student with a guardian and group assignment**

**Preconditions:** Mentora has loaded a writable local data file.

**MSS**

1. Coordinator requests to add a guardian with the guardian's name and phone number.
2. Mentora creates the guardian record.
3. Coordinator requests to add a student with the student's name and academic level.
4. Mentora creates the student record.
5. Coordinator requests to link the student to the guardian.
6. Mentora creates the student–guardian relationship.
7. Coordinator requests to assign an existing subject or class group to the student.
8. Mentora adds the group assignment.
9. Coordinator requests to view the student's profile.
10. Mentora displays the student's academic level, group assignment, and linked guardian contact.

    Use case ends.

**Extensions**

* 1a. The guardian already has a record in Mentora.
  * 1a1. Coordinator searches for the guardian by name.
  * 1a2. Mentora displays the matching guardian record.

    Use case resumes at step 3.

* 1b. The guardian details are invalid.
  * 1b1. Mentora explains why the guardian cannot be added.

    Use case resumes at step 1.

* 3a. The student details are invalid or match an existing student record.
  * 3a1. Mentora explains why the student cannot be added.

    Use case resumes at step 3.

* 5a. The student already has the maximum number of guardians supported by the current product version.
  * 5a1. Mentora informs the coordinator that another guardian cannot be linked.

    Use case ends.

* 7a. The selected group does not exist.
  * 7a1. Mentora displays the configured groups.

    Use case resumes at step 7.

* \*a. At any time, Mentora cannot save a requested change.
  * \*a1. Mentora leaves the last valid data unchanged and reports the failure.

    Use case ends.

**Use case UC02: Handle a parent enquiry and update the guardian's phone number**

**Preconditions:** The student and linked guardian have existing records, and Mentora has loaded a writable local data file.

**MSS**

1. Coordinator searches for the student by name.
2. Mentora displays matching students.
3. Coordinator requests to view the required student's profile.
4. Mentora displays the student's academic level, assignments, and linked guardian contact.
5. Coordinator requests to update the guardian's phone number.
6. Mentora updates the guardian record and confirms the change.
7. Coordinator requests to view the student's profile again.
8. Mentora displays the updated guardian phone number.

    Use case ends.

**Extensions**

* 2a. No student matches the search.
  * 2a1. Mentora reports that no student was found.

    Use case ends.

* 5a. The new phone number is invalid.
  * 5a1. Mentora explains the required phone-number format.

    Use case resumes at step 5.

* 5b. The new phone number duplicates another guardian's number.
  * 5b1. Mentora identifies the duplicate record and does not update the guardian.

    Use case ends.

* 6a. Mentora cannot save the updated number.
  * 6a1. Mentora retains the previous number and reports the failure.

    Use case ends.

**Use case UC03: Review a cohort and archive an inactive student**

**Preconditions:** Mentora contains student records and has loaded a writable local data file.

**MSS**

1. Coordinator requests students belonging to a subject or academic level.
2. Mentora displays matching active students.
3. Coordinator selects a student who has withdrawn or graduated.
4. Coordinator requests to archive the selected student.
5. Mentora retains the record but removes it from active-student results.
6. Coordinator requests to display active students again.
7. Mentora displays the active students without the archived record.

    Use case ends.

**Extensions**

* 2a. No active students match the selected subject or level.
  * 2a1. Mentora reports that no matching students were found.

    Use case ends.

* 4a. The selected student is already archived.
  * 4a1. Mentora informs the coordinator that no change is required.

    Use case ends.

* 5a. Mentora cannot save the archival change.
  * 5a1. Mentora keeps the student active and reports the failure.

    Use case ends.

**Use case UC04: Prepare a class contact list for an external announcement**

**Preconditions:** The selected class and its students exist in Mentora.

**MSS**

1. Coordinator requests to view students in a selected class.
2. Mentora displays the matching students.
3. Coordinator requests the guardian contacts for those students.
4. Mentora displays a copyable contact list containing each shared guardian once.
5. Coordinator copies the list for use in an external messaging application.

    Use case ends.

**Extensions**

* 2a. The selected class contains no students.
  * 2a1. Mentora reports that there are no contacts to display.

    Use case ends.

* 3a. One or more students have no linked guardian.
  * 3a1. Mentora identifies those students and displays the remaining available contacts.

    Use case resumes at step 4.

* 3b. Multiple students share the same guardian.
  * 3b1. Mentora includes that guardian only once in the contact list.

    Use case resumes at step 4.

### Non-Functional Requirements

1. **NFR01 — Platform independence:** Mentora should work on Windows, Linux, and macOS computers that have Java 25 installed.
2. **NFR02 — Portability:** Mentora should run without an installer and should be distributed as a single JAR file.
3. **NFR03 — Local single-user operation:** Mentora should support one coordinator operating one local data set at a time and should not require an account, Internet connection, remote server, or real-time collaboration service.
4. **NFR04 — Human-editable storage:** Mentora should store its persistent data locally in a human-editable text format such as JSON and should not require a database management system.
5. **NFR05 — Capacity and responsiveness:** With 200 student records and their associated guardian, enrolment, and group data, Mentora should complete a valid command within one second on a reference computer with a 2 GHz four-core processor, 8 GB of RAM, an SSD, and Java 25.
6. **NFR06 — Startup performance:** With the data volume specified in NFR05, Mentora should load the local data and become ready for commands within three seconds on the same reference computer.
7. **NFR07 — Data integrity:** A state-changing command should either be saved completely or leave both the in-memory and persisted data unchanged. Mentora should preserve an unreadable or invalid data file instead of overwriting it.
8. **NFR08 — Keyboard accessibility:** Every core record-management function should be usable through typed commands without requiring a mouse.
9. **NFR09 — Screen compatibility:** The GUI should work without resolution-related inconvenience at 1920×1080 or higher with 100% or 125% display scaling, and all functions should remain usable at 1280×720 or higher with 150% scaling.
10. **NFR10 — Deliverable size:** The distributed JAR or ZIP file should not exceed 100 MB and should not contain unnecessarily large assets or unused libraries.
11. **NFR11 — External dependencies:** Any third-party libraries should be free, open-source, permissively licensed, and should not require separate installation by the user.

### Glossary

* **Academic level**: A student's current Singapore secondary-school level, such as Secondary 1 or Integrated Programme Year 4.
* **Active student**: A student currently enrolled at the tuition centre.
* **Archived student**: A withdrawn or graduated student whose record is retained but excluded from active-student lists.
* **Class group**: A tuition class to which one or more students are assigned.
* **Contact list**: A copyable collection of guardian contact details prepared for use in an external messaging application.
* **Coordinator**: The single tuition-centre staff member who operates Mentora and maintains its records.
* **Enrolment status**: The stage of a student's relationship with the centre, such as prospective, active, withdrawn, or graduated.
* **Guardian**: A parent or other responsible person whose contact record may be linked to one or more students.
* **Guardian link**: The relationship connecting a student to a guardian. The MVP supports one guardian per student and allows one guardian to be linked to multiple students; support for multiple guardians per student is deferred.
* **Group assignment**: A relationship indicating that a student belongs to a configured subject or class group.
* **Keyboard-first**: Designed so that core tasks can be completed efficiently using typed commands without requiring mouse input.
* **Prospective student**: A student whose details are recorded before enrolment is confirmed.
* **Shared guardian**: One guardian record linked to multiple students, such as siblings.
* **Student**: An active or prospective secondary-school learner whose contact and basic administrative information is managed in Mentora.
* **Subject group**: A configured tuition subject to which students may be assigned.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

These repeatable checks use synthetic records and describe current source-build behavior, with version dependencies noted below. Record the exact source revision or release, JAR filename, Java version, operating system, actual results and any deviations. When testing a historical release, verify its behavior before applying the exact expectations below. Run the workflow sections in order; the startup/recovery scenarios use separate folders. These checks are a starting point for further exploratory testing, not a claim of exhaustive coverage.

### Setup and test conventions

1. Run `java -version` in a terminal and confirm Java **25**. Obtain a JAR from the [team releases](https://github.com/AY2627S1-CS2103T-T10-4/tp/releases), or run `./gradlew shadowJar` (`.\gradlew.bat shadowJar` on Windows) in a source checkout.
   Current source builds produce `build/libs/mentora.jar`. The historical v1.4.1 release asset was named `Mentora.jar`. When testing a historical release, use its actual JAR filename and capitalization.
2. Create a new writable folder named `workflow` in a disposable test location and copy the JAR into it. Keep it separate from personal data and other running Mentora instances.
3. Before launching, create `workflow/data/addressbook.json` as a UTF-8 text file containing exactly `{"students":[]}`. This valid empty register gives no students or guardians and the five default groups. A zero-byte file is **not** a valid empty register.
4. Open a terminal **inside `workflow`** and run `java -jar "mentora.jar"` for the current source build. When testing a historical release, substitute its actual JAR filename. Mentora uses `data/addressbook.json` relative to this working folder; its location appears in the footer. Select **Students**.
5. Submit one command at a time. Record the IDs returned when adding Amy, Ben and Anita as **`SID_A`**, **`SID_B`** and **`GID`**. Replace these tokens in every later command and expected message with the actual IDs, including their `S-` or `G-` prefix. They are not literal input, list positions or assumed consecutive numbers. Student IDs also appear in Students; use `list-guardians` to recover a guardian ID.
6. For every rejection below, check the exact feedback and that records, displayed lists, filters, selection and details remain unchanged from immediately before that command. Once a data file exists, its contents should also be unchanged. Each invalid-input case keeps the other required values valid.
7. To repeat a workflow section, close Mentora, create another disposable folder with the empty-register fixture, and repeat its preceding setup steps. Do not reset by deleting files in a normal installation. Cleanup means closing only the test instance and retaining or discarding only its disposable folder.

For command syntax and field rules, see the [User Guide](UserGuide.md) or `help COMMAND`. Counted results below depend on the stated fixture and sequence. Successful edits save immediately; listing, finding, filtering and viewing do not save.

### Students: create, retrieve and update

**Starting state:** the empty `workflow` fixture above, with Students selected. Keep both created students for the following sections.

| Command/action | Expected feedback | Expected visible state |
| --- | --- | --- |
| `list-students` | `No students registered.` | Empty Students list. |
| `add-student n/Amy Tan al/Sec 2 p/91234567` | `Added student SID_A: Amy Tan (Sec 2) with phone +65 91234567.` | Amy is selected; profile shows Sec 2, the student phone, `Guardian: none` and `No groups assigned.` Record `SID_A`. |
| `add-student n/Ben Tan al/IP 4` | `Added student SID_B: Ben Tan (IP 4).` | Ben is selected; profile shows IP 4 and `Phone: none`. Record `SID_B`. |
| `list-students` | `Listed 2 student(s).` | Amy and Ben appear, in name order, with their stable IDs and levels. |
| `add-student n/Amy Tan al/Sec 2` | `A student named Amy Tan at Sec 2 already exists.` | No third student; Ben remains selected. |
| `add-student n/Cara Lim` | `Invalid command format. Use: add-student n/NAME al/ACADEMIC_LEVEL [p/PHONE]` | No Cara record. The missing level is the invalid condition. |
| `find-student n/amy` | `Found 1 student matching "amy".` | Only Amy is listed; the hidden Ben selection is cleared. |
| `find-student n/Zzz` | `No students match "Zzz".` | Empty displayed list, with both records still stored. |
| `view-student sid/SID_A` | `Opened profile for SID_A.` | The hiding filter clears; both rows return and Amy is selected. Heading shows `SID_A - Amy Tan`. |
| `filter-students al/Sec 2` | `Found 1 student(s) at Sec 2.` | Students opens, showing only Amy and `Filtered by academic level: Sec 2`. |
| `edit-student sid/SID_A al/Sec 3 p/98765432` | `Updated student SID_A: academic level is now Sec 3 and phone is now +65 98765432.` | Same Amy ID/name; both fields update. The Sec 2 filter clears to reveal Amy, and both rows are visible. |

**Boundary and invalid-value checks:** continue with Amy selected. Run each command separately; these check phone lengths just below/at/above eight digits and the Sec-level endpoints. Ben must remain unchanged.

| Command/action | Expected feedback and state |
| --- | --- |
| `edit-student sid/SID_A p/9123456` | `Phone number must be an 8-digit Singapore number, optionally prefixed by +65.` Amy retains `+65 98765432`. |
| `edit-student sid/SID_A p/91234567` | `Updated student SID_A phone to +65 91234567.` Only the student phone changes. |
| `edit-student sid/SID_A p/912345678` | Same phone-validation error; Amy retains `+65 91234567`. |
| `edit-student sid/SID_A al/Sec 0` | `Academic level must be Sec 1-5 or IP 1-6.` Amy remains Sec 3. |
| `edit-student sid/SID_A al/Sec 1`, then `edit-student sid/SID_A al/Sec 4`, then `edit-student sid/SID_A al/Sec 5` | Each succeeds: `Updated student SID_A: academic level is now Sec LEVEL.` Replace `LEVEL` with 1, 4 or 5 respectively; verify the selected profile each time. |
| `edit-student sid/SID_A al/Sec 6` | Same academic-level error; Amy remains Sec 5. |
| Reset: `edit-student sid/SID_A al/Sec 3 p/98765432` | `Updated student SID_A: academic level is now Sec 3 and phone is now +65 98765432.` Continue with these values. |

### Guardians: shared contact and phone updates

**Starting state:** Amy and Ben from the student checks; no guardians or links. Student phones and guardian phones are separate fields.

| Command/action | Expected feedback | Expected visible state |
| --- | --- | --- |
| `list-guardians` | `No guardians registered.` | Guardians opens with an empty list. |
| `add-guardian n/Anita Lim p/92345678` | `Added guardian GID: Anita Lim (+65 92345678).` | One guardian is added. Record `GID`; creation alone does not promise selection. |
| `list-guardians`, then click the row with `GID` | `Listed 1 guardian(s).` | Anita has phone `+65 92345678` and no linked students. |
| `find-guardian n/anita` | `Found 1 guardian matching "anita".` | Anita is the only matching guardian. |
| `add-guardian n/Other Guardian p/+6592345678` | `A guardian with phone +65 92345678 already exists (GID). Link that guardian instead.` | Normalized duplicate phone is rejected; one guardian remains. |
| `link-guardian sid/SID_A gid/GID` | `Linked student SID_A (Amy Tan) to guardian GID (Anita Lim, +65 92345678).` | Students opens and Amy is selected; her profile displays Anita and the guardian phone. |
| `link-guardian sid/SID_B gid/GID` | `Linked student SID_B (Ben Tan) to guardian GID (Anita Lim, +65 92345678).` | Ben is selected with the same guardian. |
| Repeat `link-guardian sid/SID_B gid/GID` | `Student SID_B is already linked to guardian GID.` | No duplicate relationship. |
| `list-guardians`, then select `GID` | `Listed 1 guardian(s).` | Linked-student count is 2; details list Amy and Ben with their recorded IDs. |
| Select Students; `view-student sid/SID_A`, then `edit-guardian gid/GID p/+65 93456789` | `Opened profile for SID_A.`, then `Updated guardian GID phone to +65 93456789. Updated contact will appear in 2 linked student profile(s).` | Amy stays selected; her guardian contact updates immediately, while her own phone remains `+65 98765432`. |
| `view-student sid/SID_B` | `Opened profile for SID_B.` | Ben also shows Anita with `+65 93456789`, while his student phone remains absent. |

Keep both links for the save/reload and unlink checks below.

### Groups: catalogue, assignments and filtering

**Starting state:** both students and their shared guardian remain. The fixture catalogue has only `ENG`, `MAT`, `SCI`, `CHI` and `S2-MAT-A`; neither `PHY` nor `S3-PHY-B` exists.

| Command/action | Expected feedback | Expected visible state |
| --- | --- | --- |
| `list-groups` | `Listed 5 configured group(s).` | Groups opens; each default row shows its code, label and kind. |
| `add-group g/PHY l/Physics k/Subject` | `Created group PHY (Physics, Subject).` | Groups selects PHY. |
| `add-group g/S3-PHY-B l/Secondary 3 Physics B k/Class` | `Created group S3-PHY-B (Secondary 3 Physics B, Class).` | Groups selects S3-PHY-B. |
| `add-group g/phy l/Other Physics k/Subject` | `Group PHY is already configured.` | No duplicate, renamed label or extra group; `list-groups` reports `Listed 7 configured group(s).` |
| `assign-group sid/SID_A g/PHY` | `Assigned group PHY (Physics) to student SID_A.` | Students opens; Amy is selected and her profile lists PHY / Physics. |
| `assign-group sid/SID_B g/S3-PHY-B` | `Assigned group S3-PHY-B (Secondary 3 Physics B) to student SID_B.` | Ben is selected and shows the Class assignment; his IP 4 level is unchanged. |
| `filter-students g/PHY` | `Found 1 student(s) assigned to PHY.` | Only Amy is listed; summary is `Filtered by group: PHY — Physics`. |
| `filter-students g/S3-PHY-B` | `Group S3-PHY-B is a Class group; use the academic-level filter.` | The previous PHY filter/results remain. |
| `filter-students g/PHY al/Sec 3` | `Invalid command format. Use: filter-students g/GROUP_CODE` | Two individually valid criteria cannot be combined; previous results remain. |
| `view-student sid/SID_B` | `Opened profile for SID_B.` | Hidden Ben is revealed and selected; both students return and the filter clears. |
| `delete-group g/PHY` | `Group PHY is assigned to 1 student(s). Remove those assignments before deleting it.` | Group and Amy’s assignment remain; Ben’s selection/details are unchanged. |

### Save and reload the completed workflow

**Starting state:** exactly two students, one guardian, seven groups; Amy has PHY and Ben has S3-PHY-B; both link to Anita.

1. Run `exit`. The application closes (the command feedback is `Closing Mentora.`). Reopen the **same JAR from the same `workflow` folder**.
2. Select Students and run `list-students`: expect `Listed 2 student(s).`. Run `view-student sid/SID_A`: expect `Opened profile for SID_A.` and Amy’s Sec 3, student phone `+65 98765432`, PHY assignment and Anita’s `+65 93456789` contact.
3. Run `view-student sid/SID_B`: expect `Opened profile for SID_B.`, IP 4, `Phone: none`, S3-PHY-B and the same guardian contact. IDs must match those recorded before restart.
4. Run `list-guardians`: expect `Listed 1 guardian(s).`; select `GID` and check both linked students. Run `list-groups`: expect `Listed 7 configured group(s).` including both newly created groups.
5. Close Mentora with `exit`. Copy this **test** `data/addressbook.json` to a file named `valid-register.json` outside the `workflow` folder but inside the disposable test location. Keep it for the recovery scenario. Relaunch the workflow folder before continuing.

### Unlink, unassign and delete safely

**Starting state:** the saved workflow above. These destructive commands affect only synthetic records.

| Command/action | Expected feedback | Expected visible state |
| --- | --- | --- |
| Select Students; `unlink-guardian sid/SID_A` | `Removed guardian GID from student SID_A. Guardian record was retained.` | Amy is selected and shows `Guardian: none`; her PHY assignment remains. |
| Repeat `unlink-guardian sid/SID_A` | `Student SID_A has no guardian to unlink.` | Amy and Anita both remain. |
| `list-guardians`, then select `GID` | `Listed 1 guardian(s).` | Only Ben is linked; linked-student count is 1. |
| `link-guardian sid/SID_A gid/GID` | `Linked student SID_A (Amy Tan) to guardian GID (Anita Lim, +65 93456789).` | Amy is selected; both students again share Anita. |
| `unassign-group sid/SID_A g/PHY` | `Removed group PHY from student SID_A.` | Amy shows `No groups assigned.`; guardian and other student fields remain. |
| `delete-group g/PHY` | `Deleted group PHY: Physics (Subject).` | Groups opens; PHY is absent and the other six groups remain. |
| Repeat `delete-group g/PHY` | `Group PHY is not configured. Run list-groups to see available groups.` | Unknown-code rejection changes no remaining group. |
| `assign-group sid/SID_A g/MAT` | `Assigned group MAT (Mathematics) to student SID_A.` | Amy is selected and has an assignment for the deletion check. |
| `delete-student sid/SID_A` | `Deleted student SID_A: Amy Tan. Guardian link removed; guardian GID was retained.` | Amy disappears and her selected profile clears. Her assignment/link are removed; Ben remains. |
| `view-student sid/SID_A` | `Student SID_A does not exist.` | The previously generated, now-deleted ID is a reliable unknown-ID case. No record reappears. |
| `view-student sid/SID_B` | `Opened profile for SID_B.` | Ben retains his ID, IP 4, Class assignment and Anita link. |
| `list-guardians`, then select `GID`; `list-groups` | `Listed 1 guardian(s).`, then `Listed 6 configured group(s).` | Anita is retained with only Ben linked; MAT is still configured. |
| `unassign-group sid/SID_B g/S3-PHY-B`, then `delete-group g/S3-PHY-B` | `Removed group S3-PHY-B from student SID_B.`, then `Deleted group S3-PHY-B: Secondary 3 Physics B (Class).` | Ben’s final assignment is removed; five default groups remain. |

**Cleanup:** close the test instance. To rerun, use a new folder and fresh IDs; guardian deletion is not supported, so do not invent a cleanup command.

### Missing data file: normal startup

**Starting state:** create a separate new folder named `missing` containing only the JAR. Do not copy preferences or a `data` directory into it.

1. Launch from `missing` using the actual JAR filename. In the current source build, Students contains sample students **Mei Lin / Sec 2** and **S. Kumar / IP 4**; no recovery warning or `Saving disabled` indicator appears.
2. Run `list-students`: expect `Listed 2 student(s).`. The register file must still be absent after startup and this read-only command; preferences/log files may exist.
3. Run `add-student n/Recovery Check al/Sec 2`. Record its generated ID as `SID_C`; expect `Added student SID_C: Recovery Check (Sec 2).` The new student is selected and `data/addressbook.json` now exists.
4. Exit and relaunch from `missing`; `list-students` reports `Listed 3 student(s).`. `view-student sid/SID_C` reports `Opened profile for SID_C.` and displays the saved student.

**Integration dependency:** [PR #123](https://github.com/AY2627S1-CS2103T-T10-4/tp/pull/123) is pending. If testing a build that includes that change, expect an empty register and counts of 0 after startup and 1 after adding the student; do not mistake that intended change for invalid-file recovery. Close this test instance when finished.

### Invalid data file: preservation and recovery

**Starting state:** the valid synthetic backup from the save/reload section is available. Create a third disposable folder named `recovery`, copy the JAR into it, and ensure no Mentora instance is using it.

1. Create `recovery/data/addressbook.json` as UTF-8 text containing exactly `broken JSON`. Keep an identical copy named `invalid-original.json` outside its `data` directory. This deliberately malformed file must never replace personal data.
2. Record its SHA-256 hash. On Windows PowerShell, from `recovery`, run `Get-FileHash -LiteralPath .\data\addressbook.json -Algorithm SHA256`; on macOS/Linux use an equivalent checksum tool. Launch the JAR from `recovery`.
3. Expect empty student/guardian lists and this recovery feedback: **`Could not load local data. Your original data file was not changed; restore or replace it before saving.`** The footer must say **`Saving disabled`**; hovering over it explains the failure.
4. Run `list-students` and `list-guardians`: expect `No students registered.` and `No guardians registered.` respectively. Run `list-groups`: expect `Listed 5 configured group(s).`. Read-only feedback may replace the warning text, but the footer must remain `Saving disabled`.
5. Run the syntactically valid mutation `add-student n/Recovery Check al/Sec 2`. Expect the same recovery message from step 3; no student is added and saving stays disabled. A malformed command would test parsing instead, so use this exact valid input.
6. Run `exit`, then recompute the register hash and compare it with step 2 (or compare the bytes with `invalid-original.json`). They must match. Recovery does not repair or back up the register automatically; preferences and logs can change independently.
7. With Mentora closed, retain the invalid copy and replace only the disposable `recovery/data/addressbook.json` with the saved `valid-register.json`. Relaunch. Expect the two students, guardian links and seven groups from that backup, with no recovery warning or `Saving disabled` indicator. Saving remains disabled for an old recovery session even if its file is replaced externally; a restart is required.
8. Select Students and run `edit-student sid/SID_B p/94567890`: expect `Updated student SID_B phone to +65 94567890.` and the phone in Ben’s profile. Exit, reopen, and use `view-student sid/SID_B` to confirm the saved phone and original relationships.

**Cleanup:** close the test instance and keep the test files/results for review, or discard only the disposable test location. File preservation in this scenario concerns load-failure recovery; it does not imply that normal saves use atomic file replacement.
