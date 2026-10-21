package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.guardian.GuardianId;
import seedu.address.model.guardian.GuardianName;
import seedu.address.model.guardian.GuardianPhone;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

class LogicManagerTest {
    @TempDir
    Path temporaryFolder;

    private ModelManager model;
    private Logic logic;

    @BeforeEach
    void setUp() {
        model = new ModelManager();
        logic = createLogic(new JsonAddressBookStorage(temporaryFolder.resolve("students.json")));
    }

    @Test
    void execute_unknownCommand_throwsParseException() {
        assertThrows(ParseException.class, () -> logic.execute("add n/Hidden Person"));
    }

    @Test
    void execute_addStudent_savesAndSelectsStudentById() throws Exception {
        CommandResult result = logic.execute("add-student n/Mei Lin al/Sec 2");
        assertEquals(1, logic.getStudentList().size());
        assertEquals("Mei Lin", logic.getStudentList().get(0).getName().toString());
        assertEquals(logic.getStudentList().get(0).getId().value, result.getSelectedStudentId());
        JsonAddressBookStorage savedStorage = new JsonAddressBookStorage(temporaryFolder.resolve("students.json"));
        assertEquals(model.getAddressBook(), savedStorage.readAddressBook().orElseThrow());
    }

    @Test
    void execute_listGroups_doesNotSaveOrModifyStudents() throws Exception {
        Student student = new Student(new StudentName("Mei Lin"), new AcademicLevel("Sec 2"));
        model.addStudent(student);
        JsonAddressBookStorage forbiddenStorage = new JsonAddressBookStorage(temporaryFolder.resolve("unused.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) {
                throw new AssertionError("list-groups must not save student data.");
            }
        };
        logic = createLogic(forbiddenStorage);

        CommandResult result = logic.execute("list-groups");

        assertEquals("Listed 5 configured group(s).", result.getFeedbackToUser());
        assertEquals(List.of(student), model.getStudentList());
        assertSame(model.getGroupCatalogue(), logic.getGroupCatalogue());
        assertFalse(Files.exists(temporaryFolder.resolve("unused.json")));
    }

    @Test
    void execute_listGroups_preservesExistingInvalidFile() throws Exception {
        Path path = temporaryFolder.resolve("invalid.json");
        String original = "invalid student data that must remain untouched";
        Files.writeString(path, original);
        logic = createLogic(new JsonAddressBookStorage(path));

        logic.execute("LIST-GROUPS");

        assertEquals(original, Files.readString(path));
        assertEquals(0, model.getStudentList().size());
    }

    @Test
    void execute_readOnlyCommands_doesNotAttemptToSave() throws Exception {
        model.addStudent(new Student(new StudentId("S-0007"), new StudentName("Mei Lin"),
                new AcademicLevel("Sec 2")));
        AddressBook beforeCommand = new AddressBook(model.getAddressBook());
        Path dataPath = temporaryFolder.resolve("read-only.json");
        JsonAddressBookStorage unwritableStorage = new JsonAddressBookStorage(dataPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) {
                throw new AssertionError("Read-only commands must not save data");
            }
        };
        logic = createLogic(unwritableStorage);

        CommandResult result = logic.execute("view-student sid/S-0007");
        assertEquals("S-0007", result.getSelectedStudentId());
        assertEquals("Opened profile for S-0007.", result.getFeedbackToUser());
        logic.execute("help");
        logic.execute("exit");

