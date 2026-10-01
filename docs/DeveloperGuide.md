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

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<img src="images/LogicClassDiagram.png" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

![Interactions Inside the Logic Component for the `delete 1` Command](images/DeleteSequenceDiagram.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</div>

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<img src="images/ParserClasses.png" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<img src="images/ModelClassDiagram.png" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<img src="images/BetterModelClassDiagram.png" width="450" />

</div>


### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<img src="images/StorageClassDiagram.png" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

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
| US31 | `*` | coordinator | create and update subject or class groups using commands | adapt Mentora when the centre's class structure changes |

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

Given below are instructions to test the app manually.

<div markdown="span" class="alert alert-info">:information_source: **Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.

</div>

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with a set of sample contacts. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases …​ }_

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases …​ }_

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases …​ }_
