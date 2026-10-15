package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

class MainAppAssignmentTest {
    private static final String STUDENTS = "\"students\":[{\"id\":\"S-0007\",\"name\":\"Mei Lin\","
            + "\"academicLevel\":\"Sec 2\"}]";

    @TempDir
    private Path testFolder;

    @Test
    void startup_savedAssignments_surviveNewApplicationAndStorageInstances() throws Exception {
        Files.writeString(testFolder.resolve("students.json"), "{" + STUDENTS + "}");
        StorageManager storage = storage();
        Model first = new MainApp().initModelManager(storage, new UserPrefs());
        LogicManager logic = new LogicManager(first, storage);
        logic.execute("assign-group sid/S-0007 g/MAT");
        logic.execute("assign-group sid/S-0007 g/S2-MAT-A");
        logic.execute("add-guardian n/Anita Lim p/91234567");

        Model restarted = new MainApp().initModelManager(storage(), new UserPrefs());

        assertEquals(first.getAddressBook(), restarted.getAddressBook());
        assertEquals(1, restarted.getGuardianList().size());
        assertEquals("+65 91234567", restarted.getGuardianList().getFirst().getPhone().toString());
        assertEquals(List.of("MAT", "S2-MAT-A"), restarted.getAssignedGroups("S-0007").stream()
                .map(group -> group.getCode().getValue()).toList());
    }

    @Test
    void startup_editedStudent_retainsLevelGuardiansAndAssignmentsAcrossRestart() throws Exception {
        Files.writeString(testFolder.resolve("students.json"), "{" + STUDENTS + "}");
        StorageManager storage = storage();
        Model first = new MainApp().initModelManager(storage, new UserPrefs());
        LogicManager logic = new LogicManager(first, storage);
        logic.execute("assign-group sid/S-0007 g/MAT");
        logic.execute("assign-group sid/S-0007 g/S2-MAT-A");
        logic.execute("add-guardian n/Anita Lim p/91234567");
        logic.execute("add-guardian n/Raj Kumar p/61234567");
        logic.execute("find-student n/Unknown");
        logic.execute("edit-student sid/S-0007 al/Sec 3");

        Model restarted = new MainApp().initModelManager(storage(), new UserPrefs());

        assertEquals(first.getAddressBook(), restarted.getAddressBook());
        assertEquals("Sec 3", restarted.findStudent("S-0007").orElseThrow().getAcademicLevel().toString());
        assertEquals(2, restarted.getGuardianList().size());
        assertEquals(List.of("MAT", "S2-MAT-A"), restarted.getAssignedGroups("S-0007").stream()
                .map(group -> group.getCode().getValue()).toList());
    }

    @Test
    void startup_deletedStudent_staysRemovedWhileUnrelatedRecordsSurvive() throws Exception {
        Files.writeString(testFolder.resolve("students.json"), "{" + STUDENTS + "}");
        StorageManager storage = storage();
        Model first = new MainApp().initModelManager(storage, new UserPrefs());
        LogicManager logic = new LogicManager(first, storage);
        String retainedId = logic.execute("add-student n/Amy Tan al/IP 1").getSelectedStudentId();
        logic.execute("assign-group sid/S-0007 g/MAT");
        logic.execute("assign-group sid/S-0007 g/S2-MAT-A");
        logic.execute("assign-group sid/" + retainedId + " g/ENG");
        logic.execute("add-guardian n/Anita Lim p/91234567");
        logic.execute("add-guardian n/Raj Kumar p/61234567");
        logic.execute("find-student n/Amy");
        logic.execute("delete-student sid/S-0007");

        Model restarted = new MainApp().initModelManager(storage(), new UserPrefs());

        assertEquals(first.getAddressBook(), restarted.getAddressBook());
        assertTrue(restarted.findStudent("S-0007").isEmpty());
        assertTrue(restarted.getAssignedGroups("S-0007").isEmpty());
        assertEquals(1, restarted.getStudentList().size());
        assertEquals(2, restarted.getGuardianList().size());
        assertEquals(List.of("ENG"), restarted.getAssignedGroups(retainedId).stream()
                .map(group -> group.getCode().getValue()).toList());
    }

    @Test
    void startup_invalidAssignments_publishesNoPartialDataAndBlocksAssignment() throws Exception {
        byte[] original = ("{" + STUDENTS + ",\"groupAssignments\":[{\"studentId\":\"S-0007\","
                + "\"groupCode\":\"UNKNOWN\"}]}").getBytes(StandardCharsets.UTF_8);
        Path path = testFolder.resolve("students.json");
        Files.write(path, original);
        StorageManager storage = storage();

        Model model = new MainApp().initModelManager(storage, new UserPrefs());
        CommandException error = assertThrows(CommandException.class, () ->
                new LogicManager(model, storage).execute("assign-group sid/S-0007 g/MAT"));

        assertEquals("Could not load local data. Your original data file was not changed; "
                + "restore or replace it before saving.", error.getMessage());
        assertTrue(model.getStudentList().isEmpty());
        assertTrue(model.getAddressBook().getGroupAssignments().isEmpty());
        assertArrayEquals(original, Files.readAllBytes(path));
    }

    private StorageManager storage() {
        return new StorageManager(new JsonAddressBookStorage(testFolder.resolve("students.json")),
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json")));
    }
}
