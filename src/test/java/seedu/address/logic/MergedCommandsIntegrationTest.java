package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.group.GroupAssignment;
import seedu.address.model.group.GroupCode;
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

class MergedCommandsIntegrationTest {
    @TempDir
    private Path testFolder;

    private ModelManager model;
    private JsonAddressBookStorage data;
    private LogicManager logic;
    private Student mei;
    private Student amy;

    @BeforeEach
    void setUp() throws IOException {
        model = new ModelManager();
        mei = new Student(new StudentId("S-9301"), new StudentName("Mei Lin"), new AcademicLevel("Sec 2"));
        amy = new Student(new StudentId("S-9302"), new StudentName("Amy Tan"), new AcademicLevel("Sec 3"));
        model.addStudent(mei);
        model.addStudent(amy);
        model.addGuardian(new Guardian(new GuardianId("G-9301"), new GuardianName("Anita Lim"),
                new GuardianPhone("91234567")));
        model.addGroupAssignment(new GroupAssignment(mei.getId(), new GroupCode("MAT")));
        data = new JsonAddressBookStorage(testFolder.resolve("students.json"));
        data.saveAddressBook(model.getAddressBook());
        logic = createLogic(model, data);
    }

    @Test
    void assignmentsAndGuardianAdds_preserveEachOthersRecordsAndSaveOnce() throws Exception {
        int[] saves = {0};
        JsonAddressBookStorage counted = new JsonAddressBookStorage(data.getAddressBookFilePath()) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook records) throws IOException {
                saves[0]++;
                super.saveAddressBook(records);
            }
        };
        logic = createLogic(model, counted);
        List<Guardian> initialGuardians = List.copyOf(model.getGuardianList());

        logic.execute("assign-group sid/S-9302 g/SCI");
        assertEquals(initialGuardians, data.readAddressBook().orElseThrow().getGuardianList());
        assertEquals(1, saves[0]);
        logic.execute("add-guardian n/Raj Kumar p/61234567");
        assertEquals(2, saves[0]);
        ReadOnlyAddressBook loaded = data.readAddressBook().orElseThrow();
        assertEquals(model.getAddressBook(), loaded);
        assertEquals(2, loaded.getGuardianList().size());
        assertEquals(2, loaded.getGroupAssignments().size());

        byte[] beforeQueries = Files.readAllBytes(data.getAddressBookFilePath());
        for (String query : List.of("view-student sid/S-9302", "find-student n/Mei", "list-students",
                "list-groups", "help", "exit")) {
            logic.execute(query);
        }
        assertEquals(2, saves[0]);
        assertArrayEquals(beforeQueries, Files.readAllBytes(data.getAddressBookFilePath()));
    }

    @Test
    void viewHiddenStudent_usesFullRegisterAndRevealsTargetWithoutSaving() throws Exception {
        logic.execute("find-student n/Mei");
        assertEquals(List.of(mei), model.getStudentList());
        byte[] before = Files.readAllBytes(data.getAddressBookFilePath());
        logic.execute("view-student sid/S-9301");
        assertEquals(List.of(mei), model.getStudentList());

        CommandResult result = logic.execute("ViEw-StUdEnT SID/S-9302");

        assertEquals("Opened profile for S-9302.", result.getFeedbackToUser());
        assertEquals("S-9302", result.getSelectedStudentId());
        assertEquals(CommandResult.Workspace.UNCHANGED, result.getWorkspace());
        assertEquals(List.of(amy, mei), model.getStudentList());
        assertArrayEquals(before, Files.readAllBytes(data.getAddressBookFilePath()));
    }

    @Test
    void assignAndAddWhileFiltered_revealTargetsAndPersistCompleteRegister() throws Exception {
        logic.execute("find-student n/Mei");
        CommandResult assignment = logic.execute("assign-group g/SCI sid/S-9302");
        assertEquals("S-9302", assignment.getSelectedStudentId());
        assertEquals(List.of(amy, mei), model.getStudentList());
        assertEquals(2, data.readAddressBook().orElseThrow().getStudentList().size());

        logic.execute("find-student n/Mei");
        CommandResult added = logic.execute("add-student n/Zara Lim al/Sec 4");
        assertEquals(3, model.getStudentList().size());
        assertEquals("Zara Lim", model.findStudent(added.getSelectedStudentId()).orElseThrow().getName().toString());
        ReadOnlyAddressBook loaded = data.readAddressBook().orElseThrow();
        assertEquals(model.getAddressBook(), loaded);
        assertEquals(3, loaded.getStudentList().size());
        assertEquals(1, loaded.getGuardianList().size());
        assertEquals(2, loaded.getGroupAssignments().size());
    }

    @Test
    void queriesDuringRecovery_executeWithoutWritingOrClearingReason() throws Exception {
        byte[] original = "invalid synthetic data".getBytes(StandardCharsets.UTF_8);
        Files.write(data.getAddressBookFilePath(), original);
        StorageManager storage = new StorageManager(data, new JsonUserPrefsStorage(testFolder.resolve("prefs.json")));
        assertThrows(DataLoadingException.class, storage::readAddressBook);
        LogicManager recovery = new LogicManager(new ModelManager(), storage);
        String reason = recovery.getDataRecoveryReason().orElseThrow();

        assertEquals("No students registered.", recovery.execute("list-students").getFeedbackToUser());
        assertEquals("No students match \"Mei\".", recovery.execute("find-student n/Mei").getFeedbackToUser());
        CommandException missing = assertThrows(CommandException.class, () ->
                recovery.execute("view-student sid/S-9301"));
        assertEquals("Student S-9301 does not exist.", missing.getMessage());
        assertEquals("Listed 5 configured group(s).", recovery.execute("list-groups").getFeedbackToUser());
        recovery.execute("help");
        recovery.execute("exit");
        for (String mutation : List.of("add-student n/Zara Lim al/Sec 4", "add-guardian n/Raj Kumar p/61234567",
                "assign-group sid/S-9301 g/SCI")) {
            CommandException error = assertThrows(CommandException.class, () -> recovery.execute(mutation));
            assertEquals(DataLoadingException.MESSAGE_RECOVERY, error.getMessage());
        }
        assertEquals(reason, recovery.getDataRecoveryReason().orElseThrow());
        assertArrayEquals(original, Files.readAllBytes(data.getAddressBookFilePath()));
    }

    @Test
    void failedMutations_restoreMixedStateAndKeepActiveFilter() throws Exception {
        logic.execute("find-student n/Mei");
        AddressBook before = new AddressBook(model.getAddressBook());
        byte[] original = Files.readAllBytes(data.getAddressBookFilePath());
        String[] commands = {"add-student n/Zara Lim al/Sec 4", "add-guardian n/Raj Kumar p/61234567",
            "assign-group sid/S-9302 g/SCI"};
        String[] messages = {"Could not save changes. No student was added.",
            "Could not save changes. No guardian was added.", "Could not save changes. The group was not assigned."};
        for (IOException failure : List.of(new IOException("synthetic pre-write failure"),
                new AccessDeniedException("synthetic denied path"))) {
            JsonAddressBookStorage failed = new JsonAddressBookStorage(data.getAddressBookFilePath()) {
                @Override
                public void saveAddressBook(ReadOnlyAddressBook records) throws IOException {
                    throw failure;
                }
            };
            LogicManager failingLogic = createLogic(model, failed);
            for (int i = 0; i < commands.length; i++) {
                String command = commands[i];
                CommandException error = assertThrows(CommandException.class, () -> failingLogic.execute(command));
                assertEquals(messages[i], error.getMessage());
                assertEquals(before, model.getAddressBook());
                assertEquals(List.of(mei), model.getStudentList());
                assertArrayEquals(original, Files.readAllBytes(data.getAddressBookFilePath()));
            }
        }
    }

    @Test
    void unknownIdAfterFiltering_doesNotAdvanceStudentIdGenerator() throws Exception {
        logic.execute("find-student n/Mei");
        String unknown = "S-999999999999999999999999";
        long before = Long.parseLong(StudentId.generate().value.substring(2));
        CommandException error = assertThrows(CommandException.class, () ->
                logic.execute("view-student sid/" + unknown));
        assertEquals("Student " + unknown + " does not exist.", error.getMessage());
        assertEquals(before + 1, Long.parseLong(StudentId.generate().value.substring(2)));
        assertEquals(List.of(mei), model.getStudentList());
    }

    private LogicManager createLogic(ModelManager target, JsonAddressBookStorage storage) {
        return new LogicManager(target, new StorageManager(storage,
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json"))));
    }
}
