package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;

class DeleteStudentCommandTest {
    @Test
    void execute_deletesRequestedStudentAndPreservesOthers() throws Exception {
        ModelManager model = new ModelManager();
        Student deleted = new Student(new StudentId("S-0301"), new StudentName("Mei Lin"),
                new AcademicLevel("Sec 2"));
        Student retained = new Student(new StudentId("S-0302"), new StudentName("Amy Tan"),
                new AcademicLevel("Sec 1"));
        model.addStudent(deleted);
        model.addStudent(retained);

        CommandResult result = new DeleteStudentCommand(deleted.getId()).execute(model);

        assertEquals("Deleted student S-0301: Mei Lin.", result.getFeedbackToUser());
        assertTrue(result.shouldClearStudentSelection());
        assertEquals(java.util.List.of(retained), model.getStudentList());
    }

    @Test
    void execute_unknownId_throwsAndLeavesStudentsUnchanged() {
        ModelManager model = new ModelManager();
        Student retained = new Student(new StudentId("S-0310"), new StudentName("Amy Tan"),
                new AcademicLevel("Sec 1"));
        model.addStudent(retained);
        DeleteStudentCommand command = new DeleteStudentCommand(new StudentId("S-0399"));

        Executable execution = () -> command.execute(model);

        assertThrows(CommandException.class, "Student S-0399 was not found.", execution);
        assertEquals(java.util.List.of(retained), model.getStudentList());
    }
}
