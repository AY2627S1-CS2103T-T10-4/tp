package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;

public class ViewStudentCommandTest {

    @Test
    public void execute_existingStudent_returnsSelectionWithoutChangingModel() throws Exception {
        ModelManager model = new ModelManager();
        Student student = new Student(new StudentId("S-0007"), new StudentName("Mei Lin"),
                new AcademicLevel("Sec 2"));
        model.addStudent(student);
        AddressBook beforeCommand = new AddressBook(model.getAddressBook());

        CommandResult result = new ViewStudentCommand("S-0007").execute(model);

        assertEquals("Opened profile for S-0007.", result.getFeedbackToUser());
        assertEquals("S-0007", result.getSelectedStudentId());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
        assertEquals(beforeCommand, model.getAddressBook());
        assertEquals(result, new ViewStudentCommand("S-0007").execute(model));
    }

    @Test
    public void execute_unknownStudent_throwsCommandExceptionWithoutChangingModel() {
        ModelManager model = new ModelManager();
        model.addStudent(new Student(new StudentId("S-0007"), new StudentName("Mei Lin"),
                new AcademicLevel("Sec 2")));
        AddressBook beforeCommand = new AddressBook(model.getAddressBook());

        CommandException error = assertThrows(CommandException.class, () ->
                new ViewStudentCommand("S-0008").execute(model));

        assertEquals("Student S-0008 does not exist.", error.getMessage());
        assertEquals(beforeCommand, model.getAddressBook());
    }

    @Test
    public void execute_emptyRegister_throwsCommandException() {
        CommandException error = assertThrows(CommandException.class, () ->
                new ViewStudentCommand("S-0007").execute(new ModelManager()));
        assertEquals("Student S-0007 does not exist.", error.getMessage());
    }

    @Test
    public void constructorAndExecute_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ViewStudentCommand(null));
        assertThrows(NullPointerException.class, () -> new ViewStudentCommand("S-0007").execute(null));
    }

    @Test
    public void equals_sameAndDifferentIds_comparesStudentId() {
        ViewStudentCommand command = new ViewStudentCommand("S-0007");
        ViewStudentCommand equalCommand = new ViewStudentCommand("S-0007");
        assertEquals(command, command);
        assertEquals(command, equalCommand);
        assertEquals(command.hashCode(), equalCommand.hashCode());
        assertNotEquals(command, new ViewStudentCommand("S-0008"));
        assertNotEquals(command, null);
        assertNotEquals(command, new HelpCommand());
    }
}
