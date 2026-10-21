package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.GuardianLinkTestUtil.getUnlinkedAddressBook;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.group.Group;
import seedu.address.model.group.GroupAssignment;
import seedu.address.model.group.GroupCode;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;

class UnassignGroupCommandTest {
    @Test
    void constructor_nullArgument_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new UnassignGroupCommand(null, new GroupCode("MAT")));
        assertThrows(NullPointerException.class, () -> new UnassignGroupCommand("S-0007", null));
    }

    @Test
    void execute_nullModel_throwsNullPointerException() {
        UnassignGroupCommand command = new UnassignGroupCommand("S-0007", new GroupCode("MAT"));

        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    void execute_subjectAmongSeveralAssignments_removesOnlyRequestedPair() throws Exception {
        ModelManager model = model("MAT", "S2-MAT-A");
        Student mei = model.findStudent("S-0007").orElseThrow();
        Student amy = model.findStudent("S-0008").orElseThrow();
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Group> catalogueBefore = List.copyOf(model.getGroupCatalogue().getGroups());
        UnassignGroupCommand command = new UnassignGroupCommand("S-0007", new GroupCode(" mat "));

        CommandResult result = command.execute(model);

        assertFalse(command.isReadOnly());
        assertEquals("Removed group MAT from student S-0007.", result.getFeedbackToUser());
        assertEquals("S-0007", result.getSelectedStudentId());
        assertEquals(CommandResult.Workspace.STUDENTS, result.getWorkspace());
        assertEquals(Set.of(assignment(mei, "S2-MAT-A"), assignment(amy, "MAT")),
                model.getAddressBook().getGroupAssignments());
        assertEquals(before.getStudentList(), model.getAddressBook().getStudentList());
        assertEquals(before.getGuardianList(), model.getAddressBook().getGuardianList());
        assertEquals(before.getGuardianLinks(), model.getAddressBook().getGuardianLinks());
        assertSame(mei, model.findStudent("S-0007").orElseThrow());
        assertSame(amy, model.findStudent("S-0008").orElseThrow());
        assertEquals(catalogueBefore, model.getGroupCatalogue().getGroups());
    }

    @Test
    void execute_finalClassAssignment_preservesStudentAndOtherAssignments() throws Exception {
        ModelManager model = model("S2-MAT-A");
        Student student = model.findStudent("S-0007").orElseThrow();
        Student otherStudent = model.findStudent("S-0008").orElseThrow();

        CommandResult result = new UnassignGroupCommand("S-0007", new GroupCode("s2-mat-a")).execute(model);

        assertEquals("Removed group S2-MAT-A from student S-0007.", result.getFeedbackToUser());
        assertEquals("S-0007", result.getSelectedStudentId());
        assertEquals(CommandResult.Workspace.STUDENTS, result.getWorkspace());
        assertTrue(model.getAssignedGroups("S-0007").isEmpty());
        assertSame(student, model.findStudent("S-0007").orElseThrow());
        assertEquals(Set.of(assignment(otherStudent, "MAT")), model.getAddressBook().getGroupAssignments());
    }

    @Test
    void execute_unknownReference_reportsExactFeedbackWithoutMutation() {
        ModelManager model = model("MAT");
        Student otherStudent = model.findStudent("S-0008").orElseThrow();
        model.updateFilteredStudentList(student -> student.equals(otherStudent));
        AddressBook before = new AddressBook(model.getAddressBook());
        Map<UnassignGroupCommand, String> invalidCommands = Map.of(
                new UnassignGroupCommand("S-0009", new GroupCode("MAT")),
                "Student S-0009 does not exist.",
                new UnassignGroupCommand("S-00007", new GroupCode("MAT")),
                "Student S-00007 does not exist.",
                new UnassignGroupCommand("S-0007", new GroupCode("missing")),
                "Group MISSING is not configured. Run list-groups to see available groups.");

        for (Map.Entry<UnassignGroupCommand, String> entry : invalidCommands.entrySet()) {
            CommandException error = assertThrows(CommandException.class, () -> entry.getKey().execute(model));
            assertEquals(entry.getValue(), error.getMessage());
            assertEquals(before, model.getAddressBook());
            assertEquals(List.of(otherStudent), model.getStudentList());
        }
    }

    @Test
    void execute_unassignedGroup_preservesModelAndDomainCause() {
        ModelManager model = model("MAT");
        Student otherStudent = model.findStudent("S-0008").orElseThrow();
        model.updateFilteredStudentList(student -> student.equals(otherStudent));
        AddressBook before = new AddressBook(model.getAddressBook());

        CommandException error = assertThrows(CommandException.class, () ->
                new UnassignGroupCommand("S-0007", new GroupCode("s2-mat-a")).execute(model));

        assertEquals("Student S-0007 is not assigned to S2-MAT-A.", error.getMessage());
        assertInstanceOf(IllegalArgumentException.class, error.getCause());
        assertEquals(error.getMessage(), error.getCause().getMessage());
        assertEquals(before, model.getAddressBook());
        assertEquals(List.of(otherStudent), model.getStudentList());
    }

    @Test
    void execute_repeatedRemoval_rejectsWithoutFurtherMutation() throws Exception {
        ModelManager model = model("MAT");
        UnassignGroupCommand command = new UnassignGroupCommand("S-0007", new GroupCode("MAT"));
        command.execute(model);
        AddressBook afterRemoval = new AddressBook(model.getAddressBook());

        CommandException error = assertThrows(CommandException.class, () -> command.execute(model));

        assertEquals("Student S-0007 is not assigned to MAT.", error.getMessage());
        assertEquals(afterRemoval, model.getAddressBook());
    }

    @Test
    void execute_hiddenStudent_resolvesFullRegisterWithoutChangingFilter() throws Exception {
        ModelManager model = model("MAT");
        Student otherStudent = model.findStudent("S-0008").orElseThrow();
        model.updateFilteredStudentList(student -> student.equals(otherStudent));

        CommandResult result = new UnassignGroupCommand("S-0007", new GroupCode("MAT")).execute(model);

        assertTrue(model.getAssignedGroups("S-0007").isEmpty());
        assertEquals(List.of(otherStudent), model.getStudentList());
        assertEquals("S-0007", result.getSelectedStudentId());
        assertEquals(CommandResult.Workspace.STUDENTS, result.getWorkspace());
    }

    @Test
    void execute_hugeUnknownId_doesNotAdvanceGenerator() {
        ModelManager model = model("MAT");
        AddressBook before = new AddressBook(model.getAddressBook());
        long previous = Long.parseLong(StudentId.generate().value.substring(2));
        String unknownId = "S-" + "9".repeat(1000);

        CommandException error = assertThrows(CommandException.class, () ->
                new UnassignGroupCommand(unknownId, new GroupCode("MAT")).execute(model));

        assertEquals("Student " + unknownId + " does not exist.", error.getMessage());
        assertEquals(previous + 1, Long.parseLong(StudentId.generate().value.substring(2)));
        assertEquals(before, model.getAddressBook());
    }

    @Test
    void execute_existingStudent_reusesResolvedId() throws Exception {
        ModelManager original = model("MAT");
        Student student = original.findStudent("S-0007").orElseThrow();
        ModelManager model = new ModelManager(original.getAddressBook(), original.getUserPrefs()) {
            @Override
            public void removeGroupAssignment(GroupAssignment assignment) {
                assertSame(student.getId(), assignment.getStudentId());
                super.removeGroupAssignment(assignment);
            }
        };

        new UnassignGroupCommand("S-0007", new GroupCode("MAT")).execute(model);

        assertTrue(model.getAssignedGroups("S-0007").isEmpty());
    }

    @Test
    void execute_unexpectedModelFailure_doesNotWrapRuntimeException() {
        ModelManager original = model("MAT");
        IllegalStateException failure = new IllegalStateException("Unexpected model failure");
        ModelManager model = new ModelManager(original.getAddressBook(), original.getUserPrefs()) {
            @Override
            public void removeGroupAssignment(GroupAssignment assignment) {
                throw failure;
            }
        };

        IllegalStateException error = assertThrows(IllegalStateException.class, () ->
                new UnassignGroupCommand("S-0007", new GroupCode("MAT")).execute(model));

        assertSame(failure, error);
        assertEquals(original.getAddressBook(), model.getAddressBook());
    }

    private static ModelManager model(String... targetGroupCodes) {
        AddressBook register = getUnlinkedAddressBook();
        Student student = register.findStudent("S-0007").orElseThrow();
        Student otherStudent = register.findStudent("S-0008").orElseThrow();
        register.linkGuardian("S-0007", "G-0003");
        register.linkGuardian("S-0008", "G-0003");
        for (String code : targetGroupCodes) {
            register.addGroupAssignment(assignment(student, code));
        }
        register.addGroupAssignment(assignment(otherStudent, "MAT"));
        return new ModelManager(register, new UserPrefs());
    }

    private static GroupAssignment assignment(Student student, String code) {
        return new GroupAssignment(student.getId(), new GroupCode(code));
    }
}