        assertThrows(ParseException.class, () -> logic.execute("view-student sid/"));
        CommandException error = assertThrows(CommandException.class, () ->
                logic.execute("view-student sid/S-999999999999999999999999999999"));
        assertEquals("Student S-999999999999999999999999999999 does not exist.", error.getMessage());
        assertEquals(beforeCommand, model.getAddressBook());
    }

    @Test
    void execute_addGuardianCanonicalizesRejectsDuplicateAndPersists() throws Exception {
        Path file = temporaryFolder.resolve("guardians.json");
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(file);
        logic = createLogic(addressBookStorage);

        CommandResult result = logic.execute("add-guardian p/+65 61234567 n/Raj   Kumar");
        String guardianId = model.getGuardianList().getFirst().getId().toString();

        assertEquals("Added guardian " + guardianId + ": Raj Kumar (+65 61234567).",
                result.getFeedbackToUser());
        assertNull(result.getSelectedStudentId());
        assertEquals(model.getAddressBook(), addressBookStorage.readAddressBook().orElseThrow());

        String duplicateCommand = "add-guardian n/Another Name p/61234567";
        CommandException duplicate = assertThrows(CommandException.class, () -> logic.execute(duplicateCommand));
        assertEquals("A guardian with phone +65 61234567 already exists (" + guardianId
                + "). Link that guardian instead.", duplicate.getMessage());
        assertEquals(1, model.getGuardianList().size());
    }

    @Test
    void execute_editGuardianPhone_persistsSharedNumberForLinkedProfiles() throws Exception {
        Path file = temporaryFolder.resolve("edit-guardian.json");
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(file);
        Guardian guardian = new Guardian(new GuardianId("G-0003"), new GuardianName("Anita Lim"),
                new GuardianPhone("91234567"));
        model.addGuardian(guardian);
        model.addStudent(new Student(new StudentId("S-0001"), new StudentName("Mei Lin"),
                new AcademicLevel("Sec 2")));
        model.linkGuardian("S-0001", "G-0003");
        logic = createLogic(addressBookStorage);

        CommandResult result = logic.execute("edit-guardian gid/G-0003 p/+65 98765432");

        assertEquals("Updated guardian G-0003 phone to +65 98765432. Updated contact will appear in 1 linked "
                + "student profile(s).", result.getFeedbackToUser());
        assertEquals(new GuardianPhone("98765432"), model.getLinkedGuardian("S-0001").orElseThrow().getPhone());
        assertEquals(model.getAddressBook(), addressBookStorage.readAddressBook().orElseThrow());
        ModelManager reloaded = new ModelManager(addressBookStorage.readAddressBook().orElseThrow(),
                model.getUserPrefs());
        assertEquals(new GuardianPhone("98765432"), reloaded.getLinkedGuardian("S-0001").orElseThrow().getPhone());
    }

    @Test
    void execute_editGuardianSaveFailure_restoresOldPhone() {
        Guardian guardian = new Guardian(new GuardianId("G-0003"), new GuardianName("Anita Lim"),
                new GuardianPhone("91234567"));
        model.addGuardian(guardian);
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(temporaryFolder.resolve("failure.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw new IOException("disk unavailable");
            }
        };
        logic = createLogic(failingStorage);

        CommandException error = assertThrows(CommandException.class, () ->
                logic.execute("edit-guardian gid/G-0003 p/98765432"));

        assertEquals("Could not save changes. Guardian G-0003 was not updated.", error.getMessage());
        assertEquals(new GuardianPhone("91234567"), model.findGuardian("G-0003").orElseThrow().getPhone());
    }

    @Test
    void execute_saveFailure_rollsBackStudent() {
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(temporaryFolder.resolve("failure.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw new IOException("disk unavailable");
            }
        };
        logic = createLogic(failingStorage);

        CommandException error = assertThrows(CommandException.class, () ->
                logic.execute("add-student n/Mei Lin al/Sec 2"));

        assertEquals("Could not save changes. No student was added.", error.getMessage());
        assertEquals(0, model.getStudentList().size());
    }

    @Test
    void execute_deleteStudent_savesAndRemovesStudent() throws Exception {
        logic.execute("add-student n/Mei Lin al/Sec 2");
        String id = logic.getStudentList().get(0).getId().value;

        CommandResult result = logic.execute("delete-student sid/" + id);

        assertEquals("Deleted student " + id + ": Mei Lin.", result.getFeedbackToUser());
        assertEquals(0, logic.getStudentList().size());
    }

    @Test
    void execute_deleteSaveFailure_rollsBackStudent() throws Exception {
        logic.execute("add-student n/Mei Lin al/Sec 2");
        String id = logic.getStudentList().get(0).getId().value;
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(temporaryFolder.resolve("failure.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw new IOException("disk unavailable");
            }
        };
        logic = createLogic(failingStorage);

        CommandException error = assertThrows(CommandException.class, () ->
                logic.execute("delete-student sid/" + id));

        assertEquals("Could not save changes. Student " + id + " was not deleted.", error.getMessage());
        assertEquals(1, model.getStudentList().size());
        assertEquals(id, model.getStudentList().get(0).getId().value);
    }

    @Test
    void execute_editSaveFailure_rollsBackAcademicLevel() throws Exception {
        model.addStudent(new seedu.address.model.student.Student(
                new seedu.address.model.student.StudentId("S-0007"),
                new seedu.address.model.student.StudentName("Mei Lin"),
                new seedu.address.model.student.AcademicLevel("Sec 2")));
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(
                temporaryFolder.resolve("edit-failure.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw new IOException("disk unavailable");
            }
        };
        logic = createLogic(failingStorage);

        CommandException error = assertThrows(CommandException.class, () ->
                logic.execute("edit-student sid/S-0007 al/Sec 3"));

        assertEquals("Could not save changes. Student S-0007 was not updated.", error.getMessage());
        assertEquals("Sec 2", model.getStudentList().get(0).getAcademicLevel().toString());
    }

    @Test
    void execute_saveFailure_rollsBackGuardian() {
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(temporaryFolder.resolve("failure.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw new IOException("disk unavailable");
            }
        };
        logic = createLogic(failingStorage);

        CommandException error = assertThrows(CommandException.class, () ->
                logic.execute("add-guardian n/Anita Lim p/91234567"));

        assertEquals("Could not save changes. No guardian was added.", error.getMessage());
        assertEquals(0, model.getGuardianList().size());
    }

    private Logic createLogic(JsonAddressBookStorage addressBookStorage) {
        JsonUserPrefsStorage prefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"));
        return new LogicManager(model, new StorageManager(addressBookStorage, prefsStorage));
    }
}
