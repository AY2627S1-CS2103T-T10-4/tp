package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.group.Group;
import seedu.address.model.group.GroupAssignment;
import seedu.address.model.group.GroupCode;
import seedu.address.model.group.GroupKind;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;

class DeleteGroupCommandTest {
    @Test
    void execute_unassignedGroup_deletesOnlyGroup() throws Exception {
        ModelManager model = new ModelManager();
        model.addGroup(new Group(new GroupCode("PHY"), "Physics", GroupKind.SUBJECT));

        CommandResult result = new DeleteGroupCommand(new GroupCode("phy")).execute(model);

        assertEquals("Deleted group PHY: Physics (Subject).", result.getFeedbackToUser());
        assertEquals(CommandResult.Workspace.GROUPS, result.getWorkspace());
        assertEquals(5, model.getGroupCatalogue().size());
    }

    @Test
    void execute_assignedGroup_rejectsAndPreservesGroupAndAssignment() {
        ModelManager model = new ModelManager();
        Student student = new Student(new StudentId("S-0007"), new StudentName("Mei Lin"),
                new AcademicLevel("Sec 2"));
        model.addStudent(student);
        model.addGroupAssignment(new GroupAssignment(student.getId(), new GroupCode("MAT")));

        CommandException error = assertThrows(CommandException.class, () ->
                new DeleteGroupCommand(new GroupCode("MAT")).execute(model));

        assertEquals("Group MAT is assigned to 1 student(s). Remove those assignments before deleting it.",
                error.getMessage());
        assertEquals(5, model.getGroupCatalogue().size());
        assertEquals(1, model.getAddressBook().getGroupAssignments().size());
    }

    @Test
    void execute_unknownGroup_reportsExistingSharedMessage() {
        CommandException error = assertThrows(CommandException.class, () ->
                new DeleteGroupCommand(new GroupCode("PHY")).execute(new ModelManager()));

        assertEquals("Group PHY is not configured. Run list-groups to see available groups.", error.getMessage());
    }

    @Test
    void equalsAndHashCode_useCanonicalGroupCode() {
        DeleteGroupCommand command = new DeleteGroupCommand(new GroupCode("PHY"));
        DeleteGroupCommand equivalent = new DeleteGroupCommand(new GroupCode("phy"));
        DeleteGroupCommand different = new DeleteGroupCommand(new GroupCode("CHEM"));

        assertEquals(command, command);
        assertEquals(command, equivalent);
        assertEquals(command.hashCode(), equivalent.hashCode());
        assertNotEquals(command, different);
        assertNotEquals(command, null);
    }
}
