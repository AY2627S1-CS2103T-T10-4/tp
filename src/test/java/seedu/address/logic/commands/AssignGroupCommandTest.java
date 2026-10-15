package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.group.GroupCode;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;

class AssignGroupCommandTest {
    @Test
    void execute_duplicateAndUnknownGroup_reportExactFeedbackWithoutMutation() throws Exception {
        ModelManager model = model();
        AssignGroupCommand command = new AssignGroupCommand("S-0601", new GroupCode("MAT"));
        assertFalse(command.isReadOnly());
        assertEquals("Assigned group MAT (Mathematics) to student S-0601.", command.execute(model).getFeedbackToUser());
        AddressBook before = new AddressBook(model.getAddressBook());

        CommandException duplicate = assertThrows(CommandException.class, () ->
                new AssignGroupCommand("S-0601", new GroupCode("mat")).execute(model));
        CommandException unknownGroup = assertThrows(CommandException.class, () ->
                new AssignGroupCommand("S-0601", new GroupCode("missing")).execute(model));

        assertEquals("Student S-0601 is already assigned to MAT.", duplicate.getMessage());
        assertEquals("Group MISSING is not configured. Run list-groups to see available groups.",
                unknownGroup.getMessage());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    void execute_nonexistentId_doesNotAdvanceGeneratorDuringParsingOrLookup() throws Exception {
        ModelManager model = model();
        long previous = Long.parseLong(StudentId.generate().value.substring(2));
        String nonexistent = "S-999999999999999999999999";
        Command command = new AddressBookParser().parseCommand("assign-group sid/" + nonexistent + " g/MAT");

        CommandException error = assertThrows(CommandException.class, () -> command.execute(model));

        assertEquals("Student " + nonexistent + " does not exist.", error.getMessage());
        assertEquals(previous + 1, Long.parseLong(StudentId.generate().value.substring(2)));
        assertEquals(0, model.getAddressBook().getGroupAssignments().size());
    }

    @Test
    void execute_studentHiddenFromVisibleList_resolvesAuthoritativeRegister() throws Exception {
        ModelManager model = new ModelManager() {
            @Override
            public ObservableList<Student> getStudentList() {
                return FXCollections.emptyObservableList();
            }
        };
        model.addStudent(student());

        new AssignGroupCommand("S-0601", new GroupCode("MAT")).execute(model);

        assertEquals(1, model.getAddressBook().getGroupAssignments().size());
    }

    private static ModelManager model() {
        ModelManager model = new ModelManager();
        model.addStudent(student());
        return model;
    }

    private static Student student() {
        return new Student(new StudentId("S-0601"), new StudentName("Mei Lin"), new AcademicLevel("Sec 3"));
    }
}
