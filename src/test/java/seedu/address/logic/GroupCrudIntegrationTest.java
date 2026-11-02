package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

class GroupCrudIntegrationTest {
    @TempDir
    private Path testFolder;

    @Test
    void execute_addThenDeleteGroup_persistsBothChangesAndRoutesWorkspace() throws Exception {
        Path dataPath = testFolder.resolve("groups.json");
        ModelManager model = new ModelManager();
        Logic logic = logic(model, new JsonAddressBookStorage(dataPath));

        CommandResult added = logic.execute("ADD-GROUP k/subject l/Physics g/phy");

        assertEquals("Created group PHY (Physics, Subject).", added.getFeedbackToUser());
        assertEquals("PHY", added.getSelectedGroupCode());
        assertEquals(CommandResult.Workspace.GROUPS, added.getWorkspace());
        assertEquals(model.getAddressBook(), new JsonAddressBookStorage(dataPath).readAddressBook().orElseThrow());

        CommandResult deleted = logic.execute("delete-group g/phy");

        assertEquals("Deleted group PHY: Physics (Subject).", deleted.getFeedbackToUser());
        assertEquals(CommandResult.Workspace.GROUPS, deleted.getWorkspace());
        assertEquals(5, model.getGroupCatalogue().size());
        assertEquals(model.getAddressBook(), new JsonAddressBookStorage(dataPath).readAddressBook().orElseThrow());
    }

    @Test
    void execute_createdGroup_isImmediatelyAssignableAndProtectedFromDeletion() throws Exception {
        Path dataPath = testFolder.resolve("group-availability.json");
        ModelManager model = new ModelManager();
        Student student = new Student(new StudentId("S-1001"), new StudentName("Mei Lin"),
                new AcademicLevel("Sec 2"));
        model.addStudent(student);
        Logic logic = logic(model, new JsonAddressBookStorage(dataPath));

        logic.execute("add-group g/PHY l/Physics k/Subject");
        assertEquals(0, model.getAddressBook().getGroupAssignments().size());
        logic.execute("assign-group sid/S-1001 g/phy");
        logic.execute("filter-students g/PHY");
        assertEquals(java.util.List.of(student), logic.getStudentList());
        CommandException assigned = assertThrows(CommandException.class, () ->
                logic.execute("delete-group g/PHY"));
        assertEquals("Group PHY is assigned to 1 student(s). Remove those assignments before deleting it.",
                assigned.getMessage());

        logic.execute("unassign-group sid/S-1001 g/PHY");
        logic.execute("delete-group g/phy");
        assertEquals(student, model.findStudent("S-1001").orElseThrow());
        assertEquals(model.getAddressBook(), new JsonAddressBookStorage(dataPath).readAddressBook().orElseThrow());
    }

    @Test
    void execute_addSaveFailure_restoresCatalogueAndPreservesFile() throws Exception {
        Path dataPath = testFolder.resolve("failure.json");
        ModelManager model = new ModelManager();
        JsonAddressBookStorage realStorage = new JsonAddressBookStorage(dataPath);
        realStorage.saveAddressBook(model.getAddressBook());
        byte[] original = Files.readAllBytes(dataPath);
        AddressBook before = new AddressBook(model.getAddressBook());
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(dataPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook ignored) throws IOException {
                throw new IOException("synthetic failure");
            }
        };

        CommandException error = assertThrows(CommandException.class, () ->
                logic(model, failingStorage).execute("add-group g/PHY l/Physics k/Subject"));

        assertEquals("Could not save changes. No group was created.", error.getMessage());
        assertEquals(before, model.getAddressBook());
        assertArrayEquals(original, Files.readAllBytes(dataPath));
    }

    @Test
    void execute_deleteSaveFailure_restoresCatalogueAndPreservesFile() throws Exception {
        Path dataPath = testFolder.resolve("delete-failure.json");
        ModelManager model = new ModelManager();
        Logic setup = logic(model, new JsonAddressBookStorage(dataPath));
        setup.execute("add-group g/PHY l/Physics k/Subject");
        byte[] original = Files.readAllBytes(dataPath);
        AddressBook before = new AddressBook(model.getAddressBook());
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(dataPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook ignored) throws IOException {
                throw new IOException("synthetic failure");
            }
        };

        CommandException error = assertThrows(CommandException.class, () ->
                logic(model, failingStorage).execute("delete-group g/PHY"));

        assertEquals("Could not save changes. Group PHY was not deleted.", error.getMessage());
        assertEquals(before, model.getAddressBook());
        assertArrayEquals(original, Files.readAllBytes(dataPath));
    }

    private Logic logic(ModelManager model, JsonAddressBookStorage dataStorage) {
        return new LogicManager(model, new StorageManager(dataStorage,
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json"))));
    }
}
