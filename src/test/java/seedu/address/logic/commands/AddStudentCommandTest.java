package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.StudentName;

class AddStudentCommandTest {
    @Test
    void execute_addsStudentAndReturnsId() throws Exception {
        ModelManager model = new ModelManager();
        CommandResult result = new AddStudentCommand(new StudentName("Mei Lin"), new AcademicLevel("Sec 2"))
                .execute(model);
        assertEquals(1, model.getStudentList().size());
        assertEquals("Added student " + model.getStudentList().get(0).getId() + ": Mei Lin (Sec 2).",
                result.getFeedbackToUser());
        assertEquals(model.getStudentList().get(0).getId().value, result.getSelectedStudentId());
    }

    @Test
    void execute_duplicateIdentity_throwsAndDoesNotAdd() throws Exception {
        ModelManager model = new ModelManager();
        new AddStudentCommand(new StudentName("Mei Lin"), new AcademicLevel("Sec 2")).execute(model);
        AddStudentCommand duplicate = new AddStudentCommand(new StudentName("mei   lin"),
                new AcademicLevel("sec 2"));
        Executable execution = () -> duplicate.execute(model);
        assertThrows(CommandException.class, "A student named mei lin at Sec 2 already exists.", execution);
        assertEquals(1, model.getStudentList().size());
    }
}
