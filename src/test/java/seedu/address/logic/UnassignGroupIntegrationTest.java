package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.GuardianLinkTestUtil.getUnlinkedAddressBook;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.group.Group;
import seedu.address.model.group.GroupAssignment;
import seedu.address.model.group.GroupCatalogue;
import seedu.address.model.group.GroupCode;
import seedu.address.model.group.GroupKind;
import seedu.address.model.student.Student;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

class UnassignGroupIntegrationTest {
    @TempDir
    private Path temporaryFolder;

    private ModelManager model;
    private TrackingStorage data;
    private StorageManager storage;
    private Logic logic;
    private Student targetStudent;
    private Student otherStudent;
    private AddressBook originalRegister;
    private List<Group> originalCatalogue;

    @BeforeEach
    void setUp() throws IOException {
        AddressBook register = getUnlinkedAddressBook();
        targetStudent = register.findStudent("S-0007").orElseThrow();
        otherStudent = register.findStudent("S-0008").orElseThrow();
        register.linkGuardian("S-0007", "G-0003");
        register.linkGuardian("S-0008", "G-0003");
        register.addGroupAssignment(assignment(targetStudent, "MAT"));
        register.addGroupAssignment(assignment(targetStudent, "S2-MAT-A"));
        register.addGroupAssignment(assignment(otherStudent, "MAT"));
        model = new ModelManager(register, new UserPrefs());
        originalRegister = new AddressBook(model.getAddressBook());
        originalCatalogue = List.copyOf(model.getGroupCatalogue().getGroups());
        data = new TrackingStorage(temporaryFolder.resolve("register.json"), model);
        // Seed the real file separately so counted writes come only from command execution.
        new JsonAddressBookStorage(data.getAddressBookFilePath()).saveAddressBook(register);
        storage = new StorageManager(data, new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json")));
        logic = new LogicManager(model, storage);
    }

    @Test
    void execute_mixedCaseAndWhitespace_removesSubjectAndPreservesOtherData() throws Exception {
        CommandResult result = logic.execute(" \tUnAsSiGn-GrOuP\tG/ mat\nSiD/ S-0007  ");

        assertSuccess(result, "MAT");
        assertEquals(1, data.saveAttempts);
        assertEquals(Set.of(assignment(targetStudent, "S2-MAT-A"), assignment(otherStudent, "MAT")),
                model.getAddressBook().getGroupAssignments());
        assertPreservedRecords(model.getAddressBook());
        assertSame(targetStudent, model.findStudent("S-0007").orElseThrow());
        assertSame(otherStudent, model.findStudent("S-0008").orElseThrow());
        assertRegisterEquals(model.getAddressBook(), data.readAddressBook().orElseThrow());
    }

    @Test
    void execute_classThenFinalAssignment_persistsZeroAssignmentsForStudent() throws Exception {
        JsonAddressBookStorage realData = new JsonAddressBookStorage(data.getAddressBookFilePath());
        Logic realLogic = new LogicManager(model, new StorageManager(realData,
                new JsonUserPrefsStorage(temporaryFolder.resolve("real-prefs.json"))));

        CommandResult classResult = realLogic.execute("UNASSIGN-GROUP g/s2-mat-a SID/S-0007");

        assertSuccess(classResult, "S2-MAT-A");
        ReadOnlyAddressBook afterClass = realData.readAddressBook().orElseThrow();
        assertEquals(Set.of(assignment(targetStudent, "MAT"), assignment(otherStudent, "MAT")),
                afterClass.getGroupAssignments());
        assertPreservedRecords(afterClass);

        CommandResult finalResult = realLogic.execute("unassign-group sid/S-0007 g/MAT");

        assertSuccess(finalResult, "MAT");
        ReadOnlyAddressBook reloaded = new JsonAddressBookStorage(realData.getAddressBookFilePath())
                .readAddressBook().orElseThrow();
        assertEquals(Set.of(assignment(otherStudent, "MAT")), reloaded.getGroupAssignments());
        assertPreservedRecords(reloaded);
        assertRegisterEquals(model.getAddressBook(), reloaded);
        ModelManager restarted = new ModelManager(reloaded, new UserPrefs());
        assertTrue(restarted.getAssignedGroups("S-0007").isEmpty());
        assertEquals(targetStudent, restarted.findStudent("S-0007").orElseThrow());
        assertEquals(List.of("MAT"), restarted.getAssignedGroups("S-0008").stream()
                .map(group -> group.getCode().getValue()).toList());

        JsonNode json = new ObjectMapper().readTree(Files.readString(realData.getAddressBookFilePath()));
        Set<String> properties = new HashSet<>();
        json.fieldNames().forEachRemaining(properties::add);
        assertEquals(Set.of("students", "guardians", "groups", "guardianLinks", "groupAssignments"), properties);
        properties.forEach(property -> assertTrue(json.get(property).isArray(), property));
    }

    @Test
    void execute_customCatalogue_preservesExactCatalogueAfterReload() throws Exception {
        List<Group> expectedCatalogue = new ArrayList<>(originalCatalogue);
        expectedCatalogue.add(new Group(new GroupCode("HIS"), "History enrichment", GroupKind.SUBJECT));
        AddressBook expectedRegister = new AddressBook(originalRegister);
        expectedRegister.setGroups(expectedCatalogue);
        model.setAddressBook(expectedRegister);
        JsonAddressBookStorage realData = new JsonAddressBookStorage(data.getAddressBookFilePath());
        realData.saveAddressBook(expectedRegister);

        CommandResult result = logic.execute("unassign-group sid/S-0007 g/MAT");
        ReadOnlyAddressBook reloaded = realData.readAddressBook().orElseThrow();

        assertSuccess(result, "MAT");
        assertEquals(1, data.saveAttempts);
        assertEquals(Set.of(assignment(targetStudent, "S2-MAT-A"), assignment(otherStudent, "MAT")),
                reloaded.getGroupAssignments());
        assertEquals(expectedCatalogue, reloaded.getGroupCatalogue().getGroups());
        assertEquals(originalRegister.getStudentList(), reloaded.getStudentList());
        assertEquals(originalRegister.getGuardianList(), reloaded.getGuardianList());
        assertEquals(originalRegister.getGuardianLinks(), reloaded.getGuardianLinks());
    }

    @Test
    void execute_successThenReadOnlyAndRepeatedRemoval_savesOnlyOnce() throws Exception {
        logic.execute("unassign-group sid/S-0007 g/MAT");
        assertEquals(1, data.saveAttempts);

        assertEquals("Listed 5 configured group(s).", logic.execute("list-groups").getFeedbackToUser());
        assertTrue(logic.execute("help").isShowHelp());
        assertTrue(logic.execute("exit").isExit());
        assertRejectedCommand("unassign-group sid/S-0007 g/MAT", "Student S-0007 is not assigned to MAT.");

        assertEquals(1, data.saveAttempts);
    }

    @Test
    void execute_unknownStudent_rejectsWithoutSavingOrReveal() throws Exception {
        assertRejectedCommand("unassign-group sid/S-9999 g/MAT", "Student S-9999 does not exist.");
    }

    @Test
    void execute_unknownGroup_reportsGroupErrorBeforeMembership() throws Exception {
        assertRejectedCommand("unassign-group sid/S-0007 g/unknown",
                "Group UNKNOWN is not configured. Run list-groups to see available groups.");
    }

    @Test
    void execute_unassignedConfiguredGroup_rejectsWithoutSavingOrReveal() throws Exception {
        assertRejectedCommand("unassign-group sid/S-0007 g/SCI", "Student S-0007 is not assigned to SCI.");
    }

    @Test
    void execute_unknownStudentAndGroup_reportsStudentErrorFirst() throws Exception {
        assertRejectedCommand("unassign-group sid/S-9999 g/UNKNOWN", "Student S-9999 does not exist.");
    }

    @Test
    void execute_malformedInput_rejectsWithoutSavingOrReveal() throws Exception {
        hideTarget();
        byte[] originalBytes = Files.readAllBytes(data.getAddressBookFilePath());

        ParseException error = assertThrows(ParseException.class, () ->
                logic.execute("unassign-group sid/S-0007"));

        assertEquals("Invalid command format. Use: unassign-group sid/STUDENT_ID g/GROUP_CODE", error.getMessage());
        assertRegisterEquals(originalRegister, model.getAddressBook());
        assertEquals(List.of(otherStudent), model.getStudentList());
        assertEquals(0, data.saveAttempts);
        assertArrayEquals(originalBytes, Files.readAllBytes(data.getAddressBookFilePath()));
    }

    @Test
    void execute_ioFailureBeforeWrite_rollsBackAndAllowsRetry() throws Exception {
        assertPreWriteFailureAndRetry(new IOException("Synthetic disk failure"));
    }

    @Test
    void execute_accessDeniedBeforeWrite_rollsBackAndAllowsRetry() throws Exception {
        assertPreWriteFailureAndRetry(new AccessDeniedException("Synthetic denied path"));
    }

    @Test
    void execute_recovery_blocksMutationAndRetainsReadOnlyAccess() throws Exception {
        hideTarget();
        byte[] malformedBytes = enterRecovery();
        String reason = storage.getDataRecoveryReason().orElseThrow();

        CommandException error = assertThrows(CommandException.class, () ->
                logic.execute("unassign-group sid/S-0007 g/MAT"));

        assertEquals(DataLoadingException.MESSAGE_RECOVERY, error.getMessage());
        assertRegisterEquals(originalRegister, model.getAddressBook());
        assertEquals(List.of(otherStudent), model.getStudentList());
        assertEquals("Listed 5 configured group(s).", logic.execute("list-groups").getFeedbackToUser());
        assertEquals(CommandResult.Workspace.GUARDIANS, logic.execute("list-guardians").getWorkspace());
        assertTrue(logic.execute("help").isShowHelp());
        assertTrue(logic.execute("exit").isExit());
        assertEquals(reason, storage.getDataRecoveryReason().orElseThrow());
        assertRegisterEquals(originalRegister, model.getAddressBook());
        assertEquals(0, data.saveAttempts);
        assertArrayEquals(malformedBytes, Files.readAllBytes(data.getAddressBookFilePath()));
    }

    @Test
    void execute_recovery_precedesLookupButFollowsParsing() throws Exception {
        byte[] malformedBytes = enterRecovery();
        String reason = storage.getDataRecoveryReason().orElseThrow();
        ModelManager guardedModel = new ModelManager(originalRegister, new UserPrefs()) {
            @Override
            public Optional<Student> findStudent(String studentId) {
                throw new AssertionError("Recovery must block student lookup.");
            }

            @Override
            public GroupCatalogue getGroupCatalogue() {
                throw new AssertionError("Recovery must block catalogue lookup.");
            }

            @Override
            public void removeGroupAssignment(GroupAssignment assignment) {
                throw new AssertionError("Recovery must block mutation.");
            }
        };
        Logic guardedLogic = new LogicManager(guardedModel, storage);

        CommandException recoveryError = assertThrows(CommandException.class, () ->
                guardedLogic.execute("unassign-group sid/S-9999 g/UNKNOWN"));
        ParseException parseError = assertThrows(ParseException.class, () ->
                guardedLogic.execute("unassign-group sid/S-0007"));

        assertEquals(DataLoadingException.MESSAGE_RECOVERY, recoveryError.getMessage());
        assertEquals("Invalid command format. Use: unassign-group sid/STUDENT_ID g/GROUP_CODE",
                parseError.getMessage());
        assertRegisterEquals(originalRegister, guardedModel.getAddressBook());
        assertEquals(reason, storage.getDataRecoveryReason().orElseThrow());
        assertEquals(0, data.saveAttempts);
        assertArrayEquals(malformedBytes, Files.readAllBytes(data.getAddressBookFilePath()));
    }

    @Test
    void execute_hiddenTarget_revealsOnlyAfterSuccessfulSave() throws Exception {
        hideTarget();

        CommandResult result = logic.execute("unassign-group sid/S-0007 g/MAT");

        assertSuccess(result, "MAT");
        assertEquals(List.of(otherStudent), data.visibleStudentsAtSave);
        assertEquals(List.of(targetStudent, otherStudent), model.getStudentList());
        assertEquals(1, data.saveAttempts);
        assertEquals(Set.of(assignment(targetStudent, "S2-MAT-A"), assignment(otherStudent, "MAT")),
                data.attemptedRegister.getGroupAssignments());
        assertRegisterEquals(model.getAddressBook(), data.readAddressBook().orElseThrow());
    }

    @Test
    void execute_visibleTarget_retainsExistingFilter() throws Exception {
        model.updateFilteredStudentList(student -> student.equals(targetStudent));

        CommandResult result = logic.execute("unassign-group sid/S-0007 g/MAT");

        assertSuccess(result, "MAT");
        assertEquals(List.of(targetStudent), data.visibleStudentsAtSave);
        assertEquals(List.of(targetStudent), model.getStudentList());
        assertEquals(1, data.saveAttempts);
        assertRegisterEquals(model.getAddressBook(), data.readAddressBook().orElseThrow());
    }

    private void assertRejectedCommand(String commandText, String expectedMessage) throws Exception {
        hideTarget();
        AddressBook before = new AddressBook(model.getAddressBook());
        byte[] originalBytes = Files.readAllBytes(data.getAddressBookFilePath());
        int savesBefore = data.saveAttempts;

        CommandException error = assertThrows(CommandException.class, () -> logic.execute(commandText));

        assertEquals(expectedMessage, error.getMessage());
        assertRegisterEquals(before, model.getAddressBook());
        assertEquals(List.of(otherStudent), model.getStudentList());
        assertEquals(savesBefore, data.saveAttempts);
        assertArrayEquals(originalBytes, Files.readAllBytes(data.getAddressBookFilePath()));
    }

    private void assertPreWriteFailureAndRetry(IOException failure) throws Exception {
        hideTarget();
        byte[] originalBytes = Files.readAllBytes(data.getAddressBookFilePath());
        data.failure = failure;

        CommandException error = assertThrows(CommandException.class, () ->
                logic.execute("unassign-group sid/S-0007 g/S2-MAT-A"));

        assertEquals("Could not save changes. The group assignment was not removed.", error.getMessage());
        assertSame(failure, error.getCause());
        assertEquals(1, data.saveAttempts);
        assertRegisterEquals(originalRegister, model.getAddressBook());
        assertSame(targetStudent, model.findStudent("S-0007").orElseThrow());
        assertEquals(List.of(otherStudent), model.getStudentList());
        assertEquals(List.of(otherStudent), data.visibleStudentsAtSave);
        Set<GroupAssignment> expectedAssignments = Set.of(assignment(targetStudent, "MAT"),
                assignment(otherStudent, "MAT"));
        assertEquals(expectedAssignments, data.attemptedRegister.getGroupAssignments());
        // This double throws before writing; unchanged bytes do not establish atomic disk writes.
        assertArrayEquals(originalBytes, Files.readAllBytes(data.getAddressBookFilePath()));

        data.failure = null;
        CommandResult retry = logic.execute("unassign-group sid/S-0007 g/S2-MAT-A");

        assertSuccess(retry, "S2-MAT-A");
        assertEquals(2, data.saveAttempts);
        assertEquals(List.of(otherStudent), data.visibleStudentsAtSave);
        assertEquals(List.of(targetStudent, otherStudent), model.getStudentList());
        assertEquals(expectedAssignments, model.getAddressBook().getGroupAssignments());
        assertPreservedRecords(model.getAddressBook());
        assertRegisterEquals(model.getAddressBook(), data.readAddressBook().orElseThrow());
    }

    private byte[] enterRecovery() throws IOException {
        byte[] malformedBytes = "{invalid}\r\n".getBytes(StandardCharsets.UTF_8);
        Files.write(data.getAddressBookFilePath(), malformedBytes);
        assertThrows(DataLoadingException.class, storage::readAddressBook);
        return malformedBytes;
    }

    private void hideTarget() {
        model.updateFilteredStudentList(student -> student.equals(otherStudent));
    }

    private void assertPreservedRecords(ReadOnlyAddressBook actual) {
        assertEquals(originalRegister.getStudentList(), actual.getStudentList());
        assertEquals(originalRegister.getGuardianList(), actual.getGuardianList());
        assertEquals(originalRegister.getGuardianLinks(), actual.getGuardianLinks());
        assertEquals(originalCatalogue, actual.getGroupCatalogue().getGroups());
    }

    private static void assertRegisterEquals(ReadOnlyAddressBook expected, ReadOnlyAddressBook actual) {
        assertEquals(expected.getStudentList(), actual.getStudentList());
        assertEquals(expected.getGuardianList(), actual.getGuardianList());
        assertEquals(expected.getGuardianLinks(), actual.getGuardianLinks());
        assertEquals(expected.getGroupAssignments(), actual.getGroupAssignments());
        assertEquals(expected.getGroupCatalogue(), actual.getGroupCatalogue());
    }

    private static void assertSuccess(CommandResult result, String groupCode) {
        assertEquals("Removed group " + groupCode + " from student S-0007.", result.getFeedbackToUser());
        assertEquals("S-0007", result.getSelectedStudentId());
        assertEquals(CommandResult.Workspace.STUDENTS, result.getWorkspace());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
        assertFalse(result.shouldClearStudentSelection());
    }

    private static GroupAssignment assignment(Student student, String groupCode) {
        return new GroupAssignment(student.getId(), new GroupCode(groupCode));
    }

    /** Records the save boundary and optionally fails before any file write. */
    private static class TrackingStorage extends JsonAddressBookStorage {
        private final ModelManager model;
        private int saveAttempts;
        private IOException failure;
        private List<Student> visibleStudentsAtSave;
        private AddressBook attemptedRegister;

        TrackingStorage(Path path, ModelManager model) {
            super(path);
            this.model = model;
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            saveAttempts++;
            visibleStudentsAtSave = List.copyOf(model.getStudentList());
            attemptedRegister = new AddressBook(addressBook);
            if (failure != null) {
                throw failure;
            }
            super.saveAddressBook(addressBook);
        }
    }
}
