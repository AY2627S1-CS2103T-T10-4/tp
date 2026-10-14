package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.group.GroupAssignment;
import seedu.address.model.group.GroupCode;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

class AssignGroupIntegrationTest {
    @TempDir
    private Path testFolder;

    @Test
    void execute_assignGroup_returnsExactSuccessAndPreservesStudent() throws Exception {
        ModelManager model = new ModelManager();
        Student student = new Student(new StudentId("S-0007"), new StudentName("Mei Lin"), new AcademicLevel("Sec 2"));
        model.addStudent(student);
        Logic logic = new LogicManager(model, new StorageManager(
                new JsonAddressBookStorage(testFolder.resolve("students.json")),
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json"))));

        CommandResult result = logic.execute("assign-group sid/S-0007 g/S2-MAT-A");

        assertEquals("Assigned group S2-MAT-A (Secondary 2 Mathematics A) to student S-0007.",
                result.getFeedbackToUser());
        assertEquals(List.of(student), model.getStudentList());
        assertEquals(Set.of(new GroupAssignment(student.getId(), new GroupCode("S2-MAT-A"))),
                model.getAddressBook().getGroupAssignments());
        assertEquals(model.getAddressBook(), new JsonAddressBookStorage(testFolder.resolve("students.json"))
                .readAddressBook().orElseThrow());
    }

    @Test
    void execute_assignmentOnlyChange_isUnequalToSnapshotAndSavedExactlyOnce() throws Exception {
        ModelManager model = new ModelManager();
        Student student = new Student(new StudentId("S-0007"), new StudentName("Mei Lin"), new AcademicLevel("Sec 2"));
        model.addStudent(student);
        AddressBook before = new AddressBook(model.getAddressBook());
        int[] saves = {0};
        JsonAddressBookStorage data = new JsonAddressBookStorage(testFolder.resolve("counted.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                saves[0]++;
                super.saveAddressBook(addressBook);
            }
        };
        Logic logic = new LogicManager(model, new StorageManager(data,
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json"))));

        logic.execute("assign-group sid/S-0007 g/MAT");

        assertEquals(before.getStudentList(), model.getStudentList());
        assertNotEquals(before, model.getAddressBook());
        assertEquals(model.getAddressBook(), data.readAddressBook().orElseThrow());
        assertEquals(1, saves[0]);
        assertThrows(CommandException.class, () -> logic.execute("assign-group sid/S-0007 g/mat"));
        logic.execute("list-groups");
        logic.execute("help");
        logic.execute("exit");
        assertEquals(1, saves[0]);
    }

    @Test
    void execute_saveFailure_restoresCompleteModelAndExactFeedback() throws Exception {
        for (IOException failure : new IOException[] {new IOException("disk unavailable"),
            new AccessDeniedException("synthetic denied path")}) {
            ModelManager model = new ModelManager();
            Student student = new Student(new StudentId("S-0007"), new StudentName("Mei Lin"),
                    new AcademicLevel("Sec 2"));
            model.addStudent(student);
            model.addGroupAssignment(new GroupAssignment(student.getId(), new GroupCode("MAT")));
            AddressBook before = new AddressBook(model.getAddressBook());
            Path path = testFolder.resolve("before-failure.json");
            new JsonAddressBookStorage(path).saveAddressBook(before);
            byte[] original = Files.readAllBytes(path);
            JsonAddressBookStorage failing = new JsonAddressBookStorage(path) {
                @Override
                public void saveAddressBook(ReadOnlyAddressBook ignored) throws IOException {
                    throw failure;
                }
            };
            Logic logic = new LogicManager(model, new StorageManager(failing,
                    new JsonUserPrefsStorage(testFolder.resolve("prefs.json"))));

            CommandException error = assertThrows(CommandException.class, () ->
                    logic.execute("assign-group sid/S-0007 g/SCI"));

            assertEquals("Could not save changes. The group was not assigned.", error.getMessage());
            assertEquals(before, model.getAddressBook());
            assertArrayEquals(original, Files.readAllBytes(path));
        }
    }
}
